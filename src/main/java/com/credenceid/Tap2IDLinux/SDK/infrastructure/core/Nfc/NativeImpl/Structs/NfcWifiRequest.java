package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.List;

public class NfcWifiRequest extends Structure {
    public static class ByReference extends NfcWifiRequest implements Structure.ByReference {}
    public static class ByValue extends NfcWifiRequest implements Structure.ByValue {}

    public int has_wifi;
    public Pointer ndef;
    public int ndef_length;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("has_wifi", "ndef", "ndef_length");
    }
}
