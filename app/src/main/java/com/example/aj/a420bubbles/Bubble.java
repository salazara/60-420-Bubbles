package com.example.aj.a420bubbles;

public class Bubble implements Runnable {

    private int x;
    private int y;
    private int radius;
    private int speed;
    private BubblesView bubblesView;
    private boolean isPopped;
    private boolean isRunning;
    private boolean isLocked;

    public Bubble(int x, int y, int radius, BubblesView bubblesView) {

        this.x = x;
        this.y = y;
        this.radius = radius;
        this.speed = 11;
        this.bubblesView = bubblesView;
        this.isRunning = true;
        this.isLocked = false;
        this.isPopped = false;
    }

    @Override
    public void run(){

        while(isRunning){

            while(isLocked){

            }

            try{

                Thread.sleep(20);

                y -= speed;

                if(y < 0 || isPopped){

                    isPopped = false;
                    y = bubblesView.getViewHeight();
                }
            } catch ( Exception e ) {

                System.out.println("Bubble:: isRunning == false");
            }
        }
    }

    // stops thread
    public void stop(){

        this.isRunning = false;
    }

    // pauses thread
    public void lock(){

        this.isLocked = true;
    }

    // resumes thread (after being paused)
    public void unlock(){

        this.isLocked = false;
    }


    public void detectPop(int x, int y){

        if(Math.abs(x - this.x) <= radius && Math.abs(y - this.y) <= radius){

            isPopped = true;
            updateGameScore();
        }
    }

    public void updateGameScore() {

        int oldScore = Integer.parseInt(bubblesView.getGameScore().getText().toString());
        int newScore = oldScore + 1;

        bubblesView.getGameScore().setText(Integer.toString(newScore));
    }

    public int getX() {

        return x;
    }

    public int getY() {

        return y;
    }
}