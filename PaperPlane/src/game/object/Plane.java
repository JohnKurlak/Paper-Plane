/**
 * @author		John Kurlak <kurlak@vt.edu>
 * @date		12.2.2011
 */

package game.object;

import game.*;
import java.awt.Color;
import javax.media.opengl.GL;

/**
 * The paper airplane.  We're flying first class today, people!
 */
public class Plane extends GLObject {
	private int battery = 0;
	private int tick = 0;

	public Plane() {
		super(-1, 62, 22);
		reset();
	}

	public int getBattery() {
		return this.battery;
	}

	public void setBattery(int num) {
		this.battery = num;
	}

	public void offsetBattery(int offset) {
		this.battery += offset;
	}

	@Override
	public void draw(GL gl, CanvasModel cm) {
		super.draw(gl, cm);
		gl.glTranslatef(cm.room * cm.cameraWidth, 0f, 0f);

		float scalef = (getScale() / 100f);
		gl.glScalef(scalef, scalef, 1f);

		// Tilt if going backwards (left)
		if (cm.leftArrow && cm.gameState != GameState.DEAD && cm.gameState !=
			GameState.GAME_OVER) {
			setRotation(-10);
		}
		else if (cm.gameState != GameState.TITLE_SCREEN) {
			setRotation(0);
		}

		doRotation(gl);

		int alpha = (int) (getOpacity() * 2.55);
		final Color light = new Color(240, 240, 240, alpha);
		final Color dark = new Color(220, 220, 220, alpha);
		final Color border = new Color(120, 120, 120, alpha);

		// Top light left triangle
		GLHelper.drawPoly(gl, light, border,
			new Point(1, 3),
			new Point(5, 18),
			new Point(11, 3));

		// Bottom dark left triangle
		GLHelper.drawPoly(gl, dark, border,
			new Point(5, 18),
			new Point(11, 2),
			new Point(17, 18));

		// Top dark right quad
		GLHelper.drawPoly(gl, dark, border,
			new Point(25, 5),
			new Point(50, 5),
			new Point(53, 8),
			new Point(42, 17));

		// Big light middle quad
		GLHelper.drawPoly(gl, light, border,
			new Point(11, 2),
			new Point(31, 2),
			new Point(36, 22),
			new Point(16, 22));

		// Dark nose cone
		GLHelper.drawPoly(gl, dark, border,
			new Point(53, 7),
			new Point(61, 14),
			new Point(49, 14));

		// Bottom light right body
		GLHelper.drawPoly(gl, light, border,
			new Point(29, 7),
			new Point(53, 7),
			new Point(46, 22),
			new Point(17, 22));

		// Big light middle quad overlap
		GLHelper.drawPoly(gl, light, border,
			new Point(11, 2),
			new Point(31, 2),
			new Point(36, 22),
			new Point(16, 22));

		// Bottom dark right flap quad
		GLHelper.drawPoly(gl, dark, border,
			new Point(16, 22),
			new Point(24, 8),
			new Point(34, 8),
			new Point(45, 22));
	}

	@Override
	public boolean doAction(CanvasModel cm) {
		if (cm.gameState == GameState.GAME_OVER_SCREEN) {
			return false;
		}

		if (cm.gameState == GameState.TITLE_SCREEN) {
			if (++tick == 360) {
				tick = 0;
			}

			// Make plane teeter on title screen
			setRotation(-10);
			setPosition(10 * tick, 100 + (int) (Math.sin(10 * tick * Math.PI /
				180) * 20f));

			// Move plane left
			if (getX() + getWidth() > cm.cameraWidth) {
				tick = -50;
			}

			return true;
		}
		else if (cm.gameState == GameState.HOUSE_COMPLETE) {
			return false;
		}

		int planeSpeed = 4;
		int gravity = 2;

		// If just died
		if (cm.dead()) {
			// Fade away
			offsetOpacity(-3);

			// If faded
			if (getOpacity() <= 0) {
				setOpacity(100);
				setX(0);
				setY(0);

				// Advance
				if (cm.gameState == GameState.DEAD) {
					cm.startTime = System.currentTimeMillis();
					cm.gameState = GameState.GAME_PLAY;
				}
				else {
					cm.gameState = GameState.GAME_OVER_SCREEN;
				}
			}

			return true;
		}

		// Battery speed boost
		if (cm.enterKey && battery > 0) {
			planeSpeed = 20;
			battery--;
		}

		// Plane movement

		if (cm.rightArrow) {
			offsetX(planeSpeed);
		}

		if (cm.leftArrow) {
			offsetX(-planeSpeed);
		}

		if (cm.downArrow) {
			offsetY(2 * gravity);
		}

		// Gravity
		if (getY() <= cm.cameraHeight - getDimensions().y) {
			offsetY(gravity);
		}

		int halfWidth = getWidth() / 2;
		int planeMiddle = getX() + halfWidth;

		// Next room?
		if (planeMiddle > cm.cameraWidth) {
			if (cm.room < cm.NUM_ROOMS - 1) {
				cm.gameState = GameState.NEXT_ROOM;
			}
			else {
				offsetX(-planeSpeed);
			}
		}

		// Previous room?
		if (planeMiddle < 0) {
			if (cm.room > 0) {
				cm.gameState = GameState.PREV_ROOM;
			}
			else {
				offsetX(planeSpeed);
			}
		}

		// Hit floor?
		if (getY() > cm.cameraHeight - 40) {
			takeLife(cm);
		}

		return true;
	}

	public void takeLife(CanvasModel cm) {
		cm.numLives--;
		tick = 0;

		if (cm.numLives < 0) {
			cm.gameState = GameState.GAME_OVER;
		}
		else {
			cm.gameState = GameState.DEAD;
		}
	}

	@Override
	public void initDL(GL gl, CanvasModel cm) {}

	@Override
	public final void reset() {
		battery = 0;
		setOpacity(100);
		setScale(100);
		setRotation(0);
		setPosition(0, 0);
	}
}