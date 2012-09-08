/**
 * @author		John Kurlak <kurlak@vt.edu>
 * @date		12.2.2011
 */

package game.object;

import game.*;
import java.awt.Color;
import javax.media.opengl.GL;

/**
 * A wall in a room.
 */
public class Wall extends GLObject {
	private final Color[] paperColor = new Color[4];
	private int type = 0;
	public static final int BEIGE = 0;
	public static final int GRAY = 1;
	public static final int RED = 2;
	public static final int GREEN = 3;

	public Wall(CanvasModel cm, int room, int x, int y, int width, int height,
		int type) {
		super(room);
		x = roomX(cm, room, x);
		this.setPosition(x, y);
		this.setDimensions(width, height);
		this.type = type;

		// Options!
		paperColor[BEIGE] = new Color(162, 162, 140);
		paperColor[GRAY] = new Color(162, 162, 162);
		paperColor[RED] = new Color(177, 148, 148);
		paperColor[GREEN] = new Color(156, 180, 145);
	}

	@Override
	public void draw(GL gl, CanvasModel cm) {
		super.draw(gl, cm);
		int width = this.getWidth();
		int height = this.getHeight();

		GLHelper.drawPoly(gl, paperColor[type], paperColor[type],
			new Point(0, 0),
			new Point(width, 0),
			new Point(width, height),
			new Point(0, height));

		final Color baseboard = new Color(128, 64, 0);
		GLHelper.drawPoly(gl, Color.BLACK, Color.BLACK,
			new Point(0, height - 20),
			new Point(width, height - 20),
			new Point(width, height),
			new Point (0, height));
		GLHelper.drawPoly(gl, baseboard, baseboard,
			new Point(0, height - 19),
			new Point(width, height - 19),
			new Point(width, height - 1),
			new Point (0, height - 1));
		GLHelper.drawLine(gl, Color.LIGHT_GRAY,
			new Point(0, height - 18),
			new Point(width, height - 18));

		GLHelper.drawPoly(gl, Color.BLACK, Color.BLACK,
			new Point(0, height - 5),
			new Point(width, height - 5),
			new Point(width, height),
			new Point (0, height));
		GLHelper.drawPoly(gl, baseboard, baseboard,
			new Point(0, height - 4),
			new Point(width, height - 4),
			new Point(width, height - 1),
			new Point (0, height - 1));
		GLHelper.drawLine(gl, Color.LIGHT_GRAY,
			new Point(0, height - 3),
			new Point(width, height - 3));
	}

	@Override
	public boolean doAction(CanvasModel cm) {
		return true;
	}

	@Override
	public void reset() {}
}