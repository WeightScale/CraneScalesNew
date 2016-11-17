package com.konst.scaleslibrary;

import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import com.konst.scaleslibrary.module.ErrorDeviceException;
import com.konst.scaleslibrary.module.InterfaceModule;
import com.konst.scaleslibrary.module.Module;
import com.konst.scaleslibrary.module.boot.BootModule;
import com.konst.scaleslibrary.module.scale.InterfaceCallbackScales;
import com.konst.scaleslibrary.module.scale.ScaleModule;

import java.util.Map;
import java.util.Set;

/** Класс индикатора весового модуля.
 * @author Kostya on 26.09.2016.
 */
public class ScalesView extends LinearLayout implements ScalesFragment.OnInteractionListener, SearchFragment.OnFragmentInteractionListener {
    private static ScalesView instance;
    /** Настройки для весов. */
    public Settings settings;
    private ScaleModule scaleModule;
    private BootModule bootModule;
    private ScalesFragment scalesFragment;
    private SearchFragment searchFragment;
    private FragmentManager fragmentManager;
    private BaseReceiver baseReceiver;
    private String version;
    private String addressDevice;
    /** Версия пограммы весового модуля. */
    private final int microSoftware = 5;
    private InterfaceCallbackScales interfaceCallbackScales;
    private static final String TAG_FRAGMENT = ScalesView.class.getName() + "TAG_FRAGMENT";

    /** Создаем новый обьект индикатора весового модуля.
     * @param context the context
     */
    public ScalesView(Context context) {
        super(context);
    }

    /** Создаем новый обьект индикатора весового модуля.
     * @param context the context
     * @param attrs   the attrs
     */
    public ScalesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        instance = this;

        settings = new Settings(context, Settings.SETTINGS);
        //settings = new Settings(context);
        addressDevice = settings.read(Settings.KEY.KEY_ADDRESS, "");

        fragmentManager = ((AppCompatActivity)getContext()).getFragmentManager();

        searchFragment = SearchFragment.newInstance("", this);

        baseReceiver = new BaseReceiver(context);
        baseReceiver.register();

