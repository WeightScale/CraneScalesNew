package com.konst.scaleslibrary.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;
import com.konst.scaleslibrary.R;

/**
 * @author Kostya 26.10.2016.
 */
public class FragmentSettingsAdmin extends PreferenceFragment {
    private EditText input;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_settings_admin);
        //startDialog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    void startDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("ВВОД КОДА");
        input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        input.setGravity(Gravity.CENTER);
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
                            if (!key){
                                try {
                                    finalize();
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();
                                }
                                return;
                            }
                        }catch (Exception e){}

                    }

                }
                Toast.makeText(getActivity(), "Неверный код", Toast.LENGTH_SHORT).show();
                onDestroy();
            }
        });
        dialog.setNegativeButton(getString(R.string.Close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                onDestroy();
            }
        });
        dialog.setMessage("Введи код доступа к административным настройкам");
        dialog.show();
    }

}
