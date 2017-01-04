/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.konst.scaleslibrary.module.scale;

import com.konst.scaleslibrary.module.Commands;
import com.konst.scaleslibrary.module.ErrorModuleException;
import com.konst.scaleslibrary.module.InterfaceModule;
import com.konst.scaleslibrary.module.Module;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Kostya
 */
public class ScaleVersion4 extends ScaleVersion {

    /** Разница знечений между значение ноля до и после. */
    private int offset;
    /** Предельное показани датчика. */
    protected int marginTenzo;

    public ScaleVersion4(Module module){
        scaleModule = module;
    }

    @Override
    public void extract() throws Exception {
        //======================================================================
        scaleModule.setFilterADC(Integer.valueOf(Commands.FAD.getParam()));
        if (scaleModule.getFilterADC() < 0 || scaleModule.getFilterADC() > MAX_ADC_FILTER) {
            if (!scaleModule.setModuleFilterADC(DEFAULT_ADC_FILTER))
                throw new ErrorModuleException("Фильтер АЦП не установлен в настройках");
        }
        //======================================================================
        scaleModule.setTimeOff(Integer.valueOf(Commands.TOF.getParam()));
        if (scaleModule.getTimeOff() < MIN_TIME_OFF || scaleModule.getTimeOff() > MAX_TIME_OFF) {
            if (!scaleModule.setModuleTimeOff(MIN_TIME_OFF))
                throw new ErrorModuleException("Таймер выключения не установлен в настройках");
        }
        //======================================================================
        int speed = Integer.valueOf(Commands.BST.getParam());
        if (speed < 1 || speed > 5) {
            if (!Commands.BST.setParam(5))
                throw new ErrorModuleException("Скорость передачи не установлена в настройках");
        }
        //======================================================================
        try {
            offset = Integer.valueOf(Commands.GCO.getParam());
        } catch (Exception e) {
            throw new ErrorModuleException("Сделать обнуление в настройках");
        }
        //======================================================================
        scaleModule.setSpreadsheet(Commands.SGD.getParam());
        scaleModule.setUserName(Commands.UGD.getParam());
        scaleModule.setPassword(Commands.PGD.getParam());
        scaleModule.setPhone(Commands.PHN.getParam());
        //======================================================================

        parserData(Commands.DAT.getParam());

        scaleModule.setWeightMargin((int) (weightMax * 1.2));
        marginTenzo = (int) ((weightMax / scaleModule.getCoefficientA()) * 1.2);
    }

    /** Проверка лимита нагрузки сенсора.
     * @return true - Лимит нагрузки сенсора превышен.  */
    @Override
    public boolean isLimit() {
        //return Math.abs(sensorTenzoOffset + offset) > scaleModule.getLimitTenzo();
        return Math.abs(getSensor()) > scaleModule.getLimitTenzo();
    }

    @Override
    public boolean isMargin() {
        //return Math.abs(sensorTenzoOffset + offset) > marginTenzo;
        return Math.abs(getSensor()) > marginTenzo;
    }

    @Override
    public boolean setOffsetScale() {
        try {
            //int offset = Integer.valueOf(Commands.CMD_GET_OFFSET.getParam());
            if(Commands.SCO.getParam().equals(Commands.SCO.getName())){
                offset = Integer.valueOf(Commands.GCO.getParam());
                return true;
            }
        }catch (Exception e){}
        return false;
    }

    /** Обновить значения веса.
     * Получаем показания сенсора и переводим в занчение веса.
     * @return Значение веса. */
    @Override
    public synchronized int updateWeight() {
        try {
            sensorTenzoOffset = Integer.valueOf(Commands.DCO.getParam());
            return weight = (int) (sensorTenzoOffset * scaleModule.getCoefficientA());
        } catch (Exception e) {
            return sensorTenzoOffset= weight = Integer.MIN_VALUE;
        }
    }

