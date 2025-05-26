package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.helpers;

import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Native.NfcManager;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NdefInfo;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcFriendlyTypeRef;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcTagCallback;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcTagInfo;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.nfc.NFCReader;
import com.credenceid.identity.iso18013.DeviceEngagement;
import com.credenceid.identity.transactionPerf.DeviceEngagementPerfLogger;
import com.sun.jna.CallbackReference;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.Instant;

import static com.credenceid.Tap2IDLinux.SDK.Utils.JavaUtils.bytesToHex;

public class LinuxNfcCommunication implements NfcCommunication{
    private static final int DEFAULT_NFA_TECH_MASK = -1;
    private static final Logger LOG = LoggerFactory.getLogger(LinuxNfcCommunication.class);
    private NfcManager nfcManager;
    private NfcTagInfo currentIsoDepTag = new NfcTagInfo(); // Non-static, default invalid
    private static final int TIMEOUT_IN_SECONDS = 5000;
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final DeviceEngagementPerfLogger deviceEngagementPerfLogger;
    private CompletableFuture<NfcTagInfo> tagArrivalFuture = new CompletableFuture<>();
    private NdefInfo ndefInfo = new NdefInfo.ByReference();
    private NfcFriendlyTypeRef.ByReference nfcFriendlyTypeRef = new NfcFriendlyTypeRef.ByReference();

    class NfcCallback {
        public final NfcTagCallback.OnTagArrivalCallback onTagArrivalCallback;
        public final NfcTagCallback.OnTagDepartureCallback onTagDepartedCallback;

        public NfcCallback() {
            this.onTagArrivalCallback = (pTagInfo) -> {
                if (pTagInfo == null) {
                    LOG.error("Received null pTagInfo in onTagArrivalCallback");
                    tagArrivalFuture.completeExceptionally(new IllegalArgumentException("pTagInfo is null"));
                    return;
                }
                currentIsoDepTag = pTagInfo;
                LOG.info("Tag Arrived: {}", currentIsoDepTag.toString());
                LOG.info("UID: {}", bytesToHex(currentIsoDepTag.uid));

                if (!isPaused.get()) {
                    deviceEngagementPerfLogger.setEngagementStartTimeInMillis(
                            System.currentTimeMillis()
                    );
                    LOG.info("Completing Future tagArrived");
                    tagArrivalFuture.complete(currentIsoDepTag);
                }
            };
            this.onTagDepartedCallback = () -> {
                LOG.info("Tag Departed");
                currentIsoDepTag = new NfcTagInfo(); // Reset to default
                tagArrivalFuture.complete(null);
                tagArrivalFuture = new CompletableFuture<>();
                if (nfcManager != null) {
                    nfcManager.doEnableDiscovery(DEFAULT_NFA_TECH_MASK, 0x01, 0x00, 0);
                }
                LOG.info("Prepared for next tag detection");
            };
        }
    }

