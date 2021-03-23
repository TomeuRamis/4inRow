package com.example.fourinarow;

public class Square {
    Man man;

    public Square(){
        this.man = Man.EMPTY;
    }
    public Square(Man m){
        this.man = m;
    }

    public void playMan(Man m){
        this.man = m;
    }

    public boolean isEmpty(){
        return this.man == Man.EMPTY;
    }
    public Man getMan(){
        return this.man;
    }

    public void clearMan(){
        this.man = Man.EMPTY;
    }

    @Override
    public String toString(){
        String aux = "";
        switch(man){
            case EMPTY:
                aux =  "     ";
                break;
            case BLACK:
                aux =  " X ";
                break;
            case WHITE:
                aux =  " O ";
                break;
        }
        return aux;
    }
}
