package com.konst.scaleslibrary.settings;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import com.konst.scaleslibrary.R;

/**
 * @author Kostya 31.10.2016.
 */
    public class SealingLayout extends Preference implements View.OnClickListener {
    private OnClickButton onClickButton;
    public SealingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    interface OnClickButton{
        void onButtonYes();
        void onButtonNo();
    }

    @Override
    protected void onBindView(View rootView) {
        super.onBindView(rootView);

        rootView.findViewById(R.id.buttonSealing).setOnClickListener(this);
        rootView.findViewById(R.id.buttonSealingNo).setOnClickListener(this);

        // do something with myView
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.buttonSealing){
            onClickButton.onButtonYes();
        }else if (view.getId() == R.id.buttonSealingNo){
            onClickButton.onButtonNo();
        }
    }

    public void setOnClickButton(OnClickButton onClickButton) {
        this.onClickButton = onClickButton;
    }
}
