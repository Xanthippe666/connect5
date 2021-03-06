
import javax.imageio.ImageIO;
import javax.swing.*;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.EventListener;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.regex.*;

public class game extends JFrame implements MouseListener, MouseMotionListener,ActionListener {
    public static final int LINES = 15;
    public static final int SIZEX = 600;
    public static final int SIZEY = 600;
    public static final int SPACE = 50;
    public static final int PIECERADIUS = 30;

    public static boolean blackTurn;
    public static int gameState = 0;

    public static LinkedList<pieces> gamePieces;
    public static specialPiece mousePiece;

    public static Random randomDefiniteSeed = new Random();

    public static Semaphore s = new Semaphore(0);

    public static void main(String[] args) throws InterruptedException{
        new game();
    }

    public game() throws InterruptedException{
        this.setLayout(null);
        this.setBounds(0,0,SIZEX,SIZEY);
        //this.getContentPane().setBackground(Color.orange);
        this.setResizable(false);
        this.setVisible(true);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);


        this.addWindowListener(new WindowAdapter(){
            public void windowDeiconified(WindowEvent e) {
                //System.out.println("game resume");
                //paintBoard(((game) e.getSource()).getGraphics());
                paintBoard();
            }
        });
        //AudioClip gameAudio = Applet.newAudioClip(this.getClass().getResource(""));

        s.acquire();

        JButton restartButton = new JButton("Restart");
        int buttonHeight = 30;
        int buttonWidth = 50;
        restartButton.setLocation(SIZEX - buttonWidth - 10
                ,SIZEY - SPACE*6/8 - buttonHeight);
        restartButton.setSize(buttonWidth,buttonHeight);
        restartButton.addActionListener(this);
        restartButton.setEnabled(true);
        this.add(restartButton);

        JButton undoButton = new JButton("Undo");
        undoButton.setLocation( buttonWidth + 10
                ,SIZEY - SPACE*6/8 - buttonHeight);
        undoButton.setSize(buttonWidth,buttonHeight);
        undoButton.addActionListener(this);
        undoButton.setEnabled(true);
        this.add(undoButton);
        //Timer t = new Timer(1000,this);

