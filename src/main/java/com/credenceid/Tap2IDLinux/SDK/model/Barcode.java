package com.credenceid.Tap2IDLinux.SDK.model;

public class Barcode {
    private final String value;

    public Barcode(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Barcode value cannot be null or empty");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Barcode{" + "value='" + value + '\'' + '}';
    }
}
