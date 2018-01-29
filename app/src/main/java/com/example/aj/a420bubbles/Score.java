package com.example.aj.a420bubbles;

// this was created with the help of http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
public class Score {

    private int ID;
    private String playerName;
    private int score;

    public Score(){

    }

    public Score(String playerName, int score){

        this.playerName = playerName;
        this.score = score;
    }

    public void setID(int ID){

        this.ID = ID;
    }

    public void setPlayerName(String playerName){

        this.playerName = playerName;
    }

    public void setScore(int score){

        this.score = score;
    }

    public int getID(){

        return this.ID;
    }

    public String getPlayerName(){

        return this.playerName;
    }

    public int getScore(){

        return this.score;
    }
}