        //Game loop
        while(true){
            switch(gameState) {
                case (0)://reset, initialize
                    //t.stop();

                    //Set up playing game state 1 settings
                    blackTurn = true;
                    gamePieces = new LinkedList<>();
                    mousePiece = specialPiece.getInstance();
                        //gamePieces.add(new pieces("black",1,2));

                    //Victory state game 2 settings
                    setUpVictoryCheck();
                    victoryBoardImage = null;
                    fireworkTimer = 0;
                    fireworkList = new LinkedList<>();
                    //Redraw the board
                    repaint();
                    s.acquire();

                    //Transition to gameState 2 immediately
                    gameState = 1;
                    this.setTitle(String.valueOf(gameState));
                    break;
                case (1)://playings
                    s.acquire();
                    if(!gamePieces.isEmpty() && checkVictory()){
                        gameState = 2;
                    }
                    this.setTitle(String.valueOf(gameState));
                    break;
                case (2)://Victory state
                    this.setTitle(gameState
                            + ": " + (!blackTurn? "black":"white") + " victory!");
                    repaint();
                    Thread.sleep(100);
                    //t.start();
                    break;
                default:
                    break;
            }

        }
    }

   private final static Pattern blackVictory = Pattern.compile(".*bbbbb.*");
   private final static Pattern whiteVictory = Pattern.compile(".*wwwww.*");

   private static Image victoryBoardImage;

   private static char[][] SituationHorz;
   private static char[][] SituationVert;
   private static char[][] SituationDiagRight;
    private static char[][] SituationDiagLeft;

   public static void setUpVictoryCheck(){
       //System.out.println("here");
       SituationHorz = new char[LINES+1][LINES+1];
       SituationVert = new char[LINES+1][LINES+1];
       SituationDiagRight = new char[(LINES+1)*2-1][(LINES+1)*2-1];
       SituationDiagLeft = new char[(LINES+1)*2-1][(LINES+1)*2 - 1];

       for(int i = 0; i < (LINES+1)*2 - 1; i++){
           for(int j = 0; j < (LINES+1)*2 - 1; j++){
               SituationDiagLeft[i][j] = '*';
               SituationDiagRight[i][j] = '*';
           }
       }

       for(int i = 0; i < LINES+1; i++){
           for(int j = 0; j < LINES+1; j++){
               SituationHorz[i][j] = '*';
               SituationVert[i][j] = '*';
           }

       }
   }

    public static boolean checkVictory(){

        for(pieces p:gamePieces){
            int x = p.x;
            int y = p.y;
            if(p.color.equals("black")){
                SituationHorz[x][y] = 'b';
                SituationVert[y][x] = 'b';
                SituationDiagLeft[x+y][x] = 'b';
                SituationDiagRight[LINES+1 - x+y][x] = 'b';
            }
            else{
                SituationHorz[x][y] = 'w';
                SituationVert[y][x] = 'w';
                SituationDiagLeft[x+y][x] = 'w';
                SituationDiagRight[LINES+1 - x+y][x] = 'w';
            }
        }
        /*
        System.out.println("-------horz--------");
        for(int i = 0; i < LINES+1; i++){
            for(int j = 0; j < LINES+1; j++){
                System.out.print(SituationHorz[i][j]);
            }
            System.out.println("");
        }
        System.out.println("-------vert---------");
        for(int i = 0; i < LINES+1; i++){
            for(int j = 0; j < LINES+1; j++){
                System.out.print(SituationVert[i][j]);
            }
            System.out.println("");
        }
        System.out.println("-------diag left--------");
        for(int i = 0; i < (LINES+1)*2-1; i++){
            for(int j = 0; j < (LINES+1)*2-1; j++){
                System.out.print(SituationDiagLeft[i][j]);
            }
            System.out.println("");
        }
        System.out.println("-------diag right--------");
        for(int i = 0; i < (LINES+1)*2-1; i++){
            for(int j = 0; j < 2*(LINES+1)-1; j++){
                System.out.print(SituationDiagRight[i][j]);
            }
            System.out.println(" ");
        }
        */
        boolean diagLeft = checkCharSequences(SituationDiagLeft);
        boolean diagRight =checkCharSequences(SituationDiagRight);
        boolean horz = checkCharSequences(SituationHorz);
        boolean vert = checkCharSequences(SituationVert);
        /*
        System.out.println("diagLeft: " + diagLeft);
        System.out.println("diagRight: " + diagRight);
        System.out.println("horz: " + horz);
        System.out.println("vert: " + vert);
        */
        return diagLeft ||
                diagRight ||
                horz ||
                vert;
    }

    private static boolean checkCharSequences(char[][] seq){
        for(char[] a:seq){
            Matcher w = whiteVictory.matcher(String.valueOf(a));
            Matcher b = blackVictory.matcher(String.valueOf(a));
            if(w.matches() || b.matches()){
                return true;
            }
        }
        return false;
    }

    //public static Graphics myGraphics;

    public void paintBoard() {
        Graphics g = this.getGraphics();
        paintBoard(g);
    }

    public void paintBoard(Graphics g){
        g.setColor(new Color(255, 226, 76));//board color
        g.fillRect(0, 0, SIZEX, SIZEY);//Game board
        g.setColor(Color.black);//Line color
        for (int j = 0; j <= game.LINES; j++) {//Lines
            g.drawLine(SPACE, SPACE + j * (SIZEY - SPACE * 2) / LINES,
                    SIZEX - SPACE, SPACE + j * (SIZEY - SPACE * 2) / LINES);
        }
        for (int i = 0; i <= game.LINES; i++) {//Lines
            g.drawLine(SPACE + i * (SIZEX - SPACE * 2) / LINES, SPACE,
                    SPACE + i * (SIZEY - SPACE * 2) / LINES, SIZEY - SPACE);
        }
    }


    //Initial paint of the board layout
    public void paint(Graphics g){
        if(gameState == 0) {
            super.paint(g);
            paintBoard();
            s.release();
        }
        else if(gameState == 1){
            //Pieces
            for(pieces p:gamePieces){
                p.draw(g);
            }
        }
        else{//victory state
            if(victoryBoardImage == null){
                victoryBoardImage = this.createImage(game.SIZEX,game.SIZEY);
                Graphics victoryG = victoryBoardImage.getGraphics();
                paintBoard(victoryG);
                for(pieces p:gamePieces){
                    p.draw(victoryG);
                }
            }
            g.drawImage(victoryBoardImage,0,0,game.SIZEX,game.SIZEY,null);
            victoryFireworks(g);
        }
    }

    //Fireworks to generate
    private LinkedList<fireworkHead> fireworkList;

    private int fireworkTimer;
    public void victoryFireworks(Graphics g){
        if(fireworkTimer % 30 == 0){ //Every 3 seconds
            fireworkList.addLast(new fireworkHead());
        }
        if(fireworkTimer % 500 == 499){
            fireworkList.removeFirst();
        }
        fireworkTimer++;
        for(fireworkHead f: fireworkList){
            f.draw(g);
        }
    }

    public void updateBoard(Graphics g,int X, int Y) {
        int i = X;
        int j = Y;
        boolean flag = true;
        if(i <= game.LINES && j <= game.LINES){
            for(pieces pie:gamePieces){
                if(pie.x == i && pie.y == j){
                    flag = false;
                    break;
                }
            }

            if(flag){
                pieces p = new pieces(blackTurn?"black":"white",i,j);
                gamePieces.addFirst(p);
                p.draw(g);
                //repaint();
                blackTurn = !blackTurn;
                s.release();
            }
        }
    }

    public int[] nearestNode(int x,int y){
        //Inverse function to get from coordinate to the line
        int X = (x + pieces.PIECERADIUS/2 - game.SPACE)*game.LINES/(game.SIZEY-game.SPACE*2);
        int Y = (y + pieces.PIECERADIUS/2 - game.SPACE)*game.LINES/(game.SIZEY-game.SPACE*2);

        int[] pair = {X,Y};
        return pair;
    }

    //MOUSE LISTENERS
    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     */
    public void mouseClicked(MouseEvent e){
        //Find nearest node if exists
        if(gameState == 1){
            int[] pair = nearestNode(e.getX(),e.getY());
            updateBoard(((game) e.getSource()).getGraphics(),pair[0],pair[1]);
        }
    }

    public void refreshMousePiece(Graphics g, int x, int y){
        boolean flag = true;

        if(x <= game.LINES && y <= game.LINES){
            for(pieces pie:gamePieces){
                if((pie.x == x && pie.y == y)){
                    flag = false;
                    break;
                }
            }

            if(flag && (mousePiece.x != x || mousePiece.y != y)){

                //Clear the previous mousePiece
                    //clearing pink rectangle
                    //clearing line

                g.setColor(new Color(255, 226, 76));//board color
                g.fillRect(SPACE + mousePiece.x * (SIZEX - SPACE * 2) / LINES - game.PIECERADIUS/2,
                        SPACE + mousePiece.y * (SIZEY - SPACE * 2) / LINES - game.PIECERADIUS/2,
                        game.PIECERADIUS, game.PIECERADIUS);//Game board
                g.setColor(Color.black);
                g.drawLine(SPACE + mousePiece.x * (SIZEX - SPACE * 2) / LINES, SPACE,
                        SPACE + mousePiece.x * (SIZEY - SPACE * 2) / LINES, SIZEY - SPACE);
                g.drawLine(SPACE, SPACE + mousePiece.y * (SIZEY - SPACE * 2) / LINES,
                        SIZEX - SPACE, SPACE + mousePiece.y * (SIZEY - SPACE * 2) / LINES);

                mousePiece.x = x;
                mousePiece.y = y;
                mousePiece.color = blackTurn? "black": "white";
                mousePiece.draw(g);
                //Redraw the pieces
                repaint();
            }
        }

    }

    //DOES NOTHING RIGHT NOW
    @Deprecated
    public void mouseMoved(MouseEvent e){
        if(gameState == 1){
            int[] pair = nearestNode(e.getX(),e.getY());
            refreshMousePiece(((game) e.getSource()).getGraphics(),pair[0],pair[1]);
        }

        //refreshMousePiece(((game) e.getSource()).getGraphics());
        //repaint();
        //System.out.println(e);
    }

    public void actionPerformed(ActionEvent e){
        switch(e.getActionCommand()){
            /*
            case "t":
                if(gameState == 2){
                    System.out.println("timer ticks");
                    repaint();
                }
                break
             */
            case "Restart":
                gameState = 0;
                repaint();
                break;
            case "Undo":
                if(gameState == 1){
                    if(!gamePieces.isEmpty()){
                        //System.out.println(gamePieces.pollFirst());
                        gamePieces.pollFirst();
                        //System.out.println(gamePieces.pollLast());
                        //gamePieces.pollFirst();
                        blackTurn = !blackTurn;
                        paintBoard();
                        repaint();
                    }
                }
                break;
        }

    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    public void mousePressed(MouseEvent e){}

    /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(MouseEvent e){

    }

    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(MouseEvent e){}

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent e){}

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     */
    public void mouseDragged(MouseEvent e){}
}

