/**
 * @author		John Kurlak <kurlak@vt.edu>
 * @date		12.2.2011
 */

package game;

import game.object.*;

/**
 * Stores game state information.
 */
public class CanvasModel {
	public float halfWidth = 0f;
	public float halfHeight = 0f;
	public float cameraWidth = 0f;
	public float cameraHeight = 0f;
	public final int WIDTH = 800;
	public final int HEIGHT = 625;
	public int NUM_ROOMS = -1;
	public boolean rightArrow = false;
	public boolean leftArrow = false;
	public boolean upArrow = false;
	public boolean downArrow = false;
	public boolean enterKey = false;
	public boolean jKey = false;
	public boolean kKey = false;
	public Plane plane = new Plane();
	public int gameState = GameState.TITLE_SCREEN;
	public int room = 0;
	public int numLives = 0;
	public int numPoints = 0;
	public int cameraOffset = 0;
	public int maxRoom = 0;
	public long startTime = 0;

	public boolean dead() {
		return (gameState == GameState.DEAD ||
				gameState == GameState.GAME_OVER);
	}
}
