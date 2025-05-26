package com.credenceid.Tap2IDLinux.SDK.application.Scanner;

import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcTagInfo;
import com.credenceid.Tap2IDLinux.SDK.application.listener.BarcodeScanListener;
import com.credenceid.Tap2IDLinux.SDK.application.listener.NFCTagReadListener;
import com.credenceid.Tap2IDLinux.SDK.model.Barcode;
import com.credenceid.Tap2IDLinux.SDK.domain.usecase.ReadNfcTagUseCase;
import com.credenceid.Tap2IDLinux.SDK.domain.usecase.ScanBarcodeUseCase;
import com.credenceid.identity.iso18013.DeviceEngagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ScannerFactory {
    private final ScanBarcodeUseCase barcodeUseCase;
    private final ReadNfcTagUseCase nfcUseCase;
    private static final Logger LOG = LoggerFactory.getLogger(ScannerFactory.class);

    public ScannerFactory(ScanBarcodeUseCase barcodeUseCase, ReadNfcTagUseCase nfcUseCase) {
        this.barcodeUseCase = barcodeUseCase;
        this.nfcUseCase = nfcUseCase;
    }

    public Tap2IDScanner createScanner(ScannerType mode) {
        switch (mode) {
            case BARCODE:
                LOG.info("Create Barcode Scanner");
                return new BarcodeScannerAdapter(barcodeUseCase);
            case NFC:
                LOG.info("Create NFC Scanner");
                return new NfcScannerAdapter(nfcUseCase);
            default:
                throw new IllegalArgumentException("Unknown scanner mode: " + mode);
        }
    }

    public Tap2IDScanner createScanner(List<ScannerType> types) {
        if (types.isEmpty()) {
            throw new IllegalArgumentException("At least one scanner type must be specified");
        }
        if (types.size() == 1) {
            return createScanner(types.get(0));
        }
        return new CompositeScanner(
                types.stream()
                        .map(this::createScanner)
                        .toArray(Tap2IDScanner[]::new)
        );
    }

    // Adapter for barcode scanner
    private static class BarcodeScannerAdapter implements Tap2IDScanner {
        private final ScanBarcodeUseCase barcodeUseCase;

        BarcodeScannerAdapter(ScanBarcodeUseCase barcodeUseCase) {
            this.barcodeUseCase = barcodeUseCase;
        }

        @Override
        public void start() {
            barcodeUseCase.startScanning();
        }

        @Override
        public void stop() {
            barcodeUseCase.stopScanning();
        }

        @Override
        public void setListener(ScannerListener listener) {
            barcodeUseCase.setListener(new BarcodeScanListener() {
                @Override
                public void onBarcodeScanned(Barcode barcode) {
                    listener.onBarcodeScanned(barcode.getValue());
                }

                @Override
                public void onError(String message) {
                    listener.onError(message);
                }
            });
        }
    }

    // Adapter for NFC scanner
    private static class NfcScannerAdapter implements Tap2IDScanner {
        private final ReadNfcTagUseCase nfcUseCase;

        NfcScannerAdapter(ReadNfcTagUseCase nfcUseCase) {
            this.nfcUseCase = nfcUseCase;
        }

        @Override
        public void start() {
            nfcUseCase.startReading();
        }

        @Override
        public void stop() {
            nfcUseCase.stopReading();
        }

        @Override
        public void setListener(ScannerListener listener) {
            nfcUseCase.setListener(new NFCTagReadListener() {

                @Override
                public void onNfcTagRead(DeviceEngagement deviceEngagement) {
                    listener.onNfcTagRead(deviceEngagement);
                }

                @Override
                public void onError(String message) {
                    listener.onError(message);
                }
            });
        }
    }

}
