package com.example.norbertlukwayi.tasktimer;

import android.app.DatePickerDialog;
import android.content.Context;

public class UnbuggyDatePickerDialog extends DatePickerDialog {

    public  UnbuggyDatePickerDialog(Context context, OnDateSetListener callback, int year, int monthOfYear, int dayOfMonth) {
        super(context, callback, year, monthOfYear, dayOfMonth);
    }

    @Override
    protected void onStop() {
        // do nothing, do not call super method
    }
}