        LayoutInflater.from(context).inflate(R.layout.indicator, this);
        findViewById(R.id.buttonSearch).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                openSearchDialog("Выбор устройства для соединения");
            }
        });
        findViewById(R.id.buttonSettings).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                openSearchDialog("Выбор устройства для соединения");
            }
        });
    }

    public static ScalesView getInstance(){
        return instance;
    }

    /**
     * Интерфейс обратного вызова.
     */
    protected interface OnCreateScalesListener{
        /** Процедура вызывается при создании класса весового модуля.
         * @param device Адресс bluetooth весового модуля.
         */
        void onCreate(String device);
    }

    @Override
    public void onLinkBroken() {
        fragmentManager.beginTransaction().remove(searchFragment).commit();
    }

    /** Процедура обратного вызова {@link ScalesFragment.OnInteractionListener}.
     * Открыть диалог для поиска весов.
     * @param msg Текст в поле сообщение формы диалога.
     */
    @Override
    public void openSearchDialog(String msg) {
        /*SearchDialog dialog = new SearchDialog(getContext(), msg, new OnCreateScalesListener() {
            @Override
            public void onCreate(String device) {
                Fragment fragment = fragmentManager.findFragmentByTag(scalesFragment.getClass().getName());
                if (fragment != null) {
                    fragmentManager.beginTransaction().remove(fragment).commit();
                }
                createScalesModule(device);
            }
        });
        dialog.show();*/
    }

    @Override
    public void onScaleModuleCallback(ScaleModule obj) {
        scaleModule = obj;
    }

    public int getMicroSoftware() { return microSoftware; }

    public ScaleModule getScaleModule() {
        return scaleModule;
    }

    /**
     * Прцедура вызывается при закрытии главной программы.
     */
    public void exit(){
        baseReceiver.unregister();
        BluetoothAdapter.getDefaultAdapter().disable();
        while (BluetoothAdapter.getDefaultAdapter().isEnabled());
    }

    /** Создаем обьект весовой модуль.
     * @param version Имя версии весового модуля.
     * @throws ErrorDeviceException Ошибка создания удаленного устройства.
     * @throws Exception            Ошибка создания обьекта.
     */
    public void create(String version, InterfaceCallbackScales listener) {
        this.version = version;
        interfaceCallbackScales = listener;
        scalesFragment = ScalesFragment.newInstance(version, addressDevice, this);
        fragmentManager.beginTransaction().replace(R.id.fragment, scalesFragment, scalesFragment.getClass().getName()).commit();
    }

    /*private void createScalesModule(String device){
        try {
            ScaleModule.create(getContext(), version, device, new InterfaceCallbackScales() {
                *//** Сообщение о результате соединения.
                 * @param module Модуль с которым соединились. *//*
                @Override
                public void onCallback(Module module) {
                    if (module instanceof ScaleModule){
                        scaleModule = (ScaleModule)module;
                        updateSettings(settings);
                        scaleModule.scalesProcessEnable(true);
                    }else if (module instanceof BootModule){
                        bootModule = (BootModule)module;
                    }
                    addressDevice = module.getAddressBluetoothDevice();
                    settings.write(Settings.KEY.KEY_ADDRESS, module.getAddressBluetoothDevice());
                    interfaceCallbackScales.onCallback(module);
                }
            });
        }catch (Exception | ErrorDeviceException e) {
            openSearchDialog(e.getMessage());
        }
    }*/

    /** Устанавливаем необходимую дискретность отображения значения веса.
     * @param discrete Значение дискретности (1/2/5/10/20/50).
     */
    public void setDiscrete(int discrete){
        if (scaleModule != null)
            scaleModule.setStepScale(discrete);
        settings.write(Settings.KEY.KEY_DISCRETE, discrete);
    }

    /** Устанавливаем флаг определять стабильный вес.
     * @param stable
     */
    public void setStable(boolean stable){
        if (scaleModule != null)
            scaleModule.stableActionEnable(stable);
        settings.write(Settings.KEY.KEY_STABLE, stable);
    }

    public void updateSettings(Settings settings){

        for (Settings.KEY key : Settings.KEY.values()){
            switch (key){
                case KEY_DISCRETE:
                    scaleModule.setStepScale(settings.read(key, 5));
                break;
                case KEY_STABLE:
                    scaleModule.stableActionEnable(settings.read(key, false));
                break;
                case KEY_TIMER_ZERO:
                    scaleModule.setTimerZero(settings.read(key, 120));
                break;
                case KEY_MAX_ZERO:
                    scaleModule.setTimerZero(settings.read(key, 50));
                break;
                default:
            }
        }
    }

    /** Приемник сообщений. */
    private class BaseReceiver extends BroadcastReceiver {
        /** Контекст программы. */
        final Context mContext;
        /** Диалог отображения подключения к весовому модулю. */
        ProgressDialog dialogSearch;
        /** Фильтер намерений. */
        final IntentFilter intentFilter;
        /** Флаг если приемник зарегестрированый.*/
        protected boolean isRegistered;

        /** Конструктор нового приемника.
         * @param context the context
         */
        BaseReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(/*InterfaceModule.ACTION_LOAD_OK*/);
            //intentFilter.addAction(InterfaceModule.ACTION_RECONNECT_OK);
            intentFilter.addAction(InterfaceModule.ACTION_ATTACH_START);
            intentFilter.addAction(InterfaceModule.ACTION_ATTACH_FINISH);
            //intentFilter.addAction(InterfaceModule.ACTION_CONNECT_ERROR);
            intentFilter.addAction(InterfaceModule.ACTION_BOOT_MODULE);
        }

        @Override
        public void onReceive(Context context, Intent intent) { //обработчик Bluetooth
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    /*case InterfaceModule.ACTION_LOAD_OK:
                        //unlockOrientation();
                        scalesFragment = ScalesFragment.newInstance(ScalesView.this);
                        scalesFragment.loadModule(scaleModule);
                        scalesFragment.setOnInteractionListener(ScalesView.this);
                        fragmentManager.beginTransaction().replace(R.id.fragment, scalesFragment, scalesFragment.getClass().getName()).commit();
                        break;*/

                    case InterfaceModule.ACTION_ATTACH_START:
                        String msg = intent.getStringExtra(InterfaceModule.EXTRA_DEVICE_NAME);
                        if (msg!=null){
                            Bundle bundle = new Bundle();
                            bundle.putString(SearchFragment.ARG_MESSAGE, msg);
                        }
                        searchFragment = SearchFragment.newInstance(msg, ScalesView.this);
                        searchFragment.setCancelable(false);
                        //fragmentManager.beginTransaction().hide(scalesFragment).commit();
                        fragmentManager.beginTransaction().add(R.id.fragment, searchFragment, searchFragment.getClass().getName()).commit();
                        break;
                    case InterfaceModule.ACTION_ATTACH_FINISH:
                        fragmentManager.beginTransaction().remove(searchFragment).commit();
                        break;
                    /*case InterfaceModule.ACTION_CONNECT_ERROR:
                        String message = intent.getStringExtra(InterfaceModule.EXTRA_MESSAGE);
                        if (message == null)
                            message = "";
                        openSearchDialog(message);
                        break;*/
                    case InterfaceModule.ACTION_BOOT_MODULE:
                        //test();
                        fragmentManager.beginTransaction().replace(R.id.fragment, BootFragment.newInstance("BOOT", addressDevice), BootFragment.class.getName()).commitAllowingStateLoss();
                        break;
                    default:
                }
            }
        }

        /** Регистрация приемника. */
        public void register() {
            isRegistered = true;
            mContext.registerReceiver(this, intentFilter);
        }

        /** Разрегистрация приемника. */
        public void unregister() {
            if (isRegistered) {
                mContext.unregisterReceiver(this);  // edited
                isRegistered = false;
            }
        }
    }

    void test(){
        LinearLayout linearLayout = this;
        LayoutInflater ltInflater = LayoutInflater.from(getContext());
        //ltInflater.inflate(R.layout.fragment_boot, (ViewGroup) getParent());
        addView(ltInflater.inflate(R.layout.fragment_boot, null));
    }

}
