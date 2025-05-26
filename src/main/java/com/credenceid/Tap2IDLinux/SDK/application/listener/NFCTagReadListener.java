package com.credenceid.Tap2IDLinux.SDK.application.listener;

import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcTagInfo;
import com.credenceid.identity.iso18013.DeviceEngagement;

public interface NFCTagReadListener {
    void onNfcTagRead(DeviceEngagement deviceEngagement);
    void onError(String message);
}
