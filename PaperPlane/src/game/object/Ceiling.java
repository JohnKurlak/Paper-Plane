/**
 * @author		John Kurlak <kurlak@vt.edu>
 * @date		12.2.2011
 */

package game.object;

import game.*;
import java.awt.Color;
import javax.media.opengl.GL;

/**
 * The ceiling of a room.
 */
public class Ceiling extends GLObject {
	private final Color[] ceilingColor = new Color[4];
	private final Color brown = new Color(128, 64, 0);
	private int type = 0;
	public static final int BEIGE = 0;
	public static final int GRAY = 1;
	public static final int RED = 2;
	public static final int GREEN = 3;

	public Ceiling(CanvasModel cm, int room, int x, int y, int width,
		int height, int type) {
		super(room);
		x = roomX(cm, room, x);
		this.setPosition(x, y);
		this.setDimensions(width, height);
		this.type = type;

		// Options!
		ceilingColor[BEIGE] = new Color(162, 162, 140).brighter();
		ceilingColor[GRAY] = new Color(162, 162, 162).brighter();
		ceilingColor[RED] = new Color(177, 148, 148).brighter();
		ceilingColor[GREEN] = new Color(156, 180, 145).brighter();
	}

	@Override
	public void draw(GL gl, CanvasModel cm) {
		super.draw(gl, cm);
		int width = this.getWidth();
		int height = this.getHeight();

		GLHelper.drawPoly(gl, ceilingColor[type], ceilingColor[type],
			new Point(0, 0),
			new Point(width, 0),
			new Point(width, height),
			new Point(0, height));

		GLHelper.drawPoly(gl, brown, Color.BLACK,
			new Point(0, height - 4),
			new Point(width, height - 4),
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