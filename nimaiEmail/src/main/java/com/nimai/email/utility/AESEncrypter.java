package com.nimai.email.utility;

import org.springframework.context.annotation.Configuration;
import org.springframework.util.SerializationUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.AttributeConverter;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.sql.Timestamp;
import java.util.Base64;

//Encryption-Decryption added by Prateek Bhawsar

@Configuration
public class AESEncrypter implements AttributeConverter<Object,String> {

    private static final String encrypttokens="this-is-test-key";
    private static final String encryptcypher="AES";

    private static Key key;
    private static Cipher cipher;

    public static Key getKey() {
        if(key==null){
            key=new SecretKeySpec(encrypttokens.getBytes(), encryptcypher);

        }
        return  key;
    }

    private static Cipher getCipher() throws GeneralSecurityException {
        if(cipher==null){
            cipher=Cipher.getInstance(encryptcypher);
        }
        return cipher;

    }

    private void initCipher(int encryptMode) throws GeneralSecurityException {
        getCipher().init(encryptMode,getKey());
    }
    @Override
    public String convertToDatabaseColumn(Object attribute) {
    	System.out.println("Inside GeneralSecurityException 1"+attribute);
        if(attribute== null) {
        	System.out.println("Inside GeneralSecurityException 2"+attribute);
            return null;
        }
        try {
            initCipher(cipher.ENCRYPT_MODE);
        	System.out.println("Inside GeneralSecurityException 3"+ cipher.ENCRYPT_MODE);
        } catch (GeneralSecurityException e) {
        	System.out.println("Inside GeneralSecurityException 4"+ e.getMessage());
            throw new RuntimeException(e);
        }
        byte[] bytes= SerializationUtils.serialize(attribute);
        try {
        	System.out.println("Inside GeneralSecurityException 5"+ Base64.getEncoder().encodeToString(getCipher().doFinal(bytes)));
            return Base64.getEncoder().encodeToString(getCipher().doFinal(bytes));
        } catch (GeneralSecurityException e) {
        	System.out.println("Inside GeneralSecurityException 6"+ e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
    	System.out.println("Inside convertToEntityAttribute 1"+dbData);
        byte[] bytes=null;
        if(dbData==null) {
        	System.out.println("Inside convertToEntityAttribute 2"+dbData);
            return null;
        }
        try {
            initCipher(Cipher.DECRYPT_MODE);
            System.out.println("Inside convertToEntityAttribute 3"+Cipher.DECRYPT_MODE);
        } catch (GeneralSecurityException e) {
        	System.out.println("Inside convertToEntityAttribute 4"+e.getMessage());
            throw new RuntimeException(e);
        }
        try {
             bytes= getCipher().doFinal(Base64.getDecoder().decode(dbData));
             System.out.println("Inside convertToEntityAttribute 5"+Cipher.DECRYPT_MODE);
        } catch (GeneralSecurityException e) {
        	System.out.println("Inside convertToEntityAttribute 6"+e.getMessage());
            throw new RuntimeException(e);
        }
        System.out.println("Inside convertToEntityAttribute 7"+SerializationUtils.deserialize(bytes));
        return  SerializationUtils.deserialize(bytes);

    }
    
	/*
	 * public Timestamp convertToDatabaseColumn(Long aLong) { if (aLong == null)
	 * return null; return new Timestamp(aLong); }
	 */
    public static void main(String[] args) {
    	AESEncrypter aesC=new AESEncrypter();
    	aesC.convertToDatabaseColumn("lovekshitij101@yopmail.com");
    	System.out.println(	aesC.convertToDatabaseColumn("lovekshitij101@yopmail.com"));
	}
}
