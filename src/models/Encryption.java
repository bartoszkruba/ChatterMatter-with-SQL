package models;

import models.Sendable;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class Encryption {

    private final char[] password;
    private final byte[] salt;
    private SecretKeyFactory factory;
    private KeySpec spec;
    private SecretKey tmp;
    private Cipher cipher;
    SecretKey secret;


    public Encryption() {
        this.password = "ch4773r_m477er".toCharArray();
        this.salt = "YouAreTheSaltOfTheWorld".getBytes();
        try {
            this.factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            this.spec = new PBEKeySpec(password, salt, 1024, 256);
            this.tmp = factory.generateSecret(spec);
            this.cipher = Cipher.getInstance("AES");
            this.secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        } catch (InvalidKeySpecException e) {
            System.out.println("Wrong key for encryption/decryption.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SealedObject encryptObject(Sendable obj) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            return new SealedObject((Serializable) obj, cipher);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
//            new SerializeObject().serializeObjectToFile(secret, Paths.get("networkKey.txt"));
        return null;
    }

    public Object decryptObject(SealedObject obj) throws InvalidKeyException, ClassNotFoundException, BadPaddingException, IllegalBlockSizeException, IOException {
        cipher.init(Cipher.DECRYPT_MODE, secret);
        return obj.getObject(cipher);

    }
}