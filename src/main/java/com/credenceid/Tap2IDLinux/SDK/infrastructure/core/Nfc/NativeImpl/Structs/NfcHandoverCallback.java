package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs;

import com.sun.jna.Callback;
import com.sun.jna.Structure;

import java.util.List;

public class NfcHandoverCallback extends Structure {
    public static class ByReference extends NfcHandoverCallback implements Structure.ByReference {}
    public static class ByValue extends NfcHandoverCallback implements Structure.ByValue {}

    // Interface for the callback onHandoverRequestReceived
    public interface OnHandoverRequestReceivedCallback extends Callback {
        void invoke(byte[] msg, int length);
    }

    // Interface for the callback onHandoverSelectReceived
    public interface OnHandoverSelectReceivedCallback extends Callback {
        void invoke(byte[] msg, int length);
    }

    public OnHandoverRequestReceivedCallback onHandoverRequestReceived;
    public OnHandoverSelectReceivedCallback onHandoverSelectReceived;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("onHandoverRequestReceived", "onHandoverSelectReceived");
    }
}
