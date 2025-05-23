package com.credenceid.Tap2IDLinux.SDK.Utils;

import co.nstant.in.cbor.model.SimpleValue;
import com.credenceid.identity.IdType;
import com.credenceid.identity.Identity;
import com.credenceid.identity.iso18013.*;
import com.credenceid.identity.iso18013.communication.Communication;
import com.credenceid.identity.iso18013.cosekey.CoseKey;
import com.credenceid.identity.iso18013.exceptions.DeviceRetrievalStatusCborDecodingError;
import com.credenceid.identity.iso18013.exceptions.DeviceRetrievalStatusCborValidationError;
import com.credenceid.identity.iso18013.exceptions.DeviceRetrievalStatusGeneralError;
import com.credenceid.identity.iso18013.exceptions.UserCancelledException;
import com.credenceid.identity.iso18013.DeviceResponseParser;
import com.credenceid.identity.iso18013.DeviceResponseParser.DeviceResponse;

import com.credenceid.identity.iso18013.usecase.DataRetrievalUseCase;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.credenceid.Tap2IDLinux.SDK.Utils.JavaUtils.bytesToHex;

public class IdentityUtils {
    private static final Logger LOG = LoggerFactory.getLogger(IdentityUtils.class);
    private static final int TIMEOUT = 30;
    private static SessionEncryptionReader sessionEncryptionReader;
    private static final String DOC_TYPE_ORG_ISO_180135_5_1_MDL = "org.iso.18013.5.1.mDL";
    private static String NAMESPACE_ORG_ISO_180135_5_1 = "org.iso.18013.5.1";
    private final HashMap<String, Boolean> hashMapOfDataItemsInRequest = new HashMap<>();
    private final HashMap<String, Boolean> hashMapOfDataItemsInResponse = new HashMap<>();
    DocumentHelper documentHelper = new DocumentHelper();
    private DeviceEngagement deviceEngagement;
    DataRetrievalUseCase _dataRetrievalUseCase = new DataRetrievalUseCase();
    KeyPair ephemeralKeyPair;

