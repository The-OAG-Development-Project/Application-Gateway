package ch.gianlucafrei.nellygateway.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Base64;

public class JWEGenerator {

    private SecretKey secretKey;

    public JWEGenerator(){

        secretKey = loadKey();
    }

    protected SecretKey loadKey(){

        String key = System.getenv("NELLY-KEY");
        // decode the base64 encoded string
        byte[] decodedKey = Base64.getDecoder().decode(key);
        // rebuild key using SecretKeySpec
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    private String generateKey(){

        try {
            // Generate symmetric 128 bit AES key
            SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();
            String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            return encodedKey;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("AES key coule not be generated", e);
        }
    }

    public String encryptObject(Object payload)
    {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String payloadString = objectMapper.writeValueAsString(payload);
            return encrypt(payloadString);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not encode Json", e);
        }
    }

    private String encrypt(String payload){

        try {
            // Create JWT
            JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128GCM);
            Payload jwePayload = new Payload(payload);
            JWEObject jweObject = new JWEObject(header, jwePayload);
            jweObject.encrypt(new DirectEncrypter(secretKey));

            // Serialise to compact JOSE form
            String jweString = jweObject.serialize();
            return  jweString;

        } catch (JOSEException e) {
            throw new RuntimeException("JWE could not be encrypted", e);
        }
    }

    public <T> T decryptObject(String jwe, Class<T> clazz){
        String payload = decrypt(jwe);


        try {
            ObjectMapper objectMapper = new ObjectMapper();
            T obj = objectMapper.readValue(payload, clazz);
            return obj;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JWE could not be deserialized", e);
        }
    }

    private String decrypt(String jwe) {
        try{
            // Parse into JWE object again...
            JWEObject jweObject = JWEObject.parse(jwe);

            // Decrypt
            jweObject.decrypt(new DirectDecrypter(secretKey));

            // Get the plain text
            Payload payload = jweObject.getPayload();
            return payload.toString();
        }
        catch (JOSEException | ParseException e) {
            throw new RuntimeException("JWE could not be decrypted", e);
        }
    }
}
