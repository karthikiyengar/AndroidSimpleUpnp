package com.example.libTest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.AndroidSimpleUpnp.AndroidUpnpProvider;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.json.JSONArray;
import org.json.JSONException;

public class MyActivity extends Activity {

    private String details;
    private JSONArray jsonArray;
    TextView textStatus;
    AndroidUpnpProvider upnpProvider;

    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        /**
         * Instantiate a AndroidUpnpProvider object
         */

        upnpProvider = new AndroidUpnpProvider();

        /**
         * Bind to the service and initialize search so that you can enumerate devices asynchronously.
         */

        getApplicationContext().bindService(
                new Intent(this, AndroidUpnpServiceImpl.class),
                upnpProvider.serviceConnection,
                Context.BIND_AUTO_CREATE
        );


        /**
         * Enumerate Button
         */

        Button btnEnum = (Button) findViewById(R.id.btnEnum);
        btnEnum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * Log available devices to console output. Also set it to a Textview on the Screen
                 */
                try {
                    Log.d("Test", upnpProvider.getDevices());
                    jsonArray = new JSONArray(upnpProvider.getDevices());
                    textStatus = (TextView) findViewById(R.id.textViewStatus);
                    textStatus.setText(jsonArray.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

        /**
         * Select a Device Button
         */

        Button btnSelect = (Button) findViewById(R.id.btnSelect);
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * Parse the JSON String devices. Then, fetch the UUID of required device and select it.
                 * Here, it's fetching the UUID of a dummy Plex Media Renderer on my network.
                 */

                try {
                    String UUID = jsonArray.getJSONObject(2).getString("deviceUUID"); //Change index to choose device
                    Log.d("Test", UUID);
                    upnpProvider.selectDevice("7c94075d-88a7-c9ef-fbfd-6eabba1aa291");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("Test", "Improper JSON Index");
                }
            }
        });

        /**
         * Send Stream button. Sends a URL to the renderer and initiates play.
         */

        Button btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upnpProvider.sendStream("http://cloudwalker.bc.cdn.bitgravity.com/Movies/IshqInParis/IshqInParis_200_180p.mp4");
            }
        });

        Button btnPlay = (Button) findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upnpProvider.play();
            }
        });


        Button btnPause = (Button) findViewById(R.id.btnPause);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upnpProvider.pause();
            }
        });

        Button btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upnpProvider.stop();
            }
        });


        Button btnSeek = (Button) findViewById(R.id.btnSeek);
        btnSeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * Get seek time from Textbox and seek.
                 */
                EditText seekValue = (EditText) findViewById(R.id.editText);
                upnpProvider.seek(seekValue.getText().toString());
            }
        });


        /**
         * Get current status details of the player in a JSON String. Set to TextView and Log
         */

        Button btnDetails = (Button) findViewById(R.id.btnDetails);
        btnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                details = upnpProvider.getDetails();
                textStatus = (TextView) findViewById(R.id.textViewStatus);
                textStatus.setText(details);
                Log.d("Test", details);
            }
        });


        /**
         * Cleanup Operations
         */

        Button btnClean = (Button) findViewById(R.id.btnClean);
        btnClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upnpProvider.cleanup();
                getApplicationContext().unbindService(upnpProvider.serviceConnection);
            }
        });
    }
}
