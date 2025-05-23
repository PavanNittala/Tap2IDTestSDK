package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs;

import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Enums.NfcFriendlyType;
import com.sun.jna.Structure;

import java.util.List;

public class NfcFriendlyTypeRef extends Structure {
    public int value;

    public NfcFriendlyTypeRef() {}
    public NfcFriendlyTypeRef(NfcFriendlyType type) {
        this.value = type.getValue();
    }

    public NfcFriendlyType getEnum() {
        return NfcFriendlyType.fromValue(value);
    }

    @Override
    protected List<String> getFieldOrder() {
        return List.of("value");
    }

    public static class ByReference extends NfcFriendlyTypeRef implements Structure.ByReference {}
    public static class ByValue extends NfcFriendlyTypeRef implements Structure.ByValue {}
}
