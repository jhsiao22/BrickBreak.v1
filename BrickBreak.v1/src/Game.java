import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
/******************************************************************************
 *
 * Name:		Joweina Hsiao
 * Block:		E
 * Date:		5/23/12
 *
 *  Program #22: Final Project
 *  Description:
 * 		My final project is a brick breaking game. A ball bounces off a
 * moving platform and breaks blocks. To start the game, click on either the
 * "Start" or "Rules" button. The "Start" button will take you straight to the
 * game, while the "Rules" button allows you to read the rules, then continue
 * on to the game. To move the platform, use VK_LEFT and VK_RIGHT. To change
 * the color of the ball, use VK_1, VK_2, VK_3, and VK_4.
 * VK_1 = red
 * VK_2 = yellow
 * VK_3 = green
 * VK_4 = blue
 * To reset the game, use VK_R. To reset the level and start where you lost,
 * use VK_C. To pause the game, use VK_P.
 ******************************************************************************/
public class Game extends JFrame
        implements ActionListener, KeyListener, MouseListener
{
    // DATA:
    public static final int MAX_WIDTH = 600;			//window size
    public static final int MAX_HEIGHT = 750;			//window size
    public static final int TOP_OF_WINDOW = 22;			//top of the visible window

    private static Ball b;								//a ball object

    private static final int RADIUS = 10;				//radius of the ball
    private static final int MAX_VELOCITY = 10;      	//maximum velocity
    private static final int MIN_VELOCITY = 9;      	//minimum velocity

    private static Box box1; 							//creating a box object
    private static final int BOX_HEIGHT = 5;			//height of the box
    private static final int BOX_WIDTH = 300;			//width of the box
    public static final int BOX_STEP_SIZE = 20;			//step size to move the box by

    public static final int X_CORNER_START = 0;			//x coordinate where the bricks start
    public static final int Y_CORNER_START = 42;		//y coordinate where the bricks start
    public static final int BRICK_WIDTH = 100;			//max width of the brick
    public static int widthUsed = 300;					//width used for the bricks
    public static final int BRICK_HEIGHT = 20;			//height of the brick

    private static final int NUM_ROWS = 10;				//max number of rows of bricks
    private static final int NUM_COLS = 				//max number of columns of bricks
            MAX_WIDTH/BRICK_WIDTH;
    private static int colsUsed = MAX_WIDTH/widthUsed;	//number of cols used
    private static Brick brickList [][] = 				//array of bricks
            new Brick[NUM_ROWS][NUM_COLS];

    private static int rowsUsed;						//number of rows used in each level

    private static int inactiveBrickCounter = 0;		//counter for inactive bricks

    private static final int NUM_LIVES = 3;				//number of lives the player has
    private static int lives = 1;						//number of lives the player has used

    private static final int DELAY_IN_MILLISEC = 15;  	//time delay between ball updates

    private static int level = 1;						//int that represents the level drawn
    private static final int MAX_LEVEL = 3;				//maximum number of levels
    private static int lastLevel;						//last level the user played

    private static int mode = 0;						//int that represents the user's mode
    //final variables that represent specific modes the user can be in
    private static final int START_MODE = 0;
    private static final int INSTRUCTIONS_MODE = 1;
    private static final int PLAY_MODE = 2;
    private static final int WIN_MODE = 3;
    private static final int LOSE_MODE = 4;

    private static Image startImage;					//background for the start up page
    private static Image rulesImage;					//background for the rules page
    private static Image winImage;						//background for the win page
    private static Image loseImage;						//background for the lose page
    private static Image levelOne;						//background for level one
    private static Image levelTwo;						//background for level two
    private static Image levelThree;					//background for level three

    // METHODS:

    /**
     * main -- Start up the window.
     *
     * @param	args
     */
    public static void main(String[] args)
    {
        // Create the window.
        Game bb = new Game();
        bb.addKeyListener(bb);
        bb.addMouseListener(bb);
        bb.setTitle("Brick Break!");
        bb.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * BrickBreak constructor: creates ball objects based on NUM_BALLS
     * 		and simulate the "clock"
     */
    public Game()
    {
        //import the backgrounds for the game
        startImage = new ImageIcon(getClass().getResource("/Resource/BrickBreakStartUpPage.gif")).getImage();
        rulesImage = new ImageIcon(getClass().getResource("/Resource/Rules.gif")).getImage();
        winImage = new ImageIcon(getClass().getResource("/Resource/Win.gif")).getImage();
        loseImage = new ImageIcon(getClass().getResource("/Resource/Lose.gif")).getImage();
        levelOne = new ImageIcon(getClass().getResource("/Resource/1.gif")).getImage();
        levelTwo = new ImageIcon(getClass().getResource("/Resource/2.gif")).getImage();
        levelThree = new ImageIcon(getClass().getResource("/Resource/3.gif")).getImage();

        //initializing the ball
        initializeBall();

        //initializing the platform
        initializePlatform();

        //initializing the array of bricks
        initializeBricks();

        //Show the window with the ball in its initial position.
        setSize(MAX_WIDTH, MAX_HEIGHT);
        setVisible(true);

        //Sets up a timer but does not start it.  Once started, the timer will go
        //off every DELAY_IN_MILLISEC milliseconds.  When it goes off all it does
        //is call this.actionPerformed().  It then goes back to sleep for another
        //DELAY_IN_MILLISEC.
        Timer clock= new Timer(DELAY_IN_MILLISEC, this);

        //Now actually start the timer.
        clock.start();
    }

    /**
     * actionPerformed() is called automatically by the timer every time the requested
     * delay has elapsed.  It will keep being called until the clock is stopped or the
     * program ends.  All actions that we want to happen then should be performed here.
     * Any class that implements ActionListener MUST have this method.
     *
     * In this example it is called to move the ball every DELAY_IN_MILLISEC.
     *
     * @param e		Contains info about the event that caused this method to be called
     */
    public void actionPerformed(ActionEvent e)
    {
        //move the ball.
        b.move();
        b.bounce(0, MAX_WIDTH, TOP_OF_WINDOW, MAX_HEIGHT);

        //make sure the ball bounces off the platform
        b.bounceOffBox(box1);

        //make sure the ball bounces off the bricks
        for (int row = 0; row < rowsUsed; row ++)
        {
            for (int col = 0; col < colsUsed; col ++)
            {
                boolean active = brickList[row][col].isTheBrickActive();
                if (active)
                {
                    b.bounceOffBrick(brickList[row][col]);
                    boolean touchingBrick = b.touchingBrick(brickList[row][col]);
                    if (touchingBrick)
                    {
                        if (brickList[row][col].getColor() == b.getColor())
                        {
                            brickList[row][col].setStatus(false);
                            inactiveBrickCounter++;
                        }
                    }
                }
            }
        }

        //test to see if the level should be changed
        //reinitializes everything (bricks, ball, and platform)
        if (inactiveBrickCounter == colsUsed * rowsUsed && level <= MAX_LEVEL)
        {
            if (level == 3)
            {
                mode = WIN_MODE;
            }
            level++;
            inactiveBrickCounter = 0;
            initializeBricks();
            initializeBall();
            initializePlatform();

            //if the user has 1 or 2 lives, and they finish a level,
            //give them another life
            if (lives < 3)
            {
                lives--;
            }
        }

        //test to see if a new ball is needed
        if (b.needANewBall(MAX_HEIGHT) && lives <= NUM_LIVES)
        {
            int xSpace = 500;

            //create a new ball
            double x = (MAX_WIDTH/2);
            double y = MAX_HEIGHT - xSpace;
            int dx = 0;
            int dy = 0;

            //initializing the ball object
            b = new Ball((int)x, (int)y, dx, dy, RADIUS, (Color.red));
            lives ++;

            //test to see if the game should end
            if (lives == 4)
            {
                lastLevel = level;
                mode = LOSE_MODE;
            }
        }

        //update the window.
        repaint();
    }

    /**
     * Called when a key is first pressed
     * Required for any KeyListener
     *
     * @param e		Contains info about the key pressed
     */
    public void keyPressed(KeyEvent e)
    {
        //tests the user input to move the box to the left or right, change the
        //color of the ball, restart the game, restart the level,
        //pause the game, and drop the ball
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT)
        {
            box1.moveLeft();
        }
        else if (keyCode == KeyEvent.VK_RIGHT)
        {
            box1.moveRight();
        }
        else if (keyCode == KeyEvent.VK_1)
        {
            b.setColor(Color.red);
        }
        else if (keyCode == KeyEvent.VK_2)
        {
            b.setColor(Color.yellow);
        }
        else if (keyCode == KeyEvent.VK_3)
        {
            b.setColor(Color.green);
        }
        else if (keyCode == KeyEvent.VK_4)
        {
            b.setColor(Color.blue);
        }
        else if (keyCode == KeyEvent.VK_S)
        {
            dropTheBall();
        }
        else if (keyCode == KeyEvent.VK_R)
        {
            resetGame();
        }
        else if (keyCode == KeyEvent.VK_C)
        {
            resetLevel();
        }
        else if (keyCode == KeyEvent.VK_P)
        {
            b.pause();
            b.move();
        }
    }

    /**
     * Called when typing of a key is completed
     * Required for any KeyListener
     *
     * @param e		Contains info about the key typed
     */
    public void keyTyped(KeyEvent e)
    {
    }

    /**
     * Called when a key is released
     * Required for any KeyListener
     *
     * @param e		Contains info about the key released
     */
    public void keyReleased(KeyEvent e)
    {
    }

    /**
     * Called when the mouse is clicked (= pressed and released without moving
     *    while the mouse is in our window)
     * Required for any MouseListener
     * Rotate through the colors on mouse clicks.
     *
     * @param e		Contains info about the mouse click
     */
    public void mouseClicked(MouseEvent e)
    {
        int x = e.getX();
        int y = e.getY();
        if (mode == START_MODE)
        {
            //dimensions of the start box
            int xMinBox1 = 120;
            int xMaxBox1 = 240;
            int yMinBox1 = 580;
            int yMaxBox1 = 640;

            //dimensions of the rules box
            int xMinBox2 = 360;
            int xMaxBox2 = 480;

            //test to see if the user hit the start box
            if (x <= xMaxBox1 && x >= xMinBox1 && y <= yMaxBox1 && y >= yMinBox1)
            {
                mode = PLAY_MODE;
            }

            //test to see if the user hit the rules box
            else if (x <= xMaxBox2 && x >= xMinBox2 && y <= yMaxBox1 && y >= yMinBox1)
            {
                mode = INSTRUCTIONS_MODE;
            }
        }
        else if (mode == INSTRUCTIONS_MODE)
        {
            //dimensions of the start box
            int xMinBox3 = 480;
            int xMaxBox3 = 600;
            int yMinBox3 = 680;
            int yMaxBox3 = 740;

            //test to see if the user hit the start box
            if (x <= xMaxBox3 && x >= xMinBox3 && y <= yMaxBox3 && y >= yMinBox3)
            {
                mode = PLAY_MODE;
            }
        }
    }

    /**
     * Called when the mouse is pressed (in our window)
     * Required for any MouseListener
     *
     * @param e		Contains info about the mouse click
     */
    public void mousePressed(MouseEvent e)
    {

    }

    /**
     * Called when the mouse is released (in our window)
     * Required for any MouseListener
     *
     * @param e		Contains info about the mouse click
     */
    public void mouseReleased(MouseEvent e)
    {

    }

    /**
     * Called when the mouse enters our window.
     * Required for any MouseListener
     *
     * @param e		Contains info about the mouse click
     */
    public void mouseEntered(MouseEvent e)
    {

    }

    /**
     * Called when the mouse exits our window.
     * Required for any MouseListener
     *
     * @param e		Contains info about the mouse click
     */
    public void mouseExited(MouseEvent e)
    {

    }

    /**
     * paint 		draw the window
     *
     * @param g		Graphics object to draw on
     */
    public void paint(Graphics g)
    {
        // Clear the window.
        g.setColor(Color.black);
        g.fillRect(0, 0, MAX_WIDTH, MAX_HEIGHT);

        int livesLeft = NUM_LIVES - lives;	//number of lives the user has left

        //test to see which mode should be drawn
        if (mode == START_MODE)
        {
            g.drawImage(startImage, 0, 0, this);
        }
        else if (mode == INSTRUCTIONS_MODE)
        {
            g.drawImage(rulesImage, 0, 0, this);
        }
        else if (mode == PLAY_MODE)
        {
            //display the background
            displayBackground(g);

            //draw the status/lives bar and display the lives and status
            displayLivesBar(g, livesLeft);
            displayStatus(g);
            displayLives(g, livesLeft);

            //display the constant parts of the game
            //example: ball, platform, bricks
            displayConstant(g);
        }

        else if (mode == WIN_MODE)
        {
            g.drawImage(winImage, 0, 0, this);
        }

        else if (mode == LOSE_MODE)
        {
            g.drawImage(loseImage, 0, 0, this);
        }
    }

    /**
     * Display the background image depending
     * on the number of lives.
     * @param g graphics object to draw with
     */
    public void displayBackground(Graphics g)
    {
        if (level == 1)
        {
            g.drawImage(levelOne, 0, 0, this);
        }
        else if (level == 2)
        {
            g.drawImage(levelTwo, 0, 0, this);
        }
        else if (level == 3)
        {
            g.drawImage(levelThree, 0, 0, this);
        }
    }

    /**
     * Display all the constant aspects of the game.
     * @param g graphics object to draw with
     */
    public void displayConstant(Graphics g)
    {
        //draw the ball
        b.draw(g);

        //draw the box
        box1.draw(g);

        //draw the array of bricks
        for (int row = 0; row < rowsUsed; row ++)
        {
            for (int col = 0; col < colsUsed; col ++)
            {
                boolean active = brickList[row][col].isTheBrickActive();
                if (active)
                {
                    brickList[row][col].draw(g, row, col);
                }
            }
        }
    }

    /**
     * Creates a brick.
     * @param color color of the brick
     * @param row integer that represents the row of the brick
     * @param col integer that represents the column of the brick
     * @return a brick object
     */
    public Brick createABrick(Color color, int row, int col)
    {
        int xCorner = X_CORNER_START + col * widthUsed;
        int yCorner = Y_CORNER_START + row * BRICK_HEIGHT;
        Brick b = new Brick(xCorner, yCorner, widthUsed, BRICK_HEIGHT, color, true);
        return b;
    }

    /**
     * Initializes the platform.
     */
    public void initializePlatform()
    {
        //sets the box to start in the middle of the window, at the bottom of the window
        double boxX = (MAX_WIDTH/2) - (BOX_WIDTH/2);
        double boxY = MAX_HEIGHT - BOX_HEIGHT;

        //initializing the box object
        box1 = new Box (boxX, boxY, BOX_WIDTH, BOX_HEIGHT, (Color.white));
    }

    /**
     * Initializes the array of bricks.
     */
    public void initializeBricks()
    {
        //test to see which level is being played
        //an appropriate number of rows, columns, and widths are used
        if (level == 1)
        {
            rowsUsed = 4;
            widthUsed = 300;
            colsUsed = 2;
        }
        else if (level == 2)
        {
            rowsUsed = 6;
            widthUsed = 150;
            colsUsed = 4;
        }
        else if (level == 3)
        {
            rowsUsed = 10;
            widthUsed = 100;
            colsUsed = 6;
        }
        for (int row = 0; row < rowsUsed; row ++)
        {
            for (int col = 0; col < colsUsed; col ++)
            {
                //tests the row and column number to figure out the color of the brick
                //if (row % 2 == 0 && col % 2 == 0)
                Color brickColor = Color.red;
                if (row % 2 != 0 && col % 2 == 0)
                {
                    brickColor = Color.yellow;
                }
                else if (row % 2 == 0 && col % 2 != 0)
                {
                    brickColor = Color.green;
                }
                else if (row % 2 != 0 && col % 2 != 0)
                {
                    brickColor = Color.blue;
                }
                brickList[row][col] = createABrick(brickColor, row, col);
            }
        }
    }

    /**
     * Initializes the ball.
     */
    public void initializeBall()
    {
        //variable for how high above the platform the ball appears
        int xSpace = 500;

        //sets the ball to start in the middle of the window, above the platform, with a
        //set speed of zero
        double x = (MAX_WIDTH/2);
        double y = MAX_HEIGHT - xSpace;
        int dx = 0;
        int dy = 0;

        //initializing the ball object
        b = new Ball((int)x, (int)y, dx, dy, RADIUS, (Color.red));
    }

    /**
     * Drops the ball to start the game.
     */
    public void dropTheBall()
    {
        Random gen = new Random();
        int dxIn = MIN_VELOCITY + gen.nextInt(MAX_VELOCITY - MIN_VELOCITY);
        int dyIn = MIN_VELOCITY + gen.nextInt(MAX_VELOCITY - MIN_VELOCITY);
        b.setDX(dxIn);
        b.setDY(dyIn);
    }

    /**
     * Display the lives bar.
     * @param g graphics object to draw with
     */
    public void displayLivesBar(Graphics g, int livesLeft)
    {
        int height = 20;				//height of the bar
        int width = MAX_WIDTH;			//width of the bar
        int x = 0;						//x coordinate of the bar
        int y = TOP_OF_WINDOW;			//y coordinate of the bar
        int radius = 8;					//radius of the ball
        int xSpace = 20;				//space between the balls

        g.setColor(Color.black);
        g.fillRect(x ,y, width, height);

        g.setColor(Color.red);

        //test to see how many lives the user has
        //draw the number of balls accordingly
        if (lives == 1)
        {
            g.fillOval(x, y, radius * 2, radius * 2);
            g.fillOval(x + xSpace, y, radius * 2, radius * 2);
            g.fillOval(x + (xSpace * 2), y, radius * 2, radius * 2);
        }
        else if (lives == 2)
        {
            g.fillOval(x, y, radius * 2, radius * 2);
            g.fillOval(x + xSpace, y, radius * 2, radius * 2);
        }
        else if (lives == 3)
        {
            g.fillOval(x, y, radius * 2, radius * 2);
        }
    }

    /**
     * Display the lives message.
     * @param livesLeft int for the number of lives left
     * @param g graphics object to draw with
     */
    public void displayLivesMessage(int livesLeft, Graphics g)
    {
        //variables needed to draw the strings
        int xStart = 0;						//x coordinate where the strings start
        int yStart = MAX_HEIGHT - 100; 		//y coordinate where the strings start

        g.setColor(Color.white);
        g.setFont(new Font("Cracked", Font.BOLD, 30));
        g.drawString("Press 's' to drop the ball again.", xStart, yStart);
    }

    /**
     * Test to see how the lives message should be displayed.
     * @param g graphics object to draw with
     * @param livesLeft number of lives the user has left
     */
    public void displayLives(Graphics g, int livesLeft)
    {
        //tests to check how many lives the user has used,
        //a relevant message is displayed each time
        if (lives == 2)
        {
            displayLivesMessage(livesLeft, g);
        }

        else if (lives == 3)
        {
            displayLivesMessage(livesLeft, g);
        }
    }

    /**
     * Display the status of the ball.
     * @param g graphics object to draw with
     */
    public void displayStatus(Graphics g)
    {
        int y = TOP_OF_WINDOW;
        int height = 17;
        g.setColor(Color.white);
        g.setFont(new Font("Cracked", Font.BOLD, 20));
        g.drawString(Ball.status, MAX_WIDTH - 50, y + height);
    }

    /**
     * Reset the entire game.
     */
    public void resetGame()
    {
        lives = 1;
        level = 1;
        mode = START_MODE;
        inactiveBrickCounter = 0;
        initializeBricks();
        initializePlatform();
        initializeBall();
    }

    /**
     * Reset the level.
     */
    public void resetLevel()
    {
        lives = 1;
        level = lastLevel;
        mode = PLAY_MODE;
        inactiveBrickCounter = 0;
        initializeBricks();
        initializePlatform();
        initializeBall();
    }
}
/*********************************************************************************
 * Ball class
 * Stores all of the information about a single ball including:
 * 		center, velocity, radius, color, and status of the ball
 * It provides methods to move the ball, set the color of the ball, get the color
 * of the ball, set the velocity, get the velocity, bounce within a rectangle,
 * bounce off a platform, bounce off the brick, check to see if it touches the
 * brick, check to see if a new ball is needed, drawing itself, and pausing the ball.
 ***********************************************************************************/
