package com.fundingsocieties.jumpcloudbot.services;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CredentialService {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${aws.secretName}")
    private String secretName;

    @Value("${aws.region}")
    private String region;

    @Autowired
    private Environment env;

    public String getJumpCloudToken() throws JsonProcessingException {
        String key = "jc-api-key";
        if(activeProfile.equals("prod")){
            return getSecret(key);
        }else{
            return env.getProperty(key);
        }
    }
    public String getSlackToken() throws JsonProcessingException {
        String key = "slack-token";
        if(activeProfile.equals("prod")){
            return getSecret(key);
        }else{
            return env.getProperty(key);
        }
    }

    public String getSlackSigningSecret() throws JsonProcessingException {
        String key = "slack-signing-secret";
        if(activeProfile.equals("prod")){
            return getSecret(key);
        }else{
            return env.getProperty(key);
        }
    }


    private String getSecret(String secretKey) throws JsonProcessingException {
        // Create a Secrets Manager client
        AWSSecretsManager client  = AWSSecretsManagerClientBuilder.standard()
                .withRegion(region)
                .build();
        ObjectMapper objectMapper  =  new  ObjectMapper();

        JsonNode secretsJson  =  null;
        String secret, decodedBinarySecret;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);
        GetSecretValueResult getSecretValueResult = null;
        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        } catch (DecryptionFailureException e) {
            // Secrets Manager can't decrypt the protected secret text using the provided KMS key.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (InternalServiceErrorException e) {
            // An error occurred on the server side.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (InvalidParameterException e) {
            // You provided an invalid value for a parameter.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (InvalidRequestException e) {
            // You provided a parameter value that is not valid for the current state of the resource.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (ResourceNotFoundException e) {
            // We can't find the resource that you asked for.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        }
        // Decrypts secret using the associated KMS CMK.
        // Depending on whether the secret is a string or binary, one of these fields will be populated.
        if (getSecretValueResult.getSecretString() != null) {
            secret = getSecretValueResult.getSecretString();
            secretsJson  =  objectMapper.readTree(secret);
            return secretsJson.get(secretKey).textValue();
        }
       return null;
    }
}
