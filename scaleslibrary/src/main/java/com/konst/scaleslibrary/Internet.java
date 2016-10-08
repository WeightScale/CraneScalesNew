package com.konst.scaleslibrary;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Управляет соединениями (Bluetooth, Wi-Fi, мобильная сеть)
 *
 * @author Kostya
 */
public class Internet {
    private final Context context;
    /**
     * Менеджер телефона
     */
    private TelephonyManager telephonyManager;
    /**
     * Слушатель менеджера телефона
     */
    private PhoneStateListener phoneStateListener;

    public static final String INTERNET_CONNECT = "com.kostya.cranescale.INTERNET_CONNECT";
    public static final String INTERNET_DISCONNECT = "com.kostya.cranescale.INTERNET_DISCONNECT";

    public Internet(Context c) {
        context = c;
    }

    /**
     * Проверяем подключение к интернету.
     * @return true - есть соединение.
     */
    public static boolean isOnline() {
        try {
            Process p1 = Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            return returnVal == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Выполнить соединение с интернетом по wifi.
     * @param on true - включить.
     */
    public void turnOnWiFiConnection(boolean on) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return;
        }
        wifi.setWifiEnabled(on);
        while (wifi.isWifiEnabled() != on) ;
    }

    /**
     * Послать ссылку в интернет.
     *
     * @param url Ссылка.
     * @return true - ответ ОК.
     */
    protected static boolean send(URL url) {
        try {
            URLConnection urlConnection = url.openConnection();
            if (!(urlConnection instanceof HttpURLConnection)) {
                throw new IOException("URL is not an Http URL");
            }
            HttpURLConnection connection = (HttpURLConnection) urlConnection;
            //connection.setReadTimeout(3000);
            //connection.setConnectTimeout(3000);
            connection.connect();
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;

        } catch (MalformedURLException ignored) {
            return false;
        } catch (IOException ignored) {
            return false;
        }
    }

}
