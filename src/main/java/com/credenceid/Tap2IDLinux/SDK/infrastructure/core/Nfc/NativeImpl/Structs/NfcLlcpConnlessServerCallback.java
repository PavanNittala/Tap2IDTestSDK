package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs;

import com.sun.jna.Callback;
import com.sun.jna.Structure;

import java.util.List;

public class NfcLlcpConnlessServerCallback extends Structure {
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