class Ball
{
    // DATA:
    private double x, y;						// Center of the ball
    private double dx, dy;						// Velocity - how much to move the
    //ball in one time unit
    private double radius;						// Radius of the ball
    private Color color;						// Color of the ball
    public static boolean movingBall = true; 	//this is a boolean that keeps
    //track of the status of the ball.
    public static String status = "playing"; 	//this is a String that is displayed
    //on the screen for each ball.

    // METHODS:

    /**
     * Ball constructor initializes the Ball object
     *
     * @param xIn		x coordinate of center
     * @param yIn		y coordinate of center
     * @param dxIn		x velocity
     * @param dyIn		y velocity
     * @param radiusIn	radius
     * @param colorIn	color
     */
    public Ball (double xIn, double yIn, double dxIn, double dyIn, double radiusIn,
                 Color colorIn)
    {
        // Nothing to do but save the data in the object's data fields.
        x = xIn;
        y = yIn;
        dx = dxIn;
        dy = dyIn;
        radius = radiusIn;
        color = colorIn;
    }

    /**
     * Move the ball.  Add the velocity to its center.
     */
    public void move()
    {
        //tests to see if the ball should be moving, if not, the ball won't move.
        if (movingBall == true)
        {
            //making it move by the step size
            x = x + dx;
            y = y + dy;
        }
    }

