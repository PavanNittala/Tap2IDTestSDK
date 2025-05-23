package com.credenceid.Tap2IDLinux.SDK.domain.usecase;

import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcTagInfo;
import com.credenceid.Tap2IDLinux.SDK.application.listener.NFCTagReadListener;
import com.credenceid.Tap2IDLinux.SDK.domain.repository.NFCReaderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadNfcTagUseCaseImpl implements ReadNfcTagUseCase {
    private final NFCReaderRepository repository;
    private static final Logger LOG = LoggerFactory.getLogger(ReadNfcTagUseCaseImpl.class);

    public ReadNfcTagUseCaseImpl(NFCReaderRepository repository) {
        this.repository = repository;
    }

    @Override
    public void startReading() {
        repository.start();
    }

    @Override
    public void stopReading() {
        repository.stop();
    }

    @Override
    public void setListener(NFCTagReadListener listener) {
       repository.setNFCTagListener(new NFCTagReadListener() {
           @Override
           public void onNfcTagRead(NfcTagInfo tag) {
               LOG.info("Pavankn onNfcTagRead: " + tag);
               if (listener != null) {
                   listener.onNfcTagRead(tag);
               }
           }

           @Override
           public void onError(String message) {
               LOG.info("Pavankn onNFCErrpr: ");
               listener.onError("");
           }
       });
    }
}
