package com.example.fourinarow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.view.SurfaceHolder;

import androidx.annotation.RequiresApi;

public class Controller {

    Board board;
    MainActivity main;
    Boolean loading = true;
    Boolean devmode = false;
    int turn = 0;
    Boolean gameOver = false;

    IA ia;
    Thread iaThread;

    Boolean blackPlays = true;
    Man playingMan = Man.BLACK;
    Man manPlayer = null;
    Man manIA = null;

    int[][] inRow;
    Man winner;

    //private Typeface fontJoc;
    private Context context;
    private int screenWidth;
    private int screenHeight;
    private Canvas canvas;
    private GameView myView;

    // Dimensions of the ideal screen, to fit any other resolution
    private int modelViewX = 1583;
    private int modelViewY = 1080;
    private int modelCenterX = modelViewX / 2;
    private int modelCenterY = modelViewY / 2;

    // Drawing Ratio for this screen
    private float ratioH = 1;
    private float ratioV = 1;
    private float ratio;

    private Paint paint;
    private Point center;
    private Image imatge, blackMan, whiteMan, blankMan;
    private Button helpButton;
    private int animation = 0;
    private int animationIncrease = 1;
    private final int animationMax = 10;

    private int boardx1, boardx2, boardy1, boardy2;
    private int fingerPosX, FingerPosY;

    public Controller(Context context, GameView m, int width, int height, int screenWidth, int screenHeight) {
        board = new Board(width, height);
        this.main = main;

        //fontJoc = Typeface.createFromAsset(context.getAssets(), "fonts/arkitechbold.ttf");

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

        blackMan = new Image(m, R.drawable.red_man);
        whiteMan = new Image(m, R.drawable.yellow_man);
        blankMan = new Image(m, R.drawable.white_man);
        helpButton = new Button(m, R.drawable.helpon, R.drawable.helpoff, 300, 500, 400, 200);

        boardx1 = screenWidth / 15;
        boardx2 = boardx1 * 14;
        boardy1 = screenHeight / 4;
        boardy2 = boardy1 * 3;

        initGame();
    }

    public void initGame() {
        // inicialitzacions
        assignTeams();
        ia = new IA(this.board, this, this.manIA);
        //main.doneLoad();
    }

    public void update(long fps) {
        if (devmode) {
            turnPlayer();
        } else {
            if (playingMan == manPlayer) {
                //main.updateTextViewState("Make a move!");
                turnPlayer();
            } else {
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

            //draw a background color
            canvas.drawColor(Color.argb(255, 135, 206, 230));

            // Unlock and draw the scene
            paint = new Paint();
            paint.setColor(Color.argb(255, 0, 0, 128));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(boardx1, boardy1, boardx2, boardy2, 50, 50, paint);

            paint.setColor(Color.argb(255, 135, 206, 230));
            //paint.setStyle(Paint.Style.FILL);
            int columnSpacing = (boardx2 - boardx1) / board.width;
            int rowSpacing = (boardy2 - boardy1) / board.height;
            for (int i = 0; i < board.width; i++) {
                for (int j = board.height - 1; j >= 0; j--) {
                    canvas.drawCircle(boardx1 + columnSpacing * i + columnSpacing / 2, boardy1 + rowSpacing * (board.height - j - 1) + rowSpacing / 2, 50, paint);
                    if (board.getSquare(j, i) != Man.EMPTY) {
                        if (board.getSquare(j, i) == Man.BLACK) {
                            blackMan.draw(canvas, boardx1 + columnSpacing * i + this.toScreenX(23), boardy1 + rowSpacing * (board.height - j - 1) + this.toScreenY(18), 100, 100);
                        } else {
                            whiteMan.draw(canvas, boardx1 + columnSpacing * i + this.toScreenX(23), boardy1 + rowSpacing * (board.height - j - 1) + this.toScreenY(18), 100, 100);
                        }
                    }
                }
            }
            if (fingerPosX != -1) {
                blackMan.filteredDraw(canvas, boardx1 + columnSpacing * fingerPosX + this.toScreenX(23), boardy1 + rowSpacing * (board.height - board.getTopPos(fingerPosX) - 2) + this.toScreenY(10), 100, 100, -10, 99);
            }
            //imatge.draw(canvas, 300,800,200,324);
            //helpButton.draw(canvas);

            long fps = myView.getFPS();
            drawCenteredText(canvas, "FPS: " + fps, 80, Color.RED, 500);

            if (gameOver) {
                drawCenteredText(canvas, "GAME OVER", 80, Color.RED, 200);
                for (int i = 0; i < this.inRow.length; i++) {
                    if (winner == Man.BLACK) {
                        blackMan.filteredDraw(canvas, boardx1 + columnSpacing * inRow[i][1] + this.toScreenX(23), boardy1 + rowSpacing * (board.height - inRow[i][0] - 1) + this.toScreenY(18), 100, 100, animation, 100);
                    } else {
                        whiteMan.filteredDraw(canvas, boardx1 + columnSpacing * inRow[i][1] + this.toScreenX(23), boardy1 + rowSpacing * (board.height - inRow[i][0] - 1) + this.toScreenY(18), 100, 100, animation, 100);
                    }
                }
            }

            if(animation >= animationMax){
                animationIncrease = -1;
            }else if(animation <= -animationMax){
                animationIncrease = 1;
            }
            animation = animation + animationIncrease;


            holder.unlockCanvasAndPost(canvas);
        }
    }


    public void handleInput(int x, int y) {
        //
        if(gameOver){

        }

        //Board
        if (x > boardx1 && x < boardx2 && y > boardy1 && y < boardy2) {
            if (this.playingMan == this.manPlayer) {
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
        //Board
        if (x > boardx1 && x < boardx2 && y > boardy1 && y < boardy2) {
            if (this.playingMan == this.manPlayer) {
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

    public void drawCenteredText(Canvas canvas, String txt, int size, int color, int pos) {
        Paint paintText = new Paint();
        //paintText.setTypeface(fontJoc);
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

    public void run() {
        /*if(ia != null && ia.getState() != State.TERMINATED){
            ia.interrupt();
        }*/
        System.out.println("Game start!");
        startGame();
        System.out.println("Game end");
    }

    public void startGame() {
        //main.updateTextViewBoard(board.toString());
        assignTeams();
        ia = new IA(this.board, this, this.manIA);
        //main.doneLoad();

        loading = false;

        //main.updateTextViewBoard(board.toString());
    }

    public void toggleDevMode() {
        if (!gameOver) {
            devmode = !devmode;
        }
    }

    public void assignTeams() {
        /* //Not for now
        Random ran = new Random(10);
        if(ran.nextInt(10)%2 == 0){
            player = Man.BLACK;
            ia = Man.WHITE;
        } else{
            player = Man.WHITE;
            ia = Man.BLACK;
        }
        */
        manPlayer = Man.BLACK;
        manIA = Man.WHITE;
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
            this.ia.play();
        } catch (Exception e) {
            e.printStackTrace();;
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
            newTurn();
        } catch (GameOverException e) {
            gameOver = true;
            this.winner = e.winner();
            this.inRow = e.getInRow();
            //newTurn();
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
}