    /**
     * Set the ball color.
     */
    public void setColor(Color colorIn)
    {
        color = colorIn;
    }

    /**
     * Get the color of the ball.
     * @return the color of the ball
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * Set the x velocity of the ball.
     * @param dxIn x amount for velocity of the ball
     */
    public void setDX(int dxIn)
    {
        dx = dxIn;
    }

    /**
     * Set the y velocity of the ball.
     * @param dyIn y amount for velocity of the ball
     */
    public void setDY(int dyIn)
    {
        dy = dyIn;
    }

    /**
     * Get the x amount of velocity of the ball.
     */
    public double getDX()
    {
        return dx;
    }

    /**
     * Get the y amount of velocity of the ball.
     */
    public double getDY()
    {
        return dy;
    }

    /**
     * Check if the ball should bounce off any of the walls.  It will only
     * bounce if it was heading toward the wall and went a bit past it.  If
     * so just change the sign of the corresponding velocity.  Not a very
     * accurate way to handle this, but it is simple and it mostly works.
     *
     * @param xLow		x coord of left wall
     * @param xHigh		x coord of right wall
     * @param yLow		y coord of top wall
     * @param yHigh		y coord of bottom wall
     */
    public void bounce(int xLow, int xHigh, int yLow, int yHigh)
    {
        // Check for an x bounce.  Note that we bounce if the x is too
        //  low or too high AND IS HEADING IN THE WRONG DIRECTION.
        if ((x - radius <= xLow && dx < 0) || (x + radius >= xHigh && dx > 0))
        {
            dx = -dx;
        }

        // Now check for a y bounce.
        if (y - radius <= yLow && dy < 0)
        {
            dy = -dy;
        }
    }

