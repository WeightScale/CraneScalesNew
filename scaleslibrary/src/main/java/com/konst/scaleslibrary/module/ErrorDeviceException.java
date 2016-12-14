package com.konst.scaleslibrary.module;

/**
 * @author Kostya  on 08.02.2016.
 */
public class ErrorDeviceException extends Throwable {

    private static final long serialVersionUID = 1155565806109146017L;

    /** Ошибка при работе с удаленным устройством bluetooth.
     * @param detailMessage Текст ошибки.
     */
    public ErrorDeviceException(String detailMessage) {
        super(detailMessage);
    }


}
