package com.credenceid.Tap2IDLinux.SDK.application.listener;

import com.credenceid.Tap2IDLinux.SDK.model.Barcode;

public interface BarcodeScanListener {
    void onBarcodeScanned(Barcode barcode);
    void onError(String message);
}