    // bounceOffBox -- Check for bouncing off the box.
    //  This assumes we've already moved the ball but have not yet done
    //  the wall bounce check.
    public void bounceOffBox(Box box)
    {
        // We need the old position before the current move
        double xOld = x - dx;
        double yOld = y - dy;

        // Next we need the edges of the box
        double xMinBox = box.getXMin();
        double xMaxBox = box.getXMax();
        double yMinBox = box.getYMin();
        double yMaxBox = box.getYMax();

        // Now see if we've hit or crossed into the box.  If so, bounce.
        if (xOld + radius < xMinBox && x + radius >= xMinBox)
        {
            // The ball crossed xMinBox moving right.
            // See if it crossed between yMinBox and yMaxBox.
            double yIntercept = y + (xMinBox - x) * dy / dx;
            if (yIntercept > yMinBox - radius && yIntercept < yMaxBox + radius)
            {
                dx = - dx;
            }
        }
        else if (x - radius < xMaxBox && xOld - radius >= xMaxBox)
        {
            // The ball crossed xMaxBox moving left.
            // See if it crossed between yMinBox and yMaxBox.
            double yIntercept = y + (xMaxBox - x) * dy / dx;
            if (yIntercept > yMinBox - radius && yIntercept < yMaxBox + radius)
            {
                dx = - dx;
            }
        }

        if (yOld + radius < yMinBox && y + radius >= yMinBox)
        {
            // The ball crossed yMinBox moving down.
            // See if it crossed between xMinBox and xMaxBox.
            double xIntercept = x + (yMinBox - y) * dx / dy;
            if (xIntercept > xMinBox - radius && xIntercept < xMaxBox + radius)
            {
                dy = - dy;
            }
        }
        else if (y - radius < yMaxBox && yOld - radius >= yMaxBox)
        {
            // The ball crossed yMaxBox moving up.
            // See if it crossed between xMinBox and xMaxBox.
            double xIntercept = x + (yMaxBox - y) * dx / dy;
            if (xIntercept > xMinBox - radius && xIntercept < xMaxBox + radius)
            {
                dy = - dy;
            }
        }
    }

