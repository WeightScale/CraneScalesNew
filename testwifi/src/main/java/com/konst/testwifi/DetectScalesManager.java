package com.konst.testwifi;

import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * @author Kostya 04.02.2017.
 */
public class DetectScalesManager {
    private WifiManager wifiManager;
    private DhcpInfo dhcpInfo;
    private int port;
    private int timeout;
    private OnCallBackDetectScalesManager onCallBackDetectScalesManager;
    private static final String TAG = MainActivity.class.getSimpleName();

    interface OnCallBackDetectScalesManager{
        void onReceivedIpAddress(InetSocketAddress address);
        void onExceptionFound(String ex);
    }

    DetectScalesManager(WifiManager wifiManager, int port, OnCallBackDetectScalesManager callBack){
        this.wifiManager = wifiManager;
        onCallBackDetectScalesManager = callBack;
        dhcpInfo = wifiManager.getDhcpInfo();
        this.port = port;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    private InetAddress getHostIp() throws UnknownHostException {
        /*byte[] ip = BigInteger.valueOf(dhcpInfo.dns1).toByteArray();
        return InetAddress.getByName(InetAddress.getByAddress(ip).getHostAddress());*/
        return intToInetAddress(dhcpInfo.dns1);
    }

    private InetSocketAddress getIpScales() throws ScalesNoFoundException {
        Socket mSocket;
        byte[] hostIp;
        try {
            hostIp = getHostIp().getAddress();
        } catch (UnknownHostException e) {
            throw new ScalesNoFoundException(e.getMessage());
        }
        Log.d(TAG, "Start Found Scales :");
        for(int i = 1; i <= 254; i++){
            mSocket = new Socket();
            hostIp[3] = (byte) i;
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getByAddress(hostIp), 1011);
                mSocket.connect(inetSocketAddress, timeout);
                Log.d(TAG, "Socket connected :" + inetSocketAddress.getHostName() + " timeout :" + timeout);
                BufferedReader successResult = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                int time = 10;
                StringBuilder successMsg = new StringBuilder();
                //while (!successResult.ready() /*&& --time > 0*/){
                    if (successResult.ready())
                        successMsg.append(successResult.readLine());
                    try {TimeUnit.MILLISECONDS.sleep(100);} catch (InterruptedException e) {}
                //}
                mSocket.close();
                return inetSocketAddress;
            }catch (IOException e){
                try {mSocket.close();} catch (IOException e1) {
                    //throw new ScalesNoFoundException(e1.getMessage());
                }
                //throw new ScalesNoFoundException(e.getMessage());
            }
        }
        throw new ScalesNoFoundException("No found scales to network");
    }

    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = { (byte)(0xff & hostAddress),
                (byte)(0xff & (hostAddress >> 8)),
                (byte)(0xff & (hostAddress >> 16)),
                (byte)(0xff & (hostAddress >> 24)) };

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    private Runnable foundScales = new Runnable() {
        @Override
        public void run() {
            try {
                onCallBackDetectScalesManager.onReceivedIpAddress(getIpScales());
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                onCallBackDetectScalesManager.onExceptionFound(e.getMessage());
            }
        }
    };

    public void startFound(){
        new Thread(foundScales).start();
    }
}
