/**
 * @author		John Kurlak <kurlak@vt.edu>
 * @date		12.2.2011
 */

package game;

import java.awt.Color;
import javax.media.opengl.GL;

/**
 * Provides a lot of utility functions for dealing with OpenGL.
 */
public class GLHelper {
	public static void drawLine(GL gl, Color color, Point one, Point two) {
		setColor(gl, color);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2i(one.x, one.y);
		gl.glVertex2i(two.x, two.y);
		gl.glEnd();
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2i(one.x, one.y);
		gl.glVertex2i(two.x, two.y);
		gl.glEnd();
	}

	public static void drawCircle(GL gl, Color colorOutside, Color colorInside,
		Color stroke, Point center, int radius) {
		double increment = 2 * Math.PI / 50;

		// Draw a bunch of triangles
		for (double angle = 0; angle < 2 * Math.PI; angle += increment) {
			float x1 = center.x + (float) Math.cos(angle) * radius;
			float y1 = center.y + (float) Math.sin(angle) * radius;
			float x2 = center.x + (float) Math.cos(angle + increment) * radius;
			float y2 = center.y + (float) Math.sin(angle + increment) * radius;

			gl.glBegin(GL.GL_TRIANGLES);
			setColor(gl, colorInside);
			gl.glVertex2d(center.x, center.y);
			setColor(gl, colorOutside);
			gl.glVertex2f(x1, y1);
			gl.glVertex2f(x2, y2);
			gl.glEnd();
		}

		gl.glBegin(GL.GL_LINE_LOOP);

		// Highlight the stroke edge of each triangle
		for (double angle = 0; angle < 2 * Math.PI; angle += increment) {
			float x1 = center.x + (float) Math.cos(angle) * radius;
			float y1 = center.y + (float) Math.sin(angle) * radius;
			float x2 = center.x + (float) Math.cos(angle + increment) * radius;
			float y2 = center.y + (float) Math.sin(angle + increment) * radius;

			setColor(gl, stroke);
			gl.glVertex2f(x1, y1);
			gl.glVertex2f(x2, y2);
		}

		gl.glEnd();
	}

	public static void drawCircle(GL gl, Color color, Color stroke,
		Point center, int radius) {
		drawCircle(gl, color, color, stroke, center, radius);
	}

	public static void drawPoly(GL gl, Color color, Color stroke, Point one,
		Point two, Point three) {
		drawPoly(gl, color, color, color, null, stroke, one, two, three, null);
	}

	public static void drawPoly(GL gl, Color color, Color stroke, Point one,
		Point two, Point three, Point four) {
		drawPoly(gl, color, color, color, color, stroke, one, two, three, four);
	}

	public static void drawPoly(GL gl, Color colorOne, Color colorTwo, Color
		colorThree, Color stroke, Point one, Point two, Point three) {
		drawPoly(gl, colorOne, colorTwo, colorThree, null, stroke, one, two,
			three, null);
	}

	public static void drawPoly(GL gl, Color colorOne, Color colorTwo, Color
		colorThree, Color colorFour, Color stroke, Point one, Point two, Point
		three, Point four) {
		// Draw the shape

		if (four == null) {
			gl.glBegin(GL.GL_TRIANGLES);
		}
		else {
			gl.glBegin(GL.GL_QUADS);
		}

		setColor(gl, colorOne);
		gl.glVertex2i(one.x, one.y);
		setColor(gl, colorTwo);
		gl.glVertex2i(two.x, two.y);
		setColor(gl, colorThree);
		gl.glVertex2i(three.x, three.y);

		if (four != null) {
			setColor(gl, colorFour);
			gl.glVertex2i(four.x, four.y);
		}

		gl.glEnd();

		// Draw the stroke

		setColor(gl, stroke);
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex2i(one.x, one.y);
		gl.glVertex2i(two.x, two.y);
		gl.glVertex2i(three.x, three.y);

		if (four != null) {
			gl.glVertex2i(four.x, four.y);
		}

		gl.glEnd();
	}

	public static void setColor(GL gl, Color color) {
		if (color != null) {
			float red = color.getRed() / 255f;
			float green = color.getGreen() / 255f;
			float blue = color.getBlue() / 255f;
			float alpha = color.getAlpha() / 255f;

			gl.glColor4f(red, green, blue, alpha);
		}
	}

	public static void setDrawOffset(GL gl, int offsetX, int offsetY) {
		gl.glTranslatef((float) offsetX, (float) offsetY, 0f);
	}

	public static void setDrawPosition(GL gl, CanvasModel cm, int x, int y) {
		resetPosition(gl, cm);
		setDrawOffset(gl, x, y);
	}

	public static void resetPosition(GL gl, CanvasModel cm) {
		gl.glLoadIdentity();
		gl.glScalef(1f, -1f, 0f);
		gl.glTranslatef(-cm.halfWidth, -cm.halfHeight, 0f);
	}
}