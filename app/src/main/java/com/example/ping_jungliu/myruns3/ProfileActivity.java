package com.example.ping_jungliu.myruns3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class ProfileActivity extends AppCompatActivity implements com.example.ping_jungliu.myruns3.ProfileDialogFragment.PosNegListener {

    // initialize useful variables
    private ImageView picture;
    boolean photoo = false;
    Uri picUri;
    Uri temp_picUri;
    Uri temp_galUri;
    Button camera;
    String PHOTO_NAME = "profile.jpg";
    String TEMP_PHOTO = "profile_temp.jpg";

    protected void onCreate(Bundle savedInstanceState) {

        //fix uri exposure problem
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        camera = (Button) findViewById(R.id.camera);
        picture = (ImageView) findViewById(R.id.picture);

        // specify uri paths
        picUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), PHOTO_NAME));
        temp_picUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO));
        temp_galUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "profile_gal_temp.jpg"));
        // load saved information
        loadUserInfo();

        // load savedInstanceState data
        if (savedInstanceState != null) {
            try {
                if (savedInstanceState.getBoolean("photo_taken")) {
                    photoo = true;
                    picUri = savedInstanceState.getParcelable("photo");
                    Bitmap bm = BitmapFactory.decodeStream(getContentResolver().openInputStream(picUri));
                    picture.setImageBitmap(bm);
                }
            } catch (IOException e) {
                Log.d("no", "no");
            }
        }
        // onclick function

    }

    // profile picture dialog listener
    public void onPos() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, temp_picUri);
        startActivityForResult(intent, 0);
    }

    public void onNeg() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 10);
    }

    // save current photo uri
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the image capture uri before the activity goes into background
        outState.putParcelable("photo", picUri);
        outState.putBoolean("photo_taken", photoo);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // camera
        if (requestCode == 0 && resultCode == RESULT_OK) {
            beginCrop(temp_picUri);
        }
        // gallery
        else if (requestCode == 10 && resultCode == RESULT_OK ) {
            temp_galUri = data.getData();
            beginCrop(temp_galUri);
        }
        // manage the crop results
        else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }

    }

    private void beginCrop(Uri source) {

        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            Log.d("crop", "ok");
            photoo = true;
            picture.setImageURI(null);
            picUri = Crop.getOutput(result);
            picture.setImageURI(picUri);
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserInfo(){

        picture.buildDrawingCache();
        Bitmap bitmap = picture.getDrawingCache();
        try {
            FileOutputStream fileee = openFileOutput(
                    PHOTO_NAME, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileee);
            fileee.flush();
            fileee.close();
        } catch (IOException ioe) {
            Log.d("no", "no");
        }

        SharedPreferences prefs = getSharedPreferences("prefs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();

        EditText textID = (EditText) findViewById(R.id.namee);
        String storee = textID.getText().toString();
        editor.putString("NAME", storee);

        textID = (EditText) findViewById(R.id.emaill);
        storee = textID.getText().toString();
        editor.putString("EMAIL", storee);

        textID = (EditText) findViewById(R.id.phonee);
        storee = textID.getText().toString();
        editor.putString("PHONE", storee);

        RadioGroup radioID = (RadioGroup) findViewById(R.id.rdGroup);
        int genderr = radioID.indexOfChild(findViewById(radioID
                .getCheckedRadioButtonId()));
        editor.putInt("GENDER", genderr);

        textID = (EditText) findViewById(R.id.classss);
        storee = textID.getText().toString();
        editor.putString("CLASS", storee);

        textID = (EditText) findViewById(R.id.majorr);
        storee = textID.getText().toString();
        editor.putString("MAJOR", storee);

        editor.commit();
    }

    private void loadUserInfo(){

        try {
            // Retrieve photo from internal storage
            FileInputStream rFile = openFileInput(PHOTO_NAME);
            Bitmap bmap = BitmapFactory.decodeStream(rFile);
            picture.setImageBitmap(bmap);
            rFile.close();

        } catch (IOException e) {
            // Set to default profile pict is nothing is stored
            picture.setImageResource(R.drawable.dartmouth);
        }

        SharedPreferences prefs = getSharedPreferences("prefs", 0);

        EditText textID = (EditText) findViewById(R.id.namee);
        String loadd = prefs.getString("NAME", "");
        textID.setText(loadd);

        textID = (EditText) findViewById(R.id.emaill);
        loadd = prefs.getString("EMAIL", "");
        textID.setText(loadd);

        textID = (EditText) findViewById(R.id.phonee);
        loadd = prefs.getString("PHONE", "");
        textID.setText(loadd);

        int genderr = prefs.getInt("GENDER", -1);
        RadioButton buttonID = (RadioButton) ((RadioGroup) findViewById(R.id.rdGroup))
                .getChildAt(genderr);
        if (genderr >= 0) {buttonID.setChecked(true);}

        textID = (EditText) findViewById(R.id.classss);
        loadd = prefs.getString("CLASS", "");
        textID.setText(loadd);

        textID = (EditText) findViewById(R.id.majorr);
        loadd = prefs.getString("MAJOR", "");
        textID.setText(loadd);
    }

    ////////////
    //buttons
    public void onClick_photo(View view) {
        // indicate file path
        //picUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), PHOTO_NAME));
        //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // save result into picUri
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, temp_picUri);
        //startActivityForResult(intent, 0);
        new com.example.ping_jungliu.myruns3.ProfileDialogFragment().show(getFragmentManager(), "tag");
    }

    public void onClick_save(View v) {

        // Save the user information into a "shared preferences"
        // using private helper function
        saveUserInfo();

        Toast.makeText(getApplicationContext(), "Saved",
                Toast.LENGTH_SHORT).show();

        finish();
    }

    public void onClick_exitt(View view) {

        SharedPreferences prefs = getSharedPreferences("prefs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();

        ImageView imageID = (ImageView) findViewById(R.id.picture);
        if (temp_picUri != null){
            String tagString = temp_picUri.toString();
            editor.putString("PROFILE", tagString);
        }
        finish();
    }
}

