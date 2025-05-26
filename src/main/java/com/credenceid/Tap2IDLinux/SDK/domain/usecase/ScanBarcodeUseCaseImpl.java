package com.credenceid.Tap2IDLinux.SDK.domain.usecase;

import com.credenceid.Tap2IDLinux.SDK.model.Barcode;
import com.credenceid.Tap2IDLinux.SDK.application.listener.BarcodeScanListener;
import com.credenceid.Tap2IDLinux.SDK.domain.repository.BarcodeScannerRepository;

public class ScanBarcodeUseCaseImpl implements ScanBarcodeUseCase {
    private final BarcodeScannerRepository repository;

    public ScanBarcodeUseCaseImpl(BarcodeScannerRepository repository) {
        this.repository = repository;
    }

    @Override
    public void startScanning() {
        repository.start();
    }

    @Override
    public void stopScanning() {
        repository.stop();
    }

    @Override
    public void setListener(BarcodeScanListener listener) {
        repository.setBarcodeListener(new BarcodeScanListener() {
            @Override
            public void onBarcodeScanned(Barcode barcode) {
                if (listener != null) {
                    listener.onBarcodeScanned(barcode);
                }
            }

            @Override
            public void onError(String message) {
                if (listener != null) {
                    listener.onError(message);
                }
            }
        });
    }
}
