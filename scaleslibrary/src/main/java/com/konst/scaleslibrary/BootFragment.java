package com.konst.scaleslibrary;

import android.app.*;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.konst.scaleslibrary.avr.AVRProgrammer;
import com.konst.scaleslibrary.avr.HandlerBootloader;
import com.konst.scaleslibrary.module.ErrorDeviceException;
import com.konst.scaleslibrary.module.InterfaceModule;
import com.konst.scaleslibrary.module.Module;
import com.konst.scaleslibrary.module.boot.BootModule;
import com.konst.scaleslibrary.module.scale.InterfaceCallbackScales;

import java.io.InputStream;
import java.util.Arrays;

/**
 * @author Kostya 08.11.2016.
 */
public class BootFragment extends Fragment implements View.OnClickListener {
    BootModule bootModule;
    BaseReceiver baseReceiver;
    TextView textLog;
    TextView textStatus;
    TextProgressBar progressBarJob;
    ImageView imageViewBack, imageViewBoot;
    private String version;
    private String device;
    protected boolean flagProgramsFinish = true;
    protected String hardware = "362";
    /** Версия пограммы весового модуля. */
    private final int microSoftware = 4;
    private static final String dirDeviceFiles = "device";
    private static final String dirBootFiles = "bootfiles";
    private static final String ARG_VERSION = BootFragment.class.getSimpleName()+"VERSION";
    private static final String ARG_DEVICE = BootFragment.class.getSimpleName()+"DEVICE";
    boolean flagAutoPrograming;

    enum CodeDevice{
        ATMEGA88("atmega88.xml", 0x930a),   /* 37642 */
        ATMEGA168("atmega168.xml", 0x9406), /* 37894 */
        ATMEGA328("atmega328.xml", 0x9514); /* 38164 */

        final String device;
        final int code;

        CodeDevice(String d, int c){
            device = d;
            code = c;
        }

        public String getDevice() {return device;}

        public static CodeDevice isContains(int c){
            for(CodeDevice choice : values())
                if (choice.code == c)
                    return choice;
            return null;
        }
    }

    public BootFragment(){
    }

