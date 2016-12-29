package com.konst.grabewifi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.konst.grabewifi.provider.InvoiceTable;
import com.konst.grabewifi.provider.WeighingTable;
import com.konst.grabewifi.task.IntentServiceGoogleForm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Kostya on 10.12.2016.
 */
public class FragmentListInvoice extends ListFragment {
    private SimpleCursorAdapter simpleCursorAdapter;
    private InvoiceTable invoiceTable;
    private String date;
    private static final String ARG_DATE = "arg_date";

    /**
     * @return A new instance of fragment InvoiceFragment.
     */
    public static FragmentListInvoice newInstance(String date) {
        FragmentListInvoice fragment = new FragmentListInvoice();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            date = getArguments().getString(ARG_DATE);
        }else {
            date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
        }
        invoiceTable = new InvoiceTable(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_invoice, container, false);
        setupBanner(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateListWeight();
    }

    private void setupBanner(View view){
        AdView mAdView = (AdView) view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice(Globals.getInstance().getDeviceId())
                .build();
        mAdView.loadAd(adRequest);
        //requestNewInterstitial();
    }

    private void openInvoice(long _id){
        ((MainActivity)getActivity()).openFragmentInvoice(String.valueOf(_id));
    }

    /** Обновляем данные листа загрузок. */
    private void updateListWeight() {
        Cursor cursor = invoiceTable.getToday(date);
        if (cursor == null) {
            return;
        }

        int[] to = {R.id.id_row, R.id.date_row, R.id.number_row, R.id.weight_row, R.id.imageReady};
        String[] column = {InvoiceTable.KEY_ID,InvoiceTable.KEY_DATE_CREATE,InvoiceTable.KEY_NAME_AUTO,InvoiceTable.KEY_TOTAL_WEIGHT, InvoiceTable.KEY_IS_READY};

        simpleCursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.item_list_invoice, cursor, column, to, CursorAdapter.FLAG_AUTO_REQUERY);
        //namesAdapter = new MyCursorAdapter(this, R.layout.item_check, cursor, columns, to);
        simpleCursorAdapter.setViewBinder(new ListInvoiceViewBinder());

        setListAdapter(simpleCursorAdapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, final long l) {
                Cursor cursor = invoiceTable.getEntryItem((int)l, InvoiceTable.KEY_IS_READY,InvoiceTable.KEY_TOTAL_WEIGHT);
                if (cursor != null){
                    int isReady = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_IS_READY));
                    int totalWeight = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_TOTAL_WEIGHT));
                    if (isReady != InvoiceTable.READY || totalWeight == 0){
                        if (totalWeight == 0){
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.CustomAlertDialogInvoice));
                            builder.setCancelable(false)
                                    .setTitle("Сообщение")
                                    .setMessage("НАКЛАДНАЯ" + " №"+l)
                                    .setIcon(R.drawable.ic_notification)
                                    .setPositiveButton("УДАЛИТЬ", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            invoiceTable.removeEntry((int)l);
                                            new WeighingTable(getActivity()).removeEntryInvoice((int) l);
                                        }
                                    })
                                    .setNegativeButton("ОТКРЫТЬ", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                            openInvoice(l);
                                        }
                                    });
                            builder.create().show();
                        }else {
                            openInvoice(l);
                        }
                    }else {
                        getActivity().startService(new Intent(getActivity(), IntentServiceGoogleForm.class).setAction(IntentServiceGoogleForm.ACTION_EVENT_TABLE));
                    }
                }
            }
        });
    }

    private class ListInvoiceViewBinder implements SimpleCursorAdapter.ViewBinder {
        private int ready, send;

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            switch (view.getId()){
                case R.id.imageReady:
                    ready = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_IS_READY));
                    send = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_IS_CLOUD));
                    if (ready == InvoiceTable.UNREADY){
                        ((ImageView)view).setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice_red));
                    }else if(send == InvoiceTable.READY){
                        ((ImageView)view).setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice_check));
                    }else  {
                        ((ImageView)view).setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice));
                    }
                break;
                case R.id.weight_row:

                    SpannableStringBuilder w = new SpannableStringBuilder(cursor.getString(cursor.getColumnIndex(InvoiceTable.KEY_TOTAL_WEIGHT)));
                    w.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.background2)), 0, w.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    SpannableStringBuilder textKg = new SpannableStringBuilder(getResources().getString(R.string.scales_kg));
                    textKg.setSpan(new TextAppearanceSpan(getActivity(), R.style.SpanTextKgListInvoice), 0, textKg.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    w.append(textKg);
                    ((TextView)view).setText(w, TextView.BufferType.SPANNABLE);
                break;
                default:
                    return false;
            }
            return true;
        }
    }
}
