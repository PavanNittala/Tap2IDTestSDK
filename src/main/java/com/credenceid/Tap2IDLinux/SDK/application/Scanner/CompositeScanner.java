package com.credenceid.Tap2IDLinux.SDK.application.Scanner;
import java.util.Arrays;
import java.util.List;

public class CompositeScanner implements Tap2IDScanner{
    private final List<Tap2IDScanner> scanners;

    public CompositeScanner(Tap2IDScanner... scanners) {
        this.scanners = Arrays.asList(scanners);
    }

    @Override
    public void start() {
        for(Tap2IDScanner scanner : scanners){
            scanner.start();
        }
    }

    @Override
    public void stop() {
        for(Tap2IDScanner scanner : scanners){
            scanner.stop();
        }
    }

    @Override
    public void setListener(ScannerListener listener) {
        for (Tap2IDScanner scanner : scanners) {
            scanner.setListener(listener);
        }
    }
}
