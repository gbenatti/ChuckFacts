package br.com.abril.app;

import com.google.android.glass.media.Sounds;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;

public class FactsService extends CyclicService {

    private FactsManager factsHolder;
    private FactsCardManager cardManager;

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
        cardManager = new FactsCardManager(this);
        factsHolder = new FactsManager(this);
        cardManager.setText(factsHolder.getText());
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        cardManager.createMainCard();

        return START_STICKY;
    }

    @Override
    void onUpdate() {
        changeText();
        playSound();
    }

    @Override
    public void onDestroy() {
        cardManager.destroyMainCard();
        cardManager = null;
        factsHolder = null;

        super.onDestroy();
    }

    private void changeText() {
        factsHolder.selectNext();
        cardManager.setText(factsHolder.getText());
    }

    private void playSound() {
        AudioManager audio = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.TAP);
    }
}