    // bounceOffBrick -- Check for bouncing off a brick.
    //  This assumes we've already moved the ball but have not yet done
    //  the wall bounce check.
    public void bounceOffBrick(Brick brick)
    {
        if (brick.isTheBrickActive())
        {
            // We need the old position before the current move
            double xOld = x - dx;
            double yOld = y - dy;

            // Next we need the edges of the box
            double xMinBrick = brick.getXMin();
            double xMaxBrick = brick.getXMax();
            double yMinBrick = brick.getYMin();
            double yMaxBrick = brick.getYMax();

            // Now see if we've hit or crossed into the box.  If so, bounce.
            if (xOld + radius < xMinBrick && x + radius >= xMinBrick)
            {
                // The ball crossed xMinBox moving right.
                // See if it crossed between yMinBox and yMaxBox.
                double yIntercept = y + (xMinBrick - x) * dy / dx;
                if (yIntercept > yMinBrick - radius && yIntercept < yMaxBrick + radius)
                {
                    dx = - dx;
                }
            }
            else if (x - radius < xMaxBrick && xOld - radius >= xMaxBrick)
            {
                // The ball crossed xMaxBox moving left.
                // See if it crossed between yMinBox and yMaxBox.
                double yIntercept = y + (xMaxBrick - x) * dy / dx;
                if (yIntercept > yMinBrick - radius && yIntercept < yMaxBrick + radius)
                {
                    dx = - dx;
                }
            }

            if (yOld + radius < yMinBrick && y + radius >= yMinBrick)
            {
                // The ball crossed yMinBox moving down.
                // See if it crossed between xMinBox and xMaxBox.
                double xIntercept = x + (yMinBrick - y) * dx / dy;
                if (xIntercept > xMinBrick - radius && xIntercept < xMaxBrick + radius)
                {
                    dy = - dy;
                }
            }
            else if (y - radius < yMaxBrick && yOld - radius >= yMaxBrick)
            {
                // The ball crossed yMaxBox moving up.
                // See if it crossed between xMinBox and xMaxBox.
                double xIntercept = x + (yMaxBrick - y) * dx / dy;
                if (xIntercept > xMinBrick - radius && xIntercept < xMaxBrick + radius)
                {
                    dy = - dy;
                }
            }
        }
    }

