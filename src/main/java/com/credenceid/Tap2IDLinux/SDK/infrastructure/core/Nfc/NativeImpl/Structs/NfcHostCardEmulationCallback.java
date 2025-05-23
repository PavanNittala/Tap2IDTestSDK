package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs;

import com.sun.jna.Callback;
import com.sun.jna.Structure;

import java.util.List;

public class NfcHostCardEmulationCallback extends Structure {
    public static class ByReference extends NfcHostCardEmulationCallback implements Structure.ByReference {}
    public static class ByValue extends NfcHostCardEmulationCallback implements Structure.ByValue {}

    // Interface for the callback onHostCardEmulationActivated
    public interface OnHostCardEmulationActivatedCallback extends Callback {
        void invoke(byte mode);
    }

    // Interface for the callback onHostCardEmulationDeactivated
    public interface OnHostCardEmulationDeactivatedCallback extends Callback {
        void invoke();
    }

    // Interface for the callback onDataReceived
    public interface OnDataReceivedCallback extends Callback {
        void invoke(byte[] data, int dataLength);
    }

    public OnHostCardEmulationActivatedCallback onHostCardEmulationActivated;
    public OnHostCardEmulationDeactivatedCallback onHostCardEmulationDeactivated;
    public OnDataReceivedCallback onDataReceived;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("onHostCardEmulationActivated", "onHostCardEmulationDeactivated", "onDataReceived");
    }
}
