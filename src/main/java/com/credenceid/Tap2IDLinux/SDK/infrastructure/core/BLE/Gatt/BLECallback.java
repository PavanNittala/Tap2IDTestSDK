package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.BLE.Gatt;

public interface BLECallback {
    void onSuccess();
    void onConnected();
    void onIdentRead();
    void onClient2ServerRead(byte[] received);
    void onDisconnected();
}
