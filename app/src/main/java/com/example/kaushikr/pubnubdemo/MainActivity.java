package com.example.kaushikr.pubnubdemo;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerGrantResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.Arrays;
import java.util.Properties;

public class MainActivity extends Activity {

    public String PUBLISH_KEY = "pub-c-7b69bee2-a32a-41af-9685-6d1962811701";

    public String SUBSCRIBE_KEY = "sub-c-160c129e-18e4-11e8-b857-da98488f5703";

    PNConfiguration pnConfiguration = new PNConfiguration();

    PubNub pubNub;

    MediaPlayer mp;

    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Creates Unique User ID and setup publishKey and subscribeKey
        Properties prop = new Properties();
        pnConfiguration.setPublishKey(PUBLISH_KEY);
        pnConfiguration.setSubscribeKey(SUBSCRIBE_KEY);
        String uuid = prop.getProperty("UUID");
        if (uuid == null || uuid.equals("")) {
            uuid = java.util.UUID.randomUUID().toString();
            prop.setProperty("UUID", uuid);
        }
        pnConfiguration.setUuid(uuid);
        pnConfiguration.setSecure(true);
        pubNub = new PubNub(pnConfiguration);

        // Sets up authentication key and establishes read write permissions.
        pubNub.grant()
                .channels(Arrays.asList("messages")) //channels to allow grant on
                .authKeys(Arrays.asList("sec-c-YTExZGIyNzMtNGViOS00MDA1LTkyZTctMDMxOGQ3YzBjOGMy")) // the keys we are provisioning
                .write(true) // allow those keys to write (false by default)
                .manage(false) // allow those keys to manage channel groups (false by default)
                .read(true) // allow keys to read the subscribe feed (false by default)
                .ttl(0) // how long those keys will remain valid (0 for eternity)
                .async(new PNCallback<PNAccessManagerGrantResult>() {
                    @Override
                    public void onResponse(PNAccessManagerGrantResult result, PNStatus status) {
                    }
                });

        toast = Toast.makeText(MainActivity.this, "Playing Gods Plan", Toast.LENGTH_LONG);
        setupMessageListener();
    }

    /**
     * Sets up listener that will have callback method for whenever there is a new message.
     */
    public void setupMessageListener() {
        pubNub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {

            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                if (!message.getMessage().getAsString().equals(pnConfiguration.getUuid())) {
                    mp = MediaPlayer.create(MainActivity.this, R.raw.godsplan);
                    mp.start();
                    toast.show();
                }
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {

            }
        });
        pubNub.subscribe()
                .channels(Arrays.asList("messages")) // subscribe to channels
                .execute();
    }

    /**
     * On Click method thats activated when user clicks Play Song button. Sends message to Pubnub servers that will play song on everyone subscribed in channel.
     *
     * @param v view object of Button activated
     */
    public void triggerSong(View v) {
        pubNub.publish()
                .message(pnConfiguration.getUuid())
                .channel("messages")
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        // handle publish result, status always present, result if successful
                        // status.isError() to see if error happened
                        if (!status.isError()) {
                            System.out.println("pub timetoken: " + result.getTimetoken());
                        }
                        System.out.println("pub status code: " + status.getStatusCode());
                    }
                });
    }

    /**
     * Stops the music being played when user exits app.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlaying();
    }

    /**
     * Stops playing music.
     */
    private void stopPlaying() {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

}
