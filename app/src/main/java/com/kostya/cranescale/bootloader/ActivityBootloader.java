package com.kostya.cranescale.bootloader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.konst.bootloader.AVRProgrammer;
import com.konst.bootloader.HandlerBootloader;
import com.konst.module.*;
import com.konst.module.boot.BootModule;
import com.konst.module.scale.ObjectScales;
import com.konst.module.scale.ScaleModule;
import com.kostya.cranescale.*;
import com.kostya.cranescale.R;
import com.kostya.cranescale.services.ServiceScales;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;


/**
 * @author Kostya
 */
public class ActivityBootloader extends Activity /*implements View.OnClickListener*/ {
    private Globals globals;
    private BootModule bootModule;
    private FragmentTransaction fragmentTransaction;
    private FragmentBoot fragmentBoot;
    private FragmentSearch fragmentSearch;
    private BaseReceiver baseReceiver;

    protected String addressDevice = "";
    protected String hardware = "362";
    private boolean powerOff;



    protected boolean flagAutoPrograming;

    enum CodeDevice{
        ATMEGA88("atmega88.xml",0x930a),    /* 37642 */
        ATMEGA168("atmega168.xml", 0x9406), /* 37894 */
        ATMEGA328("atmega328.xml", 0x9514); /* 38164 */

        final String device;
        final int code;

        CodeDevice(String d, int c){
            device = d;
            code = c;
        }

        public String getDevice() {return device;}
        //public int getCode() {return code;}

        public static CodeDevice contains(int c){
            for(CodeDevice choice : values())
                if (choice.code == c)
                    return choice;
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boot);

        globals = Globals.getInstance();

        fragmentBoot = new FragmentBoot();
        fragmentSearch = new FragmentSearch();

        baseReceiver = new BaseReceiver(this);
        baseReceiver.register();

        addressDevice = getIntent().getStringExtra(getString(R.string.KEY_ADDRESS));
        hardware = getIntent().getStringExtra(Commands.HRW.getName());
        powerOff = getIntent().getBooleanExtra("com.konst.simple_scale.POWER", false);

