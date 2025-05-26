package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class NfcTagInfo extends Structure{
    public static class ByReference extends NfcTagInfo implements Structure.ByReference {}

    public int technology;
    public int handle;
    public byte[] uid = new byte[32];
    public int uid_length;
    public byte protocol;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("technology", "handle", "uid", "uid_length", "protocol");
    }
}
