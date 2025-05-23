package com.credenceid.Tap2IDLinux.SDK.application.ISO18013;
import com.credenceid.Tap2IDLinux.SDK.Utils.CertificateUtils;
import com.credenceid.Tap2IDLinux.SDK.Utils.DataIdentifierCreator;
import com.credenceid.Tap2IDLinux.SDK.Utils.IdentityState;
import com.credenceid.Tap2IDLinux.SDK.Utils.IdentityUtils;
import com.credenceid.Tap2IDLinux.SDK.application.listener.IdentityStateListener;
import com.credenceid.Tap2IDLinux.SDK.domain.usecase.ValidationDataUseCase;
import com.credenceid.Tap2IDLinux.SDK.domain.usecase.ValidationDataUseCaseImpl;
import com.credenceid.identity.Identity;
import com.credenceid.identity.PeripheralAvailability;
import com.credenceid.identity.iso18013.*;
import com.credenceid.identity.iso18013.communication.Communication;
import com.credenceid.identity.iso18013.usecase.CommunicationUseCase;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static com.credenceid.Tap2IDLinux.SDK.Utils.JavaUtils.bytesToHex;

public class VerifyMdoc {
    CommunicationBleCentralClientMode _communicationBleCentralClientMode = new CommunicationBleCentralClientMode();
    CommunicationUseCase _communicationUseCase = new CommunicationUseCase(null, _communicationBleCentralClientMode, null, null);
    PeripheralAvailability peripheralAvailability = new PeripheralAvailability();
    private static final Logger LOG = LoggerFactory.getLogger(VerifyMdoc.class);
    IdentityUtils identityUtils = new IdentityUtils();


    public CompletableFuture<Communication> executeCommunication(
            CommunicationUseCase communicationUseCase,
            DeviceEngagement deviceEngagement,
            PeripheralAvailability peripheralAvailability)
    {
        CompletableFuture<Communication> future = new CompletableFuture<>();

        LOG.info("Executing Communication UseCase");

        communicationUseCase.execute(deviceEngagement, peripheralAvailability)
                .subscribe(new SingleObserver<Communication>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        System.out.println("Pavankn Received onSubscribe");
                    }

                    @Override
                    public void onSuccess(Communication communication) {
                        LOG.info("Communication UseCase Received onSuccess, Sending Future");
                        future.complete(communication); // Complete future with the result
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("Pavankn Received onError: " + e.getMessage());
                        future.completeExceptionally(e); // Complete future with exception
                    }
                });

        return future;
    }

    public CompletableFuture<Identity> verifyMdocAsync(DeviceEngagement deviceEngagement, IdentityStateListener identityStateListener)
    {
        CompletableFuture<Identity> failedFuture = new CompletableFuture<>();

        try {
            peripheralAvailability.setHasBleSupport(true);
            peripheralAvailability.setIfBluetoothIsEnabled(true);
            peripheralAvailability.setHasNfcSupport(true);
            peripheralAvailability.setIfNfcIsEnabled(true);

            byte[] encodedDeviceEngagement = deviceEngagement.getInformation().getEncodedDeviceEngagement();

            if (encodedDeviceEngagement == null) {
                failedFuture.completeExceptionally(new IllegalArgumentException("Encoded DeviceEngagement is null"));
                return failedFuture;
            }

            identityStateListener.onStateChanged(IdentityState.CONNECTING);

            return executeCommunication(_communicationUseCase, deviceEngagement, peripheralAvailability)
                    .thenCompose(communication -> {
                        LOG.info("Pavankn Expected Communication Received: " + communication);
                        List<DataIdentifier> dataIdentifierList = DataIdentifierCreator.CreateDataIdentifierList();
                        LOG.info("Encoded DeviceEngagement: " + bytesToHex(encodedDeviceEngagement));

                        identityStateListener.onStateChanged(IdentityState.COMMUNICATION_ESTABLISHED);

                        try {
                            X509Certificate rootCA = CertificateUtils.getRootCaCertificate();
                            rootCA.checkValidity();
                            PrivateKey privateKey = CertificateUtils.getReaderCAPrivateKey();
                            LOG.info("CredenceID Private Key is: {}", privateKey);
                            return identityUtils.retrieveData(deviceEngagement, communication, dataIdentifierList, rootCA, privateKey)
                                    .thenApply(identityBytes -> {
                                        ValidationDataUseCase validationDataUseCase = new ValidationDataUseCaseImpl(identityUtils);
                                        Identity identity = validationDataUseCase.execute(identityBytes);
                                        if(identity == null){
                                            identityStateListener.onStateChanged(IdentityState.VALIDATION_FAILED);
                                            return null;
                                        }
                                        identityStateListener.onStateChanged(IdentityState.VALIDATION_SUCCESS);
                                        return identity;
                                    });
                        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
                            throw new RuntimeException("Certificate validation failed", e);
                        } catch (Exception e) {
                            throw new RuntimeException("Identity retrieval failed", e);
                        }
                    })
                    .exceptionally(e -> {
                        LOG.error("verifyMdocAsync Error: {}", e.getMessage(), e);
                        if (e instanceof TimeoutException) {
                            LOG.error("VerifyMdocAsync Timedout");
                        }
                        return null;
                    });
        } catch (Exception e) {
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }
}
