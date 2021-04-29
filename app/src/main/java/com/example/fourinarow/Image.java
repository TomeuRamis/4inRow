package com.example.fourinarow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;

public class Image
{
    private Bitmap bmp;
    private int x;
    private int y;
    private boolean visible;
    private GameView view;
    private int width;

    private int height;  // de la imatge
    private int dibuix;
    private int posX;
    private int posY;
    private int ampla;   // de la instància
    private int alt;     // de la instància
    private int alpha;   // nivell de transparència
    private boolean selected;

    public Image(GameView g, int dib)
    {
        selected = false;
        dibuix = dib;
        alpha = 255;  // totalment opac
        visible = true;
        view = g;
        bmp = BitmapFactory.decodeResource(view.getResources(), dibuix);
        this.width = bmp.getWidth();
        this.height = bmp.getHeight();
    }

    public void draw(Canvas canvas, int x, int y, int w, int h)
    {
        if (!visible) return;

        Rect src = new Rect(0, 0, width, height);
        Rect dst = new Rect(x, y, x+w+1, y+h+1);
        Paint paint = new Paint();
        paint.setAlpha(alpha);
        canvas.drawBitmap(bmp, src, dst, paint);
        posX = x;
        posY = y;
        ampla = w;
        alt = h;

        if (selected)
        {
            paint.setColor(Color.RED);
            paint.setStrokeWidth(5);
            //paint.setStyle(Paint.Style.STROKE);
            paint.setAlpha(125);
            canvas.drawRect(dst,paint);
            //filteredDraw(canvas, x, y, w, h, Color.GREEN, 255);
        }
    }

    public void filteredDraw(Canvas canvas, int x, int y, int w, int h, int c, int transparencia)
    {
        if (!visible) return;

        int red = (c & 0xFF0000) / 0xFFFF;
        int green = (c & 0xFF00) / 0xFF;
        int blue = c & 0xFF;

        float[] colorTransform = {
                0, 1f, 0, 0, red,
                0, 0, 0f, 0, green,
                0, 0, 0, 0f, blue,
                0, 0, 0, 1f, 0};

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0f); //Remove Colour
        colorMatrix.set(colorTransform); //Apply the Red
        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);

        Rect src = new Rect(0, 0, width, height);
        Rect dst = new Rect(x, y, x + w, y + h);
        Paint paint = new Paint();

        canvas.drawBitmap(bmp, src, dst, paint);
        paint.setAlpha(transparencia);
        paint.setColorFilter(colorFilter);
        canvas.drawBitmap(bmp, src, dst, paint);
        posX = x;
        posY = y;
        ampla = w;
        alt = h;
    }

    public boolean contains(float x2, float y2) {
        return (x2 > posX && x2 < posX + ampla) && (y2 > posY && y2 < posY + alt);
    }

    public void select()
    {
        selected = true;
    }

    public void unselect()
    {
        selected = false;
    }

    public boolean selected() {return selected;};

    public void setPosition(int a, int b)
    {
        x = a;
        y = b;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean v)
    {
        visible = v;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }
}