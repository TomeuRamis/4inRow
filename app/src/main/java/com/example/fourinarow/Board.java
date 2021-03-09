package com.example.fourinarow;

import java.util.ArrayList;

public class Board {

    Square[][] grid;
    int width;
    int height;

    public Board(int width, int height){
        this.height = height;
        this.width = width;

        grid = new Square[height][width];
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                grid[i][j] = new Square();
            }
        }
    }

    public void playMan(int col, Man m) throws ColumnFullException{
        int i = 0;
        while(i < height){
            if(grid[i][col].isEmpty()){
                grid[i][col].playMan(m);
                break;
            }
            i++;
        }
        if(i == height){
            throw new ColumnFullException("COLUMN_FULL", "the column where you are trying to play is full");
        }
    }

    @Override
    public String toString(){
        String aux = "";
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                aux += " | "+grid[i][j].toString();
            }
            aux += " |\n";
            for(int j = 0; j < width; j++){
                aux += "_ _ _ ";
            }
            aux += "\n";
        }

        return aux;
    }

}
