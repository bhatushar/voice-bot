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

class SpeechConversionControl {
    private final static String LOG_TAG = "SpeechConversion";
    private boolean active;

    private SpeechRecognizer recognizer;
    private Intent intent;
    private MainActivity activity;

    SpeechConversionControl(MainActivity context) {
        activity = context;
        active = false;
        recognizer = SpeechRecognizer.createSpeechRecognizer(this.activity);
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        recognizer.setRecognitionListener(new Listener());
        Log.d(LOG_TAG, "Recogniser objects initialized.");
    }

    boolean isActive() {
        int REQUEST_RECORD_AUDIO_PERMISSION = 200;
        String[] permissions = {Manifest.permission.RECORD_AUDIO};
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        if (SpeechRecognizer.isRecognitionAvailable(activity)) {
            Log.d(LOG_TAG, "Speech to text supported.");
            active = true;
        } else {
            Log.e(LOG_TAG, "Speech to text not supported.");
            active = false;
        }
        return active;
    }

    void start() {
        Log.d(LOG_TAG, "SpeechConversionControl.start() invoked.");
         recognizer.startListening(intent);
    }

    void stop() {
        Log.d(LOG_TAG, "SpeechConversionControl.stop() invoked.");
        recognizer.stopListening();
    }

    private class Listener implements RecognitionListener {
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
        public void onResults(Bundle bundle) {
            Log.d(LOG_TAG, "Getting results from speech.");
            ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null)
                activity.processResult(matches.get(0));
        }

        @Override
        public void onPartialResults(Bundle bundle) {}

        @Override
        public void onEvent(int i, Bundle bundle) {}
    }
}
