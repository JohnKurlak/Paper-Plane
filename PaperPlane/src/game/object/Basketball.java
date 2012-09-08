/**
 * @author		John Kurlak <kurlak@vt.edu>
 * @date		12.2.2011
 */

package game.object;

import game.*;
import java.awt.Color;
import javax.media.opengl.GL;

/**
 * A basketball enemy.
 */
public class Basketball extends GLObject {
	int range = 0;
	float baseSpeed = 13f;
	private int origY = 0;
	private float speed = 0;

	public Basketball(CanvasModel cm, int room, int x, int y, int range) {
		super(room);
		x = roomX(cm, room, x);
		y = bottomY(cm, y, 38);
		this.setPosition(x, y);
		this.setDimensions(38, 38);
		this.range = range;
		this.origY = y;

		// Cool formula I made to determine initial velocity necessary to make
		// basketball bounce a certain height... BALLER!
		int c = -(4 * range);
		this.baseSpeed = (-1f + (float) Math.sqrt(1f - 16f * c)) / 8f;
	}

	@Override
	public void draw(GL gl, CanvasModel cm) {
		super.draw(gl, cm);
		int width = this.getWidth();
		int center = width / 2;
		Point centerPoint = new Point(center, center);
		final Color orange = new Color(255, 128, 0);
		final Color lineColor = new Color(0, 0, 0, 70);

		// Ball
		GLHelper.drawCircle(gl,
			orange,
			orange.brighter(),
			Color.BLACK,
			centerPoint, 19);

		// Spin that sucker
		doRotation(gl, centerPoint);

		// Ugly basketball lines follow

		GLHelper.drawLine(gl, lineColor,
			new Point(3, 12),
			new Point(9, 11));
		GLHelper.drawLine(gl, lineColor,
			new Point(13, 8),
			new Point(9, 11));
		GLHelper.drawLine(gl, lineColor,
			new Point(13, 8),
			new Point(16, 5));
		GLHelper.drawLine(gl, lineColor,
			new Point(16, 2),
			new Point(16, 5));

		GLHelper.drawLine(gl, lineColor,
			new Point(3, 26),
			new Point(6, 22));
		GLHelper.drawLine(gl, lineColor,
			new Point(7, 21),
			new Point(6, 22));
		GLHelper.drawLine(gl, lineColor,
			new Point(7, 21),
			new Point(13, 15));
		GLHelper.drawLine(gl, lineColor,
			new Point(22, 9),
			new Point(13, 15));
		GLHelper.drawLine(gl, lineColor,
			new Point(22, 9),
			new Point(23, 8));
		GLHelper.drawLine(gl, lineColor,
			new Point(27, 7),
			new Point(23, 8));
		GLHelper.drawLine(gl, lineColor,
			new Point(27, 7),
			new Point(34, 8));

		GLHelper.drawLine(gl, lineColor,
			new Point(14, 36),
			new Point(14, 27));
		GLHelper.drawLine(gl, lineColor,
			new Point(16, 22),
			new Point(14, 27));
		GLHelper.drawLine(gl, lineColor,
			new Point(16, 22),
			new Point(17, 20));
		GLHelper.drawLine(gl, lineColor,
			new Point(22, 16),
			new Point(17, 20));
		GLHelper.drawLine(gl, lineColor,
			new Point(22, 16),
			new Point(26, 16));
		GLHelper.drawLine(gl, lineColor,
			new Point(29, 17),
			new Point(26, 16));
		GLHelper.drawLine(gl, lineColor,
			new Point(29, 17),
			new Point(30, 18));
		GLHelper.drawLine(gl, lineColor,
			new Point(33, 20),
			new Point(30, 18));
		GLHelper.drawLine(gl, lineColor,
			new Point(33, 20),
			new Point(35, 27));

		GLHelper.drawLine(gl, lineColor,
			new Point(9, 4),
			new Point(14, 9));
		GLHelper.drawLine(gl, lineColor,
			new Point(18, 15),
			new Point(14, 9));
		GLHelper.drawLine(gl, lineColor,
			new Point(18, 15),
			new Point(21, 21));
		GLHelper.drawLine(gl, lineColor,
			new Point(22, 26),
			new Point(21, 21));
		GLHelper.drawLine(gl, lineColor,
			new Point(22, 26),
			new Point(24, 31));
		GLHelper.drawLine(gl, lineColor,
			new Point(23, 36),
			new Point(24, 31));

	}

	@Override
	public boolean doAction(CanvasModel cm) {
		if (!super.doAction(cm)) {
			return false;
		}

		// Bring basketball down
		speed += 0.5;

		// Spin ball
		setRotation(getRotation() + 2);

		// Bounce on contact
		if (getY() >= origY) {
			speed = -baseSpeed;
		}

		// Velocity
		offsetY((int) speed);

		// Kill player on contact
		if (!cm.dead() && planeTouching(cm)) {
			cm.plane.takeLife(cm);
		}

		return true;
	}

	@Override
	public void initDL(GL gl, CanvasModel cm) {}

	@Override
	public void reset() {}
}