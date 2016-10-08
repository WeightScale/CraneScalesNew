package com.konst.scaleslibrary;

import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
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
import android.view.Window;
import android.widget.*;
import com.konst.scaleslibrary.module.ErrorDeviceException;
import com.konst.scaleslibrary.module.InterfaceModule;
import com.konst.scaleslibrary.module.Module;
import com.konst.scaleslibrary.module.boot.BootModule;
import com.konst.scaleslibrary.module.scale.InterfaceCallbackScales;
import com.konst.scaleslibrary.module.scale.ScaleModule;

/** Класс индикатора весового модуля.
 * @author Kostya on 26.09.2016.
 */
public class ScalesView extends LinearLayout implements ScalesFragment.OnInteractionListener {
    /** Настройки для весов. */
    public Settings settings;
    private ScaleModule scaleModule;
    private BootModule bootModule;
    private ScalesFragment scalesFragment;
    private SearchFragment searchFragment;
    private FragmentTransaction fragmentTransaction;
    private BaseReceiver baseReceiver;
    private String version;
    private String device;
    private int discrete = 1;
    public static final String SETTINGS = ScalesView.class.getName() + "SETTINGS";
    private static final String TAG_FRAGMENT = ScalesView.class.getName() + "TAG_FRAGMENT";
    private static final String KEY_DISCRETE = ScalesView.class.getPackage() +".KEY_DISCRETE";
    public static final String KEY_ADDRESS = ScalesView.class.getPackage() +".KEY_ADDRESS";

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

        settings = new Settings(context, SETTINGS);
        discrete = settings.read(KEY_DISCRETE, context.getResources().getInteger(R.integer.default_step_scale));
        device = settings.read(KEY_ADDRESS, "");

        scalesFragment = new ScalesFragment();
        searchFragment = new SearchFragment();

        baseReceiver = new BaseReceiver(context);
        baseReceiver.register();

        LayoutInflater.from(context).inflate(R.layout.indicator, this);
    }

    /** Создаем обьект весовой модуль.
     * @param version Имя версии весового модуля.
     * @throws ErrorDeviceException Ошибка создания удаленного устройства.
     * @throws Exception            Ошибка создания обьекта.
     */
    public void create(String version) {
        this.version = version;
        createScalesModule(device);
    }

    private void createScalesModule(String device){
        try {
            ScaleModule.create(getContext(), version, device, interfaceCallbackScales);
        }catch (Exception | ErrorDeviceException e) {
            openSearchDialog(e.getMessage());
        }
    }

    protected interface OnCreateScalesListener{
        void onCreate(String device);
    }

    private final InterfaceCallbackScales interfaceCallbackScales = new  InterfaceCallbackScales() {

        /** Сообщение о результате соединения.
         * @param module Модуль с которым соединились. */
        @Override
        public void onCallback(Module module) {
            if (module instanceof ScaleModule){
                scaleModule = (ScaleModule)module;
                scaleModule.setStepScale(discrete);
                scaleModule.scalesProcessEnable(true);
            }else if (module instanceof BootModule){
                bootModule = (BootModule)module;
            }
            device = module.getAddressBluetoothDevice();
            settings.write(KEY_ADDRESS, module.getAddressBluetoothDevice());
        }
    };

    /** Устанавливаем необходимую дискретность отображения значения веса.
     * @param discrete Значение дискретности (1/2/5/10/20/50).
     */
    public void setDiscrete(int discrete){
        if (scaleModule != null)
            scaleModule.setStepScale(discrete);
        this.discrete = discrete;
        settings.write(KEY_DISCRETE, discrete);
    }

    /**
     * Открыть активность поиска весов.
     */
    @Override
    public void openSearchDialog(String msg) {
        SearchDialog dialog = new SearchDialog(getContext(), msg, new OnCreateScalesListener() {
            @Override
            public void onCreate(String device) {
                fragmentTransaction = ((AppCompatActivity)getContext()).getFragmentManager().beginTransaction();
                fragmentTransaction.remove(scalesFragment);
                fragmentTransaction.commit();
                createScalesModule(device);
            }
        });
        dialog.show();
    }

    @Override
    public void detachScales() {
        if (scaleModule != null)
            scaleModule.dettach();
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
            intentFilter = new IntentFilter(InterfaceModule.ACTION_LOAD_OK);
            intentFilter.addAction(InterfaceModule.ACTION_ATTACH_START);
            intentFilter.addAction(InterfaceModule.ACTION_ATTACH_FINISH);
            intentFilter.addAction(InterfaceModule.ACTION_CONNECT_ERROR);
        }

        @Override
        public void onReceive(Context context, Intent intent) { //обработчик Bluetooth
            String action = intent.getAction();
            if (action != null) {
                fragmentTransaction = ((AppCompatActivity)context).getFragmentManager().beginTransaction();
                switch (action) {
                    case InterfaceModule.ACTION_LOAD_OK:
                        //unlockOrientation();
                        scalesFragment = new ScalesFragment();
                        scalesFragment.loadModule(scaleModule);
                        scalesFragment.setOnInteractionListener(ScalesView.this);
                        fragmentTransaction.replace(R.id.fragment, scalesFragment, scalesFragment.getClass().getName());
                        fragmentTransaction.commit();
                        break;
                    case InterfaceModule.ACTION_ATTACH_START:
                        if(dialogSearch != null){
                            if(dialogSearch.isShowing())
                                break;
                        }
                        dialogSearch = new ProgressDialog((AppCompatActivity)context);
                        dialogSearch.setCancelable(true);
                        dialogSearch.setIndeterminate(false);
                        dialogSearch.show();
                        View view = LayoutInflater.from(context).inflate(R.layout.custom_progress_dialog, null);
                        dialogSearch.setContentView(view);
                        dialogSearch.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        String msg = intent.getStringExtra(InterfaceModule.EXTRA_DEVICE_NAME);
                        TextView tv1 = (TextView) dialogSearch.findViewById(R.id.textView1);
                        tv1.setText(context.getString(R.string.Connecting) + '\n' + msg);
                        break;
                    case InterfaceModule.ACTION_ATTACH_FINISH:
                        if (dialogSearch.isShowing()) {
                            dialogSearch.dismiss();
                        }
                        break;
                    case InterfaceModule.ACTION_CONNECT_ERROR:
                        String message = intent.getStringExtra(InterfaceModule.EXTRA_MESSAGE);
                        if (message == null)
                            message = "";
                        openSearchDialog(message);
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

}
