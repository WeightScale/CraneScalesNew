package com.konst.scaleslibrary;

import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
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
import com.konst.scaleslibrary.settings.FragmentSettings;

import java.util.Map;
import java.util.Set;

/** Класс индикатора весового модуля.
 * @author Kostya on 26.09.2016.
 */
public class ScalesView extends LinearLayout implements ScalesFragment.OnInteractionListener/*, SearchFragment.OnFragmentInteractionListener */{
    private static ScalesView instance;
    /** Настройки для весов. */
    public Settings settings;
    private ScaleModule scaleModule;
    private BootModule bootModule;
    private ScalesFragment scalesFragment;
    //private SearchFragment searchFragment;
    private FragmentManager fragmentManager;
    private BaseReceiver baseReceiver;
    private String version;
    private String addressDevice;
    /** Версия пограммы весового модуля. */
    private final int microSoftware = 5;
    private InterfaceCallbackScales interfaceCallbackScales;
    private static final String TAG_FRAGMENT = ScalesView.class.getName() + "TAG_FRAGMENT";
    /** Настройки общии для модуля. */
    public static final String SETTINGS = ScalesView.class.getName() + ".SETTINGS"; //
    public static final int REQUEST_DEVICE = 1;
    public static final int REQUEST_ATTACH = 2;
    public static final int REQUEST_BROKEN = 3;

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

        settings = new Settings(context, SETTINGS);
        //settings = new Settings(context);
        addressDevice = settings.read(R.string.KEY_ADDRESS, "");

        fragmentManager = ((AppCompatActivity)getContext()).getFragmentManager();

        baseReceiver = new BaseReceiver(context);
        baseReceiver.register();

        LayoutInflater.from(context).inflate(R.layout.indicator, this);
        findViewById(R.id.buttonSearch).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        findViewById(R.id.buttonSettings).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

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
    public void onUpdateSettings(Settings settings) {
        updateSettings(settings);
    }

    @Override
    public void onScaleModuleCallback(ScaleModule obj) {
        scaleModule = obj;
    }

    @Override
    public void onSaveWeight(int weight) {

    }

    public void openSearchScales(){
        if (scalesFragment != null){
            scalesFragment.openSearchDialog("Выбор устройства для соединения.");
        }
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

    /** Устанавливаем необходимую дискретность отображения значения веса.
     * @param discrete Значение дискретности (1/2/5/10/20/50).
     */
    public void setDiscrete(int discrete){
        if (scaleModule != null)
            scaleModule.setStepScale(discrete);
        settings.write(FragmentSettings.KEY.STEP.getResId(), discrete);
    }

    /** Устанавливаем флаг определять стабильный вес.
     * @param stable
     */
    public void setStable(boolean stable){
        if (scaleModule != null)
            scaleModule.setEnableProcessStable(stable);
        settings.write(FragmentSettings.KEY.SWITCH_STABLE.getResId(), stable);
    }

    public void updateSettings(Settings settings){

        for(FragmentSettings.KEY key : FragmentSettings.KEY.values()){
            switch (key){
                case STEP:
                    scaleModule.setStepScale(settings.read(key.getResId(), 5));
                    break;
                case SWITCH_STABLE:
                    scaleModule.setEnableProcessStable(settings.read(key.getResId(), false));
                    break;
                case DELTA_STAB:
                    scaleModule.setDeltaStab(settings.read(key.getResId(), 10));
                    break;
                case SWITCH_ZERO:
                    scaleModule.setEnableAutoNull(settings.read(key.getResId(), false));
                    break;
                case TIMER_ZERO:
                    scaleModule.setTimerZero(settings.read(key.getResId(), 120));
                    break;
                case MAX_ZERO:
                    scaleModule.setWeightError(settings.read(key.getResId(), 50));
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
            intentFilter = new IntentFilter(InterfaceModule.ACTION_BOOT_MODULE);
        }

        @Override
        public void onReceive(Context context, Intent intent) { //обработчик Bluetooth
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case InterfaceModule.ACTION_BOOT_MODULE:
                        boolean powerOff = intent.getBooleanExtra("com.konst.simple_scale.POWER", false);
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                        dialog.setTitle(getContext().getString(R.string.Warning_Connect));
                        dialog.setCancelable(false);
                        dialog.setPositiveButton(getContext().getString(R.string.OK), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        fragmentManager.beginTransaction().replace(R.id.fragment, BootFragment.newInstance("BOOT", addressDevice), BootFragment.class.getName()).commitAllowingStateLoss();
                                        break;
                                    default:
                                }
                            }
                        });
                        dialog.setNegativeButton(getContext().getString(R.string.Close), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                //finish();
                            }
                        });
                        if (powerOff)
                            dialog.setMessage("На весах нажмите кнопку включения и не отпускайте пока индикатор не погаснет. После этого нажмите ОК");
                        else
                            dialog.setMessage(getContext().getString(R.string.TEXT_MESSAGE));
                        dialog.show();

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
