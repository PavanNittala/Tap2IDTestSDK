package com.credenceid.Tap2IDLinux.SDK.application.listener;

import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcTagInfo;

public interface NFCTagReadListener {
    void onNfcTagRead(NfcTagInfo tag);
    void onError(String message);
}