    public DeviceResponse getDeviceResponse(byte[] dataRead) {
        LOG.info("Trying To Decrypt : " + bytesToHex(dataRead));

        try{
            Pair<byte[], OptionalInt> decryptedMessage = sessionEncryptionReader.decryptMessageFromDevice(dataRead);
            LOG.info("DecryptedMessage: " + decryptedMessage);
            DeviceResponseParser parser = new DeviceResponseParser();
            parser.setEphemeralReaderKey(ephemeralKeyPair.getPrivate());
            parser.setSessionTranscript(sessionEncryptionReader.getSessionTranscript());
            parser.setDeviceResponse((byte[])decryptedMessage.first);
            return parser.parse();
        } catch (UserCancelledException e) {
            throw new RuntimeException(e);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private void establishSecuredSession(DeviceEngagement deviceEngagement) {
        LOG.info("retrieveData - Establishing Secure session...");

        byte[] handOver;
        switch (deviceEngagement.getType()) {
            case NFC_STATIC:
                LOG.info("retrieveData - Using NFC with static handover...");
                handOver = deviceEngagement.getHandover().getReaderHandover();
                break;
            case NFC_NEGOTIATED:
                LOG.info("retrieveData - Using NFC with negotiated handover...");
                handOver = deviceEngagement.getHandover().getReaderHandover();
                break;
            default:
                LOG.info("retrieveData - Other...");
                handOver = Util.cborEncode(SimpleValue.NULL);
                break;
        }

        CoseKey coseKey = new CoseKey(deviceEngagement.getInformation().getSecurity().getEDeviceKeyBytes());
        ephemeralKeyPair = Util.createEphemeralKeyPair(Constants.ALGORITHM_EC, coseKey);

        if(ephemeralKeyPair == null){
            LOG.info("Ephemeral KeyPair is NULL");
        }

        try{
            sessionEncryptionReader = new SessionEncryptionReader(
                    ephemeralKeyPair.getPrivate(),
                    ephemeralKeyPair.getPublic(),
                    coseKey,
                    deviceEngagement.getInformation().getEncodedDeviceEngagement(),
                    handOver
            );
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }


    public Identity getIdentityFromDeviceResponse(DeviceResponse deviceResponse) throws Exception {
        switch (deviceResponse.getStatus()) {
            case DeviceResponseParser.DEVICE_RESPONSE_STATUS_OK: {
                Identity identity = new Identity();

                for (DeviceResponseParser.Document doc : deviceResponse.getDocuments()) {
                    switch (doc.getDocType()) {
                        case DOC_TYPE_ORG_ISO_180135_5_1_MDL: {
                            identity.setType(IdType.ISO18013);
                            for (String ns : doc.getIssuerNamespaces()) {
                                LOG.debug("Mdl Namespace: {}", ns);
                                if (NAMESPACE_ORG_ISO_180135_5_1.equals(ns)) {
                                    validateDataElements(doc, ns, identity);
                                }
                            }
                            break;
                        }
                    }
                }
                return identity;
            }

            case DeviceResponseParser.DEVICE_RESPONSE_STATUS_GENERAL_ERROR: {
                if (deviceResponse.getDocuments().isEmpty()) {
                    throw new UserCancelledException();
                }
                throw new DeviceRetrievalStatusGeneralError();
            }

            case DeviceResponseParser.DEVICE_RESPONSE_STATUS_CBOR_DECODING_ERROR:
                throw new DeviceRetrievalStatusCborDecodingError();

            case DeviceResponseParser.DEVICE_RESPONSE_STATUS_CBOR_VALIDATION_ERROR:
                throw new DeviceRetrievalStatusCborValidationError();

            default:
                throw new UnknownError();
        }
    }

    private void validateDataElements(
            DeviceResponseParser.Document doc,
            String ns,
            Identity identity)
    {
        documentHelper.getDataIdentifiers(
                doc,
                ns,
                identity,
                hashMapOfDataItemsInResponse,
                hashMapOfDataItemsInRequest);

        LOG.info("Pavankn Identity FamilyName: " + identity.getFamilyName());
        LOG.info("Pavankn Identity GivenName: " + identity.getGivenNames());
    }

    public static DocumentRequests addAuthenticationAndGenerateRequest(
            DocType docType,
            List<NameSpace> nameSpaces,
            String requestInfo,
            byte[] encodedSessionTranscript) {

        ItemsRequest itemsRequest = new ItemsRequest(docType, nameSpaces, requestInfo);
        DocumentRequests docRequest = new DocumentRequests();
        ReaderAuthentication readerAuthentication = new ReaderAuthentication();

        readerAuthentication.setSessionTranscript(encodedSessionTranscript);
        docRequest.addReaderAuthentication(readerAuthentication);
        docRequest.addItemsRequest(itemsRequest);

        return docRequest;
    }

    public DeviceRequest generateDeviceRequest(List<DataIdentifier> dataIdentifiers)
    {
        DocType docType = new DocType(DOC_TYPE_ORG_ISO_180135_5_1_MDL);
        List<NameSpace> nameSpaces = new ArrayList<>();
        NameSpace namespace = new NameSpace(NAMESPACE_ORG_ISO_180135_5_1);
        nameSpaces.add(namespace);

        for (DataIdentifier dataIdentifier : DataIdentifier.values()) {
            if (dataIdentifiers.contains(dataIdentifier)) {
                hashMapOfDataItemsInRequest.put(dataIdentifier.value, true);
                namespace.addDataElement(new DataElement(dataIdentifier, false));
            }
        }
        hashMapOfDataItemsInRequest.forEach((key, value) -> LOG.info("HashMap Key: " + key + " Value: " + value));

        String emptyString = "";

        byte[] encodedSessionTranscript = deviceEngagement.getInformation().getEncodedDeviceEngagement();
        DocumentRequests docRequest = addAuthenticationAndGenerateRequest(docType, nameSpaces, emptyString, encodedSessionTranscript);

        return new DeviceRequest(docRequest);
    }


    public CompletableFuture<byte[]> retrieveData(
            DeviceEngagement deviceEngagement,
            Communication communication,
            List<DataIdentifier> dataIdentifiers,
            X509Certificate readerCA,
            PrivateKey privateKey)
    {
        CompletableFuture<byte[]> future = new CompletableFuture<>();

        LOG.info("DataRetrievalUseCase Execute");

        this.deviceEngagement = deviceEngagement;
        establishSecuredSession(deviceEngagement);
        DeviceRequest deviceRequest = generateDeviceRequest(dataIdentifiers);

        _dataRetrievalUseCase.execute(
                        communication,
                        deviceRequest,
                        sessionEncryptionReader,
                        readerCA,
                        privateKey
                )
                .timeout(TIMEOUT, TimeUnit.SECONDS) // only if needed
                .subscribe(new SingleObserver<byte[]>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        System.out.println("Data Retrieval OnSubscribe");
                    }

                    @Override
                    public void onSuccess(byte[] bytes) {
                        LOG.info("Data Retrieval Success, Validating: ");
                        try {
                            future.complete(bytes);
                        } catch (Exception ex) {
                            future.completeExceptionally(ex);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        LOG.error("Data Retrieval Error", e);
                        future.completeExceptionally(e);
                    }
                });
        return future.orTimeout(5, TimeUnit.SECONDS);
    }

}
