package com.example.fourinarow;

import java.util.ArrayList;

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

