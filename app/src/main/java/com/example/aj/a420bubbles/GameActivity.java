package com.example.aj.a420bubbles;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends AppCompatActivity {

    private TextView gameScore;

    private Button cameraButton;
    private boolean cameraToggled;
    private CameraPreview cameraPreview;

    private FrameLayout gameLayoutCamera;
    private FrameLayout gameLayoutBubbles;

    private BubblesView bubblesView;

    private SharedPreferences sharedPreferences;

    private final int MY_PERMISSIONS_REQUEST_CAMERA = 420;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        getSupportActionBar().hide();

        cameraButton = (Button)findViewById(R.id.camera_button);
        gameScore = (TextView) findViewById(R.id.game_score);

        if (savedInstanceState != null) {

            gameScore.setText(savedInstanceState.getString("gameScore"));
            cameraToggled = savedInstanceState.getBoolean("cameraToggled");
        } else {

            gameScore.setText("0");
            cameraToggled = false;
        }

        gameLayoutBubbles = (FrameLayout) findViewById(R.id.game_layout_bubbles);
        gameLayoutCamera = (FrameLayout) findViewById(R.id.game_layout_camera);

        bubblesView = new BubblesView(this, gameScore, null);
        gameLayoutBubbles.addView(bubblesView);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA }, MY_PERMISSIONS_REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_CAMERA: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                    cameraButton.setEnabled(false);
                }
                return;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(cameraToggled && cameraPreview == null) {

            if (checkCameraHardware(this)) {

                new AsyncTask<Void, Void, Void>() {

                    Camera camera;

                    @Override
                    protected Void doInBackground( Void... voids ) {

                        camera = Camera.open();
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Void param) {

                        cameraPreview = new CameraPreview(getApplicationContext(), camera);
                        gameLayoutCamera.addView(cameraPreview);
                    }
                }.execute();
            }
        }

        bubblesView.unlock();

        unlockOrientation();
    }

    @Override
    public void onPause() {
        super.onPause();

        lockOrientation();

        bubblesView.lock();

        if(cameraToggled && cameraPreview != null) {

            gameLayoutCamera.removeView(cameraPreview);
            cameraPreview = null;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);

        bubblesView.stop();

        gameLayoutBubbles.removeView(bubblesView);

        bubblesView = null;
        bubblesView = new BubblesView(this, gameScore, null);

        gameLayoutBubbles.addView(bubblesView);

        if(cameraToggled && cameraPreview != null) {

            cameraPreview.setDisplayOrientation();
        }

        System.out.println("onConfigChanged");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString("gameScore", gameScore.getText().toString());
        outState.putBoolean("cameraToggled", cameraToggled);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {

        bubblesView.stop();

        int gameScoreInteger = Integer.parseInt(gameScore.getText().toString());

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        databaseHelper.addScore(new Score(sharedPreferences.getString("playerName", "foo"), gameScoreInteger));

        Toast.makeText(getApplicationContext(), "You scored " + gameScoreInteger, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    public void onClickCameraButton(View v){

        cameraToggled = !cameraToggled;

        if(cameraToggled && cameraPreview == null) {

            if (checkCameraHardware(this)) {

                new AsyncTask<Void, Void, Void>() {

                    Camera camera;

                    @Override
                    protected Void doInBackground( Void... voids ) {

                        camera = Camera.open();
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Void param) {

                        cameraPreview = new CameraPreview(getApplicationContext(), camera);
                        gameLayoutCamera.addView(cameraPreview);
                    }
                }.execute();
            }

        } else {

            gameLayoutCamera.removeView(cameraPreview);
            cameraPreview = null;
        }
    }

    // LOCK AND UNLOCK ORIENTATION: http://stackoverflow.com/questions/2366706/how-to-lock-orientation-during-runtime
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

    // CAMERA RELATED CODE: https://developer.android.com/guide/topics/media/camera.html

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {

        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /** A basic Camera preview class */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

        private Camera camera;

        private SurfaceHolder surfaceHolder;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            this.camera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            surfaceHolder = getHolder();
            surfaceHolder.addCallback(this);

            // deprecated setting, but required on Android versions prior to 3.0
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void setDisplayOrientation(){

            switch(((WindowManager) getSystemService(WINDOW_SERVICE))
                    .getDefaultDisplay()
                    .getRotation())
            {
                case Surface.ROTATION_0:
                    camera.setDisplayOrientation(90);
                    break;
                case Surface.ROTATION_90:
                    camera.setDisplayOrientation(0);
                    break;
                case Surface.ROTATION_180:
                    camera.setDisplayOrientation(270);
                    break;
                case Surface.ROTATION_270:
                    camera.setDisplayOrientation(180);
                    break;
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {

            // The Surface has been created, now tell the camera where to draw the preview.
            final SurfaceHolder finalHolder = holder;

            new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                        try {

                            setDisplayOrientation();
                            camera.setPreviewDisplay(finalHolder);
                            camera.startPreview();
                        } catch (Exception e) {

                            System.out.println(e.getMessage());
                        }
                    }
                }
            ).start();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {

            // Take care of releasing the Camera preview in your activity.
            new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                        camera.setPreviewCallback(null);
                        camera.stopPreview();
                        camera.release();
                        camera = null;
                    }
                }
            ).start();
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

            // empty.
        }
    }
}
