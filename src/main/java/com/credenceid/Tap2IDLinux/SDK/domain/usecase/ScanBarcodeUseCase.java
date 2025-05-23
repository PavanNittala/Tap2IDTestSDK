package com.credenceid.Tap2IDLinux.SDK.domain.usecase;

import com.credenceid.Tap2IDLinux.SDK.application.listener.BarcodeScanListener;

public interface ScanBarcodeUseCase {
    void startScanning();
    void stopScanning();
    void setListener(BarcodeScanListener listener);
}
