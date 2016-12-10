package com.kostya.cranescale;

import android.app.Activity;
import android.app.ListFragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.kostya.cranescale.provider.InvoiceTable;
import com.kostya.cranescale.provider.WeighingTable;

/**
 * @author Kostya on 10.12.2016.
 */
public class FragmentListInvoice extends ListFragment {
    private SimpleCursorAdapter simpleCursorAdapter;
    private InvoiceTable invoiceTable;
    //private OnFragmentListInvoiceListener onFragmentListInvoiceListener;

    /**
     * @return A new instance of fragment InvoiceFragment.
     */
    public static FragmentListInvoice newInstance() {
        FragmentListInvoice fragment = new FragmentListInvoice();
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*if (activity instanceof OnFragmentListInvoiceListener) {
            onFragmentListInvoiceListener = (OnFragmentListInvoiceListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement OnFragmentListInvoiceListener");
        }*/
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        invoiceTable = new InvoiceTable(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_invoice, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateListWeight();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void openInvoice(long _id){
        ((ActivityTest)getActivity()).openFragmentInvoice(String.valueOf(_id));
    }

    /** Обновляем данные листа загрузок. */
    private void updateListWeight() {
        Cursor cursor = invoiceTable.getAllItem();
        if (cursor == null) {
            return;
        }

        int[] to = {R.id.id_row, R.id.date_row, R.id.number_row, R.id.weight_row, R.id.imageReady};
        String[] column = {InvoiceTable.KEY_ID,InvoiceTable.KEY_DATE_TIME_CREATE,InvoiceTable.KEY_NAME_AUTO,InvoiceTable.KEY_TOTAL_WEIGHT, InvoiceTable.KEY_IS_READY};

        simpleCursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.item_list_invoice, cursor, column, to, CursorAdapter.FLAG_AUTO_REQUERY);
        //namesAdapter = new MyCursorAdapter(this, R.layout.item_check, cursor, columns, to);
        simpleCursorAdapter.setViewBinder(new ListInvoiceViewBinder());

        setListAdapter(simpleCursorAdapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor = invoiceTable.getEntryItem((int)l, InvoiceTable.KEY_IS_READY);
                if (cursor != null){
                    int isReady = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_IS_READY));
                    if (isReady != InvoiceTable.READY){
                        openInvoice(l);
                    }
                }
            }
        });
    }

    private class ListInvoiceViewBinder implements SimpleCursorAdapter.ViewBinder {
        private int ready;

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            switch (view.getId()){
                case R.id.imageReady:
                    ready = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_IS_READY));
                    if (ready == InvoiceTable.UNREADY){
                        ((ImageView)view).setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice_red));
                    }else {
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
