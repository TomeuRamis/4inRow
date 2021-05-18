package com.example.fourinarow;

import android.graphics.Point;
import android.sax.StartElementListener;

public class Animation {

    private Image img;
    private int col;
    private int row;
    private int state = 0;
    private final int START = 5;
    private final int FALL = 8;
    private final int BOUNCE = 3;
    private final int END = 3;
    private int currentFrame = 0;
    private int frames = START + FALL + BOUNCE + END;
    private boolean done = false;

    public Animation(Image m, int c, int r) {
        img = m;
        this.col = c;
        this.row = r;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Image getImage() {
        return img;
    }

    public void newFrame() {
        if (!done) {
            currentFrame++;
            if (state == 3 && currentFrame > END ) {
                done = true;
            } else if (state == 2 && currentFrame > BOUNCE) {
                state = 3;
                currentFrame = 0;
            } else if (state == 1 && currentFrame > FALL) {
                state = 2;
                currentFrame = 0;
            } else if (state == 0 && currentFrame > START) {
                state = 1;
                currentFrame = 0;
            }
        }
    }

    public boolean isDone() {
        return done;
    }

    public double getCompletionRatio() {
        switch (state) {
            case 0:
                return (double) currentFrame / START;
            case 1:
                return (double) currentFrame / FALL;
            case 2:
                return (double) currentFrame / BOUNCE;
            case 3:
                return (double) currentFrame / END;
        }
        return (double) currentFrame / frames;
    }

    public int getState() {
        return state;
    }
}
