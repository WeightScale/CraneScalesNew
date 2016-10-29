package com.konst.scaleslibrary.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import com.konst.scaleslibrary.R;

import java.util.List;

/**
 * @author Kostya 29.10.2016.
 */
public class ActivityProperties extends PreferenceActivity {
    private EditText input;

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.settings_head, target);
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
}