    public static BootFragment newInstance(String version, String device) {
        BootFragment fragment = new BootFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VERSION, version);
        args.putString(ARG_DEVICE, device);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            version = getArguments().getString(ARG_VERSION);
            device = getArguments().getString(ARG_DEVICE);
        }
        baseReceiver = new BaseReceiver(getActivity());
        baseReceiver.register();
        createBootModule(device);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_boot, null);
        textLog = (TextView)view.findViewById(R.id.textLog);
        textStatus = (TextView)view.findViewById(R.id.statusProgress);
        progressBarJob = (TextProgressBar) view.findViewById(R.id.progressBarJob);
        imageViewBack = (ImageView)view.findViewById(R.id.buttonBack);
        imageViewBack.setOnClickListener(this);
        imageViewBoot = (ImageView)view.findViewById(R.id.buttonBoot);
        imageViewBoot.setOnClickListener(this);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        baseReceiver.unregister();
        if(bootModule != null)
            bootModule.dettach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ScalesView.REQUEST_DEVICE:
                    String device = data.getStringExtra(SearchDialogFragment.ARG_DEVICE);
                    if (bootModule != null)
                        bootModule.dettach();
                    createBootModule(device);
                    break;
                default:
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonBack) {
            getActivity().finish();
        } else if (v.getId() == R.id.buttonBoot){
            startBoot();
        }
    }

    public void openSearchDialog(String msg) {
        DialogFragment fragment = SearchDialogFragment.newInstance(msg);
        fragment.setTargetFragment(this, ScalesView.REQUEST_DEVICE);
        fragment.show(getFragmentManager(), fragment.getClass().getName());
    }

    public void openSearchProgress(String msg){
        DialogFragment fragment = SearchProgressFragment.newInstance(msg);
        fragment.setTargetFragment(this, ScalesView.REQUEST_ATTACH);
        fragment.show(getFragmentManager(), fragment.getClass().getName());
    }

    private void createBootModule(String device){
        try {
            BootModule.create(getActivity(), version, device, new InterfaceCallbackScales() {
                /** Сообщение о результате соединения.
                 * @param module Модуль с которым соединились. */
                @Override
                public void onCreate(Module module) {
                    if (module instanceof BootModule){
                        bootModule = (BootModule)module;
                    }
                }
            });
        }catch (Exception | ErrorDeviceException e) {
            openSearchDialog(e.getMessage());
        }
    }

    private void startBoot(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(getString(R.string.Warning_update));
        dialog.setCancelable(false);
        int numVersion = bootModule.getVersionNumber();
        if(numVersion > 1){
            hardware = bootModule.getModuleHardware();
            dialog.setMessage("После нажатия кнопки ОК начнется программирование");
            flagAutoPrograming = true;
        }else {
            dialog.setMessage(getString(R.string.TEXT_MESSAGE5));
        }
        dialog.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if(flagAutoPrograming){
                            //if (bootModule.startProgramming())
                                /*try {
                                    if (new ThreadDoDeviceDependent().execute().get()){
                                        flagProgramsFinish = true;
                                    }

                                } catch (InterruptedException | ExecutionException e) {
                                    log(e.getMessage());
                                } finally {
                                    flagProgramsFinish = true;
                                }*/
                                //new ThreadDoDeviceDependent().execute();
                                new ThreadTest().start();
                                /*if (!onProgramed()) {
                                    flagProgramsFinish = true;
                                }*/
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
                //finish();
            }
        });

        dialog.show();
    }

    /** Приемник сообщений. */
    private class BaseReceiver extends BroadcastReceiver {
        /** Контекст программы. */
        final Context mContext;
        /** Диалог отображения подключения к весовому модулю. */
        ProgressDialog dialogSearch;
        /** Фильтер намерений. */
        final IntentFilter intentFilter;
        /** Флаг если приемник зарегестрированый.*/
        protected boolean isRegistered;

        /** Конструктор нового приемника.
         * @param context the context
         */
        BaseReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(InterfaceModule.ACTION_LOAD_OK);
            intentFilter.addAction(InterfaceModule.ACTION_RECONNECT_OK);
            intentFilter.addAction(InterfaceModule.ACTION_ATTACH_START);
            intentFilter.addAction(InterfaceModule.ACTION_CONNECT_ERROR);
        }

        @Override
        public void onReceive(Context context, Intent intent) { //обработчик Bluetooth
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case InterfaceModule.ACTION_LOAD_OK:
                        imageViewBoot.setEnabled(true);
                        imageViewBoot.setAlpha(255);
                        startBoot();
                        textLog.append("Есе готово для обновления.");
                    break;
                    case InterfaceModule.ACTION_RECONNECT_OK:

                    break;
                    case InterfaceModule.ACTION_ATTACH_START:
                        String msg = intent.getStringExtra(InterfaceModule.EXTRA_DEVICE_NAME);
                        openSearchProgress(msg);
                        break;
                    case InterfaceModule.ACTION_CONNECT_ERROR:
                        String message = intent.getStringExtra(InterfaceModule.EXTRA_MESSAGE);
                        if (message == null)
                            message = "";
                        openSearchDialog(message);
                    break;
                    default:
                }
            }
        }

        /** Регистрация приемника. */
        public void register() {
            isRegistered = true;
            mContext.registerReceiver(this, intentFilter);
        }

        /** Разрегистрация приемника. */
        public void unregister() {
            if (isRegistered) {
                mContext.unregisterReceiver(this);  // edited
                isRegistered = false;
            }
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
                    //progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    //progressDialog.setMessage(msg.obj.toString());
                    textStatus.setText(msg.obj.toString());
                    progressBarJob.setMax(msg.arg1);
                    progressBarJob.setProgress(0);
                    //progressDialog.setCanceledOnTouchOutside(false);
                    //progressDialog.show();
                    break;
                case MSG_UPDATE_DIALOG:
                    progressBarJob.setHpBar(msg.arg1);
                    //progressBarJob.setProgress(msg.arg1);
                    break;
                case MSG_CLOSE_DIALOG:
                    //progressDialog.dismiss();
                    break;
                default:
            }
        }
    };

    void log(String string) { //для текста
        textLog.setText(string + '\n' + textLog.getText());
    }

    class ThreadDoDeviceDependent extends AsyncTask<Void, Message, Boolean> {
        protected AlertDialog.Builder dialog;
        protected AVRProgrammer programmer;
        private ThreadDoDeviceDependent task;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            task = this;
            imageViewBack.setEnabled(false);
            programmer = new AVRProgrammer(handlerProgrammed) {
                @Override
                public void sendByte(byte b) {
                    bootModule.sendByte(b);
                }

                @Override
                public int getByte() {
                    return bootModule.getByte();
                }

                @Override
                public void sendMessage(Message message) {
                    task.publishProgress(message);
                }
            };
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                return onProgramed();
                //programmer.doDeviceDependent();
            } catch (Exception e) {
                showMessage(e.getMessage() + " \r\n");
                //publishProgress(new Handler().obtainMessage(HandlerBootloader.Result.MSG_LOG.ordinal(), e.getMessage() + " \r\n"));
                //handlerProgrammed.obtainMessage(HandlerBootloader.Result.MSG_LOG.ordinal(), e.getMessage() + " \r\n").sendToTarget();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            flagProgramsFinish = true;
            imageViewBack.setEnabled(true);
            dialog = new AlertDialog.Builder(getActivity());
            dialog.setCancelable(false);

            if (b) {
                dialog.setTitle(getString(R.string.Warning_Loading_settings));
                dialog.setMessage(getString(R.string.TEXT_MESSAGE1));
                dialog.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case DialogInterface.BUTTON_POSITIVE:
                                /*Intent intent = new Intent(mContext, ActivityConnect.class);
                                intent.putExtra("address", ((ActivityBootloader)activity).addressDevice);
                                startActivityForResult(intent, REQUEST_CONNECT_SCALE);*/
                                openSearchDialog("");
                                break;
                            default:
                        }
                    }
                });
                dialog.setNegativeButton(getString(R.string.Close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getActivity().finish();
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

        @Override
        protected void onProgressUpdate(Message... value) {
            super.onProgressUpdate(value);
            Message msg = value[0];
            switch (HandlerBootloader.Result.values()[msg.what]) {
                case MSG_LOG:
                    log(msg.obj.toString());// обновляем TextView
                    break;
                case MSG_SHOW_DIALOG:
                    //progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    //progressDialog.setMessage(msg.obj.toString());
                    progressBarJob.setMax(msg.arg1);
                    progressBarJob.setProgress(0);
                    //progressDialog.setCanceledOnTouchOutside(false);
                    //progressDialog.show();
                    break;
                case MSG_UPDATE_DIALOG:
                    progressBarJob.setProgress(msg.arg1);
                    break;
                case MSG_CLOSE_DIALOG:
                    //progressDialog.dismiss();
                    break;
                default:
            }
        }

        protected void showMessage(String text){
            Message message= new Message();
            message.what = HandlerBootloader.Result.MSG_LOG.ordinal();
            message.obj = text + " \r\n";
            task.publishProgress(message);
        }

        boolean onProgramed() throws Exception {
            if (!bootModule.startProgramming())
                throw new Exception("Программатор не запустился");

            if (!programmer.isProgrammerId()) {
                throw new Exception(getString(R.string.Not_programmer));
                //log(getString(R.string.Not_programmer));
                //return false;
            }
            flagProgramsFinish = false;
            showMessage(getString(R.string.Programmer_defined));
            //log(getString(R.string.Programmer_defined));
            //try {

                int descriptor = programmer.getDescriptor();
                CodeDevice codeDevice = CodeDevice.isContains(descriptor);
                if(codeDevice == null){
                    throw new Exception("Фаил с дескриптором " + descriptor + " не найден! ");
                }

                String deviceFileName = codeDevice.getDevice();
                if (deviceFileName.isEmpty()) {
                    throw new Exception("Device name not specified!");
                }
                showMessage("Device " + deviceFileName);
                //log("Device " + deviceFileName);

            /*37894_mbc04.36.2_4.hex пример имени файла прошивки
            |desc||hardware ||version     desc- это сигнатура 1 и сигнатура 2     микроконтролера 0x94 ## 0x06
                                        hardware- это версия платы              mbc04.36.2
                                        version- этоверсия программы платы      4                   */
                String constructBootFile = new StringBuilder()
                        .append(descriptor).append('_')               //дескриптор сигнатура 1 и сигнатура 2
                        .append(hardware.toLowerCase())         //hardware- это версия платы
                        .append('_')
                        .append(microSoftware)             //version- этоверсия программы платы
                        .append(".hex").toString();
                showMessage(getString(R.string.TEXT_MESSAGE3) + constructBootFile);
                //log(getString(R.string.TEXT_MESSAGE3) + constructBootFile);
                String[] bootFiles = getActivity().getAssets().list(dirBootFiles);
                String bootFileName = "";
                if (Arrays.asList(bootFiles).contains(constructBootFile)) {
                    bootFileName = constructBootFile;
                }

                if (bootFileName.isEmpty()) {
                    throw new Exception("Boot фаил отсутствует для этого устройства!\r\n");
                }

                InputStream inputDeviceFile = getActivity().getAssets().open(dirDeviceFiles + '/' + deviceFileName);
                InputStream inputHexFile = getActivity().getAssets().open(dirBootFiles + '/' + bootFileName);


                //imageViewBoot.setEnabled(false);
                //imageViewBoot.setAlpha(128);
                programmer.doJob(inputDeviceFile, inputHexFile);
                programmer.doDeviceDependent();
                //new ThreadDoDeviceDependent().execute();
            /*} catch (IOException e) {
                handlerProgrammed.obtainMessage(HandlerBootloader.Result.MSG_LOG.ordinal(), e.getMessage()).sendToTarget();
                return false;
            } catch (Exception e) {
                handlerProgrammed.obtainMessage(HandlerBootloader.Result.MSG_LOG.ordinal(), e.getMessage()).sendToTarget();
                return false;
            }*/
            return true;
        }
    }

    class ThreadTest extends Thread{
        protected AlertDialog.Builder dialog;
        protected final AVRProgrammer programmer;

        ThreadTest(){
            imageViewBack.setEnabled(false);
            programmer = new AVRProgrammer(handlerProgrammed) {
                @Override
                public void sendByte(byte b) {
                    bootModule.sendByte(b);
                }

                @Override
                public int getByte() {
                    return bootModule.getByte();
                }

                @Override
                public void sendMessage(Message message) {
                    //task.publishProgress(message);
                }
            };
            setPriority(MAX_PRIORITY);
        }

        @Override
        public void run() {
            super.run();
            try {
                onProgramed();
                //programmer.doDeviceDependent();
            } catch (Exception e) {
                //showMessage(e.getMessage() + " \r\n");
                //publishProgress(new Handler().obtainMessage(HandlerBootloader.Result.MSG_LOG.ordinal(), e.getMessage() + " \r\n"));
                //handlerProgrammed.obtainMessage(HandlerBootloader.Result.MSG_LOG.ordinal(), e.getMessage() + " \r\n").sendToTarget();

            }
        }

        void onProgramed() throws Exception {
            if (!bootModule.startProgramming())
                throw new Exception("Программатор не запустился");

            if (!programmer.isProgrammerId()) {
                throw new Exception(getString(R.string.Not_programmer));
                //log(getString(R.string.Not_programmer));
                //return false;
            }
            flagProgramsFinish = false;
            //showMessage(getString(R.string.Programmer_defined));
            //log(getString(R.string.Programmer_defined));
            //try {

            int descriptor = programmer.getDescriptor();
            CodeDevice codeDevice = CodeDevice.isContains(descriptor);
            if(codeDevice == null){
                throw new Exception("Фаил с дескриптором " + descriptor + " не найден! ");
            }

            String deviceFileName = codeDevice.getDevice();
            if (deviceFileName.isEmpty()) {
                throw new Exception("Device name not specified!");
            }
            //showMessage("Device " + deviceFileName);
            //log("Device " + deviceFileName);

            /*37894_mbc04.36.2_4.hex пример имени файла прошивки
            |desc||hardware ||version     desc- это сигнатура 1 и сигнатура 2     микроконтролера 0x94 ## 0x06
                                        hardware- это версия платы              mbc04.36.2
                                        version- этоверсия программы платы      4                   */
            String constructBootFile = new StringBuilder()
                    .append(descriptor).append('_')               //дескриптор сигнатура 1 и сигнатура 2
                    .append(hardware.toLowerCase())         //hardware- это версия платы
                    .append('_')
                    .append(microSoftware)             //version- этоверсия программы платы
                    .append(".hex").toString();
            //showMessage(getString(R.string.TEXT_MESSAGE3) + constructBootFile);
            //log(getString(R.string.TEXT_MESSAGE3) + constructBootFile);
            String[] bootFiles = getActivity().getAssets().list(dirBootFiles);
            String bootFileName = "";
            if (Arrays.asList(bootFiles).contains(constructBootFile)) {
                bootFileName = constructBootFile;
            }

            if (bootFileName.isEmpty()) {
                throw new Exception("Boot фаил отсутствует для этого устройства!\r\n");
            }

            InputStream inputDeviceFile = getActivity().getAssets().open(dirDeviceFiles + '/' + deviceFileName);
            InputStream inputHexFile = getActivity().getAssets().open(dirBootFiles + '/' + bootFileName);


            //imageViewBoot.setEnabled(false);
            //imageViewBoot.setAlpha(128);
            programmer.doJob(inputDeviceFile, inputHexFile);
            programmer.doDeviceDependent();
            //new ThreadDoDeviceDependent().execute();
            /*} catch (IOException e) {
                handlerProgrammed.obtainMessage(HandlerBootloader.Result.MSG_LOG.ordinal(), e.getMessage()).sendToTarget();
                return false;
            } catch (Exception e) {
                handlerProgrammed.obtainMessage(HandlerBootloader.Result.MSG_LOG.ordinal(), e.getMessage()).sendToTarget();
                return false;
            }*/
            }


    }

}
