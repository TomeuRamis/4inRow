package com.example.fourinarow;

import android.widget.TextView;

import java.util.Random;

public class Controller extends Thread {

    Board board;
    MainActivity main;

    Boolean devmode = false;
    int turn = 0;
    Boolean gameover = false;

    Boolean blackPlays = true;
    Man playingMan = Man.BLACK;
    Man player = null;
    Man ia = null;

    public Controller(MainActivity main, int width, int height) {
        board = new Board(width, height);
        this.main = main;
    }

    @Override
    public void run() {
        System.out.println("Game start!");
        startGame();
        System.out.println("Game end");
    }

    public void startGame() {
        main.updateTextViewBoard(board.toString());
        assignTeams();
        gameLoop();

        main.updateTextViewCol("En of game");
        main.updateTextViewBoard(board.toString());
    }

    public void toggleDevMode(){
        if(!gameover){
            devmode = !devmode;
        }
    }

    public void assignTeams() {
        /* //Not for now
        Random ran = new Random(10);
        if(ran.nextInt(10)%2 == 0){
            player = Man.BLACK;
            ia = Man.WHITE;
        } else{
            player = Man.WHITE;
            ia = Man.BLACK;
        }
        */
        player = Man.BLACK;
        ia = Man.WHITE;
    }

    public void gameLoop() {
        while (!gameover) {
            if (devmode) {
                turnPlayer();
            } else {
                if (playingMan == player) {
                    turnPlayer();
                } else {
                    turnIA();
                }
            }
        }
        main.updateTextViewCol("En of game");
    }

    public void turnPlayer() {
        while (playingMan == player) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void turnIA() {
        int col = 0;
        while (playingMan == ia) {
            try {
                playMan(col);
            } catch (ColumnFullException e) {
                if (col < 6) {
                    col += 1;
                } else { //All columns are full
                    gameover = true;
                    break;
                }
            }
        }
    }

    /* Increase counter of turns.
    Invert blackplays, indicating that its the other player's turn.
    And set the values of PlayingMan accordingly.
    Update text view */
    private void newTurn() {

        turn += 1;
        blackPlays = !blackPlays;

        Man aux;
        if (blackPlays) {
            playingMan = Man.BLACK;
        } else {
            playingMan = Man.WHITE;
        }
        main.updateTextViewBoard(board.toString());
    }

    /* Plays a man on a column.
    Throws ColumnFullException, so it has to be handled individually.
    Ends the game when GameOverException.*/
    public void playMan(int col) throws ColumnFullException {
        try {
            board.playMan(col, playingMan);
            newTurn();
        } catch (GameOverException e) {
            gameover = true;
            newTurn();
        }
    }

    public int getWidth() {
        return this.board.width;
    }
}
