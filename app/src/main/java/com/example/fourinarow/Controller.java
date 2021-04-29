package com.example.fourinarow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.view.SurfaceHolder;

public class Controller{

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

    //private Typeface fontJoc;
    private Context context;
    private int screenWidth;
    private int screenHeight;
    private Canvas canvas;
    private GameView myView;

    // Dimensions of the ideal screen, to fit any other resolution
    private int modelViewX = 1583;
    private int modelViewY = 1080;
    private int modelCenterX = modelViewX/2;
    private int modelCenterY = modelViewY/2;

    // Drawing Ratio for this screen
    private float ratioH = 1;
    private float ratioV = 1;
    private float ratio;

    private Paint paint;
    private Point center;
    private Image imatge;
    private Button helpButton;

    private int posX=0, posY=0;

    public Controller(Context context, GameView m, int width, int height, int screenWidth, int screenHeight) {
        board = new Board(width, height);
        this.main = main;

        //fontJoc = Typeface.createFromAsset(context.getAssets(), "fonts/arkitechbold.ttf");

        this.context = context;
        this.myView = m;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        // Ratio to fit the modelView resolution to this screen. For fixed screen drawing or writing
        float ratio1 = modelViewX/(float) screenWidth;
        float ratio2 = modelViewY/(float) screenHeight;

        center = new Point(screenWidth/2, screenHeight/2);

        ratioH = 1/ratio1;
        ratioV = 1/ratio2;

        imatge = new Image(m, R.drawable.red_man);
        helpButton = new Button(m, R.drawable.helpon, R.drawable.helpoff, 300, 500, 400, 200);

        initGame();
    }

    public void initGame()
    {
        // inicialitzacions
        assignTeams();
        ia = new IA(this.board, this, this.manIA);
        //main.doneLoad();

        loading  = false;
    }

    public void update(long fps)
    {
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

    public void draw(SurfaceHolder holder)
    {

        if (holder.getSurface().isValid()) {
            //First we lock the area of memory we will be drawing to
            canvas = holder.lockCanvas();

            //draw a background color
            canvas.drawColor(Color.argb(255, 135, 206, 230));

            // Unlock and draw the scene
            paint = new Paint();
            paint.setColor(Color.argb(255, 0,0,128));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0,100,screenWidth,screenHeight,paint);

            paint.setColor(Color.argb(255, 135, 206, 230));
            paint.setStyle(Paint.Style.FILL);
            for(int i = 0; i < board.width; i++) {
                canvas.drawCircle((screenWidth/board.width)*i, screenHeight/board.height, 50, paint);
            }
            //imatge.draw(canvas, 300,800,200,324);
            //helpButton.draw(canvas);

            long fps = myView.getFPS();
            drawCenteredText(canvas, "FPS: "+fps, 80, Color.RED, 500);

            holder.unlockCanvasAndPost(canvas);
        }
    }


    public void handleInput(int x, int y)
    {
        Point p = new Point(fromScreenX(x), fromScreenY(y));

        if (imatge.contains(p.x,p.y))
        {
            if (imatge.selected()) imatge.unselect();
            else imatge.select();
        }

    }
    public void handleStopInput(int x, int y)
    {
        if (helpButton.contains(x, y)) {
            helpButton.toggle();
        }
    }

    public void drawCenteredText(Canvas canvas, String txt, int size, int color, int pos)
    {
        Paint paintText = new Paint();
        //paintText.setTypeface(fontJoc);
        paintText.setTextSize(toScreenX(size));

        // Centrat horitzontal
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setColor(color);
        paintText.setAlpha(200);

        int xPos = modelViewX / 2;
        //int yPos = (int) (pos + ((paintText.descent() + paintText.ascent()))/4) ;
        int yPos = pos;
        canvas.drawText(txt, toScreenX(xPos), toScreenY(yPos), paintText);
    }
    private int toScreenX(int in)
    {
        return (int)(in*ratioH);
    }
    private int toScreenY(int in)
    {
        return (int)(in*ratioV);
    }
    private int fromScreenX(int in)
    {
        return (int)(in/ratioH);
    }
    private int fromScreenY(int in)
    {
        return (int)(in/ratioV);
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

        loading  = false;

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
                //this.ia.play();
            }catch(Exception e){
                System.err.println(e.getMessage());
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
            newTurn();
        }
    }

    public void playerTryPlayMan(int col) throws ColumnFullException{
        if(devmode || playingMan == manPlayer){
            playMan(col);
        }
    }

    public void IATryPlayMan(int col) throws ColumnFullException{
        if(!devmode && playingMan == manIA){
            playMan(col);
        }
    }

    public void setResume()
    {
    }
    public void saveStatus()
    {
    }
    public void recoverStatus()
    {
    }

    public int getWidth() {
        return this.board.width;
    }
    public int getHeight() {
        return this.board.height;
    }
    public Man getSquare(int col, int row){
        return board.getSquare(row, col);
    }
}
