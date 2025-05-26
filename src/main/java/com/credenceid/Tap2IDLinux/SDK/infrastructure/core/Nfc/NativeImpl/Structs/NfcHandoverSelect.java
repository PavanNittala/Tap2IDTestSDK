package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs;

import com.sun.jna.Structure;

import java.util.List;

public class NfcHandoverSelect extends Structure {
    public static class ByReference extends NfcHandoverSelect implements Structure.ByReference {}
    public static class ByValue extends NfcHandoverSelect implements Structure.ByValue {}

    public NfcBtoobPairing bluetooth; // previously defined
    public NfcWifiPairing wifi;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("bluetooth", "wifi");
    }
}