abstract class genericPiece{
    int x;
    int y;
    String color;
    public static final int PIECERADIUS = game.PIECERADIUS;

    public genericPiece(String color, int x, int y){
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public abstract void draw(Graphics g);

}

class pieces extends genericPiece{
    public pieces(String color, int x, int y){
        super(color,x,y);
    }

    @Override
    public String toString(){
        return color + " piece object: located at x = " + x + ",y = " + y;
    }


    public void draw(Graphics g){
        if(color.toLowerCase().equals("black")){
            g.setColor(Color.BLACK);
        }
        else if(color.toLowerCase().equals("white")){
            g.setColor(Color.WHITE);
        }
        else{//default is black
            g.setColor(Color.BLACK);
        }
        g.fillOval(game.SPACE + x*(game.SIZEY-game.SPACE*2)/game.LINES - pieces.PIECERADIUS/2,
                game.SPACE + y*(game.SIZEY-game.SPACE*2)/game.LINES - pieces.PIECERADIUS/2
                ,pieces.PIECERADIUS,pieces.PIECERADIUS);
    }
}

//singleton
class specialPiece extends genericPiece{
    private static specialPiece ourInstance = new specialPiece("black",0,0);

    private specialPiece(String color, int x, int y){
        super(color,x,y);
    }

    public static specialPiece getInstance() {
        return ourInstance;
    }

