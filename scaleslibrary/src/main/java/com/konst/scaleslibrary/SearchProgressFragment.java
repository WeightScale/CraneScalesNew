package com.konst.scaleslibrary;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.konst.scaleslibrary.module.InterfaceModule;

/**
 *
 */
public class SearchProgressFragment extends DialogFragment {
    private BaseReceiver broadcastReceiver;                   //приёмник намерений
    private String message;
    public static final String ARG_MESSAGE = SearchProgressFragment.class.getName() +".ARG_MESSAGE";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment SearchFragment.
     */
    public static SearchProgressFragment newInstance(String message) {
        SearchProgressFragment fragment = new SearchProgressFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            message = getArguments().getString(ARG_MESSAGE);
        }
        broadcastReceiver = new BaseReceiver(getActivity());
        broadcastReceiver.register();
    }

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.connect_dialog, container, false);

        TextView textView = (TextView)view.findViewById(R.id.textView1);
        textView.setText(getActivity().getString(R.string.Connecting) + '\n' + message);

        ImageButton buttonBack = (ImageButton)view.findViewById(R.id.buttonBack);
        buttonBack.setVisibility(View.VISIBLE);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonPressed();
            }
        });
        getDialog().setCancelable(false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return view;
    }*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.connect_dialog, null);

        TextView textView = (TextView)view.findViewById(R.id.textView1);
        textView.setText(getActivity().getString(R.string.Connecting) + '\n' + message);

        ImageButton buttonBack = (ImageButton)view.findViewById(R.id.buttonBack);
        buttonBack.setVisibility(View.VISIBLE);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonPressed();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_Transparent);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        //ColorDrawable dialogColor = new ColorDrawable(Color.TRANSPARENT);
        //dialogColor.setAlpha(30);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    public void onButtonPressed() {
        getTargetFragment().onActivityResult(ScalesView.REQUEST_BROKEN, Activity.RESULT_OK, new Intent());
        dismiss();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        broadcastReceiver.unregister();
    }

    /**      */
    public interface OnFragmentInteractionListener {
        void onLinkBroken();
    }

    class BaseReceiver extends BroadcastReceiver {
        final Context mContext;
        final IntentFilter intentFilter;
        protected boolean isRegistered;

        BaseReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(InterfaceModule.ACTION_ATTACH_FINISH);
        }

        @Override
        public void onReceive(Context context, Intent intent) { //обработчик Bluetooth
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case InterfaceModule.ACTION_ATTACH_FINISH:
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, new Intent());
                        dismiss();
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
