package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.helpers;

import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Native.NfcManager;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NdefInfo;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcFriendlyTypeRef;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcTagCallback;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcTagInfo;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.nfc.NFCReader;
import com.credenceid.identity.transactionPerf.DeviceEngagementPerfLogger;
import com.sun.jna.CallbackReference;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class LinuxNfcCommunication implements NfcCommunication{
    private static final int  DEFAULT_NFA_TECH_MASK = -1;
    private static final Logger LOG = LoggerFactory.getLogger(LinuxNfcCommunication.class);
    private NfcManager nfcManager;
    public static NfcTagInfo currentIsoDepTag = new NfcTagInfo();
    private static final int TIMEOUT_IN_SECONDS = 5000;
    private static final AtomicBoolean isPaused = new AtomicBoolean(false);
    private static DeviceEngagementPerfLogger deviceEngagementPerfLogger;
    public static CompletableFuture<NfcTagInfo> tagArrivalFuture = new CompletableFuture<>();
    public static NdefInfo ndefInfo = new NdefInfo.ByReference();
    NfcFriendlyTypeRef.ByReference nfcFriendlyTypeRef = new NfcFriendlyTypeRef.ByReference();
    private NFCReader.NFCListener nfcListener;

    class NfcCallback {
        public final NfcTagCallback.OnTagArrivalCallback onTagArrivalCallback;
        public final NfcTagCallback.OnTagDepartureCallback onTagDepartedCallback;
        NdefInfo.ByReference ndefInfo = new NdefInfo.ByReference();

        public NfcCallback() {
            this.onTagArrivalCallback = (pTagInfo) -> {
                currentIsoDepTag = pTagInfo;
                LOG.info("Tag Arrived: {}", currentIsoDepTag);
                LOG.info("UID: {}", bytesToHex(currentIsoDepTag.uid));

                if (nfcListener != null) {
                    nfcListener.onTagReceived(currentIsoDepTag);
                }
            };

            this.onTagDepartedCallback = () -> {
                LOG.info("Tag Departed");
                currentIsoDepTag = null;
                if (nfcListener != null) {
                    nfcListener.onTagDeparted();
                }
            };
        }
        public void RegisterCallback(){
            // Register this callback
            NfcTagCallback.ByReference nfcTagCallback = new NfcTagCallback.ByReference();
            nfcTagCallback.onTagArrivalCallback = CallbackReference.getFunctionPointer(this.onTagArrivalCallback);
            nfcTagCallback.onTagDepartureCallback = CallbackReference.getFunctionPointer(this.onTagDepartedCallback);
            nfcManager.registerTagCallback(nfcTagCallback);
        }
    }

    public LinuxNfcCommunication(DeviceEngagementPerfLogger deviceEngagementPerfLogger, NFCReader.NFCListener nfcCallback)
    {
        LinuxNfcCommunication.deviceEngagementPerfLogger = deviceEngagementPerfLogger;
        this.nfcListener = nfcCallback;
    }

    @Override
    public @NonNull Flowable<Object> startMonitoring() {
        return Flowable.create(emitter -> {
            try{
                nfcManager = new NfcManager();
                NfcTagCallback.ByReference nfcTagCallback = new NfcTagCallback.ByReference();

                // Create NfcCallback instance with the emitter
                NfcCallback nfcCallback = new NfcCallback();

                nfcTagCallback.onTagArrivalCallback = CallbackReference.getFunctionPointer(nfcCallback.onTagArrivalCallback);
                nfcTagCallback.onTagDepartureCallback = CallbackReference.getFunctionPointer(nfcCallback.onTagDepartedCallback);

                LOG.info("Pavankn Linux NFC StartMonitoring");

                int result = nfcManager.doInitialize();
                if (result != 0x00) {
                    LOG.error("Pavankn NfcService Init Failed");
                    emitter.onNext(false);
                    emitter.onComplete();
                    return;
                }

                LOG.info("Pavankn NfcService Init Success");
                nfcManager.registerTagCallback(nfcTagCallback);
                nfcManager.doEnableDiscovery(DEFAULT_NFA_TECH_MASK, 0x01, 0x00, 0);

                try {
                    // This will block until the future is completed with the tag info.
                    NfcTagInfo tagInfo = tagArrivalFuture.get();
                    LOG.info("Pavankn Inside StartMonitor Processing Tag: {}", tagInfo.toString());

                    // Do further processing here
                    result = nfcManager.nfcTag_isNdef(tagInfo.handle, (NdefInfo.ByReference) ndefInfo);
                    LOG.info("nfcTag_isNdef result: " + result + ", is_ndef: " + ndefInfo.is_ndef + ", Length: " + ndefInfo.current_ndef_length);

                    int ndefBufferLength = ndefInfo.current_ndef_length;
                    LOG.info("NdefBufferLength: " + ndefBufferLength);

                    emitter.onNext(true);
                    emitter.onComplete();

                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    LOG.error("Error while waiting for tag arrival", e);
                }

            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
            emitter.setCancellable(() -> {
                // Cleanup logic
            });
        }, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io());
    }


    @Override
    public byte[] transceiveDataToTag(byte[] command) {
        if(nfcManager != null && currentIsoDepTag.handle != -1){
            try{
                return nfcManager.transceiveDataToTag(currentIsoDepTag.handle, TIMEOUT_IN_SECONDS, command);
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public void pauseReading(boolean pause) {
        LOG.info("Pavankn Pause Reading");
        isPaused.set(pause);
    }

    @Override
    public void dispose() {
    }
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
