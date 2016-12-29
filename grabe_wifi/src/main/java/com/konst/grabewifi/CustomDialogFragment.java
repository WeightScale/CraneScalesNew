package com.konst.grabewifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.view.View;

/**
 * @author Kostya on 04.10.2016.
 */
public class CustomDialogFragment extends DialogFragment implements View.OnClickListener{
    public static final String ARG_DIALOG = "dialog";
    public static final String ARG_BUTTON = "button";
    private DIALOG dialogID;

    enum DIALOG{
        ALERT_DIALOG1,/* Накладная открыта */
        ALERT_DIALOG2,/* Закрыть накладную */
        ALERT_DIALOG3/* Удалить запись в накладной */
    }

    enum BUTTON{
        OK,
        CANCEL,
        NO
    }

    /**
     * @param dialog Parameter .
     * @return A new instance of fragment InvoiceFragment.
     */
    public static CustomDialogFragment newInstance(DIALOG dialog) {
        CustomDialogFragment fragment = new CustomDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DIALOG, dialog.ordinal());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dialogID = DIALOG.values()[getArguments().getInt(ARG_DIALOG)];
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.CustomAlertDialogInvoice));
        builder.setCancelable(false)
                .setTitle("Сообщение");
        switch(dialogID) {
            case ALERT_DIALOG1:
                builder.setMessage("Накладная уже открыта. Закройте накладную после создайте новую.")
                        .setIcon(R.drawable.ic_notification)
                        .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //Do something here
                                dialog.dismiss();
                            }
                        });
                break;
            case ALERT_DIALOG2:
                builder.setMessage("ВЫ ХОТИТЕ ПРОВЕСТИ НАКЛАДНУЮ?")
                        .setIcon(R.drawable.ic_notification)
                        .setPositiveButton("ДА", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent().putExtra(ARG_BUTTON, BUTTON.OK.ordinal());
                                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                            }
                        }).setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton("НЕТ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent().putExtra(ARG_BUTTON, BUTTON.NO.ordinal());
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                    }
                });
                break;
            case ALERT_DIALOG3:
                builder.setMessage("ВЫ ХОТИТЕ УДАЛИТЬ ЗАПИСЬ?")
                        .setIcon(R.drawable.ic_notification)
                        .setPositiveButton("ДА", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent().putExtra(ARG_BUTTON, BUTTON.OK.ordinal());
                                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                            }
                        }).setNegativeButton("НЕТ", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                break;
            default:
                return null;
        }
        return builder.create();
    }

    @Override
    public void onClick(View view) {

    }
}
