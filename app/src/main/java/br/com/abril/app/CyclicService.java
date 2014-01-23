package br.com.abril.app;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;

import java.util.concurrent.TimeUnit;

/**
 * Created by GB on 1/23/14.
 */
abstract class CyclicService extends Service{

    private final long DELAY_MILLIS = 1000;
    private final Handler mHandler = new Handler();

    private boolean mStarted;
    private long startTime;

    abstract void onUpdate();

    public int onStartCommand(Intent intent, int flags, int startId) {
        startUpdateCycle();

        return START_STICKY;
    }

    private void startUpdateCycle() {
        if (!mStarted) {
            mStarted = true;
            startTime = SystemClock.elapsedRealtime();
            mHandler.postDelayed(mUpdateNewsRunnable, DELAY_MILLIS);
        }
    }

    private final Runnable mUpdateNewsRunnable = new Runnable() {
        @Override
        public void run() {
        	if (mStarted) {
                if (shouldUpdate()) {
                    startTime = SystemClock.elapsedRealtime();

                    onUpdate();
                }
                mHandler.postDelayed(mUpdateNewsRunnable, DELAY_MILLIS);
        	}
        }

    };

    private boolean shouldUpdate() {
        return SystemClock.elapsedRealtime() - startTime > TimeUnit.SECONDS.toMillis(60);
    }
}
