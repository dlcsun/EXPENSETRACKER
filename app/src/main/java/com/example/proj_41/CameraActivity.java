package com.example.proj_41;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity {
    ImageButton buttonBack;
    Button buttonAdd;
    Button buttonSave;
    float x1,x2,y1,y2;

    ArrayList<String> filenames = new ArrayList<String>();

    EditText mResultEt;
    ImageView mPreviewIv;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    String[] cameraPermission;
    String[] storagePermission;


    Uri image_uri;
    public String path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mResultEt = findViewById(R.id.resultEt);
        mPreviewIv = findViewById(R.id.imageIv);

        //Camera permission
        cameraPermission = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //Storage permission
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //Back Button
        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMain();
            }
        });

        //Add Receipt Button
        //buttonSa.setVisibility(View.VISIBLE);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageImportDialog();
            }
        });


        //Save Button
        buttonSave =findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm:ss");
                String txtfile = DateFormat.getDateInstance().format(calendar.getTime())+"-"+ mdformat.format(calendar.getTime());

                String filename = txtfile + ".txt";
                FileOutputStream outstream;
                try {
                    outstream = openFileOutput(filename, Context.MODE_PRIVATE);
                    outstream.write(mResultEt.getText().toString().getBytes());
                    outstream.close();
                }
                catch(Exception e) {
                    Log.d("debug", "file not found; creating new file");
                }
                buttonSave.setVisibility(View.INVISIBLE);
                mResultEt.setText("");
                mPreviewIv.setImageURI(null);

            }
        });
    }
    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = touchevent.getX();
                y1 = touchevent.getY();

                break;
            case MotionEvent.ACTION_UP:
                x2 = touchevent.getX();
                y2 = touchevent.getY();
                if (x2 < x1) {
                    Intent intent = new Intent(this, AboutActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
                if (x1 < x2) {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                }
                break;
        }
        return false;
    }

    //Back to Main
    public void openMain(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    //popup
    private void showImageImportDialog() {
        String[] items = {"Camera", "Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Select Image");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0) {
                    //camera option clicked
                    /* for OS marshmallow and above we need to ask runtime permission
                    for camera and storage*/
                    if(!checkCameraPermission()) {
                        //camera permission not allowed, request it
                        requestCameraPermission();
                    }
                    else {
                        //permission allowed, take picture
                        pickCamera();
                    }
                }
                if(which==1) {
                    //gallery option clicked
                    if(!checkStoragePermission()) {
                        //Storage permission not allowed, request it
                        requestStoragePermission();
                    }
                    else {
                        //permission allowed, take picture
                        pickGallery();
                    }
                }
            }
        });
        dialog.create().show();
    }

    private void pickGallery() {
        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        //set intent type to image
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        //intent to take image from camera, it will also be saved to storage to get high quality image
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Track My Expenses"); //title of the picture
        values.put(MediaStore.Images.Media.DESCRIPTION, "Receipt");// description
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);

    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)== (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        /* Check camera permission and return the result
         * In order to get high quality image we have to save image to external storage first
         * before inserting image view that is why storage permission will also be required*/
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)== (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)== (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    //handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted&&writeStorageAccepted){
                        pickCamera();
                    }
                    else{
                        Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if(grantResults.length>0) {
                    boolean writeStorageAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickGallery();
                    } else {
                        Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    //handle image result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //got image from camera
        if(resultCode==RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                //got image from gallery now crop it
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON) //enable image guidelines
                        .start(this);
            }
            if(requestCode==IMAGE_PICK_CAMERA_CODE){
                //got image from camera now crop it
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON) //enable image guidelines
                        .start(this);
            }
        }
        //get cropped image
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){
                Uri resultUri = result.getUri(); // get image Uri
                //set image to image view
                mPreviewIv.setImageURI(resultUri);
                //Asshole protection
                buttonSave.setVisibility(View.VISIBLE);
                //get drawable bitmap for text recognition
                BitmapDrawable bitmapDrawable = (BitmapDrawable)mPreviewIv.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();

                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                if(!recognizer.isOperational()){
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();

                }
                else{
                    //loop through everything, each time total appears, see if it's related to a value
                    //if not, skip it
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = recognizer.detect(frame);
                    StringBuilder sb = new StringBuilder();

                    boolean endoffile = false;
                    int totals = 0;
                    result test = new result(null, null, 0, -1, -1, 0, 0, 0);
                    //ArrayList<result> results = new ArrayList<result>();
                    //String out = "";
                    while (!endoffile){
                        test = getlines(items, ".*\\btotal\\b.*|.*\\bbalance\\b.*|.*\\bsale amount\\b.*|.*\\bcharge\\b.*|.*\\bdebit\\b.*", ".*\\$?\\s*[0-9]+(\\.\\s?[0-9][0-9])?$", test.keyblock, test.keyline + 1, test.keyword);//set text to edit text
                        endoffile = test.key == null;
                        //results.add(test);
                        if(test.key != null && test.price != null){
                            sb.append("Receipt " + ++totals + " total: " + test.price.getValue() + "\n");
                        }
                    }

                    //then loop through for items
                    //
                    mResultEt.setText(sb.toString());
                }
            }
            else if (resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                //if there is any error show it
                Exception error = result.getError();
                Toast.makeText(this, ""+error, Toast.LENGTH_SHORT).show();

            }
        }
    }

    private class result{
        Text key, price;
        int keyblock, keyline, keyword, priceblock, priceline, priceword;
        public result(Text k, Text p, int kb, int kl, int kw, int pb, int pl, int pw){
            key = k;
            price = p;
            keyblock = kb;
            keyline = kl;
            keyword = kw;
            priceblock = pb;
            priceline = pl;
            priceword = pw;
        }
    }

    public result getlines(SparseArray<TextBlock> items, String rawregexname, String rawregexprice){
        return getlines(items, rawregexname, rawregexprice, 0, 0, 0);
    }

    public result getlines(SparseArray<TextBlock> items, String rawregexname, String rawregexprice, int blockplace, int lineplace, int wordplace){
        Text key = null;
        Text price = null;
        result out = new result(key, price, 0, 0, 0, 0, 0, 0);
        //get text from sb until there is no text
        Log.d("Processing", "Stating parse at " + blockplace + ", " + lineplace + ", " + wordplace);
        int i = 0, j = 0, i2 = 0, j2 = 0, k2 = 0;
        for(i = blockplace;i<items.size() && key == null;i++) {     //these are blocks
            if (items.valueAt(i) != null) {
                Log.d("Processing", "i = " + i + ": " + items.valueAt(i).getValue() + " [" + items.valueAt(i).getBoundingBox().left + ", " + items.valueAt(i).getBoundingBox().top + "]");
            }
            TextBlock section = items.valueAt(i);
            if (section != null && section.getValue() != null) {
                List<? extends Text> lines = section.getComponents();
                if (i == blockplace) {
                    j = lineplace;
                } else {
                    j = 0;
                }
                for (; j < lines.size() && key == null; j++) {    //these are lines
                    if (lines.get(j) != null) {
                        Log.d("Processing", "  j = " + j + ": " + lines.get(j).getValue() + " [" + lines.get(j).getBoundingBox().left + ", " + lines.get(j).getBoundingBox().top + "]");
                    }
                    //Log.d("Processing", "    k = " + k + ": " + lines.get(k).getValue() + " [" + lines.get(k).getBoundingBox().left + ", " + lines.get(k).getBoundingBox().top + "]");
                    //boolean sameLine =Math.abs(word.getBoundingBox().top-total.getBoundingBox().top)<10;
                    Text line = lines.get(j);
                    if (line.getValue().toLowerCase().matches(rawregexname)) {
                        key = line;
                        Log.d("Processing", "Key Hit: " + key.getValue() + " [" + key.getBoundingBox().left + ", " + key.getBoundingBox().top + "]");
                        //sb.append(key.getValue());
                        //sb.append()
                    }
                }
            }
        }
        if(key!=null) {
            boolean complete = false;
            out.key = key;
            out.keyblock = i - 1;
            out.keyline = j - 1;
            out.keyword = -1;
            for (i2 = 0; i2 < items.size() && !complete; i2++) {     //these are blocks
                TextBlock section = items.valueAt(i2);
                if (section != null && section.getValue() != null) {
                    List<? extends Text> lines = section.getComponents();
                    for (j2 = 0; j2 < lines.size() && !complete; j2++) {    //these are lines
                        Text line = lines.get(j2);
                        boolean sameLine = Math.abs(line.getBoundingBox().top - key.getBoundingBox().top) < 100 && line.getBoundingBox().left > key.getBoundingBox().right;
                        if (line.getValue().toLowerCase().matches(rawregexprice) && sameLine) {
                            if (price == null || line.getBoundingBox().left < price.getBoundingBox().left) {
                                price = line;
                                out.priceblock = i2 - 1;
                                out.priceline = j2 - 1;
                                out.priceword = k2 - 1;
                                Log.d("Processing", "Price Hit: " + price.getValue() + " [" + price.getBoundingBox().left + ", " + price.getBoundingBox().top + "]");
                            }
                            //sb.append(key.getValue());
                            //sb.append()
                        }
                    }
                }
            }
        }
        out.price = price;
        return out;
    }


}