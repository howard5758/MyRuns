package com.example.ping_jungliu.myruns3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by Ping-Jung Liu on 2018/1/13.
 */

public class MyAlertDialogFragment extends DialogFragment {

    public Calendar date_time;
    EditText input;
    int dialog_id;
    Activity paren_act;

    public static MyAlertDialogFragment newInstance(int dialog_id) {

        MyAlertDialogFragment dialogg = new MyAlertDialogFragment();
        Bundle input_bundle = new Bundle();
        input_bundle.putInt("dialog", dialog_id);
        dialogg.setArguments(input_bundle);

        return dialogg;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        date_time = Calendar.getInstance();
        paren_act  = getActivity();
        dialog_id = getArguments().getInt("dialog");
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(paren_act);


        if(dialog_id == 0){
            DatePickerDialog.OnDateSetListener date_pick = new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    date_time.set(Calendar.YEAR, year);
                    date_time.set(Calendar.MONTH, monthOfYear);
                    date_time.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    ((Manual_Input) paren_act).entry.setDateTime(date_time.getTimeInMillis());
                }
            };
            return new DatePickerDialog(getActivity(), date_pick,
                    date_time.get(Calendar.YEAR),
                    date_time.get(Calendar.MONTH),
                    date_time.get(Calendar.DAY_OF_MONTH));
        }
        else if(dialog_id == 1){
            TimePickerDialog.OnTimeSetListener time_pick = new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    date_time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    date_time.set(Calendar.MINUTE, minute);
                    date_time.set(Calendar.SECOND,0);
                    ((Manual_Input) paren_act).entry.setDateTime(date_time.getTimeInMillis());
                }
            };
            return new TimePickerDialog(getActivity(), time_pick,
                    date_time.get(Calendar.HOUR_OF_DAY),
                    date_time.get(Calendar.MINUTE), true);
        }
        else if(dialog_id == 2){
            dialogBuilder.setTitle("Duration");
            input = new EditText(paren_act);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            dialogBuilder.setView(input);
            setButtons(dialogBuilder);
        }
        else if(dialog_id == 3){
            dialogBuilder.setTitle("Distance");
            input = new EditText(paren_act);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            dialogBuilder.setView(input);
            setButtons(dialogBuilder);
        }
        else if(dialog_id == 4){
            dialogBuilder.setTitle("Calories");
            input = new EditText(paren_act);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            dialogBuilder.setView(input);
            setButtons(dialogBuilder);
        }
        else if(dialog_id == 5){
            dialogBuilder.setTitle("Heart Rate");
            input = new EditText(paren_act);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            dialogBuilder.setView(input);
            setButtons(dialogBuilder);
        }
        else if(dialog_id == 6){
            dialogBuilder.setTitle("Comment");
            input = new EditText(paren_act);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setHint("How did it go? Notes here.");
            input.setLines(4);
            dialogBuilder.setView(input);
            setButtons(dialogBuilder);
        }
        return dialogBuilder.create();
    }

    public void setButtons(AlertDialog.Builder dialogBuilder){
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int btnid) {
                String val = input.getText().toString();

                if(dialog_id == 2){
                    double inSeconds = Double.parseDouble(val);
                    if(val.equals("")){
                        ((Manual_Input) paren_act).entry.setDuration(0);
                    }
                    else{
                        ((Manual_Input) paren_act).entry.setDuration(inSeconds);
                        Log.d("hey", "hi");
                    }
                }
                else if(dialog_id == 3){
                    double result;
                    if(!val.equals(""))
                        result = Double.parseDouble(val);
                    else
                        result = 0;

                    ((Manual_Input) paren_act).entry.setDistance(result);
                }
                else if(dialog_id == 4){
                    double cal = Double.parseDouble(val);
                    if(val.equals("")){
                        ((Manual_Input) paren_act).entry.setCalories(0);
                    }
                    else{
                        ((Manual_Input) paren_act).entry.setCalories(cal);
                        Log.d("hey", "hi");
                    }
                }
                else if(dialog_id == 5){
                    double hr = Double.parseDouble(val);
                    if(val.equals("")){
                        ((Manual_Input) paren_act).entry.setHeartRate(0);
                    }
                    else{
                        ((Manual_Input) paren_act).entry.setHeartRate(hr);
                        Log.d("hey", "hi");
                    }
                }
                else if(dialog_id == 6){
                    if(val.equals("")){
                        ((Manual_Input) paren_act).entry.setComment("");
                    }
                    else{
                        ((Manual_Input) paren_act).entry.setComment(val);
                        Log.d("hey", "hi");
                    }
                }

                return;
            }
        });
        dialogBuilder.setNegativeButton("CENCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int btnid) {
                return;
            }
        });
    }
}