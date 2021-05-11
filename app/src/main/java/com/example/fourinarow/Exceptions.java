package com.example.fourinarow;

class ColumnFullException extends Exception{

    private String code;

    public ColumnFullException(String code, String message) {
        super(message);
        this.setCode(code);
    }

    public ColumnFullException(String code, String message, Throwable cause) {
        super(message, cause);
        this.setCode(code);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

class GameOverException extends Exception {

    private String code;
    private Man man;
    private int[][] inRow;

    public GameOverException(String code, String message, Man man) {
        super(message);
        this.setCode(code);
        this.man = man;
    }

    public GameOverException(String code, String message, Man man, int[][] inRow) {
        super(message);
        this.setCode(code);
        this.man = man;
        this.inRow = inRow;
    }

    public GameOverException(String code, String message, Throwable cause) {
        super(message, cause);
        this.setCode(code);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Man winner(){
        return man;
    }

    public int[][] getInRow(){return this.inRow;}
}