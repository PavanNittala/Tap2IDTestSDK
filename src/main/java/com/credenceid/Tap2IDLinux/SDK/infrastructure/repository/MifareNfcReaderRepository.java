package com.credenceid.Tap2IDLinux.SDK.infrastructure.repository;

import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcTagInfo;
import com.credenceid.Tap2IDLinux.SDK.application.listener.NFCTagReadListener;
import com.credenceid.Tap2IDLinux.SDK.domain.repository.NFCReaderRepository;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.nfc.NFCReader;
import com.credenceid.identity.iso18013.DeviceEngagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MifareNfcReaderRepository implements NFCReaderRepository {
    private final NFCReader nfcReader;
    private static final Logger LOG = LoggerFactory.getLogger(MifareNfcReaderRepository.class);

    public MifareNfcReaderRepository(NFCReader nfcReader) {
        LOG.info("MifareNfcReader Repo Set");
        this.nfcReader = nfcReader;
    }

    @Override
    public void start() {
        LOG.info("MifareNfcReader nfcReader Start");
        nfcReader.start();
    }

    @Override
    public void stop() {
        nfcReader.stop();
    }

    @Override
    public void setNFCTagListener(NFCTagReadListener listener) {
        LOG.info("Pavankn Mifare setNFCTagListener");
        nfcReader.setListener(new NFCReader.NFCListener() {
            @Override
            public void onTagReceived(DeviceEngagement deviceEngagement) {
                LOG.info("MifareNfcReader onTagReceived: " + deviceEngagement);
                listener.onNfcTagRead(deviceEngagement);
            }

            @Override
            public void onTagDeparted() {
                LOG.info("MifareNfcReader onTagDeparted");
            }

            @Override
            public void onError(String message) {
                LOG.info("MifareNfcReader onError");
            }
        });
    }
}
