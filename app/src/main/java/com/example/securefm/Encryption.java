package com.example.securefm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.bouncycastle.crypto.digests.GOST3411_2012_256Digest;
import org.bouncycastle.crypto.engines.GOST28147Engine;
import org.bouncycastle.crypto.engines.GOST3412_2015Engine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption extends AppCompatActivity {
    private Context context;
    Encryption(Context context) {
        this.context = context;
    }

    TimerDataBaseHelper timerDataBaseHelper;
    SQLiteDatabase db;
    static {
        BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
        Security.removeProvider(bouncyCastleProvider.getName());
        Security.addProvider(bouncyCastleProvider);
    }

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

    //Генерация вектора инициализации для "Магма"/"Кузнечик" размером 8/16 байт
    public byte[] generateIv(int i){
        SecureRandom ivRandom = new SecureRandom();
        byte[] iv = new byte[i];
        ivRandom.nextBytes(iv);
        return iv;
    }

    //Генерация ключа на основе пользовательского пароля
    public SecretKey generateSecretKey(String passwordString, byte[] salt, String algorithm) {
        SecretKeySpec keySpec = null;
        try {
            char[] passwordChar = passwordString.toCharArray();
            PBEKeySpec pbKeySpec = new PBEKeySpec(passwordChar, salt, 1324, 256);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHMacGOST3411");
            byte[] keyBytes = secretKeyFactory.generateSecret(pbKeySpec).getEncoded();
            Security.addProvider(new BouncyCastleProvider());
            keySpec = new SecretKeySpec(keyBytes, algorithm);
        } catch (Exception ex) {
            Log.e("Key generation", ex.getMessage());
        }
        return keySpec;
    }

    //Шифрование файла
    public void encryptFile(File file, String pass) {
        String algorithm = context.getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("algorithm", "GOST-28147");
        String path = context.getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE).getString("homeDir", "/storage");
        String mode = context.getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("mode", "ECB");
        try {
            FileInputStream fin = context.openFileInput("salt");
            byte[] salt = new byte[fin.available()];
            fin.read(salt);
            fin.close();

            SecretKey secretKey = generateSecretKey(pass, salt, algorithm);

            Cipher cipher;
            if (algorithm.equals("GOST-28147")) {
                if (mode.equals("ECB")) {
                    cipher = Cipher.getInstance(algorithm + "/" + mode + "/PKCS7Padding", "BC");
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                } else if (mode.equals("CTR")) {
                    cipher = Cipher.getInstance(algorithm + "/" + mode + "/NoPadding", "BC");

                    fin = context.openFileInput("IV4");
                    byte[] IV4 = new byte[fin.available()];
                    fin.read(IV4);
                    fin.close();

                    cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV4));
                } else {
                    cipher = Cipher.getInstance(algorithm + "/" + mode + "/PKCS7Padding", "BC");

                    fin = context.openFileInput("IV8");
                    byte[] IV8 = new byte[fin.available()];
                    fin.read(IV8);
                    fin.close();

                    cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV8));
                }
            } else {
                if (mode.equals("ECB")) {
                    cipher = Cipher.getInstance(algorithm + "/" + mode + "/PKCS7Padding", "BC");
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                } else if (mode.equals("CTR")) {
                    cipher = Cipher.getInstance(algorithm + "/SIC/NoPadding", "BC");

                    fin = context.openFileInput("IV8");
                    byte[] IV8 = new byte[fin.available()];
                    fin.read(IV8);
                    fin.close();

                    cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV8));
                } else {
                    cipher = Cipher.getInstance(algorithm + "/" + mode + "/PKCS7Padding", "BC");

                    fin = context.openFileInput("IV16");
                    byte[] IV16 = new byte[fin.available()];
                    fin.read(IV16);
                    fin.close();

                    cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV16));
                }
            }

            FileOutputStream fos = new FileOutputStream(path + "/" + file.getName() + "_encrypted" + algorithm);
            CipherOutputStream cos = new CipherOutputStream(fos, cipher);
            fin = new FileInputStream(file);
            int  b;
            byte[] d = new byte[128*1024];
            long start = System.currentTimeMillis();
            while ((b = fin.read(d)) != -1) {
                cos.write(d, 0, b);
            }
            cos.flush();
            cos.close();
            fin.close();
            long stop = System.currentTimeMillis();

            timerDataBaseHelper = new TimerDataBaseHelper(context);
            db = timerDataBaseHelper.getWritableDatabase();
            timerDataBaseHelper.insertTime(db, file.getName(), (int)file.length(), mode, algorithm, (int)(stop - start));
            Log.i("Encryption time with", algorithm + "/" + mode + ": " +
                    file.getName() + " (" + file.length() + " б) - " + (double)(stop - start)/10 + " мс");
        } catch (Exception ex) {
            Log.e("Encryption error", algorithm + "/" + mode + ": " + ex.getMessage());
        }
    }

    public void decryptFile(File file, String pass) {
        String algorithm = context.getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("algorithm", "GOST-28147");
        String path = context.getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE).getString("homeDir", "/storage");
        String mode = context.getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("mode", "ECB");
        try {
            FileInputStream fin = context.openFileInput("salt");
            byte[] salt = new byte[fin.available()];
            fin.read(salt);
            fin.close();

            SecretKey secretKey = generateSecretKey(pass, salt, algorithm);

            Cipher cipher;
            if (algorithm.equals("GOST-28147")) {
                if (mode.equals("ECB")) {
                    cipher = Cipher.getInstance(algorithm + "/" + mode + "/PKCS7Padding", "BC");
                    cipher.init(Cipher.DECRYPT_MODE, secretKey);
                } else if (mode.equals("CTR")) {
                    cipher = Cipher.getInstance(algorithm + "/" + mode + "/NoPadding", "BC");

                    fin = context.openFileInput("IV4");
                    byte[] IV4 = new byte[fin.available()];
                    fin.read(IV4);
                    fin.close();

                    cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV4));
                } else {
                    cipher = Cipher.getInstance(algorithm + "/" + mode + "/PKCS7Padding", "BC");

                    fin = context.openFileInput("IV8");
                    byte[] IV8 = new byte[fin.available()];
                    fin.read(IV8);
                    fin.close();

                    cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV8));
                }
            } else {
                if (mode.equals("ECB")) {
                    cipher = Cipher.getInstance(algorithm + "/" + mode + "/PKCS7Padding", "BC");
                    cipher.init(Cipher.DECRYPT_MODE, secretKey);
                } else if (mode.equals("CTR")) {
                    cipher = Cipher.getInstance(algorithm + "/SIC/NoPadding", "BC");

                    fin = context.openFileInput("IV8");
                    byte[] IV8 = new byte[fin.available()];
                    fin.read(IV8);
                    fin.close();

                    cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV8));
                } else {
                    cipher = Cipher.getInstance(algorithm + "/" + mode + "/PKCS7Padding", "BC");

                    fin = context.openFileInput("IV16");
                    byte[] IV16 = new byte[fin.available()];
                    fin.read(IV16);
                    fin.close();

                    cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV16));
                }
            }

            fin = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(path + "/" + file.getName() + "_decrypted" + algorithm);
            CipherOutputStream cos = new CipherOutputStream(fos, cipher);
            int  b;
            byte[] d = new byte[128*1024];
            long start = System.currentTimeMillis();
            while ((b = fin.read(d)) != -1) {
                cos.write(d, 0, b);
            }
            cos.flush();
            cos.close();
            fin.close();
            long stop = System.currentTimeMillis();

            timerDataBaseHelper = new TimerDataBaseHelper(context);
            db = timerDataBaseHelper.getWritableDatabase();
            timerDataBaseHelper.insertTime(db, file.getName(), (int)file.length(), "DECRYPTION", algorithm, (int)(stop - start));
        } catch (Exception ex) {
            Log.e("Encryption error", ex.getMessage());
        }
    }
}
