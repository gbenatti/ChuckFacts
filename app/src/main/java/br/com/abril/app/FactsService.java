package br.com.abril.app;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class FactsService extends Service {

	private TimelineManager mTimelineManager;
	private boolean mStarted;

    private LiveCard liveCard;
    private TextToSpeech speech;

	private static final String TAG = "FactsService";
    private static final String LIVE_CARD_ID = "facts";
	private static final long DELAY_MILLIS = 1000;
    private static final int SOUND_PRIORITY = 1;
    private static final int MAX_STREAMS = 1;

    private SoundPool soundPool;
    private final Random random = new Random();
    private int[] chuckTalkIds;
    private long startTime;
    private boolean useSystemAudio = true;
    private String lastText;

    /**
     * A binder that gives other components access to the speech capabilities provided by the
     * service.
     */
    public class FactsBinder extends Binder {
        /**
         * Read the current heading aloud using the text-to-speech engine.
         */
        public void readFactAloud() {
            speech.speak(lastText, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private final FactsBinder binder = new FactsBinder();

    @Override
	public void onCreate() {
		super.onCreate();
		mTimelineManager = TimelineManager.from(this);
		
		Log.d(TAG, "Creating TimelineManager");

        speech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // Do nothing.
            }
        });
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Start Command");
        createMainCard();
        initSoundSystem();
		startUpdateCycle();

        return START_STICKY;
    }

    private void createMainCard() {
        if (liveCard == null) {
            Log.d(TAG, "Publishing LiveCard");
            liveCard = mTimelineManager.createLiveCard(LIVE_CARD_ID);

            setLivecardText("Chuck Norris facts !!");

            Intent menuIntent = new Intent(this, MenuActivity.class);
            liveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));

            liveCard.publish(LiveCard.PublishMode.REVEAL);
            Log.d(TAG, "Done publishing LiveCard");
        } else {
            // TODO(alainv): Jump to the LiveCard when API is available.
        }
    }

    private void setLivecardText(String text) {
        lastText = text;

        RemoteViews views = new RemoteViews(this.getPackageName(),
                R.layout.card_fact);

        views.setCharSequence(R.id.fact, "setText", text);
        liveCard.setViews(views);
    }

    private void initSoundSystem() {
        chuckTalkIds = new int[4];

        soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);

        chuckTalkIds[0] = soundPool.load(this, R.raw.drink_it, SOUND_PRIORITY);
        chuckTalkIds[1] = soundPool.load(this, R.raw.ha_ha_ha_ha, SOUND_PRIORITY);
        chuckTalkIds[2] = soundPool.load(this, R.raw.oh_really, SOUND_PRIORITY);
        chuckTalkIds[3] = soundPool.load(this, R.raw.youre_in, SOUND_PRIORITY);
    }

    @Override
    public void onDestroy() {
		Log.d(TAG, "Destroy Command");

        destroyMainCard();

        speech = null;
        soundPool = null;

        super.onDestroy();
    }

    private void destroyMainCard() {
        if (liveCard != null && liveCard.isPublished()) {
            Log.d(TAG, "Unpublishing LiveCard");
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

                    setLivecardText(getNextFact());
                    playSound(getNextChuckSoundId());
                }
                mHandler.postDelayed(mUpdateNewsRunnable, DELAY_MILLIS);
        	}
        }

    };

    private String getNextFact() {
        String facts = this.getResources().getString(R.string.facts);
        String[] factsArray = facts.split(";");
        return factsArray[random.nextInt(factsArray.length)];
    }

    private int getNextChuckSoundId() {
        return chuckTalkIds[random.nextInt(4)];
    }

    private void startUpdateCycle() {
        if (!mStarted) {
            mStarted = true;
            startTime = SystemClock.elapsedRealtime();
            mHandler.postDelayed(mUpdateNewsRunnable, DELAY_MILLIS);
        }
    }

    private void playSound(int soundId) {
        if (useSystemAudio) {
            AudioManager audio = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            audio.playSoundEffect(Sounds.TAP);
        } else {
            soundPool.play(soundId,
                    1 /* leftVolume */,
                    1 /* rightVolume */,
                    SOUND_PRIORITY,
                    0 /* loop */,
                    1 /* rate */);
        }
    }
}
