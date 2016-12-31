package com.konst.scaleslibrary.module;

import com.konst.scaleslibrary.module.scale.ObjectScales;

/** Интерфейс обратного вызова при соеденении с модулем.
 * @author Kostya
 */
public interface InterfaceResultCallback {

    /**Сообщения о результате соединения c модулем.
     * Используется при создании модуля метод create().
     * @param what Результат соединения константа ResultConnect.
     * @see  Module.ResultConnect
     * @param msg Информация о результате соединения.
     * @param module Обьект модуля с которым соеденялись ScaleModule или BootModule.
     */
    void resultConnect(Module.ResultConnect what, String msg, Object module);

    void eventData(Module.ResultWeight what, ObjectScales obj);
}
