package com.kostya.cranescale.bootloader;

import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.*;
import android.widget.*;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.konst.module.InterfaceModule;
import com.konst.module.Module;
import com.konst.module.scale.ObjectScales;
import com.konst.module.scale.ScaleModule;
import com.konst.scaleslibrary.module.boot.BootModule;
import com.kostya.cranescale.*;
import com.kostya.cranescale.services.ServiceScales;
import com.kostya.cranescale.settings.ActivityPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;

public class FragmentBoot extends Fragment implements View.OnClickListener{
    private Activity activity;
    private Context mContext;
    private ImageView startBoot, buttonBack;
    private TextView textViewLog;
    private ProgressDialog progressDialog;
    private Globals globals;
    private BootModule bootModule;
    private Vibrator vibrator; //вибратор
    private BaseReceiver baseReceiver; //приёмник намерений
    protected boolean flagProgramsFinish = true;
    private static final String dirDeviceFiles = "device";
    private static final String dirBootFiles = "bootfiles";
    static final int REQUEST_CONNECT_BOOT = 1;
    static final int REQUEST_CONNECT_SCALE = 2;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        mContext = activity;
        baseReceiver = new BaseReceiver(mContext);
        baseReceiver.register();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            screenUnlock();
        }catch (Exception e){}

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        globals = Globals.getInstance();
        bootModule = globals.getBootModule();

        vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bootloder, null);

        AdView mAdView = (AdView) view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice(Main.getInstance().getDeviceId())
                .build();
        mAdView.loadAd(adRequest);

        textViewLog = (TextView) view.findViewById(R.id.textLog);
        startBoot = (ImageView) view.findViewById(R.id.buttonBoot);
        startBoot.setOnClickListener(this);
        startBoot.setEnabled(false);
        startBoot.setAlpha(128);
        buttonBack = (ImageView) view.findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(this);

        progressDialog = new ProgressDialog(mContext);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonBack:
                activity.finish();
                break;
            case R.id.buttonBoot:
                if (!startProgramed()) {
                    flagProgramsFinish = true;
                }
                break;
            default:
        }
    }

    @Override
    public void onDestroy() { //при разрушении активности
        super.onDestroy();
        exit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_scales, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.preferences:
                startActivity(new Intent(mContext, ActivityPreferences.class));
            break;
            case R.id.search:
                vibrator.vibrate(100);
                ((ActivityBootloader)activity).openSearch();
            break;
            case R.id.exit:
                //closeOptionsMenu();
            break;
            case R.id.power_off:
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setTitle(getString(R.string.Scale_off));
                dialog.setCancelable(false);
                dialog.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == DialogInterface.BUTTON_POSITIVE) {
                            //if (globals.isScaleConnect())
                            mContext.startService(new Intent(mContext, ServiceScales.class).setAction(ServiceScales.ACTION_POWER_OFF_SCALES));
                            //try {scaleModule.powerOff();}catch (Exception e){}
                            activity.finish();
                        }
                    }
                });
                dialog.setNegativeButton(getString(R.string.Close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });
                dialog.setMessage(getString(R.string.TEXT_MESSAGE15));
                dialog.show();
            break;
            default:

        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            log("Connected...");
            switch (requestCode) {
                case REQUEST_CONNECT_BOOT:
                    //connectResultCallback.resultConnect(Module.ResultConnect.STATUS_LOAD_OK, "", BootModule.getInstance()); // TODO: 27.08.2016 что то сделать при соединении
                    break;
                case REQUEST_CONNECT_SCALE:
                    log(getString(R.string.Loading_settings));
                    /*if (ScaleModule.isScales()) {
                        //restorePreferences(); //todo сделать загрузку настроек которые сохранены пере перепрограммированием.
                        log(getString(R.string.Settings_loaded));
                        break;
                    }*/
                    log(getString(R.string.Scale_no_defined));
                    log(getString(R.string.Setting_no_loaded));
                    break;
                default:
            }
        } else {
            log("Not connected...");
        }
    }

    boolean startProgramed() {

        if (!programmer.isProgrammerId()) {
            log(getString(R.string.Not_programmer));
            return false;
        }
        flagProgramsFinish = false;
        log(getString(R.string.Programmer_defined));
        try {

            int descriptor = programmer.getDescriptor();

            ActivityBootloader.CodeDevice codeDevice = ActivityBootloader.CodeDevice.contains(descriptor);

            if(codeDevice == null){
                throw new Exception("Фаил с дескриптором " + descriptor + " не найден! ");
            }

            /*if (mapCodeDevice.get(desc) == null) {
                throw new Exception("Фаил с дескриптором " + desc + " не найден! ");
            }*/

            //String deviceFileName = mapCodeDevice.get(desc);
            String deviceFileName = codeDevice.getDevice();
            if (deviceFileName.isEmpty()) {
                throw new Exception("Device name not specified!");
            }

            log("Device " + deviceFileName);

            /*37894_mbc04.36.2_4.hex пример имени файла прошивки
            |desc||hardware ||version     desc- это сигнатура 1 и сигнатура 2     микроконтролера 0x94 ## 0x06
                                        hardware- это версия платы              mbc04.36.2
                                        version- этоверсия программы платы      4                   */
            String constructBootFile = new StringBuilder()
                    .append(descriptor).append('_')               //дескриптор сигнатура 1 и сигнатура 2
                    .append(((ActivityBootloader)activity).hardware.toLowerCase())         //hardware- это версия платы
                    .append('_')
                    .append(globals.getMicroSoftware())             //version- этоверсия программы платы
                    .append(".hex").toString();
            log(getString(R.string.TEXT_MESSAGE3) + constructBootFile);
            String[] bootFiles = activity.getAssets().list(dirBootFiles);
            String bootFileName = "";
            if (Arrays.asList(bootFiles).contains(constructBootFile)) {
                bootFileName = constructBootFile;
            }

            if (bootFileName.isEmpty()) {
                throw new Exception("Boot фаил отсутствует для этого устройства!\r\n");
            }

            InputStream inputDeviceFile = activity.getAssets().open(dirDeviceFiles + '/' + deviceFileName);
            InputStream inputHexFile = activity.getAssets().open(dirBootFiles + '/' + bootFileName);


            startBoot.setEnabled(false);
            startBoot.setAlpha(128);
            programmer.doJob(inputDeviceFile, inputHexFile);
            new ThreadDoDeviceDependent().execute();
        } catch (IOException e) {
            handlerProgrammed.obtainMessage(HandlerBootloader.Result.MSG_LOG.ordinal(), e.getMessage()).sendToTarget();
            return false;
        } catch (Exception e) {
            handlerProgrammed.obtainMessage(HandlerBootloader.Result.MSG_LOG.ordinal(), e.getMessage()).sendToTarget();
            return false;
        }
        return true;
    }

    class ThreadDoDeviceDependent extends AsyncTask<Void, Void, Boolean> {
        protected AlertDialog.Builder dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            buttonBack.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                programmer.doDeviceDependent();
            } catch (Exception e) {
                handlerProgrammed.obtainMessage(HandlerBootloader.Result.MSG_LOG.ordinal(), e.getMessage() + " \r\n").sendToTarget();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            flagProgramsFinish = true;
            buttonBack.setEnabled(true);
            dialog = new AlertDialog.Builder(mContext);
            dialog.setCancelable(false);

            if (b) {
                dialog.setTitle(getString(R.string.Warning_Loading_settings));
                dialog.setMessage(getString(R.string.TEXT_MESSAGE1));
                dialog.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Intent intent = new Intent(mContext, ActivityConnect.class);
                                intent.putExtra("address", ((ActivityBootloader)activity).addressDevice);
                                startActivityForResult(intent, REQUEST_CONNECT_SCALE);
                                break;
                            default:
                        }
                    }
                });
                dialog.setNegativeButton(getString(R.string.Close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        activity.finish();
                    }
                });
            } else {
                dialog.setTitle(getString(R.string.Warning_Error));
                dialog.setMessage(getString(R.string.TEXT_MESSAGE2));
                dialog.setNegativeButton(getString(R.string.Close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
            dialog.show();
        }
    }

    final HandlerBootloader handlerProgrammed = new HandlerBootloader() {

        @Override
        public void handleMessage(Message msg) {
            switch (HandlerBootloader.Result.values()[msg.what]) {
                case MSG_LOG:
                    log(msg.obj.toString());// обновляем TextView
                    break;
                case MSG_SHOW_DIALOG:
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setMessage(msg.obj.toString());
                    progressDialog.setMax(msg.arg1);
                    progressDialog.setProgress(0);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    break;
                case MSG_UPDATE_DIALOG:
                    progressDialog.setProgress(msg.arg1);
                    break;
                case MSG_CLOSE_DIALOG:
                    progressDialog.dismiss();
                    break;
                default:
            }
        }
    };

    private final AVRProgrammer programmer = new AVRProgrammer(handlerProgrammed) {
        @Override
        public void sendByte(byte b) {
            bootModule.sendByte(b);
        }

        @Override
        public int getByte() {
            return bootModule.getByte();
        }
    };

    void log(String string) { //для текста
        textViewLog.setText(string + '\n' + textViewLog.getText());
    }

    protected void exit() {
        if (flagProgramsFinish) {
            //Preferences.load(getSharedPreferences(Preferences.PREFERENCES, Context.MODE_PRIVATE));
            globals.getPreferencesScale().write(getString(R.string.KEY_FLAG_UPDATE), true);
            if(bootModule != null)
                bootModule.dettach();
            BluetoothAdapter.getDefaultAdapter().disable();
            while (BluetoothAdapter.getDefaultAdapter().isEnabled()) ;
            activity.finish();
        }
        baseReceiver.unregister();
        //todo System.exit(0);
    }

    private void wakeUp(){
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        wakeLock.acquire();
    }

    private void screenUnlock(){
        KeyguardManager keyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
        keyguardLock.disableKeyguard();
    }

    class BaseReceiver extends BroadcastReceiver {
        private final Context mContext;
        private SpannableStringBuilder w;
        private Rect bounds;
        private ProgressDialog dialogSearch;
        private final IntentFilter intentFilter;
        protected boolean isRegistered;

        BaseReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            intentFilter.addAction(InterfaceModule.ACTION_SCALES_RESULT);
            intentFilter.addAction(InterfaceModule.ACTION_WEIGHT_STABLE);
        }

        @Override
        public void onReceive(Context context, Intent intent) { //обработчик Bluetooth
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        switch (BluetoothAdapter.getDefaultAdapter().getState()) {
                            case BluetoothAdapter.STATE_OFF:
                                Toast.makeText(mContext, R.string.bluetooth_off, Toast.LENGTH_SHORT).show();
                                new Internet(mContext).turnOnWiFiConnection(false);
                                BluetoothAdapter.getDefaultAdapter().enable();
                            break;
                            case BluetoothAdapter.STATE_TURNING_ON:
                                Toast.makeText(mContext, R.string.bluetooth_turning_on, Toast.LENGTH_SHORT).show();
                            break;
                            case BluetoothAdapter.STATE_ON:
                                Toast.makeText(mContext, R.string.bluetooth_on, Toast.LENGTH_SHORT).show();
                            break;
                            default:
                                break;
                        }
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

    public class ReportHelper implements Thread.UncaughtExceptionHandler {
        private AlertDialog dialog;
        private final Context context;

        public ReportHelper(Context context) {
            this.context = context;
        }

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            String text = ex.getMessage();
            if(text == null){
                text = "";
            }
            showToastInThread(text);
        }

        public void showToastInThread(final CharSequence str){
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(str)
                            .setTitle("Ошибка приложения")
                            .setCancelable(false)
                            .setNegativeButton("Выход", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    exit();
                                }
                            });
                    dialog = builder.create();

                    //Toast.makeText(context, str, Toast.LENGTH_LONG).show();
                    if(!dialog.isShowing())
                        dialog.show();
                    Looper.loop();
                }
            }.start();
        }
    }

    void showNotify(){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle("test").build();
        mBuilder.setContentIntent(PendingIntent.getActivity(mContext, 0, new Intent(mContext, FragmentBoot.class), PendingIntent.FLAG_UPDATE_CURRENT));
        NotificationManager mNotificationManager =  (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }

}
