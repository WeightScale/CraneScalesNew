package com.konst.scaleslibrary.settings;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import com.konst.scaleslibrary.R;

import java.util.List;

public class ActivitySettings extends PreferenceActivity {
    private EditText input;

    enum EnumSettings{
        ADMIN(R.string.KEY_ADMIN){
            Context context;
            private EditText input;
            @Override
            void setup(Preference name)throws Exception {
                context = name.getContext();
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startDialog();
                        return true;
                    }
                });
            }

            void startDialog(){
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle("ВВОД КОДА");
                input = new EditText(context);
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                dialog.setView(input);
                dialog.setCancelable(false);
                dialog.setPositiveButton(context.getString(R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (input.getText() != null) {
                            boolean key = false;
                            String string = input.getText().toString();
                            if (!string.isEmpty()){
                                try{
                                    if ("343434".equals(string))
                                        key = true;
                                    /*else if (string.equals(scaleModule.getModuleServiceCod()))
                                        key = true;*/
                                    if (key){
                                        ((PreferenceActivity)context).startPreferenceFragment(new FragmentSettingsAdmin(), false);
                                        return;
                                    }
                                }catch (Exception e){}
                            }
                        }
                    }
                });
                dialog.setNegativeButton(context.getString(R.string.Close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.setMessage("Введи код доступа к административным настройкам");
                dialog.show();
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
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.settings_head, target);
        /*Header header = new Header();
        header.title = "Закрыть Окно";
        header.summary = "Закрываем окно настроек";
        header.id = 0;
        header.fragment = FragmentSettingsAdmin.class.getName();

        Bundle b = new Bundle();
        b.putString("category", "MoreSettings");
        header.fragmentArguments = b;
        target.add(header);*/
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        if (header.id == R.id.settingsHeaderAdmin) {
            startDialog(header);
            //startPreferencePanel(FragmentSettingsAdmin.class.getName(), header.fragmentArguments, header.titleRes, header.title, null, 0);
        }else if (header.id == R.id.settingsHeader){
            //startPreferenceFragment(new FragmentSettings(), true);
            startPreferencePanel(FragmentSettings.class.getName(), header.fragmentArguments, header.titleRes, header.title, null, 0);
        }else if (header.id == R.id.closedHeader){
            finish();
        }
        //this.startPreferencePanel(FragmentSettingsAdmin.class.getName(), header.fragmentArguments, header.titleRes, header.title, null, 0);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        //return true;
        return FragmentSettings.class.getName().equals(fragmentName) || FragmentSettingsAdmin.class.getName().equals(fragmentName);
    }

    void startDialog(final Header header){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("ВВОД КОДА");
        input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        dialog.setView(input);
        dialog.setCancelable(false);
        dialog.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (input.getText() != null) {
                    boolean key = false;
                    String string = input.getText().toString();
                    if (!string.isEmpty()){
                        try{
                            if ("343434".equals(string))
                                key = true;
                                    /*else if (string.equals(scaleModule.getModuleServiceCod()))
                                        key = true;*/
                            if (key){
                                startPreferencePanel(FragmentSettingsAdmin.class.getName(), header.fragmentArguments, header.titleRes, header.title, null, 0);
                                //startPreferenceFragment(new FragmentSettingsAdmin(), false);
                                return;
                            }
                        }catch (Exception e){}
                    }
                }
            }
        });
        dialog.setNegativeButton(getString(R.string.Close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setMessage("Введи код доступа к административным настройкам");
        dialog.show();
    }

    public static class FragmentSettings extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.fragment_settings);
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
}
