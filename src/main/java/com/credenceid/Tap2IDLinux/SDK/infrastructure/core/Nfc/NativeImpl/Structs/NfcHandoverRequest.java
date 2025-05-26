package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs;

import com.sun.jna.Structure;

import java.util.List;

public class NfcHandoverRequest extends Structure {
    public static class ByReference extends NfcHandoverRequest implements Structure.ByReference {}
    public static class ByValue extends NfcHandoverRequest implements Structure.ByValue {}

    public NfcBtoobPairing bluetooth; // previously defined
    public NfcWifiRequest wifi;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("bluetooth", "wifi");
    }
}
