package com.konst.scaleslibrary;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.konst.scaleslibrary.module.ErrorDeviceException;
import com.konst.scaleslibrary.module.InterfaceModule;
import com.konst.scaleslibrary.module.Module;
import com.konst.scaleslibrary.module.boot.BootModule;
import com.konst.scaleslibrary.module.scale.InterfaceCallbackScales;
import com.konst.scaleslibrary.module.scale.ScaleModule;

/**
 * @author Kostya 08.11.2016.
 */
public class BootFragment extends Fragment {
    BootModule bootModule;
    BaseReceiver baseReceiver;
    TextView textLog;
    ProgressBar progressBarJob;
    ImageView imageViewBack, imageViewBoot;
    private static final String ARG_VERSION = BootFragment.class.getSimpleName()+"VERSION";
    private static final String ARG_DEVICE = BootFragment.class.getSimpleName()+"DEVICE";
    private String version;
    private String device;

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
        progressBarJob = (ProgressBar)view.findViewById(R.id.progressBarJob);
        imageViewBack = (ImageView)view.findViewById(R.id.buttonBack);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSearchDialog("test");
            }
        });
        imageViewBoot = (ImageView)view.findViewById(R.id.buttonBoot);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        baseReceiver.unregister();
    }

    public void openSearchDialog(String msg) {
        SearchDialog dialog = new SearchDialog(getActivity(), msg, new ScalesView.OnCreateScalesListener() {
            @Override
            public void onCreate(String d) {
                device = d;
                createBootModule(device);
            }
        });
        dialog.show();
    }

    private void createBootModule(String device){
        try {
            BootModule.create(getActivity(), version, device, new InterfaceCallbackScales() {
                /** Сообщение о результате соединения.
                 * @param module Модуль с которым соединились. */
                @Override
                public void onCallback(Module module) {
                    if (module instanceof BootModule){
                        bootModule = (BootModule)module;
                    }
                }
            });
        }catch (Exception | ErrorDeviceException e) {
            openSearchDialog(e.getMessage());
        }
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
            intentFilter.addAction(InterfaceModule.ACTION_CONNECT_ERROR);
        }

        @Override
        public void onReceive(Context context, Intent intent) { //обработчик Bluetooth
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case InterfaceModule.ACTION_LOAD_OK:
                        //unlockOrientation();

                    break;
                    case InterfaceModule.ACTION_RECONNECT_OK:

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

}
