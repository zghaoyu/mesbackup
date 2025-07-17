package com.cncmes.utils;

import java.io.IOException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.codec.binary.Base64;

public class DesUtils {
    private final static String DES = "DES";
    private final static String ENCODE = "GBK";
    private final static String defaultKey = "test1234";
    
    /**
     * 
     * @param data the string to be encrypted
     * @return the encryption string of the data
     * @throws Exception
     */
    public static String encrypt(String data) throws Exception {
        byte[] bt = encrypt(data.getBytes(ENCODE), defaultKey.getBytes(ENCODE));
        String strs = new Base64().encodeToString(bt);
        
        return strs;
    }
    
    /**
     * 
     * @param data the string to be decrypted
     * @return the decryption string of the data
     * @throws IOException
     * @throws Exception
     */
    public static String decrypt(String data) throws IOException, Exception {
        if (data == null)
            return null;
        Base64 decoder = new Base64();
        byte[] buf = decoder.decode(data);
        byte[] bt = decrypt(buf, defaultKey.getBytes(ENCODE));
        return new String(bt, ENCODE);
    }
    
    /**
     * 
     * @param data the string to be encrypted
     * @param key the key to encrypt the data
     * @return the encryption string of the data
     * @throws Exception
     */
    public static String encrypt(String data, String key) throws Exception {
        byte[] bt = encrypt(data.getBytes(ENCODE), defaultKey.getBytes(ENCODE));
        String strs = new Base64().encodeToString(bt);
        return strs;
    }
    
    /**
     * 
     * @param data the string to be decrypted
     * @param key the key to decrypt the data
     * @return the decryption string of the data
     * @throws IOException
     * @throws Exception
     */
    public static String decrypt(String data, String key) throws IOException,
            Exception {
        if (data == null)
            return null;
        Base64 decoder = new Base64();
        byte[] buf = decoder.decode(data);
        byte[] bt = decrypt(buf, key.getBytes(ENCODE));
        return new String(bt, ENCODE);
    }

    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        // Create a secure randomized source
        SecureRandom sr = new SecureRandom();
        
        // Create DESKeySpec Object with the provided key
        DESKeySpec dks = new DESKeySpec(key);
        
        // Transfer DESKeySpec into SecretKey
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);
        
        // Create Cipher to complete the encryption
        Cipher cipher = Cipher.getInstance(DES);
        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
        
        return cipher.doFinal(data);
    }
    
    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
    	// Create a secure randomized source
        SecureRandom sr = new SecureRandom();
        
        // Create DESKeySpec Object with the provided key
        DESKeySpec dks = new DESKeySpec(key);
        
        // Transfer DESKeySpec into SecretKey
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);
        
        // Create Cipher to complete the decryption
        Cipher cipher = Cipher.getInstance(DES);
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
        
        return cipher.doFinal(data);
    }
}