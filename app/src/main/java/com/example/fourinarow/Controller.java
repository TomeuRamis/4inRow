package com.example.fourinarow;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.icu.text.StringSearch;
import android.os.Build;
import android.view.SurfaceHolder;

import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Random;

public class Controller {

    private Board board;
    private Graphics graphics;
    private Boolean devmode;
    private int turn, scorePlayer, scoreIA;
    private Boolean gameOver;

    private IA ia;
    private boolean loading = true;

    private Boolean blackPlays = true;
    private Man playingMan = Man.BLACK;
    private Man manPlayer;
    private Man manIA;

    private int[][] inRow;
    private Man winner;

    private Context context;
    private SharedPreferences sharedPref;

    public Controller(Context context, GameView m, int width, int height, int screenWidth, int screenHeight) {

        this.context = context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        devmode = sharedPref.getBoolean("devmode", false);
        turn = 0;
        scorePlayer = 0;
        scoreIA = 0;
        gameOver = false;
        board = new Board(width, height);

        initGame();
    }

    // initializations
    public void initGame() {

        assignTeams(Integer.parseInt(sharedPref.getString("starting", "0")), Integer.parseInt(sharedPref.getString("consecutive", "1")));
        if (ia != null) {
            ia.gameover();
        }

        //Get the IA's tree depth
        int depth;
        if (sharedPref.getBoolean("experimental", false)) {
            depth = 7;
        } else {
            depth = Integer.parseInt(sharedPref.getString("difficulty", "6"));
        }
        ia = new IA(this.board, this, this.manIA, depth);
        if (!devmode) {
            ia.start();
        } else {
            doneLoad();
        }
    }

    /*
    Check who has to play.
    Ad update the game accordingly.
    */
    public void update(long fps) {
        if (graphics.isLoaded()) {
            if (devmode) {
                turnPlayer();
            } else {
                if (playingMan == manPlayer) {
                    //main.updateTextViewState("Make a move!");
                    turnPlayer();
                } else if (ia.getWaiting()) {
                    //main.updateTextViewState("IA's turn. Wait.");
                    turnIA();
                }
            }
        }
    }

    /*
    Reset all needed variables to start the game anew.
     */
    public void replay() {
        turn = 0;
        board = new Board(this.board.width, this.board.height);
        graphics.setBoard(board);
        playingMan = Man.BLACK;
        blackPlays = true;
        initGame();
        gameOver = false;
    }

    /*Assigns each player a colored Man according to the game settings
     */
    public void assignTeams(int starting, int consecutive) {

        if (winner != null) {
            switch (consecutive) {
                case 0:
                    if (winner == Man.BLACK) {
                        if (manPlayer == Man.BLACK) {
                            manPlayer = Man.BLACK;
                            manIA = Man.WHITE;
                        } else {
                            manPlayer = Man.WHITE;
                            manIA = Man.BLACK;
                        }
                    } else {
                        if (manPlayer == Man.WHITE) {
                            manPlayer = Man.BLACK;
                            manIA = Man.WHITE;
                        } else {
                            manPlayer = Man.WHITE;
                            manIA = Man.BLACK;
                        }
                    }
                    break;
                case 1:
                    if (winner == Man.BLACK) {
                        if (manPlayer == Man.BLACK) {
                            manPlayer = Man.WHITE;
                            manIA = Man.BLACK;
                        } else {
                            manPlayer = Man.BLACK;
                            manIA = Man.WHITE;
                        }
                    } else {
                        if (manPlayer == Man.WHITE) {
                            manPlayer = Man.WHITE;
                            manIA = Man.BLACK;
                        } else {
                            manPlayer = Man.BLACK;
                            manIA = Man.WHITE;
                        }
                    }
                    break;

                case 2:
                    Random ran = new Random(10);
                    if (ran.nextInt(10) % 2 == 0) {
                        manPlayer = Man.BLACK;
                        manIA = Man.WHITE;
                    } else {
                        manPlayer = Man.WHITE;
                        manIA = Man.BLACK;
                    }
                    break;

            }
        } else {
            switch (starting) {
                case 0:
                    manPlayer = Man.BLACK;
                    manIA = Man.WHITE;

                    break;
                case 1:
                    manPlayer = Man.WHITE;
                    manIA = Man.BLACK;
                    break;
                case 2:
                    Random ran = new Random(10);
                    if (ran.nextInt(10) % 2 == 0) {
                        manPlayer = Man.BLACK;
                        manIA = Man.WHITE;
                    } else {
                        manPlayer = Man.WHITE;
                        manIA = Man.BLACK;
                    }
                    break;
            }

        }
    }

    public void turnPlayer() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void turnIA() {
        try {
            ia.setWaiting(false);
        } catch (IllegalThreadStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Increase counter of turns.
    Invert blackplays, indicating that it's the other player's turn.
    And set the values of PlayingMan accordingly. */
    private void newTurn() {
        turn += 1;
        blackPlays = !blackPlays;

        if (blackPlays) {
            playingMan = Man.BLACK;
        } else {
            playingMan = Man.WHITE;
        }
    }

    /* Plays a man on a column.
    Throws ColumnFullException, so it has to be handled individually.
    Ends the game when GameOverException.*/
    public void playMan(int col) throws ColumnFullException {
        try {
            board.playMan(col, playingMan);
            graphics.addAnimQ(playingMan, col, board.getTopPos(col));
            newTurn();
        } catch (GameOverException e) {
            gameOver = true;
            ia.gameover();
            this.winner = e.winner();
            this.inRow = e.getInRow();
            graphics.addAnimQ(playingMan, col, board.getTopPos(col));
            //Add the score
            if (winner == manPlayer) {
                scorePlayer += 1;
            } else if (winner == manIA) {
                scoreIA += 1;
            }
            //If the winner is EMPTY it means it's a tie
            graphics.setScoreAnimation();
        }


    }

    public void playerTryPlayMan(int col) throws ColumnFullException {
        if (devmode || playingMan == manPlayer) {
            playMan(col);
        }
    }

    public void IATryPlayMan(int col) throws ColumnFullException {
        if (!devmode && playingMan == manIA) {
            playMan(col);
            ia.setWaiting(true);
        }
    }

    //SETERS AND GETTERS

    public void setResume() {
    }

    public void saveStatus() {
    }

    public void recoverStatus() {
    }

    public int getWidth() {
        return this.board.width;
    }

    public int getHeight() {
        return this.board.height;
    }

    public Man getSquare(int col, int row) {
        return board.getSquare(row, col);
    }

    public void doneLoad() {
        loading = false;
    }

    public int getTurn() {
        return turn;
    }

    public int getScorePlayer() {
        return scorePlayer;
    }

    public int getScoreIA() {
        return scoreIA;
    }

    public Boolean getGameOver() {
        return gameOver;
    }

    public Boolean getBlackPlays() {
        return blackPlays;
    }

    public Man getPlayingMan() {
        return playingMan;
    }

    public Man getManPlayer() {
        return manPlayer;
    }

    public Man getManIA() {
        return manIA;
    }

    public int[][] getInRow() {
        return inRow;
    }

    public Man getWinner() {
        return winner;
    }

    public Board getBoard() {
        return this.board;
    }

    public Boolean getDevMode() {
        return devmode;
    }

    public Boolean getLoading() {
        return loading;
    }

    public void setGraphics(Graphics graph) {
        this.graphics = graph;
    }

    /*Stops the IA and resets it*/
    public void stopPlay() {
        this.gameOver = true;
        if (ia != null) {
            ia.gameover();
            ia = null;
        }
    }
}
