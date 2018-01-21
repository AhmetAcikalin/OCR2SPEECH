package com.ahmet.acikalin.OCRtoSpeech;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import java.io.IOException;
import java.util.Locale;

public class realtime2 extends AppCompatActivity {
    private SurfaceView cameraView;
    private TextView result;
    private CameraSource cameraSource;
    TextToSpeech tts;
    int milis=180;
    @TargetApi(23)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime2);
        Button stop=findViewById(R.id.STP);
        getPermissions();

        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    Locale locale = new Locale("eng", "UK");
                    tts.setLanguage(Locale.UK);
                }
            }
        });
        stop.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                       tts.stop();
                    }
                });
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {


            cameraView = (SurfaceView) findViewById(R.id.surface_view);
            result = (TextView) findViewById(R.id.text_value);

            TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {                }
                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    Log.d("Main", "receiveDetections");
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0) {
                        result.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder value = new StringBuilder();
                                for (int i = 0; i < items.size(); ++i) {
                                    TextBlock item = items.valueAt(i);
                                    value.append(item.getValue());
                                    value.append("\n");
                                }

                                    result.setText(value.toString());
                                if(!tts.isSpeaking()) {
                                    String toSpeak = value.toString();
                                    Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
                                    String utteranceId = this.hashCode() + "";
                                    tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                                }


                            }
                        });
                    }

                }
            });

            if (!textRecognizer.isOperational()) {
                Log.w("MainActivity", "Detector dependencies are not yet available.");
            }

            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();

            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @SuppressLint("MissingPermission")
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        //noinspection MissingPermission
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cameraSource.stop();
                }
            });
        }
    }
    @TargetApi(23)
    private void getPermissions() {
        int hasWriteStoragePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 12);
            }
        int hasWriteStoragePermission1 = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission1 != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 13);}
        int hasWriteStoragePermission2 = checkSelfPermission(Manifest.permission.CAMERA);
        if (hasWriteStoragePermission2 != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 14);

        }


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraSource.release();
    }
}
