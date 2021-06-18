package com.example.fourinarow;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.view.SurfaceHolder;

import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Random;

public class Controller {

    Board board;
    Boolean devmode;
    int turn, scorePlayer, scoreIA;
    Boolean gameOver;

    IA ia;
    boolean loading = true;

    Boolean blackPlays = true;
    Man playingMan = Man.BLACK;
    Man manPlayer;
    Man manIA;

    int[][] inRow;
    Man winner;

    //private Typeface fontJoc;
    private Context context;
    private SharedPreferences sharedPref;
    private int screenWidth;
    private int screenHeight;
    private Canvas canvas;
    private GameView myView;

    // Dimensions of the ideal screen, to fit any other resolution
    private final int modelViewX = 1080;
    private final int modelViewY = 2060;
    private final int modelCenterX = modelViewX / 2;
    private final int modelCenterY = modelViewY / 2;

    // Drawing Ratio for this screen
    private float ratioH = 1;
    private float ratioV = 1;
    private float ratio;

    private Paint paint;
    private Point center;
    private Image blackMan, whiteMan, blankMan, transMan, boardimg, background, score;
    private Typeface fontJoc;
    private Button replay, back;

    private boolean playAnimation;
    private ArrayList<Animation> animationQ;
    private Animation scoreAnim;
    private int scoreX, scoreY, scoreW, scoreH;

    private int boardx1, boardx2, boardy1, boardy2, topbar;
    private int fingerPosX, FingerPosY;

    public Controller(Context context, GameView m, int width, int height, int screenWidth, int screenHeight) {

        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        devmode = sharedPref.getBoolean("devmode", false);
        turn = 0;
        scorePlayer = 0;
        scoreIA = 0;
        gameOver = false;
        board = new Board(width, height);

        fontJoc = ResourcesCompat.getFont(context, R.font.agroundedboldbook);

        this.context = context;
        this.myView = m;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        // Ratio to fit the modelView resolution to this screen. For fixed screen drawing or writing
        float ratio1 = modelViewX / (float) screenWidth;
        float ratio2 = modelViewY / (float) screenHeight;

        center = new Point(screenWidth / 2, screenHeight / 2);

        ratioH = 1 / ratio1;
        ratioV = 1 / ratio2;

        boardx1 = screenWidth / 15;
        boardx2 = boardx1 * 14;
        //boardy1 = screenHeight / 6 * 2;
        boardy1 = toScreenY(686);
        boardy2 = boardy1 + (int) Math.floor((boardx2 - boardx1) * (double) 600 / 700);
        topbar = toScreenY(200);
        scoreX = 165;
        scoreW = 750;
        scoreH = 375;

        blackMan = new Image(m, R.drawable.red_man);
        whiteMan = new Image(m, R.drawable.yellow_man);
        blankMan = new Image(m, R.drawable.white_man);
        boardimg = new Image(m, R.drawable.board);
        background = new Image(m, R.drawable.background);

        back = new Button(m, R.drawable.back, R.drawable.back, toScreenX(20), topbar / 3 + toScreenY(0), 200, 75);
        replay = new Button(m, R.drawable.replay2, R.drawable.replay1, screenWidth - 200, topbar / 3 - toScreenY(40), 150, 150);

        fingerPosX = -1;
        animationQ = new ArrayList<>();
        playAnimation = true;
        initGame();
    }

    public void initGame() {
        // initializations


        assignTeams(Integer.parseInt(sharedPref.getString("starting", "0")), Integer.parseInt(sharedPref.getString("consecutive", "1")));

        if (ia != null) {
            ia.gameover = true;
        }

        int depth;
        if (sharedPref.getBoolean("experimental", false)) {
            depth = 7;
        } else {
            depth = Integer.parseInt(sharedPref.getString("difficulty", "6"));
        }
        ia = new IA(this.board, this, this.manIA, depth);
        if (!devmode) {
            ia.start();
        } else {
            doneLoad();
        }
    }

