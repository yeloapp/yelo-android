/*
 *
 *  * Copyright (C) 2015 yelo.red
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */package red.yelo.utils;

/**
 * Created by anshul1235 on 24/11/14.
 */

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AESEncrypter {

    public static String bin2hex(byte[] data)
    {
        if (data==null)
        {
            return null;
        }

        int len = data.length;
        String str = "";
        for (int i=0; i<len; i++) {
            if ((data[i]&0xFF)<16)
                str = str + "0" + java.lang.Integer.toHexString(data[i]&0xFF);
            else
                str = str + java.lang.Integer.toHexString(data[i]&0xFF);
        }
        return str;
    }

    public static byte[] hex2bin(String str) {
        if (str==null) {
            return null;
        } else if (str.length() < 2) {
            return null;
        } else {
            int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i=0; i<len; i++) {
                buffer[i] = (byte) Integer.parseInt(str.substring(i*2,i*2+2),16);
            }
            return buffer;
        }
    }

    public static String encrypt(String salt, String iv, String plaintext, String pwd) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(pwd.toCharArray(), hex2bin(salt), 1024, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        byte[] iv_bytes = hex2bin(iv);
        String value = new String(iv_bytes);
        IvParameterSpec ivspec = new IvParameterSpec(iv_bytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret, ivspec);

        byte[] encrypted = cipher.doFinal(plaintext.getBytes());

        return new String(bin2hex(encrypted));
    }

    public static String decrypt(String salt, String iv, String payload, String pwd) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(pwd.toCharArray(), hex2bin(salt), 1024, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        byte[] iv_bytes = hex2bin(iv);
        String value = new String(iv_bytes);
        IvParameterSpec ivspec = new IvParameterSpec(iv_bytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, ivspec);

        byte[] payload_bytes = hex2bin(payload);
        byte[] decrypted = cipher.doFinal(payload_bytes);

        return new String(decrypted);
    }
}