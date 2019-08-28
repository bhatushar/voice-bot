package com.bhatushar.voicebot;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

import java.lang.ref.WeakReference;

/**
 * The AsyncTransfer class extends AsyncTask in order to provide a waiting time before code
 * is transmitted. Since, AsyncTask is executed on background, this doesn't affect the main UI
 * thread.
 * AsyncTransfer provides a 3 second gap between code generation and transmission. During this
 * period user can cancel the transmission at any moment.
 */
class AsyncTransfer extends AsyncTask<Void, Void, Boolean> {
    private static final String LOG_TAG = "AsyncTransfer";

    // Provides sliding animation to ProgressBar
    private ProgressAnimation animation;
    // Invokes the transmit() method at the end of waiting period
    private TransmissionControl transmitter;

    /**
     * AsyncTask runs on background thread. Passing a View object to such a thread can lead to
     * memory leak. This is because if the main thread is terminated abruptly, JVM might not be
     * able to release the clone from memory.
     * Therefore, a WeakReference is used because it reference to the object rather than a copy
     * of it.
     */
    private WeakReference<MainActivity> context;

    /**
     * Constructor
     *
     * @param activity MainActivity object
     * @param tc Object containing transmission code
     */
    AsyncTransfer(MainActivity activity, TransmissionControl tc) {
        // Members of MainActivity can accessed via get() method
        context = new WeakReference<>(activity);
        transmitter = tc;
        animation = new ProgressAnimation(context.get().progressBar);

        // Adding "on click" listener to cancelTransfer button
        context.get().cancelTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If button is clicked, stop the doInBackground() method
                AsyncTransfer.super.cancel(true);
            }
        });

    }

    // Executed before doInBackground()
    @Override
    protected void onPreExecute() {
        // Activate cancelTransfer button
        context.get().cancelTransfer.setEnabled(true);
        context.get().cancelTransfer.setBackgroundResource(R.drawable.cancel_transfer_enabled);
        // Set maximum possible value of progressBar
        context.get().progressBar.setMax(100);
        context.get().progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            // Progress bar takes 3 seconds to fill
            animation.setDuration(3000);
            publishProgress(); // Calls onProgressUpdate on UI thread
            // There is a 500ms offset between animation and thread time
            // This way the thread wakes up approximately when the animation completes
            Thread.sleep(2500);
            return true;
        } catch (InterruptedException e) {
            // Thread sleep interrupted
            // cancelTransfer button pressed
            Log.d(LOG_TAG, "Wait time interrupted.");
            return false;
        }
    }

    @Override
    protected void onProgressUpdate(Void... voids) {
        // progressBar can only be accessed on UI thread
        context.get().progressBar.startAnimation(animation);
    }

    @Override
    protected void onPostExecute(Boolean transfer) {
        // Disable cancelTransfer button
        context.get().cancelTransfer.setEnabled(false);
        context.get().cancelTransfer.setBackgroundResource(R.drawable.cancel_transfer_disabled);
        // Clear progressBar
        context.get().progressBar.setMax(0);
        context.get().progressBar.setVisibility(View.GONE);

        if (transfer) {
            // cancelTransfer not pressed, transmit the data
            Log.d(LOG_TAG, "Transferring data.");
            transmitter.transmit(context.get());
        } else {
            Log.d(LOG_TAG, "Transfer cancelled.");
        }
    }

    @Override
    protected void onCancelled() {
        // Cancel transfer pressed
        onPostExecute(false);
    }

    /**
     * The ProgressAnimation class is used to animate the progression of the ProgressBar view.
     */
    private class ProgressAnimation extends Animation {
        private ProgressBar bar;

        ProgressAnimation(ProgressBar progressBar) {
            super();
            bar = progressBar;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            /*
             * The value is calculated as InitialValue + (FinalValue - InitialValue) * interpolatedTime
             * The InitialValue is the progress of the bar before the animation started
             * The FinalValue is the progress which is to be reached after animation is completed.
             * Here, the ProgressBar is going from 0 to hundred. So, the number boils down to 100.
             */
            float value = 100 * interpolatedTime;
            bar.setProgress((int) value);
        }
    }
}
