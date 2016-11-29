package com.kostya.cranescale;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.konst.module.InterfaceModule;
import com.konst.module.scale.ObjectScales;
import com.konst.scaleslibrary.ScalesFragment;
import com.konst.scaleslibrary.ScalesView;
import com.konst.scaleslibrary.module.ErrorDeviceException;
import com.konst.scaleslibrary.module.Module;
import com.konst.scaleslibrary.module.scale.InterfaceCallbackScales;
import com.konst.scaleslibrary.module.scale.ScaleModule;
import com.kostya.cranescale.settings.ActivityPreferences;

import java.util.ArrayList;

/**
 * @author Kostya on 02.10.2016.
 */
public class ActivityTest extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private ScalesView scalesView;
    private Globals globals;
    private ListView listView;
    private final ArrayList<FragmentScales.WeightObject> arrayList = new ArrayList<>();
    private ArrayAdapter<FragmentScales.WeightObject> customListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        globals = Globals.getInstance();
        globals.initialize(this);

        //listView = (ListView)findViewById(R.id.listView);
        //listView.setCacheColorHint(getResources().getColor(R.color.transparent));
        //listView.setVerticalFadingEdgeEnabled(false);
        customListAdapter = new FragmentScales.CustomListAdapter(this, R.layout.list_item_weight, arrayList);
        //listView.setAdapter(customListAdapter);
        customListAdapter.notifyDataSetChanged();

        scalesView = (ScalesView)findViewById(R.id.scalesView);
        scalesView.create(globals.getPackageInfo().versionName, new InterfaceCallbackScales() {
            @Override
            public void onCallback(Module obj) {
                globals.setScaleModule((ScaleModule)obj);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scales, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.preferences) {
            startActivity(new Intent(getApplicationContext(), ActivityPreferences.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.search_scales) {
            //scalesView.openSearchDialog("Выбор устройства для соединения");
        } else if (id == R.id.settings) {
            startActivity(new Intent(getApplicationContext(), ActivityPreferences.class));
        }

        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scalesView.exit();
    }

    class BaseReceiver extends BroadcastReceiver {
        private final Context mContext;
        private final IntentFilter intentFilter;
        protected boolean isRegistered;

        BaseReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(InterfaceModule.ACTION_WEIGHT_STABLE);
        }

        @Override
        public void onReceive(Context context, Intent intent) { //обработчик Bluetooth
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case InterfaceModule.ACTION_WEIGHT_STABLE:
                        ObjectScales obj = (ObjectScales) intent.getSerializableExtra(InterfaceModule.EXTRA_SCALES);
                        if (obj == null)
                            return;
                        arrayList.add(new FragmentScales.WeightObject(obj.getWeight()));
                        customListAdapter.notifyDataSetChanged();                 //сохраняем стабильный вес
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
}