    /**
     * This methods tests to see if a ball is touching the brick.
     * @param brick A brick object.
     * @return A boolean that tells if the ball is touching the brick.
     */
    public boolean touchingBrick(Brick brick)
    {
        boolean ballIsTouchingBox = false;

        //figures out what that xMax, yMax, yMin, and xMin of the box is.
        double xMax = brick.getXMax();
        double yMax = brick.getYMax();
        double xMin = brick.getXMin();
        double yMin = brick.getYMin();

        //test to see if the ball is touching the box.
        if (x < xMax + (radius) && x > xMin - (radius) && y < yMax + (radius)
                && y > yMin - (radius))
        {
            ballIsTouchingBox = true;
        }
        return ballIsTouchingBox;
    }

    /**
     * Checks to see if the game needs a new ball.
     * @return boolean that represents the need for a new ball
     */
    public boolean needANewBall(int yHigh)
    {
        boolean needANewBall = false;
        if (y + radius >= yHigh && dy > 0)
        {
            needANewBall = true;
        }
        return needANewBall;
    }

    /**
     * Draw the ball.
     *
     * @param g			Graphics object in which to draw
     */
    public void draw(Graphics g)
    {
        g.setColor(color);
        g.fillOval((int)(x - radius), (int)(y - radius), (int)(2 * radius),
                (int)(2 * radius));
    }

