import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.Queue;

/**
 * SpampedeBrain - The "Controller" in MVC, which includes all of the AI and
 * handling key presses
 * 
 * @author CS60 instructors
 */
public class SpampedeBrain extends SpampedeBrainParent {

	/** The "View" in MVC */
	private SpampedeDisplay theDisplay; 
	
	/** The "Model" in MVC */
	private SpampedeData theData;
	
	/** Number of animated frames displayed so far */
	private int cycleNum = 0; 

	// Mapping between direction (names) and keys
	private static final char REVERSE = 'r';
	private static final char UP      = 'i';
	private static final char DOWN    = 'k';
	private static final char LEFT    = 'j';
	private static final char RIGHT    = 'l';
	private static final char AI_MODE = 'a';
	private static final char PLAY_SPAM_NOISE = 's';


	/**
	 * Starts a new game.
	 */
	// from where the code is run 
	public void startNewGame() {
		this.theData = new SpampedeData();
		this.theData.placeSnakeAtStartLocation();
		this.theData.setStartDirection(); // default set to east-facing
		
		this.theDisplay = new SpampedeDisplay(this.theData, this.screen,
				this.getSize().width, getSize().height); // how did it get this.screen -- instance variable from parent

		
		this.theDisplay.updateGraphics(); // method in SpampedeDisplay
		
		this.playSound_spam();
		
		// Hack because pictures have a delay in loading, and we won't
		//    redraw the screen again until the game actually starts,
		//    which means we wouldn't see the image until the game
		//    does start.
		// Wait a fraction of a second (200 ms), by which time the
		//    picture should have been fetched from disk, and redraw.
		try { Thread.sleep(200); } catch (InterruptedException e) {};
		this.theDisplay.updateGraphics();
	}

	/**
	 * Declares the game over.
	 */
	public void gameOver() {
		super.pause(); // pause the game
		this.theData.setGameOver(); // tell the model that the game is over
		if (this.audioMeow != null) { // play a sound
			this.audioMeow.play();
		}
		
		
	}
	
	/* -------- */
	/* Gameplay */
	/* -------- */
	
	/**
	 * Moves the game forward one step (i.e., one frame of animation,
	 * which occurs every Preferences.SLEEP_TIME milliseconds)
	 */
	public void cycle() {

		// move the snake
		this.updateSnake();

		// update the list of Spam
		this.updateSpam();

		// draw the board
		this.theDisplay.updateGraphics();

		// make the new display visible - sends the drawing to the screen
		this.repaint();

		// update the cycle counter
		this.cycleNum++;
	}

    /** 
     * Reacts to characters typed by the user.
     * <p>
     * The provided code registers the SpampedeBrain as
     * an "observer" for key presses on the keyboard.
     * 
     * So, whenever the user presses a key,
     * Java automatically calls this keyPressed method and
     * passes it a KeyEvent describing the specific keypress.
     */
	public void keyPressed(KeyEvent evt) {
		
		switch (evt.getKeyChar()) {
		// The getKeyChar method of a keypress event
		//    returns the character corresponding to the pressed key.
		
	    // TODO: Add cases to handle other keys (set the direction!)
		case REVERSE:
			this.reverseSnake();
			break;
		case AI_MODE:
			this.theData.setMode_AI();
			break;
		case PLAY_SPAM_NOISE:
			this.playSound_spam();
			break;
		case UP:
			this.theData.setDirectionNorth();
			break;
		case DOWN:
			this.theData.setDirectionSouth();
			break;
		case RIGHT:
			this.theData.setDirectionEast();
			break;
		case LEFT:
			this.theData.setDirectionWest();
			break;
		default:
			this.theData.setDirectionEast();
			break;
		}
	}


	/**
	 *  Moves the snake forward once every REFRESH_RATE cycles,
	 *  either in the current direction, or as directed by
	 *  the AI's breadth-first search.
	 *  <p>
	 *  TODO: Called by --> cycle(), test_eatSpam(), test_noSpamEaten()
	 */
	void updateSnake() {
		if (this.cycleNum % Preferences.REFRESH_RATE == 0) {
			BoardCell nextCell;
			if (this.theData.inAImode()) {
				nextCell = this.getNextCellFromBFS();
			} else {
				nextCell = this.theData.getNextCellInDir();
			}
			this.advanceTheSnake(nextCell);
		}
	}

	/**
	 * Move the snake into the next cell (and possibly eat spam)
	 * <p>
	 * This method should probably be private, but we make it
	 * public anyway to permit unit testing.
	 * 
	 * @param nextCell  New location of the snake head (which
	 *                  must be horizontally or vertically adjacent
	 *                  to the old location of the snake head).
	 */
	public void advanceTheSnake(BoardCell nextCell) {
		// Note - do not modify provided code.
		if (nextCell.isWall() || nextCell.isBody()) { // a boundry or its own body
			// Oops...we hit something.
			this.gameOver();
			return;
		} else if (nextCell.isSpam()) {
			this.playSound_spamEaten();			
			this.theData.is_spam(nextCell);
		
		} else {
			this.theData.isNot_spam(nextCell);
		}
	}


