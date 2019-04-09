package com.example.securefm;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.spongycastle.crypto.digests.GOST3411_2012_256Digest;
import org.spongycastle.jce.provider.BouncyCastleProvider;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Encription extends AppCompatActivity {
    private Context context;


    //Хэш от пароля
    public BigInteger GetDigest(byte[] pass){
        GOST3411_2012_256Digest gost3411_2012_256Digest = new GOST3411_2012_256Digest();
        gost3411_2012_256Digest.update(pass, 0, pass.length);
        byte[] digest = new byte[gost3411_2012_256Digest.getDigestSize()];
        gost3411_2012_256Digest.doFinal(digest, 0);
        return new BigInteger(1, digest);
    }

    //Генерация соли
    public byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte salt[] = new byte[256];
        random.nextBytes(salt);
        return salt;
    }

    //Генерация вектора инициализации для ГОСТ28147 размером 8 байт (для AES - 16 байт)
    public byte[] generateIv(){
        SecureRandom ivRandom = new SecureRandom();
        byte[] iv = new byte[8];
        ivRandom.nextBytes(iv);
        return iv;
    }

    //Генерация ключа на основе пользовательского пароля
    public SecretKey generateSecretKey(String passwordString, byte[] salt) {
        SecretKeySpec keySpec = null;
        try {
            char[] passwordChar = passwordString.toCharArray();
            PBEKeySpec pbKeySpec = new PBEKeySpec(passwordChar, salt, 1324, 256);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] keyBytes = secretKeyFactory.generateSecret(pbKeySpec).getEncoded();
            Security.addProvider(new BouncyCastleProvider());
            //keySpec = new SecretKeySpec(keyBytes, "GOST3412");
            keySpec = new SecretKeySpec(keyBytes, "GOST-28147");
        } catch (Exception ex) {
            Log.e("Key generation", ex.getMessage());
        }
        return keySpec;
    }

    //Шифрование файла
    public void encryptFile(File file, String pass, String path, Context context) {
        try {
            FileInputStream fin = context.openFileInput("salt");
            byte[] salt = new byte[fin.available()];
            fin.read(salt);
            fin.close();

            fin = context.openFileInput("IV");
            byte[] IV = new byte[fin.available()];
            fin.read(IV);
            fin.close();

            SecretKey secretKey = generateSecretKey(pass, salt);

            fin = new FileInputStream(file);
            byte[] fileBytes = new byte[fin.available()];
            fin.read(fileBytes);
            fin.close();

            Security.addProvider(new BouncyCastleProvider());
            //Cipher cipher = Cipher.getInstance("GOST-28147/CBC/PKCS7Padding", "SC");
            Cipher cipher = Cipher.getInstance("GOST-28147/CBC/PKCS7Padding", "SC");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV));
            byte[] encryptedFileBytes = cipher.doFinal(fileBytes);
            FileOutputStream fos = new FileOutputStream(path + "/" + file.getName() + "_encrypted");
            fos.write(encryptedFileBytes);
            fos.close();
        } catch (Exception ex) {
            Log.e("Encryption eror", ex.getMessage());
        }
    }

    public void decryptFile(File file, String pass, String path, Context context) {
        try {
            FileInputStream fin = context.openFileInput("salt");
            byte[] salt = new byte[fin.available()];
            fin.read(salt);
            fin.close();

            fin = context.openFileInput("IV");
            byte[] IV = new byte[fin.available()];
            fin.read(IV);
            fin.close();

            SecretKey secretKey = generateSecretKey(pass, salt);

            fin = new FileInputStream(file);
            byte[] fileBytes = new byte[fin.available()];
            fin.read(fileBytes);
            fin.close();

            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance("GOST-28147/CBC/PKCS7Padding", "SC");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV));
            byte[] encryptedFileBytes = cipher.doFinal(fileBytes);
            FileOutputStream fos = new FileOutputStream(path + "/" + file.getName() + "_decrypted");
            fos.write(encryptedFileBytes);
            fos.close();
        } catch (Exception ex) {
            Log.e("Encryption eror", ex.getMessage());
        }
    }

}
