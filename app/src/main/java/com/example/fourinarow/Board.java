package com.example.fourinarow;

import java.util.ArrayList;

public class Board {

    Square[][] grid;
    int width;
    int height;

    int mans = 0; //number of total mans played

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
                mans++;
                break;
            }
            i++;
        }
        if (i == height) {
            throw new ColumnFullException("COLUMN_FULL", "the column where you are trying to play is full");
        } else {
            checkGameOver(col, i, m);
        }
    }

    public void checkGameOver(int col, int row, Man m) throws GameOverException {
        int[][] inrow = new int[4][2];
        int inRow = 0;

        //Vertical
        for (int i = 0; i < height; i++) {
            if (grid[i][col].getMan() == m) {
                inrow[inRow][0] = i;
                inrow[inRow++][1] = col;
            } else {
                inRow = 0;
            }
            //4 IN A ROW
            if (inRow == 4) {
                throw new GameOverException("GAME_OVER", "A 4 in a row has been made. Game stopped.", m, inrow);
            }
        }
        //Horizontal
        inRow = 0;
        for (int i = 0; i < width; i++) {
            if (grid[row][i].getMan() == m) {
                inrow[inRow][0] = row;
                inrow[inRow++][1] = i;
            } else {
                inRow = 0;
            }
            //4 IN A ROW
            if (inRow == 4) {
                throw new GameOverException("GAME_OVER", "A 4 in a row has been made. Game stopped.", m, inrow);
            }
        }
        //bottom left to top right
        inRow = 0;
        for (int i = -Math.min(row, col); i + row < height && i + col < width; i++) {
            if (grid[row + i][col + i].getMan() == m) {
                inrow[inRow][0] = row + i;
                inrow[inRow++][1] = col + i;
            } else {
                inRow = 0;
            }
            //4 IN A ROW
            if (inRow == 4) {
                throw new GameOverException("GAME_OVER", "A 4 in a row has been made. Game stopped.", m, inrow);
            }
        }

        //top left to bottom right
        inRow = 0;
        int aux = (height - 1) - row;
        for (int i = -Math.min(aux, col); row - i >= 0 && i + col < width; i++) {
            if (grid[row - i][col + i].getMan() == m) {
                inrow[inRow][0] = row - i;
                inrow[inRow++][1] = col + i;
            } else {
                inRow = 0;
            }
            //4 IN A ROW
            if (inRow == 4) {
                throw new GameOverException("GAME_OVER", "A 4 in a row has been made. Game stopped.", m, inrow);
            }
        }
        if(mans == height*width){
            throw new GameOverException("GAME_OVER", "A tie has been reached. Game stopped.", Man.EMPTY, null);
        }
    }

    public Man getSquare(int row, int col) {
        return grid[row][col].getMan();
    }

    //Get the man which is at the top of the y column
    public Man getTopMan(int y) {
        for (int i = height - 1; i >= 0; i--) {
            Man m = getSquare(i, y);
            if (m != Man.EMPTY) {
                return m;
            }
        }
        return Man.EMPTY;
    }

    //Get the position of the man which is at the top of the y column
    public int getTopPos(int y) {
        for (int i = height - 1; i >= 0; i--) {
            Man m = getSquare(i, y);
            if (m != Man.EMPTY) {
                return i;
            }
        }
        return -1;
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

    /* Compare two boards. Check if width, height, number of mans played and their position.*/
    public boolean equals(Board b) {
        boolean equals = true;
        if (b.height == this.height && b.width == this.width && b.mans == this.mans) {
            for (int i = 0; i < this.height; i++) {
                for (int j = 0; j < this.width; j++) {
                    if (b.getSquare(i, j) != this.getSquare(i, j)) {
                        equals = false;
                    }
                }
            }
        } else {
            equals = false;
        }
        return equals;
    }

    /*
    Returns a new Board equal to the original.
     */
    public Object clone() {
        Board b = new Board(this.width, this.height);

        b.mans = this.mans;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                b.grid[i][j] = new Square(this.getSquare(i, j));
            }
        }
        return b;
    }
}