    public LinuxNfcCommunication(DeviceEngagementPerfLogger deviceEngagementPerfLogger) {
        this.deviceEngagementPerfLogger = deviceEngagementPerfLogger;

        try {
            nfcManager = new NfcManager();
            NfcTagCallback.ByReference nfcTagCallback = new NfcTagCallback.ByReference();
            NfcCallback nfcCallback = new NfcCallback();

            nfcTagCallback.onTagArrivalCallback = CallbackReference.getFunctionPointer(nfcCallback.onTagArrivalCallback);
            nfcTagCallback.onTagDepartureCallback = CallbackReference.getFunctionPointer(nfcCallback.onTagDepartedCallback);

            LOG.info("Starting NFC monitoring");
            int result = nfcManager.doInitialize();
            if (result != 0x00) {
                LOG.error("NfcService initialization failed: {}", result);
                return;
            }

            LOG.info("NfcService initialized successfully");
            nfcManager.registerTagCallback(nfcTagCallback);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public void startTagListeningLoop() {
//        Executors.newSingleThreadExecutor().submit(() -> {
//            int result;
//            while (true) {
//                try {
//                    nfcManager.doEnableDiscovery(DEFAULT_NFA_TECH_MASK, 0x01, 0x01, 0);
//                    NfcTagInfo tagInfo = tagArrivalFuture.get(); // wait for tag
//                    if (tagInfo != null) {
//                        result = nfcManager.nfcTag_isNdef(tagInfo.handle, (NdefInfo.ByReference) ndefInfo);
//                        LOG.info("nfcTag_isNdef result: {}, is_ndef: {}, Length: {}",
//                                result, ndefInfo.is_ndef, ndefInfo.current_ndef_length);
//                        if(result == 1){
//                            nfcListener.onTagReceived(null);
//                        }
//                    } else {
//                        LOG.info("Tag departed or was null");
//                    }
//                    resetTagArrivalFuture(); // prep for next
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    LOG.warn("Tag listening interrupted");
//                    break;
//                } catch (Exception e) {
//                    LOG.error("Error in tag listening loop", e);
//                }
//            }
//        });
//    }

    private synchronized void resetTagArrivalFuture() {
        if (!tagArrivalFuture.isDone()) {
            tagArrivalFuture.complete(null);
        }
        tagArrivalFuture = new CompletableFuture<>();
    }


    @Override
    public Flowable<Object> startMonitoring() {
        return Flowable.create(emitter -> {
                    try {
                        nfcManager = new NfcManager();
                        NfcTagCallback.ByReference nfcTagCallback = new NfcTagCallback.ByReference();
                        NfcCallback nfcCallback = new NfcCallback();

                        nfcTagCallback.onTagArrivalCallback = CallbackReference.getFunctionPointer(nfcCallback.onTagArrivalCallback);
                        nfcTagCallback.onTagDepartureCallback = CallbackReference.getFunctionPointer(nfcCallback.onTagDepartedCallback);

                        LOG.info("Starting NFC monitoring");
                        int result = nfcManager.doInitialize();
                        if (result != 0x00) {
                            LOG.error("NfcService initialization failed: {}", result);
                            emitter.onError(new IllegalStateException("NFC initialization failed"));
                            return;
                        }

                        LOG.info("NfcService initialized successfully");
                        nfcManager.registerTagCallback(nfcTagCallback);
                        nfcManager.doEnableDiscovery(DEFAULT_NFA_TECH_MASK, 0x01, 0x01, 0);

                        try {
                            NfcTagInfo tagInfo = tagArrivalFuture.get();
                            if (tagInfo != null) {
                                LOG.info("Processing Tag: {}", tagInfo.toString());
                                result = nfcManager.nfcTag_isNdef(tagInfo.handle, (NdefInfo.ByReference) ndefInfo);
                                LOG.info("nfcTag_isNdef result: {}, is_ndef: {}, Length: {}",
                                        result, ndefInfo.is_ndef, ndefInfo.current_ndef_length);
                                emitter.onNext(tagInfo);
                            } else {
                                LOG.info("Tag departed");
                                emitter.onNext(null);
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            LOG.error("Interrupted while waiting for tag", e);
                            emitter.onError(e);
                        } catch (Exception e) {
                            LOG.error("Error processing tag", e);
                            emitter.onError(e);
                        }

                        emitter.setCancellable(() -> {
                            if (nfcManager != null) {
                                nfcManager.doDeinitialize();
                                nfcManager = null;
                            }
                            tagArrivalFuture.cancel(true);
                            LOG.info("NFC monitoring canceled");
                        });
                    } catch (RuntimeException e) {
                        LOG.error("Runtime error in NFC monitoring", e);
                        emitter.onError(e);
                    }
                }, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public byte[] transceiveDataToTag(byte[] command) {
        if (nfcManager == null || currentIsoDepTag.handle == -1) {
            LOG.warn("Cannot transceive: nfcManager is null or invalid tag handle");
            return null;
        }
        try {
            return nfcManager.transceiveDataToTag(currentIsoDepTag.handle, TIMEOUT_IN_SECONDS, command);
        } catch (RuntimeException e) {
            LOG.error("Transceive failed", e);
            throw e;
        }
    }

    @Override
    public void pauseReading(boolean pause) {
        LOG.info("Setting pause state to: {}", pause);
        isPaused.set(pause);
    }

    @Override
    public void dispose() {
        if (nfcManager != null) {
            nfcManager.doDeinitialize();
            nfcManager = null;
        }
        tagArrivalFuture.cancel(true);
        LOG.info("NFC resources disposed");
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
