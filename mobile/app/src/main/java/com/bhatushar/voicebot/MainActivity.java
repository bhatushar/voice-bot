package com.bhatushar.voicebot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    private SpeechConversionControl scControl;

    private EditText displayText;
    private ImageButton recordButton;
    Button cancelTransfer;
    ProgressBar progressBar;

    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayText = findViewById(R.id.displayText);
        recordButton = findViewById(R.id.recordButton);
        cancelTransfer = findViewById(R.id.cancelTransfer);
        progressBar = findViewById(R.id.progressBar);

        // Generating transmission data dictionaries
        TransmissionControl.initDataDictionary();

        // Setting up speech recorder
        scControl = new SpeechConversionControl(this);
        if(!scControl.isActive())
            recordButton.setEnabled(false);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        findViewById(R.id.recordButton).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    onStartRecord(view);
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    onStopRecord(view);
                    return true;
                } else return false;
            }
        });
    }

    // Sets recording permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean recordingPermission = false;
        if (requestCode == 200)
            recordingPermission  = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
        if (!recordingPermission ) finish();
    }

    // Invoked when the startRecord button is clicked
    public void onStartRecord(View view) {
        scControl.start();
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        recordButton.setBackgroundResource(R.mipmap.ic_record_on_foreground);
        displayText.setText("");
        displayText.setHint(getString(R.string.on_record));
    }

    // Invoked when the stopRecord button is clicked
    public void onStopRecord(View view) {
        scControl.stop();
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        recordButton.setBackgroundResource(R.mipmap.ic_record_start_foreground);
        displayText.setHint(getString(R.string.process_speech));
    }

    // Called by SpeechConversionControl once the audio is processed
    public void processResult(String command) {
        displayText.setText(command);
        TransmissionControl transmitter = new TransmissionControl();
        if (transmitter.parseCommand(command)) {
            displayText.setTextColor(ContextCompat.getColor(this, R.color.defaultText));
            // Create async task
            AsyncTransfer task = new AsyncTransfer(this, transmitter);
            task.execute();
        } else {
            displayText.setTextColor(ContextCompat.getColor(this, R.color.errorText));
        }
    }

}
