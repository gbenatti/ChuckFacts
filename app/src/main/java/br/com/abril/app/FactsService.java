package br.com.abril.app;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class FactsService extends CyclicService {

	private TimelineManager mTimelineManager;

    private FactsManager factsHolder;
    private LiveCard liveCard;

	private static final String TAG = "FactsService";
    private static final String LIVE_CARD_ID = "facts";

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
	public IBinder onBind(Intent intent) {
		return binder;
	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        createMainCard();

        return START_STICKY;
    }

    @Override
    void onUpdate() {
        changeText();
        playSound();
    }

    @Override
    public void onDestroy() {
        destroyMainCard();
        factsHolder = null;

        super.onDestroy();
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

    private void destroyMainCard() {
        if (liveCard != null && liveCard.isPublished()) {
            liveCard.unpublish();
            liveCard = null;
        }
    }

    private void setLivecardText(String text) {
        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.card_fact);
        views.setCharSequence(R.id.fact, "setText", text);

        liveCard.setViews(views);
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
