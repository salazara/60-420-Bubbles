package com.example.aj.a420bubbles;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BubblesView extends View {

    private List<Bubble> bubbles = new ArrayList<Bubble>();

    private int bubbleRadius;
    private Paint bubblePaint;
    public ExecutorService executorService;

    private RepaintTimer repaintTimer;

    private TextView gameScore;

    public BubblesView(Context context, TextView gameScore, AttributeSet attrs) {

        super(context, attrs);

        this.gameScore = gameScore;

        bubbleRadius = getViewWidth() / 10;
        bubblePaint = new Paint();
        bubblePaint.setColor(Color.parseColor("#9fbfdf"));

        int viewWidth = getViewWidth();
        int viewHeight = getViewHeight();

        bubbles.add(new Bubble((int)(0.1 * viewWidth), viewHeight, bubbleRadius, this));
        bubbles.add(new Bubble((int)(0.3 * viewWidth), viewHeight, bubbleRadius, this));
        bubbles.add(new Bubble((int)(0.5 * viewWidth), viewHeight, bubbleRadius, this));
        bubbles.add(new Bubble((int)(0.7 * viewWidth), viewHeight, bubbleRadius, this));
        bubbles.add(new Bubble((int)(0.9 * viewWidth), viewHeight, bubbleRadius, this));

        executorService = Executors.newFixedThreadPool(6);

        for(Bubble b : bubbles){

            executorService.execute(b);
        }

        repaintTimer = new RepaintTimer(this);
        executorService.execute(repaintTimer);

    }

    @Override
    protected void onDraw(Canvas canvas){

        System.out.println("BubblesView:: onDraw");

        for(Bubble b : bubbles){

            canvas.drawCircle(b.getX(), b.getY(), bubbleRadius, bubblePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){

        for(Bubble b : bubbles){

            b.detectPop((int)event.getX(), (int)event.getY());
        }

        return true;
    }

    public TextView getGameScore(){

        return gameScore;
    }

    // stops threads -> bubbles and repaint timer
    public void stop(){

        for(Bubble b : bubbles){

            b.stop();
        }

        repaintTimer.stop();

        executorService.shutdownNow();
    }

    // pauses threads -> bubbles and repaint timer
    public void lock(){

        for(Bubble b : bubbles){

            b.lock();
        }
        repaintTimer.lock();
    }

    // resumes threads (after being paused) -> bubbles and repaint timer
    public void unlock(){

        for(Bubble b : bubbles){

            b.unlock();
        }
        repaintTimer.unlock();
    }

    public int getViewWidth(){

        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public int getViewHeight(){

        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    // runnable class to render moving bubbles on the view
    private class RepaintTimer implements Runnable {

        private final BubblesView bubblesView;
        private boolean isRunning;
        private boolean isLocked;

        public RepaintTimer(BubblesView bubblesView){

            this.bubblesView = bubblesView;
            this.isRunning = true;
            this.isLocked = false;
        }

        @Override
        public void run() {

            while (isRunning){

                while(isLocked){

                }

                try {

                    Thread.sleep(20);

                    bubblesView.postInvalidate();

                    System.out.println("RepaintTimer:: postInvalidate");

                } catch (Exception e) {

                    System.out.println("RepaintTimer:: isRunning == false");
                }
            }
        }

        // stop thread
        public void stop(){

            this.isRunning = false;
        }

        // pause thread
        public void lock(){

            this.isLocked = true;
        }

        // resume thread (after being paused)
        public void unlock(){

            this.isLocked = false;
        }
    }
}