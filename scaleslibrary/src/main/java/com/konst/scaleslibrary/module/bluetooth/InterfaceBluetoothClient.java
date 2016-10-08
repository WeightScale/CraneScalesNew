package com.konst.scaleslibrary.module.bluetooth;

import com.konst.scaleslibrary.module.Commands;
import com.konst.scaleslibrary.module.ObjectCommand;

/**
 * @author Kostya  on 21.07.2016.
 */
public interface InterfaceBluetoothClient {

    void write(String data);
    ObjectCommand sendCommand(Commands cmd);

    boolean writeByte(byte ch);
    int getByte();
}
