package com.entradahealth.entrada.core.inbox.encryption;

import java.io.UnsupportedEncodingException;

import org.cryptonode.jncryptor.AES256JNCryptor;
import org.cryptonode.jncryptor.JNCryptor;

import android.util.Base64;

public class AES256Cipher {
	
	  	private JNCryptor cryptor;
	 
	    public AES256Cipher() {
	    	cryptor = new AES256JNCryptor();
		}
	    
	    public String encryptText(String text, String password){
				try {
					byte[] encrypted = encrypt(text.getBytes("UTF-8"), password);
					return Base64.encodeToString(encrypted, Base64.NO_WRAP);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return text;
	    }
	    
	    public String decryptText(String text, String password){
			if (password != null && !password.isEmpty() && text!=null && !text.isEmpty()) {
					try {
						byte[] data = Base64.decode(text, Base64.NO_WRAP);
						byte[] decrypted = decrypt(data, password);
						String decryptedText = new String(decrypted, "UTF-8");
						return decryptedText;
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return text;
			} else {
				return text;
			}
	    }
	    
	    public byte[] encrypt(byte[] bytes, String password) throws Exception {   
	    	byte[] ciphertext = cryptor.encryptData(bytes, password.toCharArray());
	    	return ciphertext;
	    }
	 
	    public byte[] decrypt(byte[] bytes, String password) throws Exception {
	    	byte[] ciphertext = cryptor.decryptData(bytes, password.toCharArray());
	    	return ciphertext;
	    }

}
