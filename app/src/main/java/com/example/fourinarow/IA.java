package com.example.fourinarow;

public class IA {

    public class Node{
        Board board = null;
        int score = 0;

        public Node(Board b){
            this.board = b;
        }

    }

    Man team;

    public IA(Man m) {
        this.team = m;
    }

    public Node minmax(Node node, int depth, boolean max){
        if( depth == 0){
            return evaluateBoard(node);
        }

        if(max){
            int maxeval = -1000;
            Node[] childs = generateChilds(node);
            for(int i = 0; i < childs.length; i++){
                int eva = minmax(childs[i], depth-1, false);
            }
        }
    }

    public Node evaluateBoard(Node node) {
        int value = 0;
        switch (endState(node.board)) {
            case BLACK:
                if(Man.BLACK == team){ //IA wins
                    value = 100;
                }else{ //IA looses
                    value = -100;
                }
                break;
            case WHITE:
                if(Man.WHITE == team){ //IA wins
                    value = 100;
                }else{ //IA looses
                    value = -100;
                }
                break;
            case EMPTY:
                if(tie(node.board)){ //TIE
                    value = 0;
                }else{ //The game has not concluded
                    value = -1;
                }
                break;

        }
        node.score = value;
        return node;
    }

    /* Identify if the game is over. If it is, return the winning team, otherwise return EMPTY */
    public Man endState(Board board) {
        Man m = Man.EMPTY;

        // FIRST ALGORITHM
        /*Loop every row to find 4 equal mans in a row.
        Save the last man found in "m", and compare it to the next Man.
        If both mans do not coincide, start the count from 1 and change m to the last man found.
        For each new row restart the count.
        If 4 mans in a row have been found, break out of both loops.

        for (int i = 0; i < board.height; i++) {
            inRow = 0;
            m = Man.EMPTY;
            for (int j = 0; j < board.width; j++) {
                if (board.getSquare(i, j) == m) {
                    inRow++;
                    if (inRow == 4 && m != Man.EMPTY) { //4 equal mans have been found in a row
                        if (m == this.team) {
                            state = 1;
                        } else {
                            state = 2;
                        }
                        return state;
                    }
                } else { //Different man from previous square
                    m = board.getSquare(i, j);
                    inRow = 1;
                }
            }
        }
        */

        //SECOND ALGORITHM
        /*
        With only one loop over the entire board we will find the first 4 in a row if there is one.
        For each square, check every direction. If a win is found, return de winner. If not, return Empty.
         */
        for (int i = 0; i < board.height; i++) {
            for (int j = 0; j < board.width; j++) {
                m = board.getSquare(i, j);
                if (m == Man.EMPTY) { //if the square is empty, skip it
                    continue;
                } else {
                    if (j + 3 < board.width &&
                            m == board.getSquare(i, j + 1) && // look right
                            m == board.getSquare(i, j + 2) &&
                            m == board.getSquare(i, j + 3))
                        return m;
                    if (i + 3 < board.height) {
                        if (m == board.getSquare(i + 1, j) && // look up
                                m == board.getSquare(i + 2, j) &&
                                m == board.getSquare(i + 3, j))
                            return m;
                        if (j + 3 < board.width &&
                                m == board.getSquare(i + 1, j + 1) && // look up & right
                                m == board.getSquare(i + 2, j + 2) &&
                                m == board.getSquare(i + 3, j + 3))
                            return m;
                        if (j - 3 >= 0 &&
                                m == board.getSquare(i + 1, j - 1) && // look up & left
                                m == board.getSquare(i + 1, j - 2) &&
                                m == board.getSquare(i + 1, j - 3))
                            return m;
                    }
                }
            }
        }
        return Man.EMPTY; //No win found
    }

    public boolean tie(Board board){
        return board.mans == board.height*board.width;
    }

}
