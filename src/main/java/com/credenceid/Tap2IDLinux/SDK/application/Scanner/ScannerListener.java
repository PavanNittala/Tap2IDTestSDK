package com.credenceid.Tap2IDLinux.SDK.application.Scanner;

import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcTagInfo;

public interface ScannerListener {
    void onBarcodeScanned(String barcode);
    void onNfcTagRead(NfcTagInfo tagId);
    void onError(String message);
}
