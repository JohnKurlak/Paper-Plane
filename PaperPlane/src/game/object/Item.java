/**
 * @author		John Kurlak <kurlak@vt.edu>
 * @date		12.2.2011
 */

package game.object;

import game.*;
import java.awt.Color;
import javax.media.opengl.GL;

/**
 * Any item in the game.
 */
public class Item extends GLObject {
	private int type = 0;
	private boolean grabbed = false;
	public static final int CLOCK = 0;
	public static final int BATTERY = 1;
	public static final int EXTRA_LIFE = 2;

	public Item(CanvasModel cm, int room, int x, int y, int type) {
		super(room);
		x = roomX(cm, room, x);

		// Set dimensions based on item

		if (type == CLOCK) {
			this.setDimensions(30, 30);
			y = bottomY(cm, y, 30);
		}
		else if (type == BATTERY) {
			this.setDimensions(13, 25);
			y = bottomY(cm, y, 25);
		}
		else if (type == EXTRA_LIFE) {
			this.setDimensions(46, 20);
			y = bottomY(cm, y, 20);
		}

		this.setPosition(x, y);
		this.type = type;
	}

	@Override
	public void draw(GL gl, CanvasModel cm) {
		super.draw(gl, cm);

		if (!grabbed) {
			if (type == CLOCK) {
				drawClock(gl);
			}
			else if (type == BATTERY) {
				drawBattery(gl);
			}
			else if (type == EXTRA_LIFE) {
				drawExtraLife(gl);
			}
		}
	}

	public void drawExtraLife(GL gl) {
		GLHelper.drawPoly(gl,
			new Color(220, 220, 220),
			new Color(220, 220, 220),
			Color.GRAY,
			Color.GRAY,
			new Point(0, 19),
			new Point(6, 0),
			new Point(11, 19));

		GLHelper.drawPoly(gl,
			Color.WHITE,
			new Color(120, 120, 120),
			new Point(6, 0),
			new Point(39, 0),
			new Point(45, 19),
			new Point(12, 19));

		GLHelper.drawLine(gl,
			new Color(128, 0, 128),
			new Point(11, 14),
			new Point(43, 14));

		GLHelper.drawLine(gl,
			new Color(128, 0, 128),
			new Point(14, 1),
			new Point(18, 14));

		// Blue rule lines
		for (int y = 1; y <= 13; y += 3) {
			for (int i = 18; i <= 36; i += 3) {
				GLHelper.drawLine(gl, new Color(0, 128, 128),
					new Point(i + (y / 3), y),
					new Point(i + (y / 3), y + 1));
			}
		}

		for (int i = 0; i < 3; i++) {
			GLHelper.drawPoly(gl,
				Color.GRAY,
				Color.BLACK,
				Color.BLACK,
				Color.GRAY,
				Color.BLACK,
				new Point(18, 16),
				new Point(19, 16),
				new Point(19, 17),
				new Point(18, 17));

			gl.glTranslatef(9f, 0f, 0f);
		}
	}

	public void drawBattery(GL gl) {
		final Color copper = new Color(243, 178, 133);

		GLHelper.drawCircle(gl, Color.LIGHT_GRAY, Color.BLACK,
			new Point(6, 3), 3);

		GLHelper.drawPoly(gl,
			Color.BLACK,
			Color.GRAY,
			Color.GRAY,
			Color.BLACK,
			Color.BLACK,
			new Point(0, 2),
			new Point(13, 2),
			new Point(13, 25),
			new Point(0, 25));

		GLHelper.drawPoly(gl,
			copper,
			copper.darker(),
			copper.darker(),
			copper,
			Color.BLACK,
			new Point(0, 2),
			new Point(13, 2),
			new Point(13, 10),
			new Point(0, 10));

		GLHelper.drawLine(gl, Color.BLACK,
			new Point(3, 6),
			new Point(9, 6));
		GLHelper.drawLine(gl, Color.BLACK,
			new Point(6, 4),
			new Point(6, 8));

		GLHelper.drawLine(gl, Color.LIGHT_GRAY,
			new Point(4, 18),
			new Point(9, 18));
	}

	public void drawClock(GL gl) {
		int width = this.getWidth();
		int center = width / 2;
		Point centerPoint = new Point(center, center);
		Point leftCenterPoint = new Point(center - 2, center + 1);

		GLHelper.drawPoly(gl, new Color(128, 128, 0), Color.BLACK,
			new Point(3, 15),
			new Point(6, 15),
			new Point(6, 30),
			new Point(3, 30));

		GLHelper.drawCircle(gl, Color.WHITE, Color.BLACK,
			new Point(5, 5), 5);

		gl.glTranslatef(20f, 0f, 0f);

		GLHelper.drawPoly(gl, new Color(128, 128, 0), Color.BLACK,
			new Point(3, 15),
			new Point(6, 15),
			new Point(6, 30),
			new Point(3, 30));

		GLHelper.drawCircle(gl, Color.WHITE, Color.BLACK,
			new Point(5, 5), 5);

		gl.glTranslatef(-20f, 0f, 0f);

		GLHelper.drawCircle(gl, Color.YELLOW, Color.BLACK,
			centerPoint, 15);
		GLHelper.drawCircle(gl, new Color(128, 128, 128), Color.BLACK,
			new Point(center, center), 12);
		GLHelper.drawCircle(gl, Color.WHITE, new Color(128, 128, 128),
			new Point(center - 1, center + 1), 11);

		GLHelper.drawLine(gl, new Color(128, 128, 128), leftCenterPoint,
			new Point(center - 2, center - 7));
		GLHelper.drawLine(gl, new Color(128, 128, 128), leftCenterPoint,
			new Point(center + 4, center + 7));
		GLHelper.drawLine(gl, Color.BLACK, centerPoint,
			new Point(center, center - 8));
		GLHelper.drawLine(gl, Color.BLACK, centerPoint,
			new Point(center + 6, center + 6));
	}

	@Override
	public boolean doAction(CanvasModel cm) {
		if (!super.doAction(cm)) {
			return false;
		}

		// If we touched the item
		if (!grabbed && planeTouching(cm)) {
			grabbed = true;
			queueRefresh();

			// Handle touch according to item

			if (type == CLOCK) {
				cm.numPoints += 300;
			}
			else if (type == BATTERY) {
				cm.plane.offsetBattery(50);
			}
			else if (type == EXTRA_LIFE) {
				cm.numLives++;
			}
		}

		return true;
	}

	public void reset() {
		grabbed = false;
		queueRefresh();
	}
}