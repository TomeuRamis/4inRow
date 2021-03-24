package com.example.fourinarow;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    int column = 0;
    Controller control;

    TextView tv;
    TextView tvcol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.textView);
        tv.setText("hola");
        tvcol = (TextView) findViewById(R.id.textView2);
        tvcol.setText("0");

        control = new Controller(this, 7, 6);

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
        if (column < control.getWidth()-1) {
            column += 1;
        }
        updateTextViewCol();
        view.invalidate();  // for refreshment
        System.out.println("RIGHT");
    }

    public void placeMan(View view) {
        try {
            control.playMan(column);
            System.out.println("MAN PLAYED");
        } catch (ColumnFullException e) {
            updateTextViewCol("This column is full");
        }
    }

    public void restart(View view){
        this.column = 0;
        setDevMode(false);
        control = new Controller(this, 7, 6);
        control.start();
    }

    public void toggleDevMode(View view){
        this.control.toggleDevMode();
    }

    public void setDevMode(Boolean devmode){
        Switch swt = this.findViewById(R.id.switch1);
        swt.setChecked(devmode);
    }

    public void updateTextViewCol() {
        tvcol.setText(Integer.toString(column));
    }

    public void updateTextViewCol(String str) {
        tvcol.setText(str);
    }

    public void updateTextViewBoard(String str) {
        tv.setText(str);
    }
}