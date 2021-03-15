package com.example.fourinarow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

public class IA {

    public class Node {
        Board board = null;
        int score = 0;
        boolean terminal = false;
        Man win = Man.EMPTY;
        ArrayList<Node> child;

        public Node(Board b) {
            this.board = b;
        }

        public Node(int val) {
            this.score = val;
        }

        /*Compares this node against a second node. If max=true it will return the biggest, otherwise return the smallest*/
        public Node compare(Node n, boolean max) {
            if (max) {
                if (this.score > n.score) {
                    return this;
                } else {
                    return n;
                }
            } else {
                if (this.score < n.score) {
                    return this;
                } else {
                    return n;
                }
            }
        }

        public void generateChildren() {
            Man m;
            //Study who is going to play next, black or white
            if (board.mans % 2 == 0) {
                m = Man.BLACK;
            } else {
                m = Man.WHITE;
            }
            //Generate all of the possible children (play one man on each column, if possible)
            for (int i = 0; i < board.width; i++) {
                try {
                    Board b = new Board(this.board, i, m);
                    Node node = new Node(b);
                    if (b.ended) { //Mark the node as a leaf node (terminal node) and save who made the winning move
                        node.terminal = true;
                        node.win = m;
                    }
                    this.child.add(node);
                } catch (ColumnFullException e) { //If the column is full, try the next one
                    continue;
                }
            }
        }
    }

    Board board;
    Man team;
    Node root;
    Iterator<Node> iterator;
    Stack<Node> decisionStack;

    public IA(Board board, Man m) {
        this.board = board;
        this.team = m;
        root = new Node(board);
        decisionStack = new Stack<Node>();
        generateTree(root, 8);
    }

    /*
    Recursive method to generate a decision tree of depth N. Max depth is 42 for a 7x6 board.
    */
    public void generateTree(Node root, int depth) {
        root.generateChildren();
        iterator = root.child.iterator();
        while (iterator.hasNext() && depth != 0) {
            generateTree(iterator.next(), depth - 1);
        }
    }

    public int minmax(Node node, int depth, boolean max) {
        if (depth == 0 || node.terminal) {
            return evaluateNode(node);
        }

        iterator = node.child.iterator();
        if (max) {
            int bestVal = -1000;
            while (iterator.hasNext()) {
                int val = minmax(iterator.next(), depth - 1, false);
                bestVal = Math.max(bestVal, val);
            }
            node.score = bestVal;
            return bestVal;
        }
    }

    public int evaluateNode(Node node) {
        int value = 0;
        switch (endState(node.board)) {
            case BLACK:
                if (Man.BLACK == team) { //IA wins
                    value = 100;
                } else { //IA looses
                    value = -100;
                }
                break;
            case WHITE:
                if (Man.WHITE == team) { //IA wins
                    value = 100;
                } else { //IA looses
                    value = -100;
                }
                break;
            case EMPTY:
                if (tie(node.board)) { //TIE
                    value = 0;
                } else { //The game has not concluded
                    value = -1;
                }
                break;

        }
        node.score = value;
        return value;
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

    public boolean tie(Board board) {
        return board.mans == board.height * board.width;
    }

}
