package com.credenceid.Tap2IDLinux.SDK.model;

public class NFCTag {
    private final String tagId;

    public NFCTag(String tagId) {
        if (tagId == null || tagId.trim().isEmpty()) {
            throw new IllegalArgumentException("NFC tag ID cannot be null or empty");
        }
        this.tagId = tagId;
    }

    public String getTagId() {
        return tagId;
    }

    @Override
    public String toString() {
        return "NfcTag{" + "tagId='" + tagId + '\'' + '}';
    }
}
