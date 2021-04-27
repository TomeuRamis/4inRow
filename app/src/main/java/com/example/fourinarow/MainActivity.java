package com.example.fourinarow;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    int column = 0;

    private Controller control;

    private ImageView backgroundLoading;
    private ProgressBar spinner;
    private TextView tv;
    private TextView tvcol;
    private TextView tvstate;

    private Button bplacen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backgroundLoading = (ImageView) findViewById(R.id.imageViewBackgroundLoading);
        spinner = (ProgressBar)findViewById(R.id.progressBar);
        backgroundLoading.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);

        tv = (TextView) findViewById(R.id.textView);
        tv.setText("hola");
        tvcol = (TextView) findViewById(R.id.textViewColumnIndicator);
        tvcol.setText("       ^ ");
        tvstate = (TextView) findViewById(R.id.textView2);
        tvstate.setText("Begin playing");

        bplacen = (Button) findViewById(R.id.buttonPlace);

        control = new Controller(this, 7, 6);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        spinner.setVisibility(View.GONE);
        fadeOutAndHideImage(backgroundLoading);
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
        if (column < control.getWidth() - 1) {
            column += 1;
        }
        updateTextViewCol();
        view.invalidate();  // for refreshment
        System.out.println("RIGHT");
    }

    public void placeMan(View view) {
        try {
            control.playerTryPlayMan(column);
            System.out.println("MAN PLAYED");
        } catch (ColumnFullException e) {
            updateTextViewState("This column is full");
        }
    }

    public void restart(View view) {
        this.column = 0;
        setDevMode(false);
        control = new Controller(this, 7, 6);
        control.start();
        tvcol.setText("       ^ ");
        tvstate.setText("Game restarted.");
    }

    public void toggleDevMode(View view) {
        this.control.toggleDevMode();
    }

    public void setDevMode(Boolean devmode) {
        Switch swt = this.findViewById(R.id.switch1);
        swt.setChecked(devmode);
    }

    public void updateTextViewCol() {
        String aux = "       ";
        for (int i = 0; i < column; i++) {
            aux += "        ";
        }
        aux += " ^ ";
        tvcol.setText(aux);
    }

    public void updateTextViewState(String str) {
        tvstate.setText(str);
    }

    public void updateTextViewBoard(String str) {
        tv.setText(str);
    }

    private void fadeOutAndHideImage(final ImageView img)
    {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(1000);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                img.setVisibility(View.GONE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        img.startAnimation(fadeOut);
    }
}