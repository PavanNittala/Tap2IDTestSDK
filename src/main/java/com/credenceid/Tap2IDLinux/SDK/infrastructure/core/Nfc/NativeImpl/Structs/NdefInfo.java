package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs;

import com.sun.jna.Structure;

import java.util.List;

public class NdefInfo extends Structure {
    public static class ByReference extends NdefInfo implements Structure.ByReference {}
    public static class ByValue extends NdefInfo implements Structure.ByValue {}

    public int is_ndef;
    public int current_ndef_length;
    public int max_ndef_length;
    public int is_writable;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("is_ndef", "current_ndef_length", "max_ndef_length", "is_writable");
    }
}
