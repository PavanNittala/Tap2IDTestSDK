package com.credenceid.Tap2IDLinux.SDK.domain.usecase;

import com.credenceid.Tap2IDLinux.SDK.application.listener.NFCTagReadListener;

public interface ReadNfcTagUseCase {
    void startReading();
    void stopReading();
    void setListener(NFCTagReadListener listener);
}
