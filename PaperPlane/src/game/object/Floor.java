/**
 * @author		John Kurlak <kurlak@vt.edu>
 * @date		12.2.2011
 */

package game.object;

import game.*;
import java.awt.Color;
import javax.media.opengl.GL;

/**
 * A floor in a room.
 */
public class Floor extends GLObject {
	private final Color[] floorColor = new Color[1];
	private final Color brown = new Color(128, 64, 0);
	private int type = 0;
	public static final int BLUE = 0;

	public Floor(CanvasModel cm, int room, int x, int y, int width, int height,
		int type) {
		super(room);
		x = roomX(cm, room, x);
		y = bottomY(cm, y - 10, height);
		this.setPosition(x, y);
		this.setDimensions(width, height);
		this.type = type;

		// No options yet :(
		floorColor[BLUE] = new Color(100, 100, 200);
	}

	@Override
	public void draw(GL gl, CanvasModel cm) {
		super.draw(gl, cm);
		int width = this.getWidth();
		int height = this.getHeight();

		GLHelper.drawPoly(gl, floorColor[type], floorColor[type],
			new Point(0, 0),
			new Point(width, 0),
			new Point(width, height),
			new Point(0, height));
	}

	@Override
	public boolean doAction(CanvasModel cm) {
		return true;
	}

	@Override
	public void reset() {}
}