        //Spinner spinnerField = (Spinner) findViewById(R.id.spinnerField);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.Warning_Connect));
        dialog.setCancelable(false);
        dialog.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        try {
                            //bootModule = new BootModule("BOOT", addressDevice, connectResultCallback);
                            Intent intent = new Intent(getApplicationContext(), ServiceScales.class);
                            intent.setAction(ServiceScales.ACTION_CONNECT_BOOTLOADER);
                            Bundle bundle = new Bundle();
                            bundle.putString(ServiceScales.EXTRA_VERSION, "BOOT");
                            bundle.putString(ServiceScales.EXTRA_DEVICE, addressDevice);
                            intent.putExtra(ServiceScales.EXTRA_BUNDLE, bundle);
                            startService(intent);
                            //BootModule.create(getApplicationContext(), "BOOT", addressDevice/*, connectResultCallback*/);
                            //log(getString(R.string.bluetooth_off));
                        } catch (Exception e) {
                            Toast.makeText(ActivityBootloader.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        } /*catch (ErrorDeviceException e) {
                            connectResultCallback.resultConnect(Module.ResultConnect.CONNECT_ERROR, e.getMessage(), null);
                        }*/
                        /*try {
                            globals.setBootModule(bootModule);
                            //bootModule.init(addressDevice);
                            //bootModule.attach();
                        } catch (Exception e) {
                            connectResultCallback.connectError(Module.ResultError.CONNECT_ERROR, e.getMessage());
                        }*/
                    break;
                    default:
                }
            }
        });
        dialog.setNegativeButton(getString(R.string.Close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        if (powerOff)
            dialog.setMessage("На весах нажмите кнопку включения и не отпускайте пока индикатор не погаснет. После этого нажмите ОК");
        else
            dialog.setMessage(getString(R.string.TEXT_MESSAGE));
        dialog.show();

    }

    @Override
    public void onBackPressed() {
        finish();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        baseReceiver.unregister();
        //exit();
    }

    /*final InterfaceResultCallback connectResultCallback = new InterfaceResultCallback() {
        private AlertDialog.Builder dialog;

        @Override
        public void resultConnect(final Module.ResultConnect result, String msg, final Object module) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (result) {
                        case STATUS_LOAD_OK:
                            bootModule = (BootModule)module;
                            globals.setBootModule(bootModule);
                            dialog = new AlertDialog.Builder(ActivityBootloader.this);
                            dialog.setTitle(getString(R.string.Warning_update));
                            dialog.setCancelable(false);
                            int numVersion = bootModule.getBootVersion();
                            if(numVersion > 1){
                                hardware = bootModule.getModuleHardware();
                                dialog.setMessage("После нажатия кнопки ОК начнется программирование");
                                flagAutoPrograming = true;
                            }else {
                                dialog.setMessage(getString(R.string.TEXT_MESSAGE5));
                            }
                            startBoot.setEnabled(true);
                            startBoot.setAlpha(255);
                            dialog.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (i) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            if(flagAutoPrograming){
                                                if (bootModule.startProgramming())
                                                    if (!startProgramed()) {
                                                        flagProgramsFinish = true;
                                                    }
                                            }
                                        break;
                                        default:
                                    }
                                }
                            });
                            dialog.setNegativeButton(getString(R.string.Close), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            });

                            dialog.show();
                            break;
                        case CONNECT_ERROR:
                            //Intent intent = new Intent(getBaseContext(), ActivityConnect.class);
                            Intent intent = new Intent(getBaseContext(), ActivityMain.class);
                            intent.putExtra("address", addressDevice);
                            intent.setAction("com.kostya.cranescale.BOOTLOADER");
                            startActivityForResult(intent, REQUEST_CONNECT_BOOT);
                            break;
                        default:
                    }
                }
            });
        }

        @Override
        public void eventData(ScaleModule.ResultWeight what, ObjectScales obj) {

        }

    };*/

    void exit() {

    }

    class BaseReceiver extends BroadcastReceiver {
        final Context mContext;
        ProgressDialog dialogSearch;
        final IntentFilter intentFilter;
        protected boolean isRegistered;

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
                fragmentTransaction = getFragmentManager().beginTransaction();
                switch (action) {
                    case InterfaceModule.ACTION_LOAD_OK:
                        fragmentBoot = new FragmentBoot();
                        fragmentTransaction.replace(R.id.fragmentCont, fragmentBoot, fragmentBoot.getClass().getName());
                        fragmentTransaction.commit();
                        break;
                    case InterfaceModule.ACTION_ATTACH_START:
                        if(dialogSearch != null){
                            if(dialogSearch.isShowing())
                                break;
                        }
                        dialogSearch = new ProgressDialog(ActivityBootloader.this);
                        dialogSearch.setCancelable(true);
                        dialogSearch.setIndeterminate(false);
                        dialogSearch.show();
                        dialogSearch.setContentView(R.layout.custom_progress_dialog);
                        String msg = intent.getStringExtra(InterfaceModule.EXTRA_DEVICE_NAME);
                        TextView tv1 = (TextView) dialogSearch.findViewById(R.id.textView1);
                        tv1.setText(getString(R.string.Connecting) + '\n' + msg);
                        break;
                    case InterfaceModule.ACTION_ATTACH_FINISH:
                        if (dialogSearch.isShowing()) {
                            dialogSearch.dismiss();
                        }
                        break;
                    case InterfaceModule.ACTION_CONNECT_ERROR:
                        String value = intent.getStringExtra(InterfaceModule.EXTRA_MESSAGE);
                        fragmentSearch = new FragmentSearch();
                        if (value!=null){
                            Bundle bundle = new Bundle();
                            bundle.putString(InterfaceModule.EXTRA_MESSAGE, value);
                            fragmentSearch.setArguments(bundle);
                        }
                        fragmentTransaction.replace(R.id.fragmentCont, fragmentSearch, fragmentSearch.getClass().getName());

                        fragmentTransaction.commit();
                        break;
                    default:
                }
            }
        }

        public void register() {
            isRegistered = true;
            mContext.registerReceiver(this, intentFilter);
        }

        public void unregister() {
            if (isRegistered) {
                mContext.unregisterReceiver(this);  // edited
                isRegistered = false;
            }
        }
    }

    /**
     * Открыть активность поиска весов.
     */
    public void openSearch() {
        try{ globals.getScaleModule().dettach(); }catch (Exception e){}
        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragmentCont, fragmentSearch, fragmentSearch.getClass().getName());
        fragmentTransaction.commit();
    }

    /*public boolean backupPreference() {
        Preferences.load(getSharedPreferences(Preferences.PREF_UPDATE, Context.MODE_PRIVATE));

        Preferences.write(InterfaceVersions.CMD_FILTER, ScaleModule.getFilterADC());
        Preferences.write(InterfaceVersions.CMD_TIMER, ScaleModule.getTimeOff());
        Preferences.write(InterfaceVersions.CMD_BATTERY, ScaleModule.getBattery());
        //Main.preferencesUpdate.write(InterfaceVersions.CMD_CALL_TEMP, String.valueOf(coefficientTemp));
        Preferences.write(InterfaceVersions.CMD_SPREADSHEET, ScaleModule.getSpreadSheet());
        Preferences.write(InterfaceVersions.CMD_G_USER, ScaleModule.getUserName());
        Preferences.write(InterfaceVersions.CMD_G_PASS, ScaleModule.getPassword());
        Preferences.write(InterfaceVersions.CMD_DATA_CFA, ScaleModule.getCoefficientA());
        Preferences.write(InterfaceVersions.CMD_DATA_WGM, ScaleModule.getWeightMax());

        //editor.apply();
        return true;
    }*/

    /*public boolean restorePreferences() {
        if (ScaleModule.isScales()) {
            log("Соединились");
            Preferences.load(getSharedPreferences(Preferences.PREF_UPDATE, Context.MODE_PRIVATE));
            ScaleModule.setModuleFilterADC(Preferences.read(InterfaceVersions.CMD_FILTER, Main.default_adc_filter));
            log("Фмльтер "+ BootModule.getFilterADC());
            ScaleModule.setModuleTimeOff(Preferences.read(InterfaceVersions.CMD_TIMER, Main.default_max_time_off));
            log("Время отключения "+ BootModule.getTimeOff());
            ScaleModule.setModuleBatteryCharge(Preferences.read(InterfaceVersions.CMD_BATTERY, Main.default_max_battery));
            log("Заряд батареи "+ BootModule.getBattery());
            //command(InterfaceScaleModule.CMD_CALL_TEMP + Main.preferencesUpdate.read(InterfaceScaleModule.CMD_CALL_TEMP, "0"));
            ScaleModule.setModuleSpreadsheet(Preferences.read(InterfaceVersions.CMD_SPREADSHEET, "weightscale"));
            log("Имя таблици "+ BootModule.getSpreadSheet());
            ScaleModule.setModuleUserName(Preferences.read(InterfaceVersions.CMD_G_USER, ""));
            log("Имя пользователя "+ BootModule.getUserName());
            ScaleModule.setModulePassword(Preferences.read(InterfaceVersions.CMD_G_PASS, ""));
            log("Пароль");
            ScaleModule.setCoefficientA(Preferences.read(InterfaceVersions.CMD_DATA_CFA, 0.0f));
            log("Коэффициент А "+ ScaleModule.getCoefficientA());
            ScaleModule.setWeightMax(Preferences.read(InterfaceVersions.CMD_DATA_WGM, Main.default_max_weight));
            log("Максимальный вес "+ ScaleModule.getWeightMax());
            ScaleModule.setLimitTenzo((int) (ScaleModule.getWeightMax() / ScaleModule.getCoefficientA()));
            log("Лимит датчика "+ ScaleModule.getLimitTenzo());
            ScaleModule.writeData();
        }
        return true;
    }*/


}
