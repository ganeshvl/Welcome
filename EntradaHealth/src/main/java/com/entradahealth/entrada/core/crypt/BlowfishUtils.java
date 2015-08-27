package com.entradahealth.entrada.core.crypt;

import com.google.common.base.Charsets;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Because we're going to do enough blowfishing that repeating it
 * everywhere could be lame.
 *
 * "Blowfishing" (C) 2012 Ed Ropple
 *
 *
 * @author edr
 * @since 13 Sep 2012
 */
public class BlowfishUtils
{
    private static final String CIPHER_TYPE = "Blowfish";

    private BlowfishUtils() {}

    public static Cipher getCipher(int mode, String key) throws EncryptionException {
        try
        {
            byte[] keyData = key.getBytes(Charsets.UTF_8);
            SecretKeySpec keySpec = new SecretKeySpec(keyData, CIPHER_TYPE);

            Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
            cipher.init(mode, keySpec);

            return cipher;
        }
        catch (Exception e)
        {
            throw new EncryptionException(e);
        }
    }

    public static byte[] encrypt(String plaintext, String key)
    {
        try
        {
            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(plaintext.getBytes(Charsets.UTF_8));
        }
        catch (Exception e)
        {
            throw new EncryptionException(e);
        }
    }

    public static String decrypt(byte[] encrypted, String key)
    {
        try
        {
            Cipher cipher = getCipher(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, Charsets.UTF_8);
        }
        catch (Exception e)
        {
            throw new EncryptionException(e);
        }
    }



    public static class EncryptionException extends RuntimeException
    {
        public EncryptionException()
        {
        }

        public EncryptionException(String s)
        {
            super(s);
        }

        public EncryptionException(String s, Throwable throwable)
        {
            super(s, throwable);
        }

        public EncryptionException(Throwable throwable)
        {
            super(throwable);
        }
    }
}
