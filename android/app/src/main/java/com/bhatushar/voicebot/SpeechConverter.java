package com.bhatushar.voicebot;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Locale;

class SpeechConverter {
    private final static String LOG_TAG = "SpeechConversion";
    // Shows whether the SpeechRecognizer is enables
    private boolean active;

    // Performs the actual speech-to-text conversion
    private SpeechRecognizer recognizer;
    // Provides extra parameters to recognizer when it starts listening
    private Intent intent;
    // Required for initializing recognizer and for invoking processResult()
    private MainActivity context;

    /**
     * Constructor
     * Initializes recognizer and intent objects.
     * Defaults the active flag to false.
     *
     * @param context Reference to MainActivity
     */
    SpeechConverter(MainActivity context) {
        this.context = context;
        active = false;

        // Initialize recognizer
        recognizer = SpeechRecognizer.createSpeechRecognizer(this.context);
        recognizer.setRecognitionListener(new Listener());
        // Initialize intent
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        Log.d(LOG_TAG, "Recogniser objects initialized.");
    }

    /**
     * Requests recording permissions.
     * Also checks if the SpeechRecognizer functionality is supported on the device.
     *
     * @return Speech-to-text conversion status
     */
    boolean isActive() {
        // Request permission
        int REQUEST_RECORD_AUDIO_PERMISSION = 200;
        String[] permissions = {Manifest.permission.RECORD_AUDIO};
        ActivityCompat.requestPermissions(context, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        // Check for recognition service
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.d(LOG_TAG, "Speech to text supported.");
            active = true;
        } else {
            Log.e(LOG_TAG, "Speech to text not supported.");
            active = false;
        }
        return active;
    }

    /**
     * Starts listening to audio speech is the active flag is set.
     */
    void start() {
        if (!active) return;
        Log.d(LOG_TAG, "SpeechConverter.start() invoked.");
        recognizer.startListening(intent);
    }

    /**
     * Stops listening and generates result.
     */
    void stop() {
        Log.d(LOG_TAG, "SpeechConverter.stop() invoked.");
        recognizer.stopListening();
    }

    /**
     * Class implements RecognitionListener which provides the onResult() method.
     * An instance of Listener class is used to initialize the SpeechRecognizer object.
     */
    private class Listener implements RecognitionListener {
        /**
         * Gets the result of the speech recognition and sends the first match to processResult()
         *
         * @param bundle recognizer's result container
         */
        @Override
        public void onResults(Bundle bundle) {
            Log.d(LOG_TAG, "Getting results from speech.");
            ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null)
                context.processResult(matches.get(0));
        }

        // Other method implementations are not needed.
        @Override
        public void onReadyForSpeech(Bundle bundle) {}
        @Override
        public void onBeginningOfSpeech() {}
        @Override
        public void onRmsChanged(float v) {}
        @Override
        public void onBufferReceived(byte[] bytes) {}
        @Override
        public void onEndOfSpeech() {}
        @Override
        public void onError(int i) {}
        @Override
        public void onPartialResults(Bundle bundle) {}
        @Override
        public void onEvent(int i, Bundle bundle) {}
    }
}
