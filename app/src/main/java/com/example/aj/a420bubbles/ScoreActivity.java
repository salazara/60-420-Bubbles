package com.example.aj.a420bubbles;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ScoreActivity extends AppCompatActivity {

    Button sendScoreButton;
    private boolean sendScoreButtonEnabled;

    Button viewScoresButton;
    private boolean viewScoresButtonEnabled;

    public Context getContext(){

        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        getSupportActionBar().hide();

        DatabaseHelper databaseHelper = new DatabaseHelper(this);

        //databaseHelper.truncateTableScores();

        List<Score> scoreList = databaseHelper.getAllScores();

        List<DataPoint> dataPointList = new ArrayList<DataPoint>();
        dataPointList.add(new DataPoint(0,0));

        Score highScore = new Score("foo", 0);

        for(Score score : scoreList){

            System.out.println(score.getID() + " " + score.getPlayerName() + " " + score.getScore());

            dataPointList.add(new DataPoint(score.getID(), score.getScore()));

            if(highScore.getScore() < score.getScore()){

                highScore = new Score(score.getPlayerName(), score.getScore());
            }
        }

        ((TextView)findViewById(R.id.high_score)).setText("The current high score on this device is " + highScore.getScore() + " by " + highScore.getPlayerName());

        int dataPointMaxY = highScore.getScore();

        LineGraphSeries<DataPoint> lineGraphSeries = new LineGraphSeries<DataPoint>(dataPointList.toArray(new DataPoint[dataPointList.size()]));

        GraphView graphView = (GraphView)findViewById(R.id.score_history);

        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(dataPointList.size() - 1);

        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(0);
        graphView.getViewport().setMaxY(dataPointMaxY);

        graphView.setTitle("History of Scores on this Device");
        graphView.addSeries(lineGraphSeries);

        if (savedInstanceState != null) {

            sendScoreButtonEnabled = savedInstanceState.getBoolean("sendScoreButtonEnabled");
            viewScoresButtonEnabled = savedInstanceState.getBoolean("viewScoresButtonEnabled");
        } else {

            sendScoreButtonEnabled = true;
            viewScoresButtonEnabled = true;
        }

        sendScoreButton = (Button)findViewById(R.id.send_score);
        sendScoreButton.setEnabled(sendScoreButtonEnabled);

        viewScoresButton = (Button)findViewById(R.id.view_scores);
        viewScoresButton.setEnabled(viewScoresButtonEnabled);
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putBoolean("sendScoreButtonEnabled", sendScoreButtonEnabled);
        outState.putBoolean("viewScoresButtonEnabled", viewScoresButtonEnabled);

        super.onSaveInstanceState(outState);
    }

    public void onClickSendScore(View v){

        lockOrientation();

        sendScoreButtonEnabled = false;
        sendScoreButton.setEnabled(sendScoreButtonEnabled);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        List<Score> scoreList = databaseHelper.getAllScores();

        Score maxScore = new Score("foo", 0);

        for(Score score : scoreList){

            if(maxScore.getScore() < score.getScore()) {
                maxScore = new Score(score.getPlayerName(), score.getScore());
            }
        }

        RequestParams requestParams = new RequestParams();
        requestParams.put("playerName", maxScore.getPlayerName());
        requestParams.put("score", maxScore.getScore());

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.post("http://salazara.myweb.cs.uwindsor.ca/420Bubbles/webService.php", requestParams, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] response) {

                    Toast.makeText(getApplicationContext(), "SENDING HIGH SCORE > SUCCESS", Toast.LENGTH_SHORT).show();

                    unlockOrientation();
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {

                    sendScoreButtonEnabled = true;
                    sendScoreButton.setEnabled(sendScoreButtonEnabled);
                    Toast.makeText(getApplicationContext(), "SENDING HIGH SCORE > FAILURE", Toast.LENGTH_SHORT).show();

                    unlockOrientation();
                }
            }
        );
    }

    public void onClickViewScores(View v){

        lockOrientation();

        viewScoresButtonEnabled = false;
        viewScoresButton.setEnabled(viewScoresButtonEnabled);

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.get("http://salazara.myweb.cs.uwindsor.ca/420Bubbles/webService.php", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                System.out.println(response);
                Toast.makeText(getApplicationContext(), "JSON RESPONSE > SUCCESS", Toast.LENGTH_SHORT).show();

                List<String> stringList = new ArrayList<String>();

                for(int i = 0 ; i < response.length() ; i++){

                    try {

                        JSONObject ithJSONObject = response.getJSONObject(i);
                        stringList.add(ithJSONObject.getInt("score") + " by " + ithJSONObject.getString("playerName"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                String[] stringArray = stringList.toArray(new String[stringList.size()]);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setTitle("High Scores from Web Service");
                alertDialogBuilder.setItems(stringArray, null);
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                viewScoresButtonEnabled = true;
                viewScoresButton.setEnabled(viewScoresButtonEnabled);

                unlockOrientation();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse){

                viewScoresButtonEnabled = true;
                viewScoresButton.setEnabled(viewScoresButtonEnabled);
                Toast.makeText(getApplicationContext(), "JSON RESPONSE > FAILURE", Toast.LENGTH_SHORT).show();

                unlockOrientation();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable){

                viewScoresButtonEnabled = true;
                viewScoresButton.setEnabled(viewScoresButtonEnabled);
                Toast.makeText(getApplicationContext(), "JSON RESPONSE > FAILURE", Toast.LENGTH_SHORT).show();

                unlockOrientation();
            }
        });
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
