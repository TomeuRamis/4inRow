package com.example.fourinarow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

public class IA extends Thread {

    public class Node {

        Board board = null;

        int score = 0;
        int column;
        boolean terminal = false;
        Man win = Man.EMPTY;
        Node father = null;
        ArrayList<Node> child = new ArrayList<Node>();

        public Node(Board b) {
            this.board = b;
        }

        public Node(Board b, int c, Node father) {
            this.board = b;
            this.column = c;
            this.father = father;
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
                        node = new Node(b, i, this);
                    } catch (ColumnFullException e) { //If the column is full, try the next one
                        continue;
                    } catch (GameOverException e) { //If the game ends we mark the node as terminal and save who made the winning move
                        node = new Node(b, i, this);
                        node.terminal = true;
                        node.win = m;
                    }
                    this.child.add(node);
                }
            }
        }


        public boolean isLeaf() {
            return this.child.isEmpty();
        }
    }

    Board board;
    Controller control;
    Man team;
    Node root;

    final int treeDepth = 6; //Constant depth of the tree
    //Node evaluation scores
    final int WIN = 400;
    final int TIE = 0;
    final int UNFINISHED = -1;
    //BLOCK(n) = BLOCK(n-1)*7 + 1
    //EG: if we have 1 3-in-a-row, and 7 2-in-a-row, we always want to stop the 3 over the 2s.
    final int BLOCK3 = 57;
    final int BLOCK2 = 8;
    final int BLOCK1 = 1;
    final int BLOCK0 = 0;
    //Variables for iterative minmax
    //Array of current father on each level
    Node[] father;
    //Array of lists of values for each level
    ArrayList<Integer>[] values;
    //Local indicator of the current level (or current depth)
    int depth;

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
        if (--depth > 0) {
            Node node;
            for (int i = 0; i < root.child.size(); i++) {
                node = root.child.get(i);
                if (!node.terminal) {
                    generateTree(node, depth);
                }
            }
        }
    }

    /*
    This method is meant to update the root node after the player places his man.
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
    this is the case. And if it finds that it is not, it will call "generateTree" to fill in the missing nodes.
     */
    public void updateDecisionTree(Node root, int depth) {
        if (depth > 0) {
            if (root.isLeaf() && !root.terminal) {
                generateTree(root, depth);
            } else {
                Iterator<Node> iterator = root.child.iterator();
                while (iterator.hasNext()) {
                    updateDecisionTree(iterator.next(), depth - 1);
                }
            }
        }
    }


    @Override
    public void run() {
    }

    /*
    Each turn we generate the decision tree, run the minmax algorithm and look to the childs of root
    to fins the best play possible.
     */
    public void play() {

        double time = System.nanoTime();
        root = updateRootBoard(root, board);
        System.out.println("update board: "+ Double.toString(System.nanoTime()-time));
        root.father = null;
        time = System.nanoTime();
        updateDecisionTree(root, treeDepth);
        System.out.println("update decision tree: "+ Double.toString(System.nanoTime()-time));

        time = System.nanoTime();
        minmax(root, treeDepth, true);
        System.out.println("minmax: "+ Double.toString(System.nanoTime()-time));
        time = System.nanoTime();
        minmax(root, treeDepth, true, -10000, 10000);
        System.out.println("minmax with pruning: "+ Double.toString(System.nanoTime()-time));
        time = System.nanoTime();
        iterativeMinmax(root, true);
        System.out.println("iterative minmax: "+ Double.toString(System.nanoTime()-time));
        time = System.nanoTime();
         iterativeMinmaxAlphaBeta(root, true);
        System.out.println("iterative minmax with \"pruning\": "+ Double.toString(System.nanoTime()-time));

        boolean equal = true;
        Node best = new Node(root.board);
        best.score = -10000;
        Iterator<Node> iterator = root.child.iterator();
        while (iterator.hasNext()) {
            Node aux = iterator.next();

            if (best.score != -10000 && best.score != aux.score) {
                equal = false;
            }

            best = best.compare(aux, true);
        }

        /*
        If all the children are equally bad (or good), we will try to play the man as centered as possible.
        This solves the opening play, because otherwise we would play at 0, instead of the recommended play
        at the middle.
         */
        if (equal) {
            if (best.score == -100) {
                System.err.println("I lost :(");
            }
            boolean found = false;
            int index = (int) Math.ceil(root.child.size() / 2); //Get the middle index
            while (!found && index >= 0) {
                try {
                    best = root.child.get(index);
                    found = true;
                } catch (IndexOutOfBoundsException ex) {
                    System.err.println("Array of children not big enough, trying smaller value.");
                    index--;
                }
            }
            if (index < 0) {
                System.err.println("What. Error at play algorithm. Why has this happened");
            }
        }
        this.root = best;
        try {
            control.IATryPlayMan(best.column);
        } catch (ColumnFullException e) {
            System.err.println();
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
            return evaluateNode(node);
        }

        Iterator<Node> iterator = node.child.iterator();
        if (max) {
            int bestVal = -1000;
            while (iterator.hasNext()) {
                int val = minmax(iterator.next(), depth - 1, false, alpha, beta);
                bestVal = Math.max(bestVal, val);
                alpha = Math.max(alpha, bestVal);
                if (beta < alpha) { //Prune all other branches
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
                if (beta < alpha) { //Prune
                    break;
                }
            }
            node.score = worstVal;
            return worstVal;
        }
    }

    public boolean increaseDepth(boolean max) {
        values[this.depth] = new ArrayList<Integer>();
        depth++;
        return !max;
    }

    public boolean decreaseDepth(boolean max) {
        depth--;
        return !max;
    }

    public int chooseBestChildScore(boolean max, ArrayList<Integer> values) {
        int i = 0;
        int score = values.get(i++);
        while (i < values.size()) {
            if (max) {
                if (score < values.get(i)) score = values.get(i);
            } else {
                if (score > values.get(i)) score = values.get(i);
            }
            i++;
        }
        return score;
    }

    public void iterativeMinmax(Node root, boolean max) {
        //Init variables
        //Local indicator of the current level (or current depth)
        depth = treeDepth + 1;
        //Array of current father on each level
        father = new Node[depth];
        //Array of lists of values for each level
        values = new ArrayList[depth];

        for (int i = 0; i < depth; i++) {
            father[i] = null;
            values[i] = new ArrayList<Integer>();
        }

        depth--;
        Node node;
        Stack stack = new Stack();
        stack.push(root);

        while (!stack.empty()) {

            node = (Node) stack.pop();

            //Node is leaf and needs to be evaluated. This node is a brother or a son of the last one
            if (node.isLeaf() && father[depth] == node.father) {
                //If the depth is 0 or its a terminal node, we have arrived at the bottom of the tree
                if(depth == 0 || node.terminal) {
                    node.score = evaluateNode(node);
                    values[depth].add(node.score);
                }else //If the depth is higher than 0 and it's not a terminal node, we need to generate the rest of the tree
                {
                    generateTree(node, depth);
                    //We add this node again in order to re-evaluate it
                    stack.add(node);
                }

            } //Node is leaf but it is not on the same depth, we need to find the current depth of this node first. This node is a uncle, granduncle or higher up of the las node.
            else if (node.isLeaf() && father[depth] != node.father) {

                int score;
                while (father[depth] != node.father) {
                    score = chooseBestChildScore(max, values[depth]);
                    father[depth].score = score;
                    max = increaseDepth(max);
                    values[depth].add(score);
                }

                //If it's a terminal node evaluate it
                if(node.terminal) {
                    node.score = evaluateNode(node);
                    values[depth].add(node.score);
                }else{ //If its not, generate a deeper tree
                    generateTree(node, depth);
                    //We add this node again in order to re-evaluate it
                    stack.add(node);
                }

            } //Node is a son of the node before him
            else if (!node.isLeaf() && father[depth] == node.father) {

                stack.addAll(node.child);
                max = decreaseDepth(max);
                father[depth] = node;

            }//Node is a uncle, granduncle, or higher up of the node before him
            else {

                father[depth].score = chooseBestChildScore(max, values[depth]);

                while (father[depth] != node.father) {
                    max = increaseDepth(max);
                }
                stack.addAll(node.child);
                max = decreaseDepth(max);
                father[depth] = node;
            }

        }
    }

    public void iterativeMinmaxAlphaBeta(Node root, boolean max) {
        //Init variables
        //Local indicator of the current level (or current depth)
        depth = treeDepth + 1;
        //Array of current father on each level
        father = new Node[depth];
        //Array of lists of values for each level
        values = new ArrayList[depth];
        //Prining variables
        int alpha = -1000;
        int beta = +1000;

        for (int i = 0; i < depth; i++) {
            father[i] = null;
            values[i] = new ArrayList<Integer>();
        }

        depth--;
        Node node;
        Stack stack = new Stack();
        stack.push(root);

         while (!stack.empty()) {

            node = (Node) stack.pop();

            //Node is leaf and needs to be evaluated. This node is a brother or a son of the last one
            if (node.isLeaf() && father[depth] == node.father) {
                //If the depth is 0 or its a terminal node, we have arrived at the bottom of the tree
                if(depth == 0 || node.terminal) {
                    node.score = evaluateNode(node);
                    values[depth].add(node.score);

                    //pruning logic
                    if(max){
                        alpha = Math.max(alpha, node.score);
                    }else{
                        beta = Math.min(beta, node.score);
                    }
                    if(beta < alpha){
                        //prune
                        stack = prune(stack, father[depth]);
                    }

                }else //If the depth is higher than 0 and it's not a terminal node, we need to generate the rest of the tree
                {
                    generateTree(node, depth);
                    //We add this node again in order to re-evaluate it
                    stack.add(node);
                }

            } //Node is leaf but it is not on the same depth, we need to find the current depth of this node first. This node is a uncle, granduncle or higher up of the las node.
            else if (node.isLeaf() && father[depth] != node.father) {

                int score;
                while (father[depth] != node.father) {
                    score = chooseBestChildScore(max, values[depth]);
                    father[depth].score = score;
                    max = increaseDepth(max);
                    values[depth].add(score);

                    if(max && beta > alpha){
                        alpha = beta;
                    }else if( !max && alpha < beta){
                        beta = alpha;
                    }
                }
                if(max){
                    beta = 1000;
                }else{
                    alpha = -1000;
                }

                //If it's a terminal node evaluate it
                if(node.terminal) {
                    node.score = evaluateNode(node);
                    values[depth].add(node.score);

                    //pruning logic
                    if(max){
                        alpha = Math.max(alpha, node.score);
                    }else{
                        beta = Math.min(beta, node.score);
                    }
                    if(beta < alpha){
                        //prune
                        stack = prune(stack, father[depth]);
                    }
                }else{ //If its not, generate a deeper tree
                    generateTree(node, depth);
                    //We add this node again in order to re-evaluate it
                    stack.add(node);
                }

            } //Node is a son of the node before him
            else if (!node.isLeaf() && father[depth] == node.father) {

                stack.addAll(node.child);
                max = decreaseDepth(max);
                father[depth] = node;

            }//Node is a uncle, granduncle, or higher up of the node before him
            else {

                int score;
                while (father[depth] != node.father) {
                    score = chooseBestChildScore(max, values[depth]);
                    father[depth].score = score;
                    max = increaseDepth(max);
                    values[depth].add(score);

                    if(max && beta > alpha){
                        alpha = beta;
                    }else if( !max && alpha < beta){
                        beta = alpha;
                    }
                }
                if(max){
                    beta = 1000;
                }else{
                    alpha = -1000;
                }

                stack.addAll(node.child);
                max = decreaseDepth(max);
                father[depth] = node;
            }

        }
    }

    public Stack prune(Stack stack, Node father){
        Node node = (Node) stack.peek();
        while(node.father == father){
            stack.pop();
            node = (Node) stack.peek();
        }

        return stack;
    }

    public int evaluateNode(Node node) {
        int value = 0;

        //If a endgame has been reached, add the corresponding score
        switch (node.win) {
            case BLACK:
            case WHITE:
                value += WIN; //Win of either team.
                break;
            case EMPTY:
                if (tie(node.board)) { //TIE
                    value += TIE;
                } else {
                    value += UNFINISHED;
                }
                break;
        }
        //Not marked as a leaf, meaning the game has not ended
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
        for (int i = col + 1; i < node.board.width; i++) {
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
