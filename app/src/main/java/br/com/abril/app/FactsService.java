package br.com.abril.app;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.concurrent.TimeUnit;

public class FactsService extends Service {

	private TimelineManager mTimelineManager;
	private boolean mStarted;
    private long startTime;

    private FactsManager factsHolder;
    private LiveCard liveCard;

	private static final String TAG = "FactsService";
    private static final String LIVE_CARD_ID = "facts";
	private static final long DELAY_MILLIS = 1000;

    /**
     * A binder that gives other components access to the speech capabilities provided by the
     * service.
     */
    public class FactsBinder extends Binder {
        /**
         * Read the current heading aloud using the text-to-speech engine.
         */
        public void readFactAloud() {
            factsHolder.sayText();
        }
    }

    private final FactsBinder binder = new FactsBinder();

    @Override
	public void onCreate() {
		super.onCreate();
		mTimelineManager = TimelineManager.from(this);
        factsHolder = new FactsManager(this);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createMainCard();
		startUpdateCycle();

        return START_STICKY;
    }

    private void createMainCard() {
        if (liveCard == null) {
            liveCard = mTimelineManager.createLiveCard(LIVE_CARD_ID);

            changeText();

            Intent menuIntent = new Intent(this, MenuActivity.class);
            liveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));

            liveCard.publish(LiveCard.PublishMode.REVEAL);
            Log.d(TAG, "Done publishing LiveCard");
        } else {
            // TODO(alainv): Jump to the LiveCard when API is available.
        }
    }

    private void setLivecardText(String text) {
        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.card_fact);
        views.setCharSequence(R.id.fact, "setText", text);

        liveCard.setViews(views);
    }

    @Override
    public void onDestroy() {
        destroyMainCard();
        factsHolder = null;

        super.onDestroy();
    }

    private void destroyMainCard() {
        if (liveCard != null && liveCard.isPublished()) {
            liveCard.unpublish();
            liveCard = null;
        }
    }

    private final Handler mHandler = new Handler();

    private final Runnable mUpdateNewsRunnable = new Runnable() {
        @Override
        public void run() {
        	if (mStarted) {
                long realtime = SystemClock.elapsedRealtime();
                long targetDelta = TimeUnit.SECONDS.toMillis(60);
                long delta = realtime - startTime;
                if (delta > targetDelta) {
                    Log.d(TAG, "Run...");
                    startTime = SystemClock.elapsedRealtime();

                    changeText();
                    playSound();
                }
                mHandler.postDelayed(mUpdateNewsRunnable, DELAY_MILLIS);
        	}
        }

    };

    private void startUpdateCycle() {
        if (!mStarted) {
            mStarted = true;
            startTime = SystemClock.elapsedRealtime();
            mHandler.postDelayed(mUpdateNewsRunnable, DELAY_MILLIS);
        }
    }

    private void changeText() {
        factsHolder.selectNext();
        setLivecardText(factsHolder.getText());
    }

    private void playSound() {
        AudioManager audio = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.TAP);
    }
}
