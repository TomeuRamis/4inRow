package com.example.fourinarow;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    /*
        - Concurrency in order to be albe to interact in real time with the game
    */
    int column = 0;
    Controller control;

    TextView tv;
    TextView tvcol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.textView);
        tvcol = (TextView) findViewById(R.id.textView2);

        Controller control = new Controller(this);

        control.start();
    }

    public void moveLeft(View view) {
        if (column > 0) {
            column -= 1;
        }
        updateTextViewCol();
        view.invalidate();  // for refreshment
        System.out.println("LEFT");
    }

    public void moveRight(View view) {
            if (column < 6) {
                column += 1;
            }
        updateTextViewCol();
        view.invalidate();  // for refreshment
        System.out.println("RIGHT");
    }

    public void placeMan(View view) {
        control.playMan(column);
        System.out.println("MAN PLAYED");
    }


    public void updateTextViewCol() {
        tvcol.setText(column);
    }

    public void updateTextViewCol(String str) {
        tvcol.setText(str);
    }

    public void updateTextViewBoard(String str) {
        tv.setText(str);
    }
}