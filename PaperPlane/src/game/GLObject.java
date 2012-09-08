/**
 * @author		John Kurlak <kurlak@vt.edu>
 * @date		12.2.2011
 */

package game;

import javax.media.opengl.GL;

/**
 * Represents an object in a room.
 */
public abstract class GLObject {
	private Point dimensions = new Point(0, 0);
	private Point position = new Point(0, 0);
	private int rotation = 0;
	private int scale = 0;
	private int opacity = 0;
	private int displayList = -1;
	private int room = 0;
	public static int REFRESH_DISPLAY = -2;

	public GLObject(int room) {
		this.room = room;
	}

	public GLObject(int room, int width, int height) {
		this(room, width, height, 0);
	}

	public GLObject(int room, int width, int height, int rotation) {
		this.room = room;
		this.setDimensions(width, height);
		this.setRotation(rotation);
	}

	public void draw(GL gl, CanvasModel cm) {
		GLHelper.setDrawPosition(gl, cm, this.position.x, this.position.y);
	}

	public boolean doAction(CanvasModel cm) {
		if (cm.gameState == GameState.TITLE_SCREEN ||
			cm.gameState == GameState.GAME_OVER_SCREEN) {
			return false;
		}

		if (cm.room != room) {
			return false;
		}

		return true;
	}

	public int getRoom() {
		return room;
	}

	public final void setDimensions(int width, int height) {
		this.dimensions.x = width;
		this.dimensions.y = height;
	}

	public final void offsetX(int offset) {
		this.position.x += offset;
	}

	public final void offsetY(int offset) {
		this.position.y += offset;
	}

	public int getX() {
		return this.position.x;
	}

	public int getY() {
		return this.position.y;
	}

	public void setX(int x) {
		this.position.x = x;
	}

	public void setY(int y) {
		this.position.y = y;
	}

	public Point getDimensions() {
		return this.dimensions;
	}

	public int getHeight() {
		return this.dimensions.y;
	}

	public int getWidth() {
		return this.dimensions.x;
	}

	public final void setPosition(int x, int y) {
		this.position.x = x;
		this.position.y = y;
	}

	public Point getPosition() {
		return this.position;
	}

	public final void setRotation(int angle) {
		this.rotation = angle;
	}

	public int getRotation() {
		return this.rotation;
	}

	public void setScale(int num) {
		this.scale = num;
	}

	public int getScale() {
		return this.scale;
	}

	public int getDisplayList() {
		return this.displayList;
	}

	public void setDisplayList(int num) {
		this.displayList = num;
	}

	public void drawFast(GL gl, CanvasModel cm) {
		// If we have a display list
		if (displayList >= 0) {
			gl.glCallList(displayList);
		}
		// If we need to update our display list
		else if (displayList == GLObject.REFRESH_DISPLAY) {
			initDL(gl, cm);
		}
		// If we don't want a display list
		else {
			draw(gl, cm);
		}
	}

	public void initDL(GL gl, CanvasModel cm) {
		int dl = gl.glGenLists(1);
		this.setDisplayList(dl);
        gl.glNewList(dl, GL.GL_COMPILE);
        draw(gl, cm);
        gl.glEndList();
	}

	public boolean planeOverHoriz(CanvasModel cm) {
		int objectX = this.getX() - cm.cameraOffset;
		int planeX = cm.plane.getX();
		return planeX + cm.plane.getWidth() >= objectX &&
			planeX <= objectX + this.getWidth();
	}

	public boolean planeOverVert(CanvasModel cm) {
		int objectY = this.getY();
		int planeY = cm.plane.getY();
		return planeY + cm.plane.getHeight() >= objectY &&
			planeY <= objectY + this.getHeight();
	}

	public boolean planeTouching(CanvasModel cm) {
		return planeOverHoriz(cm) && planeOverVert(cm);
	}

	public void queueRefresh() {
		// Ensure that we have the most up-to-date state in our display list
		setDisplayList(GLObject.REFRESH_DISPLAY);
	}

	public void doRotation(GL gl) {
		gl.glTranslatef(-this.getHeight() / 2f, -this.getWidth() / 2f, 0f);
		gl.glRotatef(rotation, 0f, 0f, 1f);
		gl.glTranslatef(this.getHeight() / 2f, this.getWidth() / 2f, 0f);
	}

	public void doRotation(GL gl, Point center) {
		gl.glTranslatef(center.x, center.y, 0f);
		gl.glRotatef(rotation, 0f, 0f, 1f);
		gl.glTranslatef(-center.x, -center.y, 0f);
	}

	public int getOpacity() {
		return this.opacity;
	}

	public void setOpacity(int num) {
		this.opacity = num;
	}

	public void offsetOpacity(int offset) {
		this.opacity += offset;
	}

	public void offsetScale(int offset) {
		this.scale += offset;
	}

	public int roomX(CanvasModel cm, int room, int x) {
		return x + room * (int) cm.cameraWidth;
	}

	public int bottomY(CanvasModel cm, int y, int height) {
		return -y + (int) cm.cameraHeight - height - 10;
	}

	public abstract void reset();
}
