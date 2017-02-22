package io.github.fahadkh.gamblefitness;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;


public class ListenerService extends WearableListenerService {

   // private static final String TAG = "Wear Listener";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        new Replicator().execute(messageEvent.getPath());
    }

    private class Replicator extends AsyncTask<String, Void, Void> {

        private static final String TAG = "Listener";

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Void doInBackground(String... messages) {

            String message = messages[0];
            String[] data = message.split(",");
            String text;

            try {

                text = URLEncoder.encode("mac", "UTF-8")
                        + "=" + URLEncoder.encode("test id", "UTF-8");
                text += "&" + URLEncoder.encode("time", "UTF-8") + "="
                        + URLEncoder.encode(data[0], "UTF-8");
                text += "&" + URLEncoder.encode("energy", "UTF-8")
                        + "=" + URLEncoder.encode(data[1], "UTF-8");

                Log.v(TAG, text);

            } catch (UnsupportedEncodingException e) {
                Log.v(TAG, "bad encoding " + e.getMessage());
                return null;
            }

            //Send POST request
            String request = "http://murphy.wot.eecs.northwestern.edu/~frk757/SQLGateway.py";
            try {
                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(false);

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Length", Integer.toString(text.length()));
                conn.setUseCaches(false);

                try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream())) {
                    wr.write(text);
                    wr.flush();
                    wr.close();
                }

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                conn.getInputStream()));
                String decodedString;
                while ((decodedString = in.readLine()) != null) {
                    Log.w(TAG, decodedString);
                }

            } catch (MalformedURLException e) {
                Log.v(TAG, e.getMessage());
                return null;
            } catch (IOException e) {
                Log.v(TAG, e.getMessage());
                return null;
            }

            return null;
        }
    }
}
