package com.credenceid.Tap2IDLinux.SDK.Utils;

import com.credenceid.identity.Identity;
import com.credenceid.identity.iso18013.DataIdentifier;
import com.credenceid.identity.iso18013.DeviceResponseParser.Document;

import java.util.HashMap;

public class DocumentHelper {

    public void getDataIdentifiers(
            Document document,
            String namespace,
            Identity identity,
            HashMap<String, Boolean> hashMapOfDataItemsInResponse,
            HashMap<String, Boolean> hashMapOfDataItemsInRequest)
    {
        for (String elem : document.getIssuerEntryNames(namespace)) {
            if (elem.equals(DataIdentifier.Portrait.value)) {
                identity.setPortrait(
                        getByteArrayValue(document, namespace, elem, hashMapOfDataItemsInResponse)
                );
            }else if (elem.equals(DataIdentifier.FamilyName.value)) {
                identity.setFamilyName(
                        getStringValue(document, namespace, elem, hashMapOfDataItemsInResponse)
                );
            } else if (elem.equals(DataIdentifier.GivenNames.value)) {
                identity.setGivenNames(
                        getStringValue(document, namespace, elem, hashMapOfDataItemsInResponse)
                );
            }
        }
    }

    public String getStringValue(
            Document document,
            String namespace,
            String dataIdentifier,
            HashMap<String, Boolean> hashMapOfDataItemsInResponse)
    {
        String value = document.getIssuerEntryString(namespace, dataIdentifier);
        hashMapOfDataItemsInResponse.put(dataIdentifier.toLowerCase(), value != null && !value.isEmpty());
        return value;
    }

    public byte[] getByteArrayValue(
            Document document,
            String namespace,
            String dataIdentifier,
            HashMap<String, Boolean> hashMapOfDataItemsInResponse)
    {
        byte[] value = document.getIssuerEntryByteString(namespace, dataIdentifier);
        hashMapOfDataItemsInResponse.put(dataIdentifier.toLowerCase(), value != null && value.length > 0);
        return value;
    }


}
