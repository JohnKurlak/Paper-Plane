/**
 * @author		John Kurlak <kurlak@vt.edu>
 * @date		12.2.2011
 */

package game.object;

import game.*;
import java.awt.Color;
import javax.media.opengl.GL;

/**
 * A cabinet object
 */
public class Cabinet extends GLObject {
	private final Color top = new Color(255, 128, 64);
	private final Color base = new Color(128, 64, 0);
	private boolean aboveGround = false;

	public Cabinet(CanvasModel cm, int room, int x, int y, int width,
		int height) {
		super(room);
		aboveGround = (y > 0);
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
		int w =  (width - 10) / 2;

		GLHelper.setColor(gl, new Color(0, 0, 0, 50));

		// Draw shadow
		for (int i = 0; i < height / 3; i++) {
			gl.glBegin(gl.GL_LINES);

			int stop = 10;

			// Main shadow goes out
			if (i < height / 3 - stop) {
				gl.glVertex2i(0, 3 * i + 2);
				gl.glVertex2i(-20, 3 * i + 22);
			}
			// Bottom shadow comes in
			else {
				int j = (int) ((float) (i - height / 3 + stop) / (float) stop * 20f);
				gl.glVertex2i(0, 3 * i + 2);
				gl.glVertex2i(-20 + j, 3 * i + 22 - j);
			}

			gl.glEnd();
		}

		int offsetY = 8;

		if (!aboveGround) {
			GLHelper.drawPoly(gl, top, Color.BLACK,
				new Point(0, 0),
				new Point(width, 0),
				new Point(width, 8),
				new Point(0, 8));
			offsetY = 0;
		}

		GLHelper.drawPoly(gl, base, Color.BLACK,
				new Point(2, 8 - offsetY),
				new Point(width - 2, 8 - offsetY),
				new Point(width - 2, height - 6),
				new Point(2, height - 6));

		GLHelper.drawPoly(gl, base, Color.BLACK,
			new Point(12, 16 - offsetY),
			new Point(w, 16 - offsetY),
			new Point(w, height - 16),
			new Point(12, height - 16));

		GLHelper.drawLine(gl, top,
			new Point(12, 16 - offsetY),
			new Point(12, height - 16));

		GLHelper.drawLine(gl, top,
			new Point(12, height - 16),
			new Point(w, height - 16));

		GLHelper.drawPoly(gl, base, Color.BLACK,
			new Point(12 + w, 16 - offsetY),
			new Point(2 * w - 2, 16 - offsetY),
			new Point(2 * w - 2, height - 16),
			new Point(12 + w, height - 16));

		GLHelper.drawLine(gl, top,
			new Point(12 + w, 16 - offsetY),
			new Point(12 + w, height - 16));

		GLHelper.drawLine(gl, top,
			new Point(12 + w, height - 16),
			new Point(2 * w - 2, height - 16));

		GLHelper.drawPoly(gl, Color.BLACK, Color.BLACK,
			new Point(4, height - 6),
			new Point(width - 4, height - 6),
			new Point(width - 4, height),
			new Point(4, height));
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