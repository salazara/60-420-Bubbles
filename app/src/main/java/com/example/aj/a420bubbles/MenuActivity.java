package com.example.aj.a420bubbles;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

public class MenuActivity extends AppCompatActivity {

    TextView playerName;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        getSupportActionBar().hide();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        playerName = (TextView) findViewById(R.id.player_name);
        playerName.setText("Hello " + sharedPreferences.getString("playerName", "foo"));
    }

    public void onClickPlayerName(View v){

        lockOrientation();

        AlertDialog.Builder alertDialogBuilder;

        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Please enter your name");

        // Set up the input
        final EditText editText = new EditText(this);

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        alertDialogBuilder.setView(editText);

        // Set up the buttons
        alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                sharedPreferencesEditor.putString("playerName", editText.getText().toString());
                sharedPreferencesEditor.commit();
                playerName.setText("Hello " + sharedPreferences.getString("playerName", "foo"));
                unlockOrientation();
            }
        });
        alertDialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                unlockOrientation();
            }
        });

        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.show();
    }

    public void onClickPlayButton(View v){

        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
        finish();
    }

    public void onClickScoresButton(View v){

        Intent intent = new Intent(this, ScoreActivity.class);
        startActivity(intent);
        finish();
    }

    private void lockOrientation(){

        int orientation = getRequestedOrientation();
        int rotation = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
            case Surface.ROTATION_90:
                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
            case Surface.ROTATION_180:
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                break;
            case Surface.ROTATION_270:
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                break;
        }

        setRequestedOrientation(orientation);
    }

    private void unlockOrientation(){

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}
