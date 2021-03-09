package com.example.fourinarow;

import android.widget.TextView;

public class Controller extends Thread {

    Board board;
    MainActivity main;

    int column = 0;
    int turn = 0;
    Boolean blackPlays = false;
    Man playingMan = null;

    public Controller(MainActivity main) {
        board = new Board(7, 6);
        this.main = main;
    }

    public void run() {
        System.out.println("Game start!");
        startGame();
    }

    public void startGame() {
        newTurn();

        Boolean gameover = false;
        while (!gameover) {

            //Wait for the user's turn to end
            while (blackPlays) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //Try to playa man in every column
            while (!blackPlays) {
                int col = 0;
                try {
                    board.playMan(col, playingMan);
                    newTurn();
                } catch (ColumnFullException e) {
                    if (col <= 6) {
                        col += 1;
                    } else {
                        gameover = true;
                        break;
                    }
                }
            }
        }
    }

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

    public void playMan(int col) {
        if (blackPlays) {
            try {
                board.playMan(col, playingMan);
                newTurn();
            } catch (ColumnFullException e) {
                main.updateTextViewCol("this column is full");
            }
        }
    }


}
