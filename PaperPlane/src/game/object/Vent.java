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
public class Vent extends GLObject {
	private boolean pointsUp;
	private boolean visibleAir;
	private final int BOTTOM_OFFSET = 10;
	private final int TOP_OFFSET = 25;
	private final int MAGIC = (int) (0xBEEF / Math.pow(Math.PI, Math.E) /
		Math.pow(Math.PI, Math.E) * Math.PI - Math.E - 1); // lol.. I don't even
	private final Color vent = new Color(128, 128, 0);
	private final Color ventB = new Color(128, 64, 0);
	private final Color hole = vent.darker().darker().darker();
	private final Color holeB = hole.brighter();
	private final Color border = new Color(0, 0, 0);
	private final Color air = new Color(0, 0, 255);
	private final Color airB = new Color(0, 255, 255);

	public Vent(CanvasModel cm, int room, int x, int height, boolean pointsUp,
		boolean visibleAir) {
		super(room);
		x = roomX(cm, room, x);
		this.setDimensions(20, height);
		int y = ((int) cm.cameraHeight - BOTTOM_OFFSET) - height;

		if (!pointsUp) {
			y = BOTTOM_OFFSET;
		}

		this.setPosition(x, y);
		this.pointsUp = pointsUp;
		this.visibleAir = visibleAir;
	}

	@Override
	public void draw(GL gl, CanvasModel cm) {
		super.draw(gl, cm);
		int h = this.getHeight();

		if (pointsUp) {
			GLHelper.drawPoly(gl, Color.BLACK, Color.BLACK,
				new Point(-25, h - 18),
				new Point(25, h - 18),
				new Point(45, h + 2),
				new Point(-5, h + 2));

			GLHelper.drawPoly(gl, ventB, ventB,
				new Point(-25, h - 19),
				new Point(25, h - 19),
				new Point(45, h + 1),
				new Point(-5, h + 1));

			GLHelper.drawPoly(gl, vent, ventB,
				new Point(-25, h - 20),
				new Point(25, h - 20),
				new Point(45, h),
				new Point(-5, h));

			// Vent grate
			for (int i = 0; i < 11; i++) {
				GLHelper.setColor(gl, Color.BLACK);
				gl.glBegin(GL.GL_LINES);
				gl.glVertex2i(-19 + 4 * i, h - 18);
				gl.glVertex2i(-2 + 4 * i, h - 2);
				gl.glEnd();
				GLHelper.setColor(gl, ventB);
				gl.glBegin(GL.GL_LINES);
				gl.glVertex2i(-19 + 4 * i + 1, h - 18);
				gl.glVertex2i(-2 + 4 * i + 1, h - 2);
				gl.glEnd();
			}
		}
		else {
			GLHelper.drawPoly(gl, hole, hole, holeB, holeB, border,
				new Point(-25, TOP_OFFSET),
				new Point(25, TOP_OFFSET),
				new Point(45, TOP_OFFSET + 20),
				new Point(-5, TOP_OFFSET + 20));
			gl.glTranslatef(0f, (float) TOP_OFFSET, 0f);
		}

		if (visibleAir) {
			gl.glBegin(GL.GL_POINTS);

			// Draw the most awesome air you've ever seen
			for (int j = 0; j < h - 20; j++) {
				int s = (int) (Math.sin(MAGIC * j * Math.PI / 180) * 2f);

				for (int i = 0; i < 5; i++) {
					GLHelper.setColor(gl, air);
					gl.glVertex2i(4 * i + s, j);
					GLHelper.setColor(gl, airB);
					gl.glVertex2i(4 * i + s + 1, j);
				}
			}

			gl.glEnd();
		}
	}

	@Override
	public boolean doAction(CanvasModel cm) {
		if (!super.doAction(cm) || cm.dead()) {
			return false;
		}

		if (planeOverHoriz(cm)) {
			if (pointsUp) {
				// If player over up vent
				if (cm.plane.getY() + (cm.plane.getHeight() / 2) >
					cm.cameraHeight - this.getHeight() - BOTTOM_OFFSET) {
					cm.plane.offsetY(-6);

					if (cm.downArrow) {
						cm.plane.offsetY(-4);
					}
				}
			}
			else {
				// If player over down vent
				if (cm.plane.getY() <= TOP_OFFSET + this.getHeight()) {
					cm.plane.offsetY(2);

					if (cm.downArrow) {
						cm.plane.offsetY(-2);
					}
				}
			}
		}

		return true;
	}

	public void setVisibleAir(boolean visible) {
		this.visibleAir = visible;
	}

	@Override
	public void initDL(GL gl, CanvasModel cm) {
		int dl = gl.glGenLists(1);
		this.setDisplayList(dl);
        gl.glNewList(dl, GL.GL_COMPILE);
        draw(gl, cm);
        gl.glEndList();
	}

	@Override
	public void reset() {}
}