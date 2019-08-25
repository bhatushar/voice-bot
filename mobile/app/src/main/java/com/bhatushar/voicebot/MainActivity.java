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

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    private SpeechConversionControl scControl;
    private TransmissionControl tControl;

    private EditText displayText;
    private ImageButton recordButton;
    Button cancelTransfer;
    ProgressBar progressBar;

    Vibrator vibrator;

    // Recording permissions
    final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayText = findViewById(R.id.displayText);
        recordButton = findViewById(R.id.recordButton);
        cancelTransfer = findViewById(R.id.cancelTransfer);
        progressBar = findViewById(R.id.progressBar);

        // Generating transmission data dictionaries
        tControl = new TransmissionControl();
        TransmissionControl.initDataDictionary();

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

        // Requesting permission for audio recording
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        // Check if speech recognition is available on the device
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.e(LOG_TAG, "No voice recognition support on your device!");
            // Disable recording button
            recordButton.setEnabled(false);
        } else {
            Log.d(LOG_TAG, "Speech to text supported on device.");
            scControl = new SpeechConversionControl(this);
        }
    }

    // Sets recording permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean recordingPermission = false;
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION)
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
        if (tControl.parseCommand(command)) {
            displayText.setTextColor(ContextCompat.getColor(this, R.color.defaultText));
            // Create async task
            AsyncTransfer task = new AsyncTransfer();
            task.execute();
        } else {
            displayText.setTextColor(ContextCompat.getColor(this, R.color.errorText));
        }
    }

    // Vibrates phone for 50 ms
    private void vibrate() {
    }

    class AsyncTransfer extends AsyncTask<Void, Void, Boolean> {
        private static final String LOG_TAG = "AsyncTransfer";
        private ProgressBarAnimation progressBarAnimation;

        AsyncTransfer() {
            cancelTransfer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AsyncTransfer.super.cancel(true);
                }
            });
            progressBarAnimation = new ProgressBarAnimation(progressBar, 0, 100);
        }

        @Override
        protected void onPreExecute() {
            cancelTransfer.setEnabled(true);
            cancelTransfer.setBackgroundResource(R.drawable.cancel_transfer_enabled);
            progressBar.setMax(100);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                progressBarAnimation.setDuration(3000);
                publishProgress();
                Thread.sleep(2500);
                return true;
            } catch (InterruptedException e) {
                Log.d(LOG_TAG, "Wait time interrupted.");
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Void... voids) {
            progressBar.startAnimation(progressBarAnimation);
        }

        @Override
        protected void onPostExecute(Boolean transfer) {
            cancelTransfer.setEnabled(false);
            cancelTransfer.setBackgroundResource(R.drawable.cancel_transfer_disabled);
            progressBar.setMax(0);
            if (transfer) {
                Log.d(LOG_TAG, "Transferring data.");
                tControl.transmit();
            } else {
                Log.d(LOG_TAG, "Transfer cancelled.");
            }
        }

        @Override
        protected void onCancelled() {
            onPostExecute(false);
        }
    }
}
