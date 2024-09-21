package com.destructo.botox;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class HashUtil {
    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    public static String hash(String data) {
        try {
            // SHA-256 algoritması için bir MessageDigest örneği oluşturulur
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Girdi verisini byte dizisine çevirir ve hash hesaplar
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            // Byte dizisini hex formata çevirir
            return  "a";//HexFormat.of().formatHex(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}