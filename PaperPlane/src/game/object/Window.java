/**
 * @author		John Kurlak <kurlak@vt.edu>
 * @date		12.2.2011
 */

package game.object;

import game.*;
import java.awt.Color;
import javax.media.opengl.GL;

/**
 *
 * @author John Kurlak <kurlak@vt.edu>
 */
public class Window extends GLObject {
	private int winOffset = -1;

	public Window(CanvasModel cm, int room, int x, int y, int width, int
		height) {
		super(room);
		x = roomX(cm, room, x);
		this.setPosition(x, y);
		this.setDimensions(width, height);
	}

	@Override
	public void draw(GL gl, CanvasModel cm) {
		super.draw(gl, cm);
		int width = this.getWidth();
		int height = this.getHeight();
		final Color frame = new Color(128, 64, 0);

		GLHelper.drawPoly(gl, frame, Color.BLACK,
			new Point(0, 0),
			new Point(width, 0),
			new Point(width, height),
			new Point(0, height));

		GLHelper.drawPoly(gl, new Color(200, 232, 244), frame.darker(),
			new Point(5, 5),
			new Point(width - 5, 5),
			new Point(width - 5, height - 5),
			new Point(5, height - 5));

		final Color green = new Color(142, 186, 120);
		GLHelper.drawPoly(gl, green, green.darker(),
			new Point(5, height * 2 / 3 + 5),
			new Point(width - 5, height * 2 / 3 + 5),
			new Point(width - 5, height - 5),
			new Point(5, height - 5));
	}

	@Override
	public boolean doAction(CanvasModel cm) {
		if (!super.doAction(cm) && cm.gameState != GameState.HOUSE_COMPLETE) {
			return false;
		}

		if (cm.gameState == GameState.HOUSE_COMPLETE) {
			// Attempt to determine which way player was moving last
			if (winOffset == -1) {
				if (cm.leftArrow && !cm.rightArrow) {
					winOffset = -3;
				}
				else {
					winOffset = 3;
				}
			}

			// Shrink plane, and move in last direction it was going
			cm.plane.offsetScale(-3);
			cm.plane.offsetX(winOffset);
			cm.plane.offsetY(1);

			// Finish when plane is too small to see
			if (cm.plane.getScale() <= 0) {
				cm.plane.setScale(100);
				cm.plane.setX(0);
				cm.plane.setY(0);
				winOffset = -1;
				cm.gameState = GameState.WINNER;
			}

			return true;
		}

		// Give player points for finishing
		if (planeTouching(cm)) {
			cm.numPoints += 5000;
			cm.gameState = GameState.HOUSE_COMPLETE;
		}

		return true;
	}

	@Override
	public void reset() {}
}