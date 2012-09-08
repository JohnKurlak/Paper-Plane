/**
 * @author		John Kurlak <kurlak@vt.edu>
 * @date		12.2.2011
 */

package game.object;

import game.*;
import java.awt.Color;
import javax.media.opengl.GL;

/**
 * A shelf.
 */
public class Shelf extends GLObject {
	private final Color shelf = new Color(255, 128, 64);
	private final Color support = new Color(128, 128, 0);
	private final Color supportH = new Color(140, 140, 10);

	public Shelf(CanvasModel cm, int room, int x, int y, int width) {
		super(room);
		x = roomX(cm, room, x);
		y = bottomY(cm, y - 8, 8);
		this.setPosition(x, y);
		this.setDimensions(width, 8);
	}

	@Override
	public void draw(GL gl, CanvasModel cm) {
		super.draw(gl, cm);
		int width = this.getWidth();

		GLHelper.setColor(gl, new Color(0, 0, 0, 50));

		// Shadow
		for (int i = 0; i < width / 3; i++) {
			gl.glBegin(gl.GL_LINES);
			gl.glVertex2i(3 * i + 2, 1);
			gl.glVertex2i(3 * i - 18, 21);
			gl.glEnd();
		}

		GLHelper.drawPoly(gl, shelf, shelf,
			new Point(0, 0),
			new Point(width, 0),
			new Point(width, 8),
			new Point(0, 8));

		GLHelper.drawLine(gl, Color.WHITE,
			new Point(2, 2),
			new Point(width - 2, 2));

		drawSupport(gl, 20);
		drawSupport(gl, width - 29);
	}

	public void drawSupport(GL gl, int x) {
		GLHelper.drawPoly(gl, support, Color.BLACK,
			new Point(x, 9),
			new Point(x + 3, 9),
			new Point(x + 3, 30),
			new Point(x, 30));

		GLHelper.drawPoly(gl, supportH, Color.BLACK,
			new Point(x + 3, 9),
			new Point(x + 6, 9),
			new Point(x + 6, 35),
			new Point(x + 3, 35));

		GLHelper.drawPoly(gl, support, Color.BLACK,
			new Point(x + 6, 9),
			new Point(x + 9, 9),
			new Point(x + 9, 30),
			new Point(x + 6, 30));
	}

	@Override
	public boolean doAction(CanvasModel cm) {
		if (!super.doAction(cm)) {
			return false;
		}

		// Kill player on contact
		if (!cm.dead() && planeTouching(cm)) {
			cm.plane.takeLife(cm);
		}

		return true;
	}

	@Override
	public void reset() {}
}