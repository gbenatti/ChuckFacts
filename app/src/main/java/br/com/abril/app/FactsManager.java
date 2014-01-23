package br.com.abril.app;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Random;

/**
* Created by GB on 1/22/14.
*/
class FactsManager {

    private final Random random = new Random();
    private final String[] factsArray;
    private int newFactIndex = -1;

    private TextToSpeech speech;

    public FactsManager(Context context) {
        factsArray = context.getResources().getStringArray(R.array.facts);

        speech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) { }
        });
    }

    public void selectNext() {
        newFactIndex = random.nextInt(factsArray.length);
    }

    public String getText() {
        if (newFactIndex != -1) {
            return factsArray[newFactIndex];
        } else {
            return "\"Chuck Norris facts !!\"";
        }
    }

    public void sayText() {
        String text = getText();
        speech.speak(text, TextToSpeech.QUEUE_FLUSH, null);

    }
}
