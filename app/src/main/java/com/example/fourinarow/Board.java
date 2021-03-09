package com.example.fourinarow;

import java.util.ArrayList;

public class Board {

    Square[][] grid;
    int width;
    int height;

    public Board(int width, int height) {
        this.height = height;
        this.width = width;

        grid = new Square[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                grid[i][j] = new Square();
            }
        }
    }

    public void playMan(int col, Man m) throws ColumnFullException, GameOverException {
        int i = 0;
        while (i < height) {
            if (grid[i][col].isEmpty()) {
                grid[i][col].playMan(m);
                break;
            }
            i++;
        }
        if (i == height) {
            throw new ColumnFullException("COLUMN_FULL", "the column where you are trying to play is full");
        } else {
            checkGameOver(i, col, m);
        }
    }

    public void checkGameOver(int row, int col, Man m) throws GameOverException {
        Boolean gameover = false;
        int inRow = 0;

        //Vertical
        for (int i = 0; i < height; i++) {
            if (grid[i][col].getMan() == m) {
                inRow++;
            } else {
                inRow = 0;
            }
            //4 IN A ROW
            if (inRow == 4) {
                throw new GameOverException("GAME_OVER", "A 4 in a row has been made. Game stopped.", m);
            }
        }
        //Horizontal
        inRow = 0;
        for (int i = 0; i < width; i++) {
            if (grid[row][i].getMan() == m) {
                inRow++;
            } else {
                inRow = 0;
            }
            //4 IN A ROW
            if (inRow == 4) {
                throw new GameOverException("GAME_OVER", "A 4 in a row has been made. Game stoped.", m);
            }
        }
        //bottom left to top right
        inRow = 0;
        for (int i = -Math.min(row, col); i + row < height && i + col < width; i++) {
            if (grid[row + i][col + i].getMan() == m) {
                inRow++;
            } else {
                inRow = 0;
            }
            //4 IN A ROW
            if (inRow == 4) {
                throw new GameOverException("GAME_OVER", "A 4 in a row has been made. Game stopped.", m);
            }
        }

        //top left to bottom right
        /*inRow = 0;
        for (int i = -Math.min(row, col); i + row < height && i + col < width; i++) {
            if (grid[row + i][col + i].getMan() == m) {
                inRow++;
            } else {
                inRow = 0;
            }
            //4 IN A ROW
            if (inRow == 4) {
                throw new GameOverException("GAME_OVER", "A 4 in a row has been made. Game stopped.", m);
            }
        }
        */
    }

    @Override
    public String toString() {
        String aux = "";
        for (int i = height - 1; i >= 0; i--) {
            for (int j = 0; j < width; j++) {
                aux += " | " + grid[i][j].toString();
            }
            aux += " |\n";
            for (int j = 0; j < width; j++) {
                aux += "_ _ _ ";
            }
            aux += "\n";
        }

        return aux;
    }

}
