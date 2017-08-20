import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

/**
 * SpampedeBrain - The "View" in MVC
 * 
 * @author CS60 instructors
 */
public class SpampedeDisplay {

	/** reference to the board/spampede data being drawn */
	private SpampedeData theData; // observe how we are calling an object of another class -- instance of containment !
	
	/** the display where the board is drawn */
	private Graphics theScreen;
	
	/** width of the display in pixels */
	private int width; // 600 
	
	/** height of the display in pixels */
	private int height; // 600
	
	/** a picture of a can of spam */
	public static Image imageSpam;

	/** Constructor
	 * 
	 * @param theBoardInput    the data being displayed
	 * @param theScreenInput  the display to draw the board
	 * @param widthInput      width of the display (in pixels)
	 * @param heightInput     height of the display (in pixels)
	 */
	public SpampedeDisplay(SpampedeData theBoardInput, Graphics theScreenInput,
			int widthInput, int heightInput) {
		this.theScreen = theScreenInput; // of class Graphics
		this.theData   = theBoardInput; // an object of type SpampedeData (the MODEL -- that contains all the data)
		this.height    = heightInput; // in pixels the display
		this.width     = widthInput; // in pixels the display
	}

	/* -------------------- */
	/* Displaying the board */
	/* -------------------- */
	
	/**
	 * Re-draws the board, spam, and snake (but not the buttons).
	 */
	void updateGraphics() {
		// Draw the background. DO NOT REMOVE!
		this.clear();
		
		// Draw the title
		this.displayTitle();

		//The method drawSquare (below) will likely be helpful :)

		int width = this.theData.getNumColumns(); //50
		int height = this.theData.getNumRows(); //30
		
		for (int row = 0; row < height; row ++){
			for(int col = 0; col < width; col ++){
				
				int x_pos = col*Preferences.CELL_SIZE;
				int y_pos = row*Preferences.CELL_SIZE;
				
				this.drawSquare(x_pos, y_pos, this.theData.getCellColor(row, col));
				
			}
		}
		
		// Display an image, just for fun.
		if (SpampedeDisplay.imageSpam != null) {								// image, x,y, null 
			this.theScreen.drawImage(SpampedeDisplay.imageSpam, 200, 370, null); //The image is drawn with its top-left corner at (x, y) in this graphics context's coordinate space. T
		}
		
		// Draw the game-over message, if appropriate.
		if (this.theData.getGameOver()) { // returns a boolean
			this.displayGameOver();
		}

	}

	/**
	 * Draws a cell-sized square with its upper-left corner
	 * at the given pixel coordinates (i.e., x pixels to the right and 
	 * y pixels below the upper-left corner) on the display.
	 * 
	 * @param x  x-coordinate of the square, between 0 and this.width-1 inclusive
	 * @param y  y-coordinate of the square, between 0 and this.width-1 inclusive
	 * @param cellColor  color of the square being drawn
	 */
	public void drawSquare(int x, int y, Color cellColor) {
		this.theScreen.setColor(cellColor);
		this.theScreen.fillRect(x +(this.width/10), y+(this.width/10), Preferences.CELL_SIZE,
				Preferences.CELL_SIZE);
	}
	
	// fillRect(x, y are references for the co-ordinate to be filled, width, height)
	
	/**
	 * Draws the background. DO NOT MODIFY!
	 */
	void clear() {
		this.theScreen.setColor(Preferences.COLOR_BACKGROUND);
		this.theScreen.fillRect(0, 0, this.width, this.height);
		this.theScreen.setColor(Preferences.TITLE_COLOR);
		this.theScreen.drawRect(0, 0, this.width - 1,
				Preferences.GAMEBOARDHEIGHT - 1);
	}

	/* ------------ */
	/* Text Display */
	/* ------------ */
	
	/**
	 * Displays the title of the game.
	 */
	public void displayTitle() {
		this.theScreen.setFont(Preferences.TITLE_FONT);
		this.theScreen.setColor(Preferences.TITLE_COLOR);
		this.theScreen.drawString(Preferences.TITLE, 
				Preferences.TITLE_X, Preferences.TITLE_Y);
	}

	/**
	 * Displays the game-over message.
	 */
	public void displayGameOver() {
		this.theScreen.setFont(Preferences.GAME_OVER_FONT);
		this.theScreen.setColor(Preferences.GAME_OVER_COLOR);
		this.theScreen.drawString(Preferences.GAME_OVER_TEXT,
				Preferences.GAME_OVER_X, Preferences.GAME_OVER_Y);
	}

}
