package com.konst.scaleslibrary.module;

/**
 * @author Kostya  on 08.02.2016.
 */
public class ErrorDeviceException extends Throwable {

    /** Ошибка при работе с удаленным устройством bluetooth.
     * @param detailMessage Текст ошибки.
     */
    public ErrorDeviceException(String detailMessage) {
        super(detailMessage);
    }


}
