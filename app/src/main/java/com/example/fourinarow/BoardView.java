package com.example.fourinarow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

public class BoardView extends View {

    Paint paint;

    float width = this.getWidth();
    float height = this.getHeight();

    public BoardView(Context context) {
        super(context);
        init();
    }

    public void init(){
        paint = new Paint();

        float xpad = (float) (getPaddingLeft()+getPaddingRight());
        float ypad = (float)(getPaddingTop() + getPaddingBottom());


        float w = 7/width;
        float h = 6/height;

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas){
        super .onDraw(canvas);
        paint.setColor(Color.BLACK);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.red_man);
        canvas.drawBitmap(bitmap, 0,0,paint);
    }

}
