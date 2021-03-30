package com.example.fourinarow;

import java.util.ArrayList;
import java.util.Iterator;

public class IA {

    public class Node {
        Board board = null;


        int score = 0;
        int column;
        boolean terminal = false;
        Man win = Man.EMPTY;
        ArrayList<Node> child = new ArrayList<Node>();

        public Node(Board b) {
            this.board = b;
        }

        public Node(Board b, int c) {
            this.board = b;
            this.column = c;
        }

        public Node(int val) {
            this.score = val;
        }

        /*Compares this node against a second node. If max=true it will return the biggest, otherwise return the smallest*/
        public Node compare(Node n, boolean max) {
            if (max) {
                if (this.score >= n.score) {
                    return this;
                } else {
                    return n;
                }
            } else {
                if (this.score <= n.score) {
                    return this;
                } else {
                    return n;
                }
            }
        }

        public void generateChildren() {
            Man m;
            if (!this.terminal) {
                //Study who is going to play next, black or white
                if (board.mans % 2 == 0) {
                    m = Man.BLACK;
                } else {
                    m = Man.WHITE;
                }
                //Generate all of the possible children (play one man on each column, if possible)
                for (int i = 0; i < board.width; i++) {
                    Board b = null;
                    Node node = null;
                    try {
                        b = (Board) this.board.clone();
                        b.playMan(i, m);
                        node = new Node(b, i);
                    } catch (ColumnFullException e) { //If the column is full, try the next one
                        continue;
                    } catch (GameOverException e) { //If the game ends we mark the node as terminal and save who made the winning move
                        node = new Node(b, i);
                        node.terminal = true;
                        node.win = m;
                    }
                    this.child.add(node);
                }
            }
        }
    }

    Board board;
    Controller control;
    Man team;
    Node root;

    final int treeDepth = 5; //Constant depth of the tree
    //Node evaluation scores
    final int WIN = 100;
    final int TIE = 0;
    final int UNFINISHED = -1;
    final int BLOCK3 = 14;
    final int BLOCK2 = 5;
    final int BLOCK1 = 0;
    final int BLOCK0 = 0;

    public IA(Board board, Controller control, Man m) {
        this.board = board;
        this.control = control;
        this.team = m;
        Board b = (Board) board.clone();
        root = new Node(b);
        generateTree(root, treeDepth);
    }

    /*
    Recursive method to generate a decision tree of depth N. Max depth is 42 for a 7x6 board.
    */
    public void generateTree(Node root, int depth) {
        root.child = new ArrayList<Node>();
        root.generateChildren();
        Iterator<Node> iterator = root.child.iterator();
        while (iterator.hasNext() && depth != 0) {
            generateTree(iterator.next(), depth - 1);
        }
    }

    /*
    This method is ment to update the root node after the player places his man.
    If the node tree has been correctly generated, the root node will have a child that "predicted" the move of the player.
     */
    public Node updateRootBoard(Node root, Board board) {
        Node newRoot = null;
        Boolean foundNewState = false;
        for (int i = 0; i < root.child.size() && !foundNewState; i++) {
            newRoot = root.child.get(i);
            if (board.equals(newRoot.board)) {
                foundNewState = true;
            }
        }
        if (!foundNewState) {
            newRoot = new Node(this.board);
            generateTree(newRoot, treeDepth);
        }
        return newRoot;
    }

    /*
    this method should go over the entire tree, generating all missing children in order to keep it at a certain length.
    For example, if we want the tree to always be 8 nodes in depth, this method will check if
    this is the case. And if it fins that it is not, it will call "generateTree" to fill in the missing nodes.
     */
    public void updateDecisionTree(Node root, int depth) {
        if (depth != 0) {
            if (root.child.isEmpty()) {
                generateTree(root, depth);
            } else {
                Iterator<Node> iterator = root.child.iterator();
                while (iterator.hasNext()) {
                    updateDecisionTree(iterator.next(), depth - 1);
                }
            }
        }
    }

    /*
    Each turn we generate the decision tree, run the minmax algorithm and look to the childs of root
    to fins the best play possible.
     */
    public void play() throws GameOverException {

        root = updateRootBoard(root, board);
        updateDecisionTree(root, treeDepth);

        minmax(root, treeDepth, true, -10000, 10000);

        Node best = new Node(root.board);
        best.score = -10000;
        Iterator<Node> iterator = root.child.iterator();
        while (iterator.hasNext()) {
            Node aux = iterator.next();
            best = best.compare(aux, true);
        }
        this.root = best;
        try {
            this.control.playMan(best.column);
        } catch (ColumnFullException e) {
            System.out.println("OOPS, this shouldn't have happened");
        }
    }


    public int minmax(Node node, int depth, boolean max) {
        if (depth == 0 || node.terminal) {
            return evaluateNode(node);
        }

        Iterator<Node> iterator = node.child.iterator();
        if (max) {
            int bestVal = -1000;
            while (iterator.hasNext()) {
                int val = minmax(iterator.next(), depth - 1, false);
                bestVal = Math.max(bestVal, val);
            }
            node.score = bestVal;
            return bestVal;
        } else {
            int worstVal = +1000;
            while (iterator.hasNext()) {
                int val = minmax(iterator.next(), depth - 1, true);
                worstVal = Math.min(worstVal, val);
            }
            node.score = worstVal;
            return worstVal;
        }
    }

