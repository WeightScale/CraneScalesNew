package com.konst.scaleslibrary.module.usb;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.konst.scaleslibrary.module.Commands;
import com.konst.scaleslibrary.module.InterfaceTransferClient;
import com.konst.scaleslibrary.module.ObjectCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.content.Context.USB_SERVICE;

/**
 * @author Kostya  on 21.01.2017.
 */

public class SerialPort implements InterfaceTransferClient {
    private Context mContext;
    private UsbBroadcastReceiver usbBroadcastReceiver;
    private UsbSerialDevice usbSerialDevice;
    private UsbManager usbManager;
    private ObjectCommand response;
    private int usbDeviceId;
    private static final String TAG = SerialPort.class.getSimpleName();
    public static final String ACTION_USB_PERMISSION = "com.kostya.scalesusb.USB_PERMISSION";

    //abstract void onReceivedData(byte[] var1);
    //abstract void dettachPort();
    //abstract void attachPort();

    public SerialPort(Context context){
        mContext = context;
        usbManager = (UsbManager) mContext.getSystemService(USB_SERVICE);
        usbBroadcastReceiver = new UsbBroadcastReceiver(mContext);
        usbBroadcastReceiver.register();
        finedUsbDevice();
    }

    private void finedUsbDevice(){
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                UsbDevice usbDevice = entry.getValue();
                usbDeviceId = usbDevice.getProductId();
                switch (usbDevice.getVendorId()){
                    case 0x2341:    /*Arduino Vendor ID*/
                    case 0x067b:    /*Prolific Technology, Inc.*/
                    case 0x0403:    /*Future Technology Devices International, Ltd (FTDI)*/
                    case 0x03eb:    /*Atmel Corp.*/
                    case 0x4348:    /*WinChipHead*/
                    case 0x1a86:    /*QinHeng Electronics*/
                    case 0x045b:    /*Hitachi, Ltd*/
                    case 0x0471:    /*Philips (or NXP)*/
                    case 0x10c4:    /*Cygnal Integrated Products, Inc.*/
                        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
                        usbManager.requestPermission(usbDevice, pi);
                        return;
                    default:
                        Log.i(TAG, "USB устройство не получило разрешение Vendor-" + usbDevice.getVendorId());
                }

            }
        }
    }

    private void setupSerialPort(UsbDevice usbDevice){
        try {
            UsbDeviceConnection connection = usbManager.openDevice(usbDevice);
            usbSerialDevice = UsbSerialDevice.createUsbSerialDevice(usbDevice, connection);
            if (usbSerialDevice.open()) { //Set Serial Connection Parameters.
                usbSerialDevice.setBaudRate(9600);                                      /* Скорость порта. */
                usbSerialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);            /* Формат данных. */
                usbSerialDevice.setStopBits(UsbSerialInterface.STOP_BITS_1);            /* Сторовый бит. */
                usbSerialDevice.setParity(UsbSerialInterface.PARITY_NONE);              /* Бит четности. */
                usbSerialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);    /** Флов контроль. */
                usbSerialDevice.read(usbReadCallback);
                Log.i(TAG, "Весы соеденены.");
            } else {
                Log.i(TAG, "Порт не открыт.");
            }

        } catch (Exception e) {
            Log.i(TAG, "Ошибка: " + e.getMessage());
        }
    }

    public void sendTextPort(String text){
        if (usbSerialDevice != null)
            usbSerialDevice.write(text.getBytes());
    }

    public void dettach(){
        usbBroadcastReceiver.unregister();
        if (usbSerialDevice != null)
            usbSerialDevice.close();
    }

    /** Обратный вызов когда пришли данные с usb. */
    private final UsbSerialInterface.UsbReadCallback usbReadCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.

        @Override
        public void onReceivedData(byte[] arg0) {
            onReceivedData(arg0);
        }
    };

    @Override
    public void write(String data) {
        usbSerialDevice.write((data+'\r'+'\n').getBytes());
    }

    @Override
    public ObjectCommand sendCommand(Commands cmd) {
        write(cmd.toString());
        response = new ObjectCommand(cmd, "");
        for (int i = 0; i < cmd.getTimeOut(); i++) {
            try { TimeUnit.MILLISECONDS.sleep(1);} catch (InterruptedException e) {e.printStackTrace();}
            try {
                if (response.isResponse()) {
                    return response;
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return null;
    }

    @Override
    public boolean writeByte(byte ch) {
        usbSerialDevice.write(new byte[]{ch});
        return true;
    }

    @Override
    public int getByte() {
        return 0;
    }

    private class UsbBroadcastReceiver extends BroadcastReceiver {
        final Context mContext;
        final IntentFilter filter;
        protected boolean isRegistered;

        UsbBroadcastReceiver(Context context){
            mContext = context;
            filter = new IntentFilter(ACTION_USB_PERMISSION);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case ACTION_USB_PERMISSION:
                    boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    usbDeviceId = device.getProductId();
                    if (granted) {
                        setupSerialPort(device);
                    } else {
                        Log.i(TAG, "Не получено разрешение PID-" + usbDeviceId);
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    finedUsbDevice();
                    //attachPort();
                    Log.i(TAG, "USB соеденено PID-"+ usbDeviceId);
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    if (usbSerialDevice != null)
                        usbSerialDevice.close();
                    //dettachPort();
                    Log.i(TAG, "USB отсоеденено PID-"+ usbDeviceId);
                    break;
                /*case ConnectivityManager.CONNECTIVITY_ACTION:
                    ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                    NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (wifi.isConnected()){
                        //startDataTransferring();
                        Log.i(TAG,"WIFI STATE CONNECTED");
                    }else if (wifi.getState() == NetworkInfo.State.DISCONNECTED){
                        //internet.turnOnWiFiConnection(true);
                        //connectToSpecificNetwork();
                        Log.i(TAG,"WIFI STATE DISCONNECTED");
                    }
                    break;*/
                default:
            }

        }



        public void register() {
            isRegistered = true;
            mContext.registerReceiver(this, filter);
        }

        public void unregister() {
            if (isRegistered) {
                mContext.unregisterReceiver(this);  // edited
                isRegistered = false;
            }
        }
    }
}
