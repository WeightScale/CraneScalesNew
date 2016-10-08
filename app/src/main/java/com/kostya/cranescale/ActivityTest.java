package com.kostya.cranescale;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.konst.scaleslibrary.ScalesFragment;
import com.konst.scaleslibrary.ScalesView;
import com.konst.scaleslibrary.module.ErrorDeviceException;

/**
 * @author Kostya on 02.10.2016.
 */
public class ActivityTest extends AppCompatActivity {
    private ScalesView scalesView;
    private Globals globals;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_main);

        globals = Globals.getInstance();
        globals.initialize(this);

        scalesView = (ScalesView)findViewById(R.id.scalesView);
        scalesView.setDiscrete(10);
        scalesView.create( globals.getPackageInfo().versionName);

    }

}
