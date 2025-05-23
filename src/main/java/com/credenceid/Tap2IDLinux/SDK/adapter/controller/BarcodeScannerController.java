package com.credenceid.Tap2IDLinux.SDK.adapter.controller;

import com.credenceid.Tap2IDLinux.SDK.application.listener.BarcodeScanListener;
import com.credenceid.Tap2IDLinux.SDK.domain.usecase.ScanBarcodeUseCase;

public class BarcodeScannerController {
    private final ScanBarcodeUseCase useCase;

    public BarcodeScannerController(ScanBarcodeUseCase useCase) {
        this.useCase = useCase;
    }

    public void startScanning() {
        useCase.startScanning();
    }

    public void stopScanning() {
        useCase.stopScanning();
    }

    public void setListener(BarcodeScanListener listener) {
        useCase.setListener(listener);
    }
}
