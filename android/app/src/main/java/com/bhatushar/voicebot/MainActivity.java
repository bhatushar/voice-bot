package com.bhatushar.voicebot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;

import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";

    // View elements
    private EditText displayText;
    private ImageButton recordButton;
    Button cancelTransfer;
    ProgressBar progressBar;

    // Used to send vibration shot one interaction with recordButton
    private Vibrator vibrator;
    // Performs speech-to-text conversion
    private SpeechConverter speechConverter;

    @SuppressLint("ClickableViewAccessibility") // Otherwise onTouchListener asks for performClick implementation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get all view elements
        displayText = findViewById(R.id.displayText);
        recordButton = findViewById(R.id.recordButton);
        cancelTransfer = findViewById(R.id.cancelTransfer);
        progressBar = findViewById(R.id.progressBar);

        // Set "on hold" listener to recordButton
        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // Button is pressed down, start recording
                    onStartRecord(view);
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // Button is released, stop recording
                    onStopRecord(view);
                    return true;
                } else return false; // Unhandled action
            }});

        // Setting up speech recorder
        speechConverter = new SpeechConverter(this);
        // Check if recording permission is set
        if(!speechConverter.isActive())
            recordButton.setEnabled(false);

        // Generating data tables required for converting command to transmission code
        TransmissionControl.initDataTables();
        // Initializing vibrator (duh)
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    }

    /**
     * Invoked when the recordButton is pressed down.
     * Starts SpeechRecognizer and continues listening as long as button is pressed down.
     *
     * @param view OnTouch View
     */
    public void onStartRecord(View view) {
        // SpeechRecognizer starts listening
        speechConverter.start();
        // 50 ms vibration on button press
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        // Change button image to active state
        recordButton.setBackgroundResource(R.mipmap.ic_record_on_foreground);
        // Clear previous result
        displayText.setText("");
        displayText.setHint(getString(R.string.on_record));
    }

    /**
     * Invoked when the recordButton is released.
     * Tells SpeechRecognizer to stop listening and get text result.
     *
      * @param view OnTouch View
     */
    public void onStopRecord(View view) {
        // Stop listening and get result
        speechConverter.stop();
        // 50 ms vibration on button release
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        // Change button image to inactive state
        recordButton.setBackgroundResource(R.mipmap.ic_record_start_foreground);
        displayText.setHint(getString(R.string.process_speech));
    }

    /**
     * Called by SpeechConverter once the speech is processed.
     * Method checks if the result is valid. Once the result is verified, it is transmitted
     * to the bot.
     *
     * @param command Result obtained from audio speech
     */
    public void processResult(String command) {
        // Displays the result obtained from speech conversion
        displayText.setText(command);
        TransmissionControl transmitter = new TransmissionControl();
        // Check is the command is valid and, if so, generate transmission code
        if (transmitter.parseCommand(command)) {
            displayText.setTextColor(ContextCompat.getColor(this, R.color.defaultText));
            // Wait for cancellation
            // Transfer if no cancellation received
            AsyncTransfer task = new AsyncTransfer(this, transmitter);
            task.execute();
        } else {
            // Parsing failed
            displayText.setTextColor(ContextCompat.getColor(this, R.color.errorText));
        }
    }

    // Sets recording permissions, invoked by SpeechConverter
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean recordingPermission = false;
        if (requestCode == 200)
            recordingPermission  = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
        if (!recordingPermission ) finish();
    }
}
