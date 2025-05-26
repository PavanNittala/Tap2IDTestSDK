package com.credenceid.Tap2IDLinux.SDK.domain.usecase;

import com.credenceid.Tap2IDLinux.SDK.Utils.IdentityUtils;
import com.credenceid.identity.Identity;
import com.credenceid.identity.iso18013.DeviceResponseParser;

public class ValidationDataUseCaseImpl implements ValidationDataUseCase {
    private final IdentityUtils identityUtils ;

    @Override
    public Identity execute(byte[] identityByte) {
        DeviceResponseParser.DeviceResponse deviceResponse =  identityUtils.getDeviceResponse(identityByte);
        try {
            return identityUtils.getIdentityFromDeviceResponse(deviceResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ValidationDataUseCaseImpl(IdentityUtils identityUtils){
        this.identityUtils = identityUtils;
    }
}
