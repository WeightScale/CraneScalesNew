package com.konst.scaleslibrary.module.bluetooth;

import android.os.Handler;
import android.os.Message;

/**
 * @author Kostya  on 21.07.2016.
 */
public class BluetoothHandler extends Handler {

    public enum MSG {
        RECEIVE,
        CONNECT,
        DISCONNECT,
        ERROR
    }

    @Override
    public void handleMessage(Message msg) {
        //super.handleMessage(msg);
        switch (MSG.values()[msg.what]){
            case RECEIVE:

                break;
            case CONNECT:

                break;
            default:
        }

    }
}
