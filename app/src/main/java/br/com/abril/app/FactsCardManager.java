package br.com.abril.app;

import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;

/**
 * Created by GB on 1/23/14.
 */
class FactsCardManager {
    private final TimelineManager timelineManager;
    private final FactsService factsService;
    private LiveCard liveCard;

    private static final String LIVE_CARD_ID = "facts";
    private static final String TAG = "FactsService";

    public FactsCardManager(FactsService factsService) {
        timelineManager = TimelineManager.from(factsService);
        this.factsService = factsService;
    }

    public void createMainCard() {
        if (liveCard == null) {
            liveCard = timelineManager.createLiveCard(LIVE_CARD_ID);

            Intent menuIntent = new Intent(factsService, MenuActivity.class);
            liveCard.setAction(PendingIntent.getActivity(factsService, 0, menuIntent, 0));

            liveCard.publish(LiveCard.PublishMode.REVEAL);
            Log.d(TAG, "Done publishing LiveCard");
        } else {
            // TODO(alainv): Jump to the LiveCard when API is available.
        }

    }

    public void destroyMainCard() {
        if (liveCard != null && liveCard.isPublished()) {
            liveCard.unpublish();
            liveCard = null;
        }
    }

    public void setText(String text) {
        RemoteViews views = new RemoteViews(factsService.getPackageName(), R.layout.card_fact);
        views.setCharSequence(R.id.fact, "setText", text);

        liveCard.setViews(views);
    }
}