    /**Записать данные параметров в модуль.
     * @return true - Данные записаны.
     * @see Commands#DAT */
    @Override
    public boolean writeData() {
        return Commands.DAT.setParam(
                InterfaceModule.CMD_DATA_CFA + '=' + scaleModule.getCoefficientA() + ' ' +
                InterfaceModule.CMD_DATA_WGM + '=' + weightMax + ' ' +
                InterfaceModule.CMD_DATA_LMT + '=' + scaleModule.getLimitTenzo()+ ' '+
                InterfaceModule.CMD_DATA_SEL+ '='+ scaleModule.getSeal());
    }

    @Override
    public int getSensor() {
        return offset + sensorTenzoOffset;
    }

    @Override
    public int getMarginTenzo() { return marginTenzo; }

    /**Проверка данных полученых от модуля.
     * Формат параметра данных: [[{@link InterfaceModule#CMD_DATA_CFA}=[значение]] [{@link InterfaceModule#CMD_DATA_WGM}=[значение]] [{@link InterfaceModule#CMD_DATA_LMT}=[значение]]]
     * @param d Данные
     * @throws Exception Данные не правельные.
     * @see ScaleVersion4#extract() ()
     * @see Commands#DAT  */
    private void parserData(String d) throws Exception {
        String[] parts = d.split(" ", 0);
        SimpleCommandLineParser data = new SimpleCommandLineParser(parts, "=");
        Iterator<String> iteratorData = data.getKeyIterator();
        if(iteratorData == null)
            throw new ErrorModuleException("Нет данных каллибровки !!!");
        //synchronized (this) {
        while (iteratorData.hasNext()) {
            switch (iteratorData.next()) {
                case InterfaceModule.CMD_DATA_CFA:
                    scaleModule.setCoefficientA(Float.valueOf(data.getValue(InterfaceModule.CMD_DATA_CFA)));//получаем коэфициент.
                    if (scaleModule.getCoefficientA() == 0.0f)
                        throw new ErrorModuleException("Коэффициент А=" + scaleModule.getCoefficientA());
                    break;
                case InterfaceModule.CMD_DATA_CFB:
                    scaleModule.setCoefficientB(Float.valueOf(data.getValue(InterfaceModule.CMD_DATA_CFB)));//получить offset.
                    break;
                case InterfaceModule.CMD_DATA_WGM:
                    weightMax = Integer.parseInt(data.getValue(InterfaceModule.CMD_DATA_WGM));//получаем макимальнай вес.
                    if (weightMax <= 0)
                        throw new ErrorModuleException("Предельный вес =" + weightMax);
                    break;
                case InterfaceModule.CMD_DATA_LMT:
                    scaleModule.setLimitTenzo(Integer.parseInt(data.getValue(InterfaceModule.CMD_DATA_LMT))); //получаем макимальнай показание перегруза.
                    break;
                case InterfaceModule.CMD_DATA_SEL:
                    scaleModule.setSeal(Integer.parseInt(data.getValue(InterfaceModule.CMD_DATA_SEL))); //получаем номер пломбы.
                    break;
                default:
            }
        }
        //}
    }

    /** Парсер комманды. */
    static class SimpleCommandLineParser {

        private final Map<String, String> argMap;

        public SimpleCommandLineParser(String[] arg, String predict) {
            argMap = new HashMap<>();
            for (String anArg : arg) {
                String[] str = anArg.split(predict, 2);
                if (str.length > 1) {
                    argMap.put(str[0], str[1]);
                }
            }
        }

        public String getValue(String... keys) {
            for (String key : keys) {
                if (argMap.get(key) != null) {
                    return argMap.get(key);
                }
            }
            return null;
        }

        public Iterator<String> getKeyIterator() {
            Set<String> keySet = argMap.keySet();
            if (!keySet.isEmpty()) {
                return keySet.iterator();
            }
            return null;
        }
    }

}