    /**
     * This toggles the boolean movingBall. It also toggles the string
     * used the variable 'status'.
     */
    public boolean pause()
    {
        if (movingBall == true)
        {
            status = "paused";
            movingBall = false;
        }
        else if (movingBall == false)
        {
            status = "playing";
            movingBall = true;
        }
        return movingBall;
    }
}
/*********************************************************************************
 * Box class
 * Stores all of the information about a single box including:
 * 		x, y, width, height, and color
 * It provides methods to move the box, draw itself, and find the xMin, yMin, xMax,
 * and yMax.
 ***********************************************************************************/
class Box
{
    private double x, y;					//x and y coordinate of box (left corner)
    private double width, height;			//width and height of box
    private Color boxColor;					//color of box

    /**
     * Box constructor.
     * @param xIn x coordinate of box
     * @param yIn y coordinate of box
     * @param widthIn width of box
     * @param heightIn height of box
     * @param colorIn color of box
     */
    public Box (double xIn, double yIn, double widthIn, double heightIn, Color colorIn)
    {
        x = xIn;
        y = yIn;
        width = widthIn;
        height = heightIn;
        boxColor = colorIn;
    }

    /**
     * Get the minimum x amount.
     * @return minimum x amount
     */
    public double getXMin()
    {
        double xMin;
        xMin = x;
        return xMin;
    }

