package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs;

import com.sun.jna.Callback;
import com.sun.jna.Structure;

import java.util.List;

public class NfcSnepServerCallback extends Structure {
    public static class ByReference extends NfcSnepServerCallback implements Structure.ByReference {}
    public static class ByValue extends NfcSnepServerCallback implements Structure.ByValue {}

    public interface OnDeviceArrivalCallback extends Callback {
        void invoke();
    }

    public interface OnDeviceDepartureCallback extends Callback {
        void invoke();
    }

    public interface OnMessageReceivedCallback extends Callback {
        void invoke();
    }

    public OnDeviceArrivalCallback onDeviceArrival;
    public OnDeviceDepartureCallback onDeviceDeparture;
    public OnMessageReceivedCallback onMessageReceived;

    @Override
    protected List<String> getFieldOrder() {
        return List.of("onDeviceArrival", "onDeviceDeparture", "onMessageReceived");
    }
}
