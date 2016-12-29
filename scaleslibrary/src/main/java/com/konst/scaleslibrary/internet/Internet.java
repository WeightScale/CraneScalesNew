package com.konst.scaleslibrary.internet;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Управляет соединениями (Bluetooth, Wi-Fi, мобильная сеть)
 *
 * @author Kostya
 */
public class Internet {
    private final Context mContext;
    /** Менеджер телефона */
    private TelephonyManager telephonyManager;
    /** Слушатель менеджера телефона */
    private PhoneStateListener phoneStateListener;

    public static final String INTERNET_CONNECT = "com.kostya.cranescale.internet.INTERNET_CONNECT";
    public static final String INTERNET_DISCONNECT = "com.kostya.cranescale.internet.INTERNET_DISCONNECT";

    public Internet(Context c) {
        mContext = c;
    }

    /**
     * Сделать соединение с интернетом
     */
    public void connect() {
        telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onDataConnectionStateChanged(int state) {
                switch (state) {
                    case TelephonyManager.DATA_DISCONNECTED:
                        if (telephonyManager != null) {
                            turnOnDataConnection(true);
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);


        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            turnOnWiFiConnection(true);
        }

        turnOnDataConnection(true);

    }

    /**
     * Выполнить отсоединение от интернета
     */
    public void disconnect() {
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            telephonyManager = null;
        }

        turnOnDataConnection(false);
        turnOnWiFiConnection(false);
    }

    /**
     * Получить интернет соединение.
     *
     * @param timeout      Задержка между попытками.
     * @param countAttempt Количество попыток.
     * @return true - интернет соединение установлено.
     */
    public boolean getConnection(int timeout, int countAttempt) {
        while (countAttempt != 0) {
            if (isOnline())
                return true;
            turnOnWiFiConnection(true);
            try {
                Thread.sleep(timeout);} catch (InterruptedException ignored) {}
            countAttempt--;
        }
        return false;
    }

    /**
     * Проверяем подключение к интернету.
     *
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

    public boolean isInternetConnect(){
        ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    /**
     * Выполнить соединение с интернетом по wifi.
     *
     * @param on true - включить.
     */
    public void turnOnWiFiConnection(boolean on) {
        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return;
        }
        wifi.setWifiEnabled(on);
        while (wifi.isWifiEnabled() != on) ;
    }

    /**
     * Выполнить соединение с интернетом по mobile data.
     *
     * @param on true - включить.
     */
    private void turnOnDataConnection(boolean on) {
        /* Настройки администратора мобильный интернет */
        //if(Globals.getInstance().getPreferencesScales().read(mContext.getString(R.string.KEY_MOBIL_INTERNET), false)){
            try {
                int bv = Build.VERSION.SDK_INT;
                //int bv = Build.VERSION_CODES.FROYO;
                if (bv == Build.VERSION_CODES.FROYO) { //2.2

                    TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

                    Class<?> telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                    Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
                    getITelephonyMethod.setAccessible(true);
                    Object ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
                    Class<?> ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());

                    Method dataConnSwitchMethod = on ? ITelephonyClass.getDeclaredMethod("enableDataConnectivity") : ITelephonyClass.getDeclaredMethod("disableDataConnectivity");

                    dataConnSwitchMethod.setAccessible(true);
                    dataConnSwitchMethod.invoke(ITelephonyStub);
                } else if (bv <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    ConnectivityManager dataManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                    Method setMobileDataEnabledMethod = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
                    setMobileDataEnabledMethod.setAccessible(true);
                    setMobileDataEnabledMethod.invoke(dataManager, on);
                } else {
                    ConnectivityManager dataManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

                    Method dataMtd = null;
                    try {
                        dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
                    } catch (SecurityException | NoSuchMethodException e) {
                        //// TODO: 09.07.2016  
                    }

                    assert dataMtd != null;
                    dataMtd.setAccessible(true);
                    try {
                        dataMtd.invoke(dataManager, on);
                    } catch (Exception e) {
                        //// TODO: 09.07.2016  
                    }
                }
                return;
            } catch (Exception ignored) {
                Log.e("hhh", "error turning on/off data");
                return;
            }
        //}
        //return false;
    }

}