    public int minmax(Node node, int depth, boolean max, int alpha, int beta) {
        if (depth == 0 || node.terminal) {
            System.err.println("NOT FROZEN");
            return evaluateNode(node);
        }

        Iterator<Node> iterator = node.child.iterator();
        if (max) {
            int bestVal = -1000;
            while (iterator.hasNext()) {
                int val = minmax(iterator.next(), depth - 1, false, alpha, beta);
                bestVal = Math.max(bestVal, val);
                alpha = Math.max(alpha, bestVal);
                if (beta <= alpha) { //Prune all other branches
                    break;
                }
            }
            node.score = bestVal;
            return bestVal;
        } else {
            int worstVal = +1000;
            while (iterator.hasNext()) {
                int val = minmax(iterator.next(), depth - 1, true, alpha, beta);
                worstVal = Math.min(worstVal, val);
                beta = Math.min(beta, worstVal);
                if (beta <= alpha) { //Prune
                    break;
                }
            }
            node.score = worstVal;
            return worstVal;
        }
    }

    public int evaluateNode(Node node) {
        int value = UNFINISHED;

        if (node.terminal) { //Marked as a leaf node
            switch (node.win) {
                case BLACK:
                    if (Man.BLACK == team) { //IA wins
                        value = WIN;
                    } else { //IA looses
                        value = -WIN;
                    }
                    break;
                case WHITE:
                    if (Man.WHITE == team) { //IA wins
                        value = WIN;
                    } else { //IA looses
                        value = -WIN;
                    }
                    break;
                case EMPTY:
                    if (tie(node.board)) { //TIE
                        value = TIE;
                    } else { //The game has not concluded "DEPRECATED" (should never occur)
                        value = UNFINISHED;
                        System.err.println("The game has not finished, yet the node is marked as terminal");
                    }
                    break;

            }
        } else { //Not marked as a leaf, meaning the game has not ended
            /* We are using the same base logic as the end game detection algorithm.
            We will look in the 7 possible directions of the block: bottom, left, right and
            the 4 diagonals.
            Each time, depending on the mans blocked we add/subtract to the score of the node.
             */
            int col = node.column;
            int row = node.board.getTopPos(col);

            Man played = node.board.getSquare(row, col);
            Man rival = played.getRival();

            int inRow = 0;
            Man aux;

            //Vertical
            for (int i = row - 1; i >= 0; i--) {
                aux = node.board.getSquare(i, col);
                if (aux == rival) { //Count how many Mans is vertically blocking
                    inRow++;
                } else if (aux == played) {
                    break;
                } else {
                    System.err.println("what? error at vertical node evaluation");
                    break;
                }
            }
            value += evaluateInRow(inRow);

            //Horizontal left
            inRow = 0;
            for (int i = col - 1; i >= 0; i--) {
                aux = node.board.getSquare(row, i);
                if (aux == rival) {
                    inRow++;
                } else if (aux == played || aux == Man.EMPTY) {
                    break;
                }
            }
            value += evaluateInRow(inRow);

            //Horizontal right
            inRow = 0;
            for (int i = col; i < node.board.width; i++) {
                aux = node.board.getSquare(row, i);
                if (aux == rival) {
                    inRow++;
                } else if (aux == played || aux == Man.EMPTY) {
                    break;
                }
            }
            value += evaluateInRow(inRow);

            //top right
            inRow = 0;
            for (int i = 0; row + i < node.board.height && col + i < node.board.width; i++) {
                aux = node.board.getSquare(row + i, col + i);
                if (aux == rival) {
                    inRow++;
                } else if (aux == played || aux == Man.EMPTY) {
                    break;
                }
            }
            value += evaluateInRow(inRow);

            //bottom right
            inRow = 0;
            for (int i = 0; row - i >= 0 && col + i < node.board.width; i++) {
                aux = node.board.getSquare(row - i, col + i);
                if (aux == rival) {
                    inRow++;
                } else if (aux == played || aux == Man.EMPTY) {
                    break;
                }
            }
            value += evaluateInRow(inRow);

            //bottom left
            inRow = 0;
            for (int i = 0; row - i >= 0 && col - i >= 0; i++) {
                aux = node.board.getSquare(row - i, col - i);
                if (aux == rival) {
                    inRow++;
                } else if (aux == played || aux == Man.EMPTY) {
                    break;
                }
            }
            value += evaluateInRow(inRow);

            //top left
            inRow = 0;
            for (int i = 0; row + i < node.board.height && col - i >= 0; i++) {
                aux = node.board.getSquare(row + i, col - i);
                if (aux == rival) {
                    inRow++;
                } else if (aux == played || aux == Man.EMPTY) {
                    break;
                }
            }
            value += evaluateInRow(inRow);

            //If the move was made by the rival, the score is negative
            if (played != team) {
                value = -value;
            }
        }
        node.score = value;
        return value;
    }

    public int evaluateInRow(int inRow) {
        int value = 0;
        switch (inRow) {
            case 0:
                value += -BLOCK0;
                break;
            case 1:
                value += BLOCK1;
                break;
            case 2:
                value += BLOCK2;
                break;
            case 3:
                value += BLOCK3;
                break;
            default:
                System.err.print("Board evaluation algorithm has found a 4 in row previously undetected.");
        }
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
