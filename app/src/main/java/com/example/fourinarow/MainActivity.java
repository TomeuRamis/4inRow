package com.example.fourinarow;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Board board;

    int column = 0;
    int turn = 0;
    Boolean blackPlays = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        board = new Board(7, 6);

        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setText(board.toString());
        updateTextViewCol();
        blackPlays = true;


    }

    public void startGame(){
        Boolean gameover = false;
        while(!gameover){
            Man man = newTurn();

            //While there are errors (placing the man on a full column) keep having the turn
            Boolean error = true;
            while (error) {
                try {
                    turn( man );
                } catch (ColumnFullException ex) {
                    continue;
                }
                error = false;
            }
        }
    }

    public void buttonPlaceMan(View view, Man man) throws ColumnFullException{
        board.playMan(column, man);
    }

    public void moveLeft(View view) {
        if (column > 0) {
            column -= 1;
        }
        updateTextViewCol();
    }

    public void moveRight(View view) {
        if (column < 6) {
            column += 1;
        }
        updateTextViewCol();
    }

    private Man newTurn() {
        turn += 1;
        blackPlays = !blackPlays;

        Man aux;
        if (blackPlays) {
            aux = Man.BLACK;
        } else {
            aux = Man.WHITE;
        }
        return aux;
    }

    private void updateTextViewCol() {
        TextView tvcol = (TextView) findViewById(R.id.textView2);
        tvcol.setText(column);
    }
}