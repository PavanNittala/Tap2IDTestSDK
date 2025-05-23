package com.credenceid.Tap2IDLinux.SDK.infrastructure.nfc;

import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcTagInfo;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.data.NdefRepositoryImpl;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.helpers.*;

import com.credenceid.identity.transactionPerf.DeviceEngagementPerfLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NFCReader {
    private static final Logger LOG = LoggerFactory.getLogger(NFCReader.class);
    private NFCListener listener;
    public static NfcTagInfo currentIsoDepTag = new NfcTagInfo();

    public interface NFCListener {
        void onTagReceived(NfcTagInfo ndef);
        void onTagDeparted();
        void onError(String message);
    }

    public void setListener(NFCListener listener) {
        LOG.info("Pavankn NFC setListener");
        this.listener = listener;
    }


    public void start(){
        LOG.info("Pavankn NFC Start");
        DeviceEngagementPerfLogger perfLogger = DeviceEngagementPerfLogger.getInstance();
        NfcCommunication nfcCommunication = new LinuxNfcCommunication(perfLogger, new NFCListener() {
            @Override
            public void onTagReceived(NfcTagInfo ndef) {
                LOG.info("NFCReader onTagReceived");
                listener.onTagReceived(ndef);
            }

            @Override
            public void onTagDeparted() {
                LOG.info("NFCReader onTagDeparted");
            }

            @Override
            public void onError(String message) {
                LOG.info("NFCReader onError");
            }
        });
        ApduCommunicationHelper apduCommunicationHelper = new ApduCommunicationHelperImpl(nfcCommunication);
        NdefCommunicationHelper ndefCommunicationHelper = new NdefCommunicationHelperImpl(apduCommunicationHelper);
        NdefRepositoryImpl ndefRepositoryImpl = new NdefRepositoryImpl(ndefCommunicationHelper, perfLogger);
        GetNfcDeviceEngagementUseCaseImpl nfcDeviceEngagementUseCaseImpl = new GetNfcDeviceEngagementUseCaseImpl(ndefRepositoryImpl, perfLogger);
        subscribeToNfc(nfcDeviceEngagementUseCaseImpl);
    }

    private static void subscribeToNfc(GetNfcDeviceEngagementUseCaseImpl useCase) {

        LOG.info("Pavankn subscribeToNfc");
        useCase.invoke().subscribe(deviceEngagement -> {
            if (deviceEngagement != null) {
                LOG.info("NFC DeviceEngagement: " + deviceEngagement);
            } else {
                LOG.info("NFC DeviceEngagement Null");
            }
            // Restart the flow after completion
            //subscribeToNfc(useCase);
        }, throwable -> {
            LOG.error("NFC DeviceEngagement Failed", throwable);
            // Optional: delay before retrying to avoid tight error loop
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            //subscribeToNfc(useCase);
        });
    }


    public void stop() {
    }

}
