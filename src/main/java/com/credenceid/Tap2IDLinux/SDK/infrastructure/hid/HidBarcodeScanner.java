package com.credenceid.Tap2IDLinux.SDK.infrastructure.hid;

import org.hid4java.*;
import org.hid4java.event.HidServicesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class HidBarcodeScanner implements HidServicesListener {
    private static final int VENDOR_ID = 0x1EAB;
    private static final int PRODUCT_ID = 0x1D10;
    private static final int REPORT_SIZE = 64;
    private static final int START_FRAME = 0x02;
    private static final Logger LOG = LoggerFactory.getLogger(HidBarcodeScanner.class);
    private static final int SCAN_INTERVAL = 5000;
    private static final int PAUSE_INTERVAL = 10000;

    private HidServices hidServices;
    private BarcodeListener listener;

    public interface BarcodeListener {
        void onBarcodeReceived(String barcode);
        void onError(String message);
    }

    public void start() {
        try {
            HidServicesSpecification hidSpec = new HidServicesSpecification();
            hidSpec.setAutoDataRead(true);
            hidSpec.setScanInterval(SCAN_INTERVAL);
            hidSpec.setPauseInterval(PAUSE_INTERVAL);

            hidServices = HidManager.getHidServices(hidSpec);
            hidServices.addHidServicesListener(this);
            hidServices.start();

            HidDevice device = hidServices.getHidDevice(VENDOR_ID, PRODUCT_ID, null);
            if (device != null) {
                LOG.info("Barcode Scanner Connected: {}", device);
                device.setNonBlocking(true);
            } else {
                LOG.warn("Scanner not found!");
                if (listener != null) {
                    listener.onError("Barcode scanner not found");
                }
            }
        } catch (HidException e) {
            LOG.error("Error starting HIDBarcodeScanner: {}", e.getMessage(), e);
            if (listener != null) {
                listener.onError("Failed to start scanner: " + e.getMessage());
            }
        }
    }

    public void stop() {
        try {
            if (hidServices != null) {
                hidServices.shutdown();
                LOG.info("HIDBarcodeScanner stopped");
            }
        } catch (Exception e) {
            LOG.error("Error stopping HIDBarcodeScanner: {}", e.getMessage(), e);
            if (listener != null) {
                listener.onError("Failed to stop scanner: " + e.getMessage());
            }
        }
    }

    public void setListener(BarcodeListener listener) {
        LOG.info("Pavankn Barcode setListener");
        this.listener = listener;
    }

    private void printAsHex(byte[] dataReceived) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("< [%02x]:", dataReceived.length));
        for (byte b : dataReceived) {
            sb.append(String.format(" %02x", b));
        }
        LOG.debug(sb.toString());
    }

    @Override
    public void hidDeviceAttached(HidServicesEvent event) {
        LOG.info("Device Attached: {}", event.getHidDevice());
    }

    @Override
    public void hidDeviceDetached(HidServicesEvent event) {
        LOG.info("Device Detached: {}", event.getHidDevice());
        if (listener != null) {
            listener.onError("Barcode scanner disconnected");
        }
    }

    @Override
    public void hidFailure(HidServicesEvent event) {
        LOG.error("Device Failure: {}", event.getHidDevice());
        if (listener != null) {
            listener.onError("Barcode scanner failure");
        }
    }

    @Override
    public void hidDataReceived(HidServicesEvent hidServicesEvent) {
        byte[] fullData = hidServicesEvent.getDataReceived();
        StringBuilder sb = new StringBuilder();

        for (int offset = 0; offset + REPORT_SIZE <= fullData.length; offset += REPORT_SIZE) {
            byte reportId = fullData[offset]; // Usually 0x02 for barcode
            if (reportId != START_FRAME) {
                LOG.warn("Skipping invalid report ID: {}", reportId);
                continue;
            }

            // Extract chunk without Report ID
            try {
                String chunk = new String(fullData, offset + 1, REPORT_SIZE - 1, StandardCharsets.UTF_8);
                // Ignore the first character in every Chunk
                sb.append(chunk.substring(1).trim());
            } catch(Exception e){
                LOG.error("Error processing chunk at offset {}: {}", offset, e.getMessage());
            }
        }
        String finalBarcode = sb.toString();

        if (!finalBarcode.isEmpty()) {
            LOG.info("Final Barcode: {}", finalBarcode);
            listener.onBarcodeReceived(finalBarcode);
        } else {
            LOG.warn("Empty barcode received");
        }
    }

}




