package com.credenceid.Tap2IDLinux.SDK.domain.repository;

import com.credenceid.Tap2IDLinux.SDK.application.listener.NFCTagReadListener;

public interface NFCReaderRepository {
    void start();
    void stop();
    void setNFCTagListener(NFCTagReadListener listener);
}
