package com.example.fourinarow;

import android.widget.TextView;

public class Controller extends Thread {

    Board board;
    MainActivity main;

    int turn = 0;
    Boolean gameover = false;

    Boolean blackPlays = false;
    Man playingMan = null;

    public Controller(MainActivity main, int width, int height) {
        board = new Board(width, height);
        this.main = main;
    }

    public void run() {
        System.out.println("Game start!");
        startGame();
        System.out.println("Game end");
    }

    public void startGame() {
        newTurn();

        while (!gameover) {

            //Wait for the user's turn to end
            while (blackPlays) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //Try to play a man in every column
            int col = 0;
            while (!blackPlays && !gameover) {
                try {
                    playMan(col);
                } catch (ColumnFullException e) {
                    if (col < 6) {
                        col += 1;
                    } else {
                        gameover = true;
                        break;
                    }
                }
            }
        }
        main.updateTextViewCol("En of game");
        main.updateTextViewBoard(board.toString());
    }

    /* Increase counter of turns.
    Invert blackplays, indicating that its the other player's turn.
    And set the values of PlayingMan accordingly.
    Update both text views */
    private void newTurn() {

        turn += 1;
        blackPlays = !blackPlays;

        Man aux;
        if (blackPlays) {
            playingMan = Man.BLACK;
            main.updateTextViewCol("Start of turn");
        } else {
            playingMan = Man.WHITE;
            main.updateTextViewCol("End of turn");
        }
        main.updateTextViewBoard(board.toString());
    }

    /* Plays a man on a column.
    Throws ColumnFullException, so it has to be handled individually.
    Ends the game when GameOverException.
    Starts a new turn after placing the man (even if a game over has been accomplished) */
    public void playMan(int col) throws ColumnFullException {
        try {
            board.playMan(col, playingMan);
            newTurn();
        } catch (GameOverException e) {
            gameover = true;
            newTurn();
        }
    }

    public int getWidth(){
        return this.board.width;
    }
}
