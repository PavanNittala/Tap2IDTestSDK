package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Callbacks;

import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcHostCardEmulationCallback;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcSnepServerCallback;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcTagCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NfcCallbacks {
    private static final Logger LOG = LoggerFactory.getLogger(NfcCallbacks.class);

    public static NfcTagCallback.OnTagArrivalCallback onTagArrivalCallback = (pTagInfo) -> {
        LOG.info("Tag Arrived: {}", pTagInfo != null ? pTagInfo.toString() : "null");
    };

    public static NfcTagCallback.OnTagDepartureCallback onTagDepartedCallback = () -> {
        LOG.info("Tag Departed");
    };

    public static NfcSnepServerCallback.OnDeviceArrivalCallback onDeviceArrivalCallback = (() -> {
    });

    public static NfcSnepServerCallback.OnDeviceDepartureCallback onDeviceDepartureCallback = (() -> {
    });

    public static NfcSnepServerCallback.OnMessageReceivedCallback onMessageReceivedCallback = (() -> {
    });

    public static NfcHostCardEmulationCallback.OnHostCardEmulationActivatedCallback
            onHCEActivatedCallback = ((data) -> {
    });

    public static NfcHostCardEmulationCallback.OnHostCardEmulationDeactivatedCallback
            onHCEDeactivatedCallback = (() -> {
    });

    public static NfcHostCardEmulationCallback.OnDataReceivedCallback
            onDataReceivedCallback = ((data, length) -> {
    });
}
