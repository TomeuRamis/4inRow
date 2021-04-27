package com.example.fourinarow;

public class Controller extends Thread {

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

    public Controller(MainActivity main, int width, int height) {
        board = new Board(width, height);
        this.main = main;
    }

    @Override
    public void run() {
        /*if(ia != null && ia.getState() != State.TERMINATED){
            ia.interrupt();
        }*/
        System.out.println("Game start!");
        startGame();
        System.out.println("Game end");
    }

    public void startGame() {
        main.updateTextViewBoard(board.toString());
        assignTeams();
        ia = new IA(this.board, this, this.manIA);
        //main.doneLoad();

        loading  = false;

        gameLoop();

        main.updateTextViewBoard(board.toString());
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

    public void gameLoop() {
        while (!gameOver) {
            if (devmode) {
                turnPlayer();
            } else {
                if (playingMan == manPlayer) {
                    main.updateTextViewState("Make a move!");
                    turnPlayer();
                } else {
                    main.updateTextViewState("IA's turn. Wait.");
                    turnIA();
                }
            }
        }
        main.updateTextViewState("Game over!");
    }

    public void turnPlayer() {
        while (devmode || playingMan == manPlayer) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void turnIA() {
        while (playingMan == manIA) {
            try {
                this.ia.play();
            }catch(Exception e){
                System.err.println(e.getMessage());
            }
        }
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
        main.updateTextViewBoard(board.toString());
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
