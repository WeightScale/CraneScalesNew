package com.konst.scaleslibrary.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;
import com.konst.scaleslibrary.R;
import com.konst.scaleslibrary.ScalesView;
import com.konst.scaleslibrary.module.InterfaceModule;
import com.konst.scaleslibrary.module.Module;

/**
 * @author Kostya 26.10.2016.
 */
public class FragmentSettingsAdmin extends PreferenceFragment {
    public static Module scaleModule;
    private static float coefficientA;

    enum EnumSettings{
        COEFFICIENT_A(R.string.KEY_COEFFICIENT_A){
            @Override
            void setup(Preference name) throws Exception {
                final Context context = name.getContext();
                name.setTitle(context.getString(R.string.ConstantA) + Float.toString(scaleModule.getCoefficientA()));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        try {
                            scaleModule.setCoefficientA(Float.valueOf(o.toString()));
                            coefficientA = Float.valueOf(o.toString());
                            preference.setTitle(context.getString(R.string.ConstantA) + Float.toString(scaleModule.getCoefficientA()));
                            Toast.makeText(context, R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                            return true;
                        } catch (Exception e) {
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                });
            }
        },
        CALL_BATTERY(R.string.KEY_CALL_BATTERY){
            @Override
            void setup(Preference name) throws Exception {
                final Context context = name.getContext();
                name.setTitle(context.getString(R.string.Battery) + scaleModule.getModuleBatteryCharge() + '%');
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty() || "0".equals(o.toString()) || Integer.valueOf(o.toString()) > context.getResources().getInteger(R.integer.default_max_battery)) {
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        if (scaleModule.setModuleBatteryCharge(Integer.valueOf(o.toString()))) {
                            preference.setTitle(context.getString(R.string.Battery) + o + '%');
                            Toast.makeText(context, R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
            }
        },
        SAVE_MAN(R.string.KEY_SAVE_MAN){
            @Override
            void setup(Preference name) throws Exception {
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {

                        ((Activity)preference.getContext()).onBackPressed();
                        return true;
                    }
                });
            }
        },
        SERVICE_COD(R.string.KEY_SERVICE_COD){
            @Override
            void setup(final Preference name) throws Exception {
                if(!scaleModule.isAttach())
                    throw new Exception(" ");
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (newValue.toString().length() > 32 || newValue.toString().length() < 4) {
                            Toast.makeText(name.getContext(), "Длина кода больше 32 или меньше 4 знаков", Toast.LENGTH_LONG).show();
                            return false;
                        }

                        try {
                            scaleModule.setModuleServiceCod(newValue.toString());
                            Toast.makeText(name.getContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                            return true;
                        } catch (Exception e) {
                            Toast.makeText(name.getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                });
            }
        },
        UPDATE(R.string.KEY_UPDATE){
            @Override
            void setup(Preference name) throws Exception {
                final Context context = name.getContext();
                try {
                    if (scaleModule.getVersion() != null) {
                        if (scaleModule.getVersionNum() < ScalesView.getInstance().getMicroSoftware()) {
                            name.setSummary(context.getString(R.string.Is_new_version));
                        } else {
                            name.setSummary(context.getString(R.string.Scale_update));
                            name.setEnabled(false);
                        }
                    }
                }catch (Exception e){
                    name.setSummary(context.getString(R.string.TEXT_MESSAGE14));
                }
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    //@TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        /*Intent intent = new Intent(context, ActivityBootloader.class);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        else
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try{
                            intent.putExtra(context.getString(R.string.KEY_ADDRESS), scaleModule.isAttach()? scaleModule.getAddressBluetoothDevice():"");
                            intent.putExtra(Commands.HRW.getName(), scaleModule.getModuleHardware());
                            intent.putExtra(Commands.VRS.getName(), scaleModule.getNumVersion());
                            if (scaleModule.isAttach()){
                                if(scaleModule.powerOff())
                                    intent.putExtra("com.konst.simple_scale.POWER", true);
                            }
                            scaleModule.dettach();
                        }catch (Exception e){ }
                        context.startActivity(intent);*/
                        boolean power = false;
                        try {
                            if (scaleModule.isAttach()){
                                if(scaleModule.powerOff())
                                    power = true;
                            }
                        }catch (Exception e){}
                        ((Activity)context).onBackPressed();
                        context.sendBroadcast(new Intent(InterfaceModule.ACTION_BOOT_MODULE).putExtra("com.konst.simple_scale.POWER", power));
                        return false;
                    }
                });
            }
        },
        CLOSED(R.string.KEY_CLOSED){
            @Override
            void setup(Preference name) throws Exception {
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        ((Activity)preference.getContext()).onBackPressed();
                        return false;
                    }
                });
            }
        };

        private final int resId;
        abstract void setup(Preference name)throws Exception;

        EnumSettings(int key){
            resId = key;
        }
        public int getResId() { return resId; }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_settings_admin);
        scaleModule = ScalesView.getInstance().getScaleModule();
        initPreferences();
    }

    public void initPreferences(){
        for (EnumSettings enumPreference : EnumSettings.values()){
            Preference preference = findPreference(getString(enumPreference.getResId()));
            if(preference != null){
                try {
                    enumPreference.setup(preference);
                } catch (Exception e) {
                    preference.setEnabled(false);
                }
            }
        }
    }


}
