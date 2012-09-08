/**
 * @author		John Kurlak <kurlak@vt.edu>
 * @date		12.2.2011
 */

package game.object;

import game.*;
import java.awt.Color;
import javax.media.opengl.GL;

/**
 * A table.
 */
public class Table extends GLObject {
	private final Color shelf = new Color(128, 64, 0);
	private final Color stand = new Color(255, 128, 64);

	public Table(CanvasModel cm, int room, int x, int y, int width, int
		height) {
		super(room);
		x = roomX(cm, room, x);
		y = bottomY(cm, y, height);
		this.setPosition(x, y);
		this.setDimensions(width, height);
	}

	@Override
	public void draw(GL gl, CanvasModel cm) {
		super.draw(gl, cm);
		int width = this.getWidth();
		int height = this.getHeight();
		int halfWidth = width / 2;

		// Shadow
		gl.glPushMatrix();
		gl.glTranslatef(0f, height - 3, 0f);
		gl.glScalef(1f, 0.3f, 1f);
		GLHelper.drawCircle(gl, new Color(0, 0, 0, 50), new Color(0, 0, 0, 50),
			new Point(35, 0), halfWidth);
		gl.glPopMatrix();

		GLHelper.drawPoly(gl, stand, Color.BLACK,
			new Point(halfWidth - 2, 8),
			new Point(halfWidth + 2, 8),
			new Point(halfWidth + 2, height - 10),
			new Point(halfWidth - 2, height - 10));

		GLHelper.drawLine(gl, Color.WHITE,
			new Point(halfWidth + 1, 8),
			new Point(halfWidth + 1, height - 10));

		GLHelper.drawPoly(gl, shelf, Color.BLACK,
			new Point(halfWidth - 6, height - 23),
			new Point(halfWidth + 6, height - 23),
			new Point(halfWidth + 6, height - 5),
			new Point(halfWidth - 6, height - 5));

		GLHelper.drawPoly(gl, shelf, shelf,
			new Point(0, 0),
			new Point(width, 0),
			new Point(width, 8),
			new Point(0, 8));

		GLHelper.drawLine(gl, stand,
			new Point(2, 2),
			new Point(width - 2, 2));
		GLHelper.drawLine(gl, Color.BLACK,
			new Point(2, 7),
			new Point(width - 2, 7));

		GLHelper.drawPoly(gl, shelf, Color.BLACK,
			new Point(halfWidth - 6, height - 20),
			new Point(halfWidth - 6, height - 10),
			new Point(halfWidth - 40, height),
			new Point(halfWidth - 40, height - 5));
		GLHelper.drawPoly(gl, shelf.darker(), Color.BLACK,
			new Point(halfWidth + 6, height - 20),
			new Point(halfWidth + 6, height - 10),
			new Point(halfWidth + 40, height),
			new Point(halfWidth + 40, height - 5));

		GLHelper.drawCircle(gl, Color.GRAY, Color.BLACK,
			new Point(halfWidth - 38, height), 3);
		GLHelper.drawCircle(gl, Color.GRAY, Color.BLACK,
			new Point(halfWidth + 38, height), 3);

		GLHelper.drawPoly(gl, shelf, Color.BLACK,
			new Point(halfWidth - 35, height),
			new Point(halfWidth - 40, height),
			new Point(halfWidth - 40, height - 8),
			new Point(halfWidth - 35, height - 8));
		GLHelper.drawPoly(gl, shelf, Color.BLACK,
			new Point(halfWidth + 35, height),
			new Point(halfWidth + 40, height),
			new Point(halfWidth + 40, height - 8),
			new Point(halfWidth + 35, height - 8));
	}

	@Override
	public boolean doAction(CanvasModel cm) {
		if (!super.doAction(cm)) {
			return false;
		}

		int width = this.getWidth();
		int height = this.getHeight();

		// Little hack to make hit box smaller than dimensions
		this.setDimensions(width, 8);

		// Kill player on contact
		if (!cm.dead() && planeTouching(cm)) {
			cm.plane.takeLife(cm);
		}

		// End hack
		this.setDimensions(width, height);

		return true;
	}

	@Override
	public void reset() {}
}