    public void update(long fps) {
        if (devmode) {
            turnPlayer();
        } else {
            if (playingMan == manPlayer) {
                //main.updateTextViewState("Make a move!");
                turnPlayer();
            } else if (ia.getWaiting()) {
                //main.updateTextViewState("IA's turn. Wait.");
                turnIA();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void draw(SurfaceHolder holder) {

        if (holder.getSurface().isValid()) {
            //First we lock the area of memory we will be drawing to
            canvas = holder.lockCanvas();

            //draw a background
            //canvas.drawColor(Color.argb(255, 135, 206, 230));
            background.draw(canvas, 0, 0, screenWidth, screenHeight);

            // Unlock and draw the scene
            paint = new Paint();

            //Top bar
            paint.setColor(Color.argb(225, 71, 40, 255));
            canvas.drawRect(0, 0, screenWidth, topbar, paint);
            back.draw(canvas);

            //Mans
            paint.setColor(Color.argb(255, 135, 206, 230));
            int columnSpacing = toScreenX((int) 128);
            //int rowSpacing = toScreenY((int) 133);
            int rowSpacing = toScreenY((int) ((boardy2 - boardy1) / board.height)) - toScreenY(6);
            for (int i = 0; i < board.width; i++) {
                for (int j = board.height - 1; j >= 0; j--) {
                    //canvas.drawCircle(boardx1 + columnSpacing * i + columnSpacing / 2, boardy1 + rowSpacing * (board.height - j - 1) + rowSpacing / 2, 50, paint);
                    if (board.getSquare(j, i) != Man.EMPTY && !inAnimationQ(j, i)) {
                        if (board.getSquare(j, i) == Man.BLACK) {
                            blackMan.draw(canvas, boardx1 + columnSpacing * i + toScreenX(33), boardy1 + rowSpacing * (board.height - j - 1) + toScreenY(33), 104, 104);
                        } else {
                            whiteMan.draw(canvas, boardx1 + columnSpacing * i + toScreenX(33), boardy1 + rowSpacing * (board.height - j - 1) + toScreenY(33), 104, 104);
                        }
                    }
                }
            }

            //Play man animation
            if (playAnimation && !animationQ.isEmpty()) {
                Animation a;
                for (int i = 0; i < animationQ.size(); i++) {
                    a = animationQ.get(i);
                    if (a.isDone()) {
                        animationQ.remove(0);
                        a.getImage().draw(canvas, boardx1 + a.getCol() * columnSpacing + toScreenX(35), boardy1 + toScreenY(35) + rowSpacing * (board.height - 1 - a.getRow()), 100, 100);
                        i--;
                    } else {
                        double r;
                        double f = (double) (board.height - a.getRow() - 1) / (board.height - 1);
                        switch (a.getState()) {
                            case 0:
                                r = a.getCompletionRatio() * toScreenY(95);
                                a.getImage().draw(canvas, boardx1 + a.getCol() * columnSpacing + toScreenX(33), boardy1 + (int) r + toScreenY(-70), 104, 104);
                                break;
                            case 1:
                                r = a.getCompletionRatio() * rowSpacing * (board.height - 1 - a.getRow());
                                a.getImage().draw(canvas, boardx1 + a.getCol() * columnSpacing + toScreenX(33), boardy1 + (int) r + toScreenY(33), 104, 104);
                                break;
                            case 2:
                                r = a.getCompletionRatio() * -toScreenY(33) * f;
                                a.getImage().draw(canvas, boardx1 + a.getCol() * columnSpacing + toScreenX(33), boardy1 + (int) r + toScreenY(33) + rowSpacing * (board.height - 1 - a.getRow()), 104, 104);
                                break;
                            case 3:
                                r = (a.getCompletionRatio() * toScreenY(33)) * f;
                                a.getImage().draw(canvas, boardx1 + a.getCol() * columnSpacing + toScreenX(33), boardy1 + (int) r - (int) (toScreenY(33) * f - toScreenY(33)) + rowSpacing * (board.height - 1 - a.getRow()), 104, 104);
                                break;
                        }
                    }
                    a.newFrame();
                }
            }

            if (fingerPosX != -1) {
                transMan.filteredDraw(canvas, boardx1 + columnSpacing * fingerPosX + toScreenX(35), boardy1 + rowSpacing * (board.height - board.getTopPos(fingerPosX) - 2) + toScreenY(35), 100, 100, -10, 99);
            }

            //Draw board
            boardimg.draw(canvas, boardx1, boardy1, boardx2 - boardx1, boardy2 - boardy1);

            //long fps = myView.getFPS();
            //drawCenteredText(canvas, "FPS: " + fps, 80, Color.RED, 500);

            if (gameOver) {
                replay.draw(canvas);
                drawCenteredText(canvas, "GAME OVER", 80, Color.rgb(255, 255, 255), topbar - toScreenY(50));
                if (animationQ.isEmpty()) {
                    //draw the score board
                    if (scoreAnim != null) {
                        if (scoreAnim.isDone()) {
                            scoreY = 275;
                        } else {
                            double r;
                            switch (scoreAnim.getState()) {
                                case 0:
                                    //scoreY = -350 + (int) (toScreenY(350) * scoreAnim.getCompletionRatio());
                                    scoreY = -500;
                                    break;
                                case 1:
                                    scoreY = -400 + (int) (toScreenY(311) * scoreAnim.getCompletionRatio());
                                    break;
                                case 2:
                                    r = scoreAnim.getCompletionRatio() * -toScreenY(75);
                                    scoreY = toScreenY(311) + (int) r;
                                    break;
                                case 3:
                                    r = (scoreAnim.getCompletionRatio() * toScreenY(39));
                                    scoreY = toScreenY(236) + (int) r;
                                    break;
                            }
                            scoreAnim.newFrame();
                        }
                        scoreAnim.getImage().draw(canvas, toScreenX(scoreX), toScreenY(scoreY), toScreenX(scoreW), toScreenY(scoreH));
                        drawCenteredText(canvas, "SCORE", 80, Color.rgb(255, 255, 255), toScreenY(scoreY + 125));
                        drawText(canvas, "player", 80, Color.rgb(255, 255, 255), toScreenX(scoreX + 175), toScreenY(scoreY + 225));
                        drawText(canvas, "IA", 80, Color.rgb(255, 255, 255), toScreenX(scoreX + 575), toScreenY(scoreY + 225));
                        drawText(canvas, Integer.toString(scorePlayer), 80, Color.rgb(255, 255, 255), toScreenX(scoreX + 175), toScreenY(scoreY + 325));
                        drawText(canvas, Integer.toString(scoreIA), 80, Color.rgb(255, 255, 255), toScreenX(scoreX + 575), toScreenY(scoreY + 325));
                    }
                    //mark the 4 in a row
                    paint.setColor(Color.argb(200, 255, 255, 255));
                    if (inRow != null) {
                        for (int i = 0; i < this.inRow.length; i++) {
                            if (winner == Man.BLACK) {
                                canvas.drawCircle(boardx1 + columnSpacing * inRow[i][1] + (int) (columnSpacing * 0.5) + toScreenX(22), boardy1 + rowSpacing * (board.height - inRow[i][0] - 1) + (int) (rowSpacing * 0.5) + toScreenY(22), 58, paint);
                                blackMan.draw(canvas, boardx1 + columnSpacing * inRow[i][1] + toScreenX(33), boardy1 + rowSpacing * (board.height - inRow[i][0] - 1) + toScreenY(33), 104, 104);
                            } else {
                                canvas.drawCircle(boardx1 + columnSpacing * inRow[i][1] + (int) (columnSpacing * 0.5) + toScreenX(22), boardy1 + rowSpacing * (board.height - inRow[i][0] - 1) + (int) (rowSpacing * 0.5) + toScreenY(22), 58, paint);
                                whiteMan.draw(canvas, boardx1 + columnSpacing * inRow[i][1] + toScreenX(33), boardy1 + rowSpacing * (board.height - inRow[i][0] - 1) + toScreenY(33), 104, 104);
                            }
                        }
                    }
                }
            } else {
                String txt = "";
                if (turn == 0) {
                    if (playingMan == this.manIA && !devmode) {
                        txt = "IA opens";
                    } else {
                        txt = "Play!";
                    }
                } else {
                    txt = "turn " + this.turn;
                }
                drawCenteredText(canvas, txt, 80, Color.rgb(255, 255, 255), topbar - toScreenY(50));
            }
            holder.unlockCanvasAndPost(canvas);
        }
    }


    public void handleInput(int x, int y) {
        if (loading) {
            return;
        }

        if (gameOver && replay.contains(x, y)) {
            replay.select();
        }

        if (back.contains(x, y)) {
            ((MainActivity) context).onBackPressed();
        }

        //Board
        if (x > boardx1 && x < boardx2 && y > boardy1 && y < boardy2 && !gameOver) {
            if (this.playingMan == this.manPlayer || devmode) {
                try {
                    int columnSpacing = (boardx2 - boardx1) / board.width;
                    //playerTryPlayMan((x-boardx1) / columnSpacing);
                    fingerPosX = (x - boardx1) / columnSpacing;
                } catch (Exception e) {
                    System.err.println("x: " + x + ", y: " + y);
                }
            }
        }

    }

    public void handleStopInput(int x, int y) {
        if (loading) {
            return;
        }

        if (gameOver && replay.contains(x, y) && replay.isSelected()) {
            replay.unSelect();
            replay();
        } else if (gameOver && !replay.contains(x, y) && replay.isSelected()) {
            replay.unSelect();
        }

        //Board
        if (x > boardx1 && x < boardx2 && y > boardy1 && y < boardy2 && !gameOver) {
            if (this.playingMan == this.manPlayer || devmode) {
                try {
                    int columnSpacing = (boardx2 - boardx1) / board.width;
                    playerTryPlayMan((x - boardx1) / columnSpacing);
                    fingerPosX = -1;
                    //fingerPosX = (x-boardx1) / columnSpacing;
                } catch (ColumnFullException e) {
                    System.err.println("x: " + x + ", y: " + y);
                }
            }
        } else {
            fingerPosX = -1;
        }
    }

    public void replay() {
        turn = 0;
        board = new Board(this.board.width, this.board.height);
        resetAniamtionQ();
        scoreAnim = null;
        playingMan = Man.BLACK;
        blackPlays = true;
        initGame();
        gameOver = false;
    }

    public void drawCenteredText(Canvas canvas, String txt, int size, int color, int pos) {
        Paint paintText = new Paint();
        paintText.setTypeface(fontJoc);
        paintText.setTextSize(toScreenX(size));

        // Centrat horitzontal
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setColor(color);
        paintText.setAlpha(200);

        int xPos = modelViewX / 2;
        int yPos = (int) (pos + ((paintText.descent() + paintText.ascent())) / 4);
        //int yPos = pos;
        canvas.drawText(txt, toScreenX(xPos), toScreenY(yPos), paintText);
    }

    public void drawText(Canvas canvas, String txt, int size, int color, int x, int y) {
        Paint paintText = new Paint();
        paintText.setTypeface(fontJoc);
        paintText.setTextSize(toScreenX(size));

        // Centrat horitzontal
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setColor(color);
        paintText.setAlpha(200);

        int xPos = x;
        int yPos = (int) (y + ((paintText.descent() + paintText.ascent())) / 4);
        //int yPos = pos;
        canvas.drawText(txt, toScreenX(xPos), toScreenY(yPos), paintText);
    }

    private int toScreenX(int in) {
        return (int) (in * ratioH);
    }

    private int toScreenY(int in) {
        return (int) (in * ratioV);
    }

    private int fromScreenX(int in) {
        return (int) (in / ratioH);
    }

    private int fromScreenY(int in) {
        return (int) (in / ratioV);
    }


    public void toggleDevMode() {
        if (!gameOver) {
            devmode = !devmode;
        }
    }

    public void assignTeams(int starting, int consecutive) {

        if (winner != null) {
            switch (consecutive) {
                case 0:
                    if (winner == Man.BLACK) {
                        if (manPlayer == Man.BLACK) {
                            manPlayer = Man.BLACK;
                            manIA = Man.WHITE;
                        } else {
                            manPlayer = Man.WHITE;
                            manIA = Man.BLACK;
                        }
                    } else {
                        if (manPlayer == Man.WHITE) {
                            manPlayer = Man.BLACK;
                            manIA = Man.WHITE;
                        } else {
                            manPlayer = Man.WHITE;
                            manIA = Man.BLACK;
                        }
                    }
                    break;
                case 1:
                    if (winner == Man.BLACK) {
                        if (manPlayer == Man.BLACK) {
                            manPlayer = Man.WHITE;
                            manIA = Man.BLACK;
                        } else {
                            manPlayer = Man.BLACK;
                            manIA = Man.WHITE;
                        }
                    } else {
                        if (manPlayer == Man.WHITE) {
                            manPlayer = Man.WHITE;
                            manIA = Man.BLACK;
                        } else {
                            manPlayer = Man.BLACK;
                            manIA = Man.WHITE;
                        }
                    }
                    break;

                case 2:
                    Random ran = new Random(10);
                    if (ran.nextInt(10) % 2 == 0) {
                        manPlayer = Man.BLACK;
                        manIA = Man.WHITE;
                    } else {
                        manPlayer = Man.WHITE;
                        manIA = Man.BLACK;
                    }
                    break;

            }
        } else {
            switch (starting) {
                case 0:
                    manPlayer = Man.BLACK;
                    manIA = Man.WHITE;
                    transMan = blackMan;
                    break;
                case 1:
                    manPlayer = Man.WHITE;
                    manIA = Man.BLACK;
                    transMan = whiteMan;
                    break;
                case 2:
                    Random ran = new Random(10);
                    if (ran.nextInt(10) % 2 == 0) {
                        manPlayer = Man.BLACK;
                        manIA = Man.WHITE;
                        transMan = blackMan;
                    } else {
                        manPlayer = Man.WHITE;
                        manIA = Man.BLACK;
                        transMan = whiteMan;
                    }
                    break;
            }

        }
        if (manPlayer == Man.BLACK) {
            transMan = blackMan;
        } else {
            transMan = whiteMan;
        }

    }

    public void turnPlayer() {
        //while (devmode || playingMan == manPlayer) {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //}
    }

    public void turnIA() {
        //while (playingMan == manIA) {
        try {
            ia.setWaiting(false);
        } catch (IllegalThreadStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            ;
        }
        //}
    }

    /* Increase counter of turns.
    Invert blackplays, indicating that its the other player's turn.
    And set the values of PlayingMan accordingly.
    Update text view */
    private void newTurn() {
        turn += 1;
        blackPlays = !blackPlays;

        Man aux;
        if (blackPlays) {
            playingMan = Man.BLACK;
        } else {
            playingMan = Man.WHITE;
        }
        //main.updateTextViewBoard(board.toString());
    }

    /* Plays a man on a column.
    Throws ColumnFullException, so it has to be handled individually.
    Ends the game when GameOverException.*/
    public void playMan(int col) throws ColumnFullException {
        try {
            board.playMan(col, playingMan);
            if (playingMan == Man.BLACK) {
                animationQ.add(new Animation(blackMan, col, board.getTopPos(col)));
            } else {
                animationQ.add(new Animation(whiteMan, col, board.getTopPos(col)));
            }
            newTurn();
        } catch (GameOverException e) {
            gameOver = true;
            ia.gameover();
            this.winner = e.winner();
            this.inRow = e.getInRow();
            if (playingMan == Man.BLACK) {
                animationQ.add(new Animation(blackMan, col, board.getTopPos(col)));
            } else {
                animationQ.add(new Animation(whiteMan, col, board.getTopPos(col)));
            }
            //Add the score
            if (winner == manPlayer) {
                scorePlayer += 1;
            } else if (winner == manIA) {
                scoreIA += 1;
            } //If the winner is EMPTY it means it's a tie
            scoreAnim = new Animation(new Image(myView, R.drawable.score), -1, -1);
        }


    }

    public void playerTryPlayMan(int col) throws ColumnFullException {
        if (devmode || playingMan == manPlayer) {
            playMan(col);
        }
    }

    public void IATryPlayMan(int col) throws ColumnFullException {
        if (!devmode && playingMan == manIA) {
            playMan(col);
            ia.setWaiting(true);
        }
    }

    public void setResume() {
    }

    public void saveStatus() {
    }

    public void recoverStatus() {
    }

    public int getWidth() {
        return this.board.width;
    }

    public int getHeight() {
        return this.board.height;
    }

    public Man getSquare(int col, int row) {
        return board.getSquare(row, col);
    }

    public boolean inAnimationQ(int row, int col) {
        boolean found = false;
        for (int i = 0; i < animationQ.size(); i++) {
            if (animationQ.get(i).getRow() == row && animationQ.get(i).getCol() == col) {
                found = true;
                break;
            }
        }
        return found;
    }

    public void doneLoad() {
        loading = false;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void loadinganim(SurfaceHolder holder, int alpha) {
        if (holder.getSurface().isValid()) {
            //First we lock the area of memory we will be drawing to
            canvas = holder.lockCanvas();

            if (alpha < 255) {
                background.draw(canvas, 0, 0, screenWidth, screenHeight);
                //Draw board
                boardimg.draw(canvas, boardx1, boardy1, boardx2 - boardx1, boardy2 - boardy1);
            }

            //draw a background
            canvas.drawColor(Color.argb(alpha, 0, 0, 0));

            // Unlock and draw the scene
            paint = new Paint();
            paint.setColor(Color.argb(alpha, 255, 255, 255));
            canvas.drawRoundRect(modelCenterX - toScreenX(250), modelCenterY - toScreenY(200), modelCenterX + toScreenX(250), modelCenterY + toScreenY(200), 20, 20, paint);
            paint.setColor(Color.argb(alpha, 0, 0, 0));
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 3; j++) {
                    canvas.drawCircle(modelCenterX - toScreenX(190) + (int) (toScreenX(125) * i), modelCenterY - toScreenY(140) + (int) (toScreenY(133) * j), 50, paint);
                }
            }

            //Play man animation
            paint.setColor(Color.argb(alpha, 255, 255, 255));
            if (playAnimation && !animationQ.isEmpty()) {
                for (int i = 0; i < animationQ.size(); i++) {
                    Animation a = animationQ.get(i);
                    Random ran = new Random();
                    if (a.getState() == 40) {
                        canvas.drawCircle(modelCenterX + a.getCol() * toScreenX(125) - toScreenX(190), modelCenterY - toScreenY(140) + toScreenY(133) * (2 - a.getRow()), 48, paint);
                        a.setState(0);
                    } else {
                        canvas.drawCircle(modelCenterX + a.getCol() * toScreenX(125) - toScreenX(190), modelCenterY - toScreenY(140) + (int) (toScreenY(133) * (2 - a.getRow()) * (double) a.getState() / 40), 48, paint);
                    }
                    a.setState(a.getState() + 1);
                }

            } else {
                Animation a = new Animation(blackMan, 0, 0);
                a.setState(30);
                animationQ.add(a);
                a = new Animation(blackMan, 1, 0);
                a.setState(20);
                animationQ.add(a);
                a = new Animation(blackMan, 2, 0);
                a.setState(10);
                animationQ.add(a);
                a = new Animation(blackMan, 3, 0);
                a.setState(0);
                animationQ.add(a);
            }
            holder.unlockCanvasAndPost(canvas);
        }
    }

    public void resetAniamtionQ() {
        animationQ = new ArrayList<>();
    }
}
