package com.credenceid.Tap2IDLinux.SDK.domain.repository;

import com.credenceid.Tap2IDLinux.SDK.application.listener.BarcodeScanListener;


public interface BarcodeScannerRepository {
    void start();
    void stop();
    void setBarcodeListener(BarcodeScanListener listener);
}
