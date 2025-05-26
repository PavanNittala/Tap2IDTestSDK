package com.credenceid.Tap2IDLinux.SDK.adapter.controller;

import com.credenceid.Tap2IDLinux.SDK.application.Scanner.Tap2IDScanner;
import com.credenceid.Tap2IDLinux.SDK.application.Scanner.ScannerFactory;
import com.credenceid.Tap2IDLinux.SDK.application.Scanner.ScannerListener;
import com.credenceid.Tap2IDLinux.SDK.application.Scanner.ScannerType;

import java.util.List;

public class ScannerController {
    private final ScannerFactory scannerFactory;
    private Tap2IDScanner scanner;

    public ScannerController(ScannerFactory scannerFactory) {
        this.scannerFactory = scannerFactory;
        this.scanner = null;
    }

    public void initializeScanner(List<ScannerType> types) {
        if (types == null || types.isEmpty()) {
            throw new IllegalArgumentException("At least one scanner type must be specified");
        }
        this.scanner = scannerFactory.createScanner(types);
    }

    public void startScanning() {
        if (scanner == null) {
            throw new IllegalStateException("Scanner not initialized. Call initializeScanner first.");
        }
        scanner.start();
    }

    public void stopScanning() {
        if (scanner != null) {
            scanner.stop();
        }
    }

    public void setScannerListener(ScannerListener listener) {
        if (scanner == null) {
            throw new IllegalStateException("Scanner not initialized. Call initializeScanner first.");
        }
        if (listener != null) {
            scanner.setListener(listener);
        }
    }
}
