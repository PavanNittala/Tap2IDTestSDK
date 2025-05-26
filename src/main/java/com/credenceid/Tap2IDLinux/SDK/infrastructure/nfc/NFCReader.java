package com.credenceid.Tap2IDLinux.SDK.infrastructure.nfc;

import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcTagInfo;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.data.NdefRepositoryImpl;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.helpers.*;

import com.credenceid.identity.iso18013.DeviceEngagement;
import com.credenceid.identity.transactionPerf.DeviceEngagementPerfLogger;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class NFCReader {
    private static final Logger LOG = LoggerFactory.getLogger(NFCReader.class);
    private NFCListener listener;
    private Disposable subscription; // To manage Flowable subscription
    private NfcCommunication nfcCommunication; // For cleanup
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public interface NFCListener {
        void onTagReceived(DeviceEngagement deviceEngagement);
        void onTagDeparted();
        void onError(String message);
    }

    public void setListener(NFCListener listener) {
        LOG.info("Pavankn NFC setListener");
        this.listener = listener;
    }

    public void start(){
        DeviceEngagementPerfLogger perfLogger = DeviceEngagementPerfLogger.getInstance();
        nfcCommunication = new LinuxNfcCommunication(perfLogger);
        ApduCommunicationHelper apduCommunicationHelper = new ApduCommunicationHelperImpl(nfcCommunication);
        NdefCommunicationHelper ndefCommunicationHelper = new NdefCommunicationHelperImpl(apduCommunicationHelper);
        NdefRepositoryImpl ndefRepositoryImpl = new NdefRepositoryImpl(ndefCommunicationHelper, perfLogger);
        GetNfcDeviceEngagementUseCaseImpl nfcDeviceEngagementUseCaseImpl = new GetNfcDeviceEngagementUseCaseImpl(ndefRepositoryImpl, perfLogger);
        subscribeToNfc(nfcDeviceEngagementUseCaseImpl);
    }

    /**
     *
     * @param useCase
     */
    private void subscribeToNfc(GetNfcDeviceEngagementUseCaseImpl useCase) {

        LOG.info("Listen to NFC");
        subscription = useCase.invoke().subscribe(deviceEngagement -> {
            if (deviceEngagement != null) {
                LOG.info("NFC DeviceEngagement: {}", deviceEngagement);
                if (listener != null) {
                    listener.onTagReceived(deviceEngagement);
                    // Retry after delay
                    Schedulers.io().scheduleDirect(() -> subscribeToNfc(useCase), 1, TimeUnit.SECONDS);
                }
            } else {
                LOG.info("NFC DeviceEngagement Null (Tag Departed)");
                if (listener != null) {
                    listener.onTagDeparted();
                }
            }
        }, throwable -> {
            LOG.error("NFC DeviceEngagement Failed", throwable);
            if (listener != null) {
                listener.onError(throwable.getMessage());
            }
            // Retry after delay
            Schedulers.io().scheduleDirect(() -> subscribeToNfc(useCase), 1, TimeUnit.SECONDS);
        });

    }

    public void stop() {
        LOG.info("NFC Stop");
        isRunning.set(false);
    }

}