	/** 
	 * Every SPAM_ADD_RATE cycles, tries to add one new spam.
	 */
	void updateSpam() {
		if (this.theData.noSpam()) {
			this.theData.addSpam();
		} else if (this.cycleNum % Preferences.SPAM_ADD_RATE == 0) {
			this.theData.addSpam();
		}
	}


	/** 
	 * Uses BFS to search for the spam closest to the snake head.
	 * 
	 * @return Where to move the snake head, if we want to head
	 *         *one step* along the shortest path to (the nearest) 
	 *         spam cell.
	 */
	
	// Queue -- FIFO -- add to back and remove from front
	public BoardCell getNextCellFromBFS() {
		// Initialize the search.
		theData.resetCellsForNextSearch(); // all cells on the board are reset with addedSearch = false and parent = null
		
		// Initialize the cellsToSearch queue with the snake head;
		// as with any cell, we mark the head cells as having been added
		// to the queue
		Queue<BoardCell> cellsToSearch = new LinkedList<BoardCell>(); // the queue
		BoardCell snakeHead = theData.getSnakeHead();
		snakeHead.setAddedToSearchList(); // sets the Boardcell's instance variable to true
		cellsToSearch.add(snakeHead); // enqueue the starting node -- adds to end
		
        // Variable to hold the closest spam cell, once we've found it.
		//BoardCell closestSpamCell = null; // the "X" to find 
		BoardCell closestSpamCell = this.theData.getRandomNeighboringCell(snakeHead);; // the "X" to find
			
		while(!cellsToSearch.isEmpty()){
			
			BoardCell dequeued = cellsToSearch.remove(); // remove from the front
			
			//if (dequeued != null){
				BoardCell[] neighbors = this.theData.getNeighbors(dequeued);
				
				for (BoardCell n: neighbors){
					
					if(n.inSearchListAlready() == false && n.isWall() == false && n.isBody() == false){
						
						cellsToSearch.add(n); // add the BoardCell to the queue
						
						n.setAddedToSearchList(); // mark neighbor as addedToSearchList
						
						n.setParent(dequeued); // set the parent of the neighbor
						
						if(n.isSpam() == true){ // if this is the spam
							closestSpamCell = n; // mark as the closest spam
							cellsToSearch.clear();
							break; // get-out of the loop
							
							//n.clear_RestartSearch();
							
						}
					}
				}
				
			}
			
			//}
		
		// pass this closetSpamCell into the private method
		return this.getFirstCellInPath(closestSpamCell);
			
		
		// If the search fails, just move somewhere.
		//return this.theData.getRandomNeighboringCell(snakeHead);
	}

	/**
	 * Follows parent pointers back from the closest spam cell
	 * to decide where the head should move. Specifically,
	 * follows the parent pointers back from the spam until we find
	 * the cell whose parent is the snake head (and which must therefore
	 * be adjacent to the previous snake head location).
	 * <p>
	 * Recursive or looping solutions are possible.
	 *  
	 * @param start   where to start following spam pointers; this will
	 *                 be (at least initially, if you use recursion) the
	 *                 location of the spam closest to the head.
	 * @return the new cell for the snake head.
	 */
	private BoardCell getFirstCellInPath(BoardCell start) {
		
		if (start.getParent().isHead() == true){ // the parent is the head of the snake
			return start; // this is the next cell to move to
		}else {
			BoardCell parent = start.getParent();
		return getFirstCellInPath(parent);
		
		}
		
	}


	/**
	 * Reverses the snake back-to-front and updates the movement 
	 * mode appropriately.
	 */
	public void reverseSnake() {
		
		this.theData.unlabel();
		
		this.theData.reverse();
		
		this.theData.relabel();
		
		this.theData.change_direction();
	}

	
	/* ------ */
	/* Sounds */
	/* ------ */
	
	/** Plays crunch noise */
	public void playSound_spamEaten() {
		if (this.audioCrunch != null) {
			this.audioCrunch.play();
		}
	}

	/** Plays spam noise */
	public void playSound_spam() {
		if (this.audioSpam != null) {
			this.audioSpam.play();
		}
	}

	/** Plays meow noise */
	public void playSound_meow() {
		if (this.audioMeow != null) {
			this.audioMeow.play();
		}
	}

	// not used - a variable added to remove a Java warning:
	private static final long serialVersionUID = 1L;

	
	/* ---------------------- */
	/* Testing Infrastructure */
	/* ---------------------- */

	public static SpampedeBrain getTestGame(TestGame gameNum) {
		SpampedeBrain brain = new SpampedeBrain();
		brain.theData = new SpampedeData(gameNum);
		return brain;
	}
	
	public String testing_toStringParent() {
		return this.theData.toStringParents();
	}

	public BoardCell testing_getNextCellInDir() {
		return this.theData.getNextCellInDir();
	}

	public String testing_toStringSpampedeData() {
		return this.theData.toString();
	}
	
	
}
