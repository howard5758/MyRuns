package com.example.ping_jungliu.myruns3;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Ping-Jung Liu on 2018/1/11.
 */


// dialog builder for profile picture selection
public class ProfileDialogFragment extends DialogFragment {

    public interface PosNegListener {
        void onPos();

        void onNeg();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String[] options = new String[]{"Take from camera","Select from gallery"};
        AlertDialog.Builder dialogg = new AlertDialog.Builder(getActivity());
        dialogg.setTitle("Pick Profile Picture");
        dialogg.setItems(options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int selected) {
                // the listeners are implemented in ProfileActivity.java
                if (selected == 0){
                    ((PosNegListener) getActivity()).onPos();
                }
                else if (selected == 1){
                    ((PosNegListener) getActivity()).onNeg();
                }
            }
        });

        return dialogg.create();
    }
}