    public void draw(Graphics g){
        if(color.toLowerCase().equals("black")){
            g.setColor(Color.BLACK);
        }
        else if(color.toLowerCase().equals("white")){
            g.setColor(Color.WHITE);
        }
        else{//default is black
            g.setColor(Color.BLACK);
        }

        g.fillOval(game.SPACE + x*(game.SIZEY-game.SPACE*2)/game.LINES - pieces.PIECERADIUS/4,
                game.SPACE + y*(game.SIZEY-game.SPACE*2)/game.LINES - pieces.PIECERADIUS/4
                ,pieces.PIECERADIUS/2,pieces.PIECERADIUS/2);
        g.setColor(Color.red);
        g.drawOval(game.SPACE + x*(game.SIZEY-game.SPACE*2)/game.LINES - pieces.PIECERADIUS/4,
                game.SPACE + y*(game.SIZEY-game.SPACE*2)/game.LINES - pieces.PIECERADIUS/4
                ,pieces.PIECERADIUS/2,pieces.PIECERADIUS/2);
        /*
        g.fillOval(x - pieces.PIECERADIUS/2,
                y - pieces.PIECERADIUS/2
                ,pieces.PIECERADIUS,pieces.PIECERADIUS);

         */
    }
}

class fireworkParticle implements Cloneable{
    //private static fireworkHead singletonHead = new fireworkHead();
    protected int x;
    protected int y;
    protected int dy;
    protected int dx;

    protected int RADIUS = 10;

    protected int[] RGB;

    public fireworkParticle(int x,int y, int dx, int dy){
        this(x,y);
        this.dx = dx;
        this.dy = dy;
    }


    public fireworkParticle(int x,int y){
        RGB = new int[3];
        this.y = y;
        this.x = x;
        //Set a random color
        Random r = game.randomDefiniteSeed;
        RGB[0] = Math.abs(r.nextInt() % 256);
        RGB[1] = Math.abs(r.nextInt() % 256);
        RGB[2] = Math.abs(r.nextInt() % 256);
    }

    public void draw(Graphics g){
        g.setColor(new Color(RGB[0],RGB[1],RGB[2]));
        g.fillOval(x-RADIUS/2,y-RADIUS/2,RADIUS,RADIUS);
        y += dy;//Speed
        x += dx;
    }

    @Override
    public Object clone(){
        fireworkParticle p = null;
        try{
            p = (fireworkParticle) super.clone();
        }
        catch(CloneNotSupportedException e){
            e.printStackTrace();
        }
        return p;
    }
}

class fireworkHead extends fireworkParticle{

    private HashSet<fireworkParticle> fireworkParticleSet;
    public static final int numberOfParticles = 17;
    private static final Double particleVelocity = 10.0;
    private static final int headVelocity = 20;

    public fireworkHead(){
        this(Math.abs(game.randomDefiniteSeed.nextInt() % game.SIZEX),game.SIZEY);
    }

    public fireworkHead(int x, int y){
        super(x,y,0,-headVelocity);
        fireworkParticle fireworkParticleDefinite = new fireworkParticle(x,game.SIZEY/2);
        fireworkParticleSet = new HashSet<>();
        for(int i = 0; i < numberOfParticles; i++){
            fireworkParticle p = (fireworkParticle) fireworkParticleDefinite.clone();
            //new fireworkParticle(x,game.SIZEY/2);//
            Double angle = (i*6.28)*(1.0/numberOfParticles);
            p.dx = (int) Math.ceil(particleVelocity*Math.cos(angle));
            p.dy = (int) Math.ceil(particleVelocity*Math.sin(angle));

            fireworkParticleSet.add(p);
        }
        //Set the size of the head
        RADIUS = 30;
    }

    public void draw(Graphics g){
        if(y < game.SIZEY/2){//firework has been set-off long enough to now explode
            for(fireworkParticle p:fireworkParticleSet ){
                p.draw(g);
                p.dy += 1; //Add in gravity's effect
            }
        }
        else{
            super.draw(g);
        }
    }

}