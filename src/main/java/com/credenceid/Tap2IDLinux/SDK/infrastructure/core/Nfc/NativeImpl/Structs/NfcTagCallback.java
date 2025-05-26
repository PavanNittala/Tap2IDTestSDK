package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs;

import com.sun.jna.CallbackReference;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class NfcTagCallback extends Structure {
    public static class ByReference extends NfcTagCallback implements Structure.ByReference {}

    // Ensure the Callback interface is from com.sun.jna
    public interface OnTagArrivalCallback extends com.sun.jna.Callback {
        void invoke(NfcTagInfo.ByReference pTagInfo);
    }

    public interface OnTagDepartureCallback extends com.sun.jna.Callback {
        void invoke();
    }

    // Use Pointer fields for native callback function pointers
    public Pointer onTagArrivalCallback;
    public Pointer onTagDepartureCallback;

    // Constructor to set callbacks
    public NfcTagCallback(OnTagArrivalCallback arrivalCallback, OnTagDepartureCallback departureCallback) {
        if (arrivalCallback == null || departureCallback == null) {
            throw new IllegalArgumentException("Callback arguments cannot be null");
        }
        this.onTagArrivalCallback = CallbackReference.getFunctionPointer(arrivalCallback);
        this.onTagDepartureCallback = CallbackReference.getFunctionPointer(departureCallback);
    }

    public NfcTagCallback() {}

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("onTagArrivalCallback", "onTagDepartureCallback");
    }
}
