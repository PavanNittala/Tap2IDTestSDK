package com.credenceid.Tap2IDLinux.SDK.Utils;

import com.credenceid.identity.iso18013.DataIdentifier;

import java.util.ArrayList;
import java.util.List;

public class DataIdentifierCreator {
    public static List<DataIdentifier> CreateDataIdentifierList() {
        List<DataIdentifier> dataIdentifierList = new ArrayList<>();

//        dataIdentifierList.add(DataIdentifier.AgeOver21);
//        dataIdentifierList.add(DataIdentifier.IssuingAuthority);
        dataIdentifierList.add(DataIdentifier.Portrait);
//        dataIdentifierList.add(DataIdentifier.AgeInYears);
        dataIdentifierList.add(DataIdentifier.GivenNames);
        dataIdentifierList.add(DataIdentifier.FamilyName);

        return dataIdentifierList;
    }

}
