package com.ahmet.acikalin.OCRtoSpeech;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Bitmap image;
    private TessBaseAPI mTess;
   public static String datapath = "";
    String language = "eng";
    private static int RESULT_LOAD_IMAGE = 1;
    private static int CAMERA_REQUEST = 2;
    TextToSpeech tts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getStorageAccessPermissions();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        datapath = getFilesDir()+ "/tesseract/";
        checkFile(new File(datapath + "tessdata/"));
        Button openGallery = findViewById(R.id.OpenGallery);
        Button openCamera = findViewById(R.id.OpenCamera);
        Button realtime = findViewById(R.id.RealTime);
        Button toSpeech=findViewById(R.id.toSpeech);
        Button stop=findViewById(R.id.TTSTOP);
        final TextView result = findViewById(R.id.textView);
        image = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);
        //initialize Tesseract API
        openGallery.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(i, RESULT_LOAD_IMAGE);


                    }
                });
        openCamera.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        startActivity(new Intent(MainActivity.this, RealTime.class));

                    }
                });
        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    Locale locale = new Locale("tr", "TR");
                    tts.setLanguage(locale);
                }
            }
        });

        realtime.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        startActivity(new Intent(MainActivity.this, realtime2.class));

                    }
                });
        toSpeech.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {

                        String toSpeak = result.getText().toString();
                        Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
                        String utteranceId=this.hashCode() + "";
                        tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                    }
                });

        stop.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                      tts.stop();
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView imageView = findViewById(R.id.imageView);
        TextView result = findViewById(R.id.textView);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap bmp = null;
            try {
                bmp = getBitmapFromUri(selectedImage);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            imageView.setImageBitmap(bmp);

            try {
                result.setText(extractText(bmp));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
            try {
                result.setText(extractText(photo));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
    private String extractText(Bitmap bitmap) throws Exception {

        TessBaseAPI tessBaseApi = new TessBaseAPI();
        tessBaseApi.init(datapath, language);
        tessBaseApi.setImage(bitmap);
        String extractedText = tessBaseApi.getUTF8Text();
        tessBaseApi.end();
        return extractedText;

    }

    private void checkFile(File dir) {
        if (!dir.exists()&& dir.mkdirs()){
            copyFiles();
        }
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);

            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    private void copyFiles() {
        try {
            String filepath = datapath + "/tessdata/eng.traineddata";
            AssetManager assetManager = getAssets();

            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }


            outstream.flush();
            outstream.close();
            instream.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @TargetApi(23)
    private void getStorageAccessPermissions() {
        int hasWriteStoragePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 12);

        }
        int hasWriteStoragePermission1 = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission1 != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 13);

        }
        int hasWriteStoragePermission2 = checkSelfPermission(Manifest.permission.CAMERA);
        if (hasWriteStoragePermission2 != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 14);

        }

    }
}
