package com.credenceid.Tap2IDLinux.SDK.infrastructure.repository;

import com.credenceid.Tap2IDLinux.SDK.model.Barcode;
import com.credenceid.Tap2IDLinux.SDK.application.listener.BarcodeScanListener;
import com.credenceid.Tap2IDLinux.SDK.domain.repository.BarcodeScannerRepository;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.hid.HidBarcodeScanner;

public class HidBarcodeScannerRepository implements BarcodeScannerRepository {
    private final HidBarcodeScanner scanner;

    public HidBarcodeScannerRepository(HidBarcodeScanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void start() {
        scanner.start();
    }

    @Override
    public void stop() {
        scanner.stop();
    }

    @Override
    public void setBarcodeListener(BarcodeScanListener listener) {
        scanner.setListener(new HidBarcodeScanner.BarcodeListener() {
            @Override
            public void onBarcodeReceived(String barcode) {
                try {
                    listener.onBarcodeScanned(new Barcode(barcode));
                } catch (IllegalArgumentException e) {
                    listener.onError("Invalid barcode: " + e.getMessage());
                }
            }

            @Override
            public void onError(String message) {
                listener.onError(message);
            }
        });
    }
}
