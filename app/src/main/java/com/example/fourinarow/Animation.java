package com.example.fourinarow;

import android.graphics.Point;

public class Animation {

    private Man man;
    private int img;
    private int frames;
    private int currentFrame = 0;
    private int col;
    private int row;
    private boolean done = false;

    public Animation(Man m, int c, int r) {
        this.man = m;
        if (man == Man.BLACK) {
            img = R.drawable.red_man;
        } else {
            img = R.drawable.yellow_man;
        }
        this.col = c;
        this.row = r;
        frames = 10;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Man getMan() {
        return man;
    }

    public void newFrame(){
        if(!done){
            currentFrame++;
            if(frames == currentFrame){
                done = true;
            }
        }
    }

    public boolean isDone(){
        return done;
    }

    public double getCompletionRatio() {
        return currentFrame/frames;
    }
}
