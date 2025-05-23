package com.credenceid.Tap2IDLinux.SDK.application.listener;

import com.credenceid.Tap2IDLinux.SDK.Utils.IdentityState;

public interface IdentityStateListener {
    void onStateChanged(IdentityState state);
}
