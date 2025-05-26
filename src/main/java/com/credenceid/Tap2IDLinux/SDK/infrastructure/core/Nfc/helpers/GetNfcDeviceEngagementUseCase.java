package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.helpers;
import com.credenceid.identity.iso18013.DeviceEngagement;
import io.reactivex.rxjava3.core.Flowable;

public interface GetNfcDeviceEngagementUseCase {
    Flowable<DeviceEngagement> invoke();
}
