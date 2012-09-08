/**
 * @author		John Kurlak <kurlak@vt.edu>
 * @date		12.2.2011
 */

package game.object;

import game.*;
import java.awt.Color;
import javax.media.opengl.GL;

/**
 * A balloon enemy.
 */
public class Balloon extends GLObject {
	int speed = 0;

	public Balloon(CanvasModel cm, int room, int x, int y, int speed) {
		super(room);
		x = roomX(cm, room, x);
		setOpacity(70);
		this.speed = speed;
		this.setPosition(x, y);
		this.setDimensions(38, 42);
	}

	@Override
	public void draw(GL gl, CanvasModel cm) {
		super.draw(gl, cm);
		int width = this.getWidth();
		int center = width / 2;
		Point centerPoint = new Point(center, center);
		int alpha = (int) (getOpacity() * 2.55);
		final Color red = new Color(177, 26, 26, alpha);

		doRotation(gl, centerPoint);

		// Mouthpiece
		GLHelper.drawPoly(gl, red, Color.BLACK,
			new Point(15, 42),
			new Point(18, 38),
			new Point(21, 42));

		// Ellipse scale
		gl.glPushMatrix();
		gl.glScalef(0.9f, 1f, 1f);

		// Balloon
		GLHelper.drawCircle(gl,
			red,
			red.brighter(),
			Color.BLACK,
			centerPoint, 19);

		// Shine
		GLHelper.drawCircle(gl,
			new Color(255, 255, 255, 0),
			new Color(255, 255, 255, 150),
			new Color(255, 255, 255, 0),
			new Point(25, 9), 10);

		gl.glPopMatrix();
	}

	@Override
	public boolean doAction(CanvasModel cm) {
		if (!super.doAction(cm)) {
			return false;
		}

		// Move balloon up
		offsetY(-speed);

		// Tittle the balloon
		float tick = (float) this.getY() / cm.cameraHeight;
		setRotation((int) (Math.sin(tick * 50) * 10));

		// Reset position
		if (getY() + getHeight() < 0) {
			setY((int) cm.cameraHeight + 20);
		}

		// Kills player on contact
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
