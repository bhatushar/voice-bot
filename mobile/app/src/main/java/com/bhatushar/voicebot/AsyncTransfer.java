package com.bhatushar.voicebot;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;

class AsyncTransfer extends AsyncTask<Void, Void, Boolean> {
    private static final String LOG_TAG = "AsyncTransfer";
    private ProgressBarAnimation progressBarAnimation;
    private TransmissionControl transmitter;

    private WeakReference<MainActivity> context;

    AsyncTransfer(MainActivity activity, TransmissionControl tc) {
        context = new WeakReference<>(activity);
        context.get().cancelTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTransfer.super.cancel(true);
            }
        });
        transmitter = tc;
        progressBarAnimation = new ProgressBarAnimation(context.get().progressBar, 0, 100);
    }

    @Override
    protected void onPreExecute() {
        context.get().cancelTransfer.setEnabled(true);
        context.get().cancelTransfer.setBackgroundResource(R.drawable.cancel_transfer_enabled);
        context.get().progressBar.setMax(100);
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
        context.get().progressBar.startAnimation(progressBarAnimation);
    }

    @Override
    protected void onPostExecute(Boolean transfer) {
        context.get().cancelTransfer.setEnabled(false);
        context.get().cancelTransfer.setBackgroundResource(R.drawable.cancel_transfer_disabled);
        context.get().progressBar.setMax(0);
        if (transfer) {
            Log.d(LOG_TAG, "Transferring data.");
            transmitter.transmit(context.get());
        } else {
            Log.d(LOG_TAG, "Transfer cancelled.");
        }
    }

    @Override
    protected void onCancelled() {
        onPostExecute(false);
    }
}
