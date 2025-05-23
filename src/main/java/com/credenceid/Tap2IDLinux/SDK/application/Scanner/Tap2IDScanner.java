package com.credenceid.Tap2IDLinux.SDK.application.Scanner;

public interface Tap2IDScanner {
    void start();
    void stop();
    void setListener(ScannerListener listener);
}
