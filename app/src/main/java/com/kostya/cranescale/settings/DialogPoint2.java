package com.kostya.cranescale.settings;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.konst.scaleslibrary.module.scale.ScaleModule;
import com.kostya.cranescale.Globals;
import com.kostya.cranescale.R;

/**
 * @author Kostya
 */
public class DialogPoint2 extends DialogPreference /*implements ScaleModule.WeightCallback*/ {
    TextView textViewSensor;
    EditText editTextPoint2;
    final ScaleModule scaleModule;
    final Context mContext;

    public DialogPoint2(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setPersistent(false);
        setDialogLayoutResource(R.layout.point2_dialog);
        scaleModule = Globals.getInstance().getScaleModule();
        //scaleModule.setWeightCallback(this);
    }

    @Override
    protected View onCreateDialogView() {
        scaleModule.scalesProcessEnable(true);
        return super.onCreateDialogView();
    }

    @Override
    protected void onBindDialogView(View view) {
        textViewSensor = (TextView)view.findViewById(R.id.textViewTitle);
        editTextPoint2 = (EditText)view.findViewById(R.id.editTextPoint2);
        super.onBindDialogView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // needed when user edits the text field and clicks OK
            setValue(editTextPoint2.getText().toString());
        }
        scaleModule.scalesProcessEnable(false);
    }


    /*@Override
    public void weight(ScaleModule.ResultWeight what, int weight, final int sensor) {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewSensor.setText("датчик:"+ sensor);
            }
        });
    }*/

    public void setValue(String value) {
        notifyChanged();
        callChangeListener(value);
    }


}