    /**
     * Get the minimum y amount.
     * @return minimum y amount
     */
    public double getYMin()
    {
        double yMin;
        yMin = y;
        return yMin;
    }

    /**
     * Get the max x amount.
     * @return max x amount
     */
    public double getXMax()
    {
        double xMax;
        xMax = x + width;
        return xMax;
    }

    /**
     * Get the max y amount.
     * @return max y amount
     */
    public double getYMax()
    {
        double yMax;
        yMax = y + height;
        return yMax;
    }

    /**
     * Move the box left.
     */
    public void moveLeft()
    {
        //test to see if the ball is paused
        //if so, don't move the platform
        if (Ball.movingBall == true)
        {
            x = x - Game.BOX_STEP_SIZE;
            if (x < 0)
            {
                x = 0;
            }
        }
    }

    /**
     * Move the box right.
     */
    public void moveRight()
    {
        //test to see if the ball is paused
        //if so, don't move the platform
        if (Ball.movingBall == true)
        {
            x = x + Game.BOX_STEP_SIZE;
            if (x + width > Game.MAX_WIDTH)
            {
                x = Game.MAX_WIDTH - width;
            }
        }
    }

    /**
     * Draw the box.
     * @param g graphics object to draw with
     */
    public void draw(Graphics g)
    {
        g.setColor(boxColor);
        g.fillRect((int)x, (int)y, (int)width, (int)height);
    }
}
/*********************************************************************************
 * Brick class
 * Stores all of the information about a single brick including:
 * 		x, y, width, height, color, and status
 * It provides methods to get the xMin, yMin, xMax, and yMax, check to see if the
 * brick is active, set the status of the brick (break the brick), get the color
 * of the brick, and draw itself.
 ***********************************************************************************/
class Brick
{
    private double x, y;			//x and y coordinate of the brick (upper left corner)
    private double width, height;	//width and height of the brick
    private Color color;			//color of the brick
    private boolean active;			//boolean that tells if the brick is active or not

    /**
     * Brick constructor.
     * @param xIn x coordinate of the brick
     * @param yIn y coordinate of the brick
     * @param widthIn width of the brick
     * @param heightIn height of the brick
     * @param colorIn color of the brick
     */
    public Brick(double xIn, double yIn, double widthIn, double heightIn, Color colorIn,
                 boolean activeIn)
    {
        x = xIn;
        y = yIn;
        width = widthIn;
        height = heightIn;
        color = colorIn;
        active = activeIn;
    }

    /**
     * Get the minimum x amount.
     * @return minimum x amount
     */
    public double getXMin()
    {
        double xMin;
        xMin = x;
        return xMin;
    }

    /**
     * Get the minimum y amount.
     * @return minimum y amount
     */
    public double getYMin()
    {
        double yMin;
        yMin = y;
        return yMin;
    }

    /**
     * Get the max x amount.
     * @return max x amount
     */
    public double getXMax()
    {
        double xMax;
        xMax = x + width;
        return xMax;
    }

    /**
     * Get the max y amount.
     * @return max y amount
     */
    public double getYMax()
    {
        double yMax;
        yMax = y + height;
        return yMax;
    }

    /**
     * Get the boolean to know if the brick exists.
     * @return boolean that tells if the brick is active
     */
    public boolean isTheBrickActive()
    {
        return active;
    }

    /**
     * Set the status of the brick.
     */
    public void setStatus(boolean statusIn)
    {
        active = statusIn;
    }

    /**
     * Get the color of the brick.
     * @return the color of the brick
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * Draws the brick.
     * @param g graphics object to draw with
     * @param row integer that represents the row of the brick
     * @param col integer that represents the column of the brick
     */
    public void draw(Graphics g, int row, int col)
    {
        if (active)
        {
            int xCorner = Game.X_CORNER_START + col *
                    Game.widthUsed;
            int yCorner = Game.Y_CORNER_START + row *
                    Game.BRICK_HEIGHT;
            g.setColor(color);
            g.fillRect(xCorner, yCorner, Game.widthUsed,
                    Game.BRICK_HEIGHT);
        }
    }
}