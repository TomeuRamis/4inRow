package com.example.fourinarow;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.RequiresApi;

public class GameView extends SurfaceView implements Runnable {

    private volatile boolean running;
    private Thread gameThread = null;

    // Game
    private Controller control;

    // For drawing
    private SurfaceHolder ourHolder;

    // Control the fps
    long fps = 30;
    long realFPS = 0;

    int fade = 0;

    GameView(Context context, int screenWidth, int screenHeight) {
        super(context);

        // Initialize our drawing objects
        ourHolder = getHolder();
        control = new Controller(context, this, 7, 6, screenWidth, screenHeight);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void run() {
        long msFrame = 1000 / fps;
        long timeThisFrame = 0;

        while (running) {
            long startFrameTime = System.currentTimeMillis();

            if (!control.gameOver) {
                control.update(fps);
            }
            if (!control.loading && fade < 255) {
                fade += 5;
            }
            if(fade < 255){
                control.loadinganim(ourHolder, 255-fade);
            }else if(fade == 255){
                control.resetAniamtionQ();
                control.draw(ourHolder);
                fade += 5;
            }else{
                control.draw(ourHolder);
            }



            // Calculate the fps this frame
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame < msFrame) // si sobra velocitat, espera un poc
            {
                try {
                    Thread.sleep(msFrame - timeThisFrame);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame != 0)
                    realFPS = 1000 / timeThisFrame;
            }
        }
    }

    public long getFPS() {
        return realFPS;
    }

    // Clean up our thread if the game is stopped
    public void pause() {
        running = false;
        control.saveStatus();
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            // Error
        }
    }

    // Make a new thread and start it
    // Execution moves to our run method
    public void resume() {
        control.recoverStatus();
        control.setResume();
        gameThread = new Thread(this);
        gameThread.start();
        running = true;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {

        handleInput(motionEvent);
        return true;
    }

    public void handleInput(MotionEvent motionEvent) {
        int pointerCount = motionEvent.getPointerCount();

        for (int i = 0; i < pointerCount; i++) {
            int x = (int) motionEvent.getX(i);
            int y = (int) motionEvent.getY(i);
            int id = motionEvent.getPointerId(i);
            int action = motionEvent.getActionMasked();
            int actionIndex = motionEvent.getActionIndex();
            String actionString = new String();


            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    control.handleInput(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    control.handleStopInput(x, y);
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    control.handleInput(x, y);
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    control.handleStopInput(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    control.handleInput(x, y);
                    break;
            }
        }
        //game.resetPoints();
    }

    public void stop(){
        control.ia.gameover = true;
        control = null;
    }
}
