package com.credenceid.Tap2IDLinux.SDK.domain.usecase;

import com.credenceid.identity.Identity;

public interface ValidationDataUseCase {
    public Identity execute(byte[] identityByte);
}
