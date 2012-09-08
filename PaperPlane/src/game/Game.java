/**
 * @author		John Kurlak <kurlak@vt.edu>
 * @date		12.2.2011
 */

package game;

import game.object.*;
import com.sun.opengl.util.FPSAnimator;
import com.sun.opengl.util.j2d.TextRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.*;

/**
 * Main game class.
 */
public class Game extends JFrame implements
	GLEventListener, KeyListener, ActionListener, MouseListener {
	// Initial values and some settings
	private final String APP_TITLE = "Paper Plane";
	private final int INIT_NUM_LIVES = 10;
	private int frames = 0;
    private long time;
	private long timestart;
    private float framerate;
	private int tempRoom = -1;
	private ArrayList<GLObject> sprites = new ArrayList<GLObject>();
	private CanvasModel cm = new CanvasModel();
	private TextRenderer titleRenderer;
	private TextRenderer hudRenderer;

    public static void main(String[] args) {
		// Run this in the AWT event thread to prevent deadlocks and race
		// conditions
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Switch to system look and feel for native font rendering,
				// etc.
                try {
                    UIManager.setLookAndFeel(UIManager.
						getSystemLookAndFeelClassName());
                } catch(Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO,
						"Cannot enable system look and feel.", ex);
                }

                Game frame = new Game();
                frame.setVisible(true);
            }
        });
    }

	public Game(){
		setupGL();
		startAnimationTimer();
		setupWindow();
	}

	private void setupGL() {
		GLCapabilities capabilities = new GLCapabilities();
		capabilities.setHardwareAccelerated(true);
		capabilities.setDoubleBuffered(true);

		// Create GL canvas
		GLCanvas canvas = new GLCanvas(capabilities);
        canvas.addGLEventListener(this);
		canvas.addKeyListener(this);
		canvas.addMouseListener(this);

		// Draw Swing widgets + OpenGL
		setLayout(new BorderLayout());
		addCheckbox();
		add(canvas);

		// Setup animator
		setupAnimator(canvas);
	}

	private void addCheckbox() {
		final JCheckBox check = new JCheckBox("Show air streams?");
		check.setSelected(true);
		check.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (GLObject sprite : sprites) {
					// Toggle air stream for each vent
					if (sprite instanceof Vent) {
						Vent vent = (Vent) sprite;
						vent.setVisibleAir(check.isSelected());
						vent.queueRefresh();
					}
				}
			}

		});

		add(check, BorderLayout.NORTH);
	}

	private void setupAnimator(GLCanvas canvas) {
		// Use JOGL's Animator utility for rendering
        //final Animator animator = new Animator(canvas);
		final FPSAnimator animator = new FPSAnimator(canvas, 60);
		animator.setRunAsFastAsPossible(true);

        // Stop the Animator when we receive a window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Run this on another thread than the AWT event queue to
                // make sure the call to Animator.stop() completes before
                // exiting
                new Thread(new Runnable() {
                    public void run() {
                        animator.stop();
                        System.exit(0);
                    }
                }).start();
            }
        });

		animator.start();
	}

	private void setupWindow() {
		setSize(cm.WIDTH, cm.HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);
		setResizable(false);
		setTitle(APP_TITLE + " | Loading...");
	}

	private void startAnimationTimer() {
		Timer timer = new Timer(25, this);
		timer.start();
	}

	/*
	 * Init event -- initialize OpenGL graphics.
	 */
    public void init(GLAutoDrawable drawable) {
        // Use debug pipeline; all OpenGL error codes will be automatically
        // converted to GLExceptions as soon as they appear
        drawable.setGL(new DebugGL(drawable.getGL()));

        GL gl = drawable.getGL();

        // Setup canvas
        gl.glClearColor(255.0f, 255.0f, 255.0f, 255.0f);
		enableAntialias(gl);


		titleRenderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 32));
		hudRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 12));

		resetGame();
    }

	private void resetGame() {
		// Undo everything that may have been changed
		cm.gameState = GameState.TITLE_SCREEN;
		cm.numLives = INIT_NUM_LIVES;
		tempRoom = cm.room;
		cm.room = 0;
		cm.maxRoom = 0;
		cm.numPoints = 0;
		updateCamera();

		// Reset each sprite
		for (GLObject sprite : sprites) {
			sprite.reset();
		}

		cm.plane.reset();
	}

	private void updateCamera() {
		cm.cameraOffset = cm.room * (int) cm.cameraWidth;
	}

	private void enableAntialias(GL gl) {
		gl.glEnable(GL.GL_LINE_SMOOTH);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_DONT_CARE);
		gl.glLineWidth(0.3f);
	}

	/*
	 * Reshape event -- called when the canvas is resized and at startup.
     * setup the viewport and camera projection transform.
	 */
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int
		height) {
        GL gl = drawable.getGL();
        GLU glu = new GLU();

        // Avoid a divide by zero error!
        if (height <= 0) {
            height = 1;
		}

		// Set a single viewport that consumes the entire canvas
		gl.glViewport(0, 0, width, height);

		// Set the camera projection transform to be orthogonal 2D
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();

		// Setup everything
		if (cm.halfWidth == 0) {
			cm.halfWidth = width / 2;
			cm.halfHeight = height / 2;
			cm.cameraWidth = width;
			cm.cameraHeight = height;

			int room = -1;

			// Prepare rooms!

			room++;
			sprites.add(new Vent(cm, room, 200, 450, true, true));
			sprites.add(new Vent(cm, room, 600, 300, true, true));
			sprites.add(new Table(cm, room, 250, 0, 200, 200));
			sprites.add(new Item(cm, room, 260, 201, Item.CLOCK));
			drawRoom(cm, room, Ceiling.BEIGE, Wall.BEIGE, Floor.BLUE);

			room++;
			sprites.add(new Vent(cm, room, 100, 450, true, true));
			sprites.add(new Cabinet(cm, room, 300, 0, 150, 150));
			sprites.add(new Vent(cm, room, 500, 500, true, true));
			sprites.add(new Cabinet(cm, room, 575, 0, 100, 250));
			sprites.add(new Cabinet(cm, room, 575, 375, 100, 100));
			sprites.add(new Shelf(cm, room, 700, 400, 75));
			sprites.add(new Item(cm, room, 730, 401, Item.EXTRA_LIFE));
			drawRoom(cm, room, Ceiling.GRAY, Wall.GRAY, Floor.BLUE);

			room++;
			sprites.add(new Vent(cm, room, 100, 470, true, true));
			sprites.add(new Vent(cm, room, 600, 300, true, true));
			sprites.add(new Basketball(cm, room, 250, 200, 100));
			sprites.add(new Basketball(cm, room, 320, 200, 400));
			sprites.add(new Basketball(cm, room, 410, 200, 300));
			sprites.add(new Balloon(cm, room, 500, 200, 3));
			sprites.add(new Balloon(cm, room, 520, 270, 4));
			sprites.add(new Balloon(cm, room, 540, 200, 5));
			sprites.add(new Table(cm, room, 250, 0, 200, 200));
			sprites.add(new Item(cm, room, 520, 0, Item.EXTRA_LIFE));
			drawRoom(cm, room, Ceiling.GREEN, Wall.GREEN, Floor.BLUE);

			room++;
			sprites.add(new Vent(cm, room, 300, 260, true, true));
			sprites.add(new Table(cm, room, 350, 0, 150, 150));
			sprites.add(new Item(cm, room, 450, 150, Item.CLOCK));
			sprites.add(new Cabinet(cm, room, 175, 375, 100, 100));
			sprites.add(new Cabinet(cm, room, 575, 375, 100, 100));
			sprites.add(new Basketball(cm, room, 650, 0, 300));
			drawRoom(cm, room, Ceiling.RED, Wall.RED, Floor.BLUE);

			room++;
			sprites.add(new Balloon(cm, room, 300, 300, 3));
			sprites.add(new Basketball(cm, room, 485, 0, 400));
			sprites.add(new Shelf(cm, room, 100, 300, 100));
			sprites.add(new Shelf(cm, room, 200, 175, 150));
			sprites.add(new Shelf(cm, room, 550, 400, 100));
			sprites.add(new Item(cm, room, 120, 301, Item.CLOCK));
			sprites.add(new Item(cm, room, 200, 176, Item.BATTERY));
			sprites.add(new Item(cm, room, 280, 176, Item.CLOCK));
			sprites.add(new Cabinet(cm, room, 550, 0, 100, 200));
			sprites.add(new Vent(cm, room, 50, 350, true, true));
			sprites.add(new Vent(cm, room, 150, 60, true, true));
			sprites.add(new Vent(cm, room, 250, 320, false, true));
			sprites.add(new Vent(cm, room, 250, 160, true, true));
			sprites.add(new Vent(cm, room, 400, 450, true, true));
			sprites.add(new Vent(cm, room, 700, 350, true, true));
			drawRoom(cm, room, Ceiling.BEIGE, Wall.BEIGE, Floor.BLUE);

			room++;
			sprites.add(new Basketball(cm, room, 160, 101, 150));
			sprites.add(new Item(cm, room, 160, 101, Item.EXTRA_LIFE));
			sprites.add(new Shelf(cm, room, 150, 400, 130));
			sprites.add(new Shelf(cm, room, 370, 400, 100));
			sprites.add(new Shelf(cm, room, 480, 400, 100));
			sprites.add(new Shelf(cm, room, 570, 300, 100));
			sprites.add(new Shelf(cm, room, 380, 200, 300));
			sprites.add(new Shelf(cm, room, 150, 300, 285));
			sprites.add(new Shelf(cm, room, 150, 100, 300));
			sprites.add(new Vent(cm, room, 100, 500, true, true));
			sprites.add(new Vent(cm, room, 600, 100, true, true));
			drawRoom(cm, room, Ceiling.GRAY, Wall.GRAY, Floor.BLUE);

			room++;
			sprites.add(new Item(cm, room, 50, 501, Item.CLOCK));
			sprites.add(new Shelf(cm, room, 50, 500, 300));
			sprites.add(new Item(cm, room, 650, 401, Item.EXTRA_LIFE));
			sprites.add(new Shelf(cm, room, 600, 400, 100));
			sprites.add(new Vent(cm, room, 50, 495, true, true));
			sprites.add(new Vent(cm, room, 150, 100, true, true));
			sprites.add(new Vent(cm, room, 270, 162, true, true));
			sprites.add(new Vent(cm, room, 450, 162, true, true));
			sprites.add(new Vent(cm, room, 550, 162, true, true));
			sprites.add(new Vent(cm, room, 720, 200, true, true));
			sprites.add(new Item(cm, room, 350, 301, Item.EXTRA_LIFE));
			sprites.add(new Item(cm, room, 400, 301, Item.EXTRA_LIFE));
			sprites.add(new Item(cm, room, 150, 301, Item.CLOCK));
			sprites.add(new Cabinet(cm, room, 150, 200, 500, 100));
			sprites.add(new Table(cm, room, 150, 0, 500, 170));
			drawRoom(cm, room, Ceiling.RED, Wall.RED, Floor.BLUE);

			room++;
			sprites.add(new Balloon(cm, room, 650, 0, 7));
			sprites.add(new Balloon(cm, room, 450, 0, 5));
			sprites.add(new Shelf(cm, room, 100, 500, 300));
			sprites.add(new Basketball(cm, room, 200, 0, 300));
			sprites.add(new Vent(cm, room, 210, 500, true, true));
			sprites.add(new Cabinet(cm, room, 50, 250, 100, 200));
			sprites.add(new Cabinet(cm, room, 350, 0, 400, 160));
			drawRoom(cm, room, Ceiling.BEIGE, Wall.BEIGE, Floor.BLUE);

			room++;
			sprites.add(new Item(cm, room, 179, 150, Item.EXTRA_LIFE));
			sprites.add(new Table(cm, room, 120, 0, 150, 150));
			sprites.add(new Cabinet(cm, room, 320, 340, 100, 160));
			sprites.add(new Vent(cm, room, 50, 500, true, true));
			sprites.add(new Vent(cm, room, 90, 500, false, true));
			sprites.add(new Vent(cm, room, 130, 500, true, true));
			sprites.add(new Vent(cm, room, 170, 500, false, true));
			sprites.add(new Vent(cm, room, 220, 500, false, true));
			sprites.add(new Vent(cm, room, 245, 500, true, true));
			sprites.add(new Vent(cm, room, 285, 500, true, true));
			sprites.add(new Basketball(cm, room, 650, 0, 300));
			sprites.add(new Balloon(cm, room, 450, 0, 5));
			drawRoom(cm, room, Ceiling.GREEN, Wall.GREEN, Floor.BLUE);

			room++;
			sprites.add(new Vent(cm, room, 50, 300, true, true));
			sprites.add(new Table(cm, room, 120, 0, 150, 150));
			sprites.add(new Item(cm, room, 179, 150, Item.CLOCK));
			sprites.add(new Vent(cm, room, 300, 300, true, true));
			sprites.add(new Window(cm, room, 400, 200, 150, 200));
			drawRoom(cm, room, Ceiling.BEIGE, Wall.BEIGE, Floor.BLUE);

			cm.NUM_ROOMS = (room + 1);

			for (GLObject sprite : sprites) {
				sprite.initDL(gl, cm);
			}
		}

        glu.gluOrtho2D(-cm.halfWidth, cm.halfWidth,
			-cm.halfHeight, cm.halfHeight);

        // Now switch to modelview transform mode, and set it to identity
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

	public void drawRoom(CanvasModel cm, int room, int ceiling, int wall,
		int floor) {
		sprites.add(new Ceiling(cm, room, 0, 0, (int) cm.cameraWidth, 65,
			ceiling));
		sprites.add(new Floor(cm, room, 0, 0, (int) cm.cameraWidth, 64, floor));
		sprites.add(new Wall(cm, room, 0, 66, (int) cm.cameraWidth,
			(int) cm.cameraHeight - 130, wall));
	}

    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
		gl.glMatrixMode(GL.GL_PROJECTION);

		switch (cm.gameState) {
			case GameState.TITLE_SCREEN:
				if (tempRoom != -1) {
					gl.glTranslatef(cm.cameraWidth * tempRoom, 0f, 0f);
					tempRoom = -1;
				}

				break;
			case GameState.NEXT_ROOM:
				gl.glTranslatef(-cm.cameraWidth, 0f, 0f);
				cm.gameState = GameState.GAME_PLAY;
				cm.plane.setX(0);
				cm.room++;

				// If we're entering a new room
				if (cm.room > cm.maxRoom) {
					cm.numPoints += 500;
					cm.maxRoom = cm.room;
					long difference = System.currentTimeMillis() - cm.startTime;

					// Record time bonus
					if (difference <= 20000) {
						cm.numPoints += (int) ((20000f - difference) / 20000f
							* 500f);
					}

					cm.startTime = System.currentTimeMillis();
				}
				// Reset stopwatch; user is trying again for a good time
				else if (cm.room == cm.maxRoom) {
					cm.startTime = System.currentTimeMillis();
				}

				updateCamera();

				break;
			case GameState.PREV_ROOM:
				gl.glTranslatef(cm.cameraWidth, 0f, 0f);
				cm.gameState = GameState.GAME_PLAY;
				cm.plane.setX((int) cm.cameraWidth - cm.plane.getWidth());
				cm.room--;
				updateCamera();

				break;
		}

        // Clear the drawing area; paints background color as set in init()
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		// Reset the current matrix to the "identity"
        gl.glMatrixMode(GL.GL_MODELVIEW);
		GLHelper.resetPosition(gl, cm);

		// Draw everything
		switch (cm.gameState) {
			case GameState.TITLE_SCREEN:
				drawTitle(gl, drawable);
				break;
			case GameState.GAME_OVER_SCREEN:
				drawGameOver(gl, drawable);
				break;
			case GameState.WINNER:
				drawWinner(gl, drawable);
				break;
			case GameState.DEAD:
			case GameState.HOUSE_COMPLETE:
			case GameState.GAME_OVER:
			case GameState.GAME_PLAY:
				drawGame(gl, drawable);
				break;
		}

		// Flush all drawing operations to the graphics card:
        gl.glFlush();

		// Update frame rate
		doFrameRate();
    }

	public void drawGame(GL gl, GLAutoDrawable drawable) {
		// Draw sprites from top to bottom
		for (int i = sprites.size() - 1; i >= 0; i--) {
			GLObject sprite = sprites.get(i);

			// Only draw sprites that are onscreen
			if (cm.room != sprite.getRoom()) {
				continue;
			}

			gl.glPushMatrix();
			//sprite.draw(gl, cm);
			sprite.drawFast(gl, cm);
			gl.glPopMatrix();
		}

		// Draw HUD
		gl.glPushMatrix();
		drawHud(gl, drawable);
		gl.glPopMatrix();

		// Draw plane
		gl.glPushMatrix();
		cm.plane.draw(gl, cm);
		gl.glPopMatrix();
	}

	public void drawGameOver(GL gl, GLAutoDrawable drawable) {
		drawCyanBackground(gl, cm.room);
		titleRenderer.beginRendering(drawable.getWidth(), drawable.getHeight());
		titleRenderer.setColor(0.4f, 0.4f, 0.4f, 1f);
		int textWidth = (int) titleRenderer.getBounds("GAME OVER").getWidth();
		titleRenderer.draw("GAME OVER", (int) (cm.cameraWidth / 2) - (int)
			(textWidth / 2), (int) (cm.cameraHeight / 2) + 25);
		titleRenderer.endRendering();

		titleRenderer.beginRendering(drawable.getWidth(), drawable.getHeight());
		titleRenderer.setColor(0.4f, 0.4f, 0.4f, 1f);
		String score = "Score: " + cm.numPoints;
		textWidth = (int) titleRenderer.getBounds(score).getWidth();
		titleRenderer.draw(score, (int) (cm.cameraWidth / 2) - (int)
			(textWidth / 2), (int) (cm.cameraHeight / 2) - 25);
		titleRenderer.endRendering();
	}

	public void drawWinner(GL gl, GLAutoDrawable drawable) {
		drawCyanBackground(gl, cm.NUM_ROOMS - 1);

		titleRenderer.beginRendering(drawable.getWidth(), drawable.getHeight());
		titleRenderer.setColor(0.4f, 0.4f, 0.4f, 1f);
		int textWidth = (int) titleRenderer.getBounds("YOU WIN!").getWidth();
		titleRenderer.draw("YOU WIN!", (int) (cm.cameraWidth / 2) - (int)
			(textWidth / 2), (int) (cm.cameraHeight / 2) + 25);
		titleRenderer.endRendering();

		titleRenderer.beginRendering(drawable.getWidth(), drawable.getHeight());
		titleRenderer.setColor(0.4f, 0.4f, 0.4f, 1f);
		String score = "Score: " + cm.numPoints;
		textWidth = (int) titleRenderer.getBounds(score).getWidth();
		titleRenderer.draw(score, (int) (cm.cameraWidth / 2) - (int)
			(textWidth / 2), (int) (cm.cameraHeight / 2) - 25);
		titleRenderer.endRendering();
	}

	public void drawCyanBackground(GL gl, int room) {
		// Fix camera position
		float offset = room * (int) cm.cameraWidth;
		gl.glTranslatef(offset, 0f, 0f);

		final Color cyan = new Color(200, 232, 244);

		GLHelper.drawPoly(gl, cyan, cyan,
			new Point(0, 0),
			new Point((int) cm.cameraWidth, 0),
			new Point((int) cm.cameraWidth, (int) cm.cameraHeight),
			new Point(0, (int) cm.cameraHeight));
	}

	public void drawTitle(GL gl, GLAutoDrawable drawable) {
		drawCyanBackground(gl, 0);

		final Color green = new Color(142, 186, 120);
		GLHelper.drawPoly(gl, green, green.darker(),
			new Point(0, (int) cm.cameraHeight * 2 / 3),
			new Point((int) cm.cameraWidth, (int) cm.cameraHeight * 2 / 3),
			new Point((int) cm.cameraWidth, (int) cm.cameraHeight),
			new Point(0, (int) cm.cameraHeight));

		titleRenderer.beginRendering(drawable.getWidth(), drawable.getHeight());
		titleRenderer.setColor(0.4f, 0.4f, 0.4f, 1f);
		int textWidth = (int) titleRenderer.
			getBounds("\"" + APP_TITLE.toUpperCase() + "\"").getWidth();
		titleRenderer.draw("\"" + APP_TITLE.toUpperCase() + "\"", (int)
			(cm.cameraWidth / 2) - (int) (textWidth / 2), (int)
			(cm.cameraHeight / 2));
		titleRenderer.endRendering();

		hudRenderer.beginRendering(drawable.getWidth(), drawable.getHeight());
		hudRenderer.setColor(1f, 1f, 1f, 1f);
		String str = "A game by John Kurlak";
		textWidth = (int) hudRenderer.getBounds(str).getWidth();
		hudRenderer.draw(str, (int) (cm.cameraWidth) - (int) (textWidth) - 20,
			20);
		hudRenderer.endRendering();

		// Draw the big plane
		gl.glPushMatrix();
		cm.plane.setScale(400);
		cm.plane.drawFast(gl, cm);
		cm.plane.setScale(100);
		gl.glPopMatrix();
	}

	private void drawHud(GL gl, GLAutoDrawable drawable) {
		gl.glPushMatrix();
		gl.glTranslatef(cm.room * cm.cameraWidth, 0f, 0f);
		drawHudBox(gl, drawable, "Points: " + cm.numPoints, (int) cm.cameraWidth
			- 485, 120);
		drawHudBox(gl, drawable, "Room: " + (cm.room + 1), (int) cm.cameraWidth
			- 365, 120);
		drawHudBox(gl, drawable, "Battery: " + cm.plane.getBattery(), (int)
			cm.cameraWidth - 245, 120);

		String lives = cm.numLives + "";

		if (cm.numLives == -1) {
			lives = "---";
		}

		drawHudBox(gl, drawable, "Lives: " + lives, (int) cm.cameraWidth - 125,
			120);
		gl.glPopMatrix();
	}

	private void drawHudBox(GL gl, GLAutoDrawable drawable, String text, int x,
		int width) {
		GLHelper.drawPoly(gl,
			Color.GRAY,
			Color.GRAY.brighter(),
			Color.GRAY,
			Color.GRAY,
			Color.BLACK,
			new Point(x, 5),
			new Point(x + width, 5),
			new Point(x + width, 25),
			new Point(x, 25));

		hudRenderer.beginRendering(drawable.getWidth(), drawable.getHeight());
		hudRenderer.setColor(1, 1f, 1f, 1f);
		hudRenderer.draw(text, x + 5, (int) cm.cameraHeight - 20);
		hudRenderer.endRendering();
	}

	private void doFrameRate() {
		// Calculate frame rate performance:
        frames++;
        time = System.currentTimeMillis();

		if (time - timestart > 1000) {
            // Wait at least one second; then, compute average rate
            framerate = frames * 1000f / (time - timestart);
            timestart = time;

			if (frames > 1) {
				frames = 0;
				setTitle(APP_TITLE + " | " + framerate + " FPS");
			}
        }
	}

    public void dispose(GLAutoDrawable arg0) {}

	public void keyTyped(KeyEvent e) {}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();

		if (key == KeyEvent.VK_RIGHT) {
			cm.rightArrow = true;
		}
		else if (key == KeyEvent.VK_LEFT) {
			cm.leftArrow = true;
		}
		else if (key == KeyEvent.VK_UP) {
			cm.upArrow = true;
		}
		else if (key == KeyEvent.VK_DOWN) {
			cm.downArrow = true;
		}
		else if (key == KeyEvent.VK_ENTER) {
			cm.enterKey = true;
		}
		else if (key == KeyEvent.VK_J) {
			cm.jKey = true;
		}
		else if (key == KeyEvent.VK_K) {
			cm.kKey = true;
		}
		else if (key == KeyEvent.VK_SPACE) {
			advance();
		}

		// Press J + K and skip a room
		if (cm.jKey && cm.kKey && cm.room < cm.NUM_ROOMS - 1 &&
			cm.gameState == GameState.GAME_PLAY) {
			cm.plane.setPosition(0, 0);
			cm.gameState = GameState.NEXT_ROOM;
		}
	}

	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();

		if (key == KeyEvent.VK_RIGHT) {
			cm.rightArrow = false;
		}
		else if (key == KeyEvent.VK_LEFT) {
			cm.leftArrow = false;
		}
		else if (key == KeyEvent.VK_UP) {
			cm.upArrow = false;
		}
		else if (key == KeyEvent.VK_DOWN) {
			cm.downArrow = false;
		}
		else if (key == KeyEvent.VK_ENTER) {
			cm.enterKey = false;
		}
		else if (key == KeyEvent.VK_J) {
			cm.jKey = false;
		}
		else if (key == KeyEvent.VK_K) {
			cm.kKey = false;
		}
	}

	public void actionPerformed(ActionEvent e) {
		cm.plane.doAction(cm);

		// Tell sprite to update itself (this is called on regular time
		// interval -- 25ms)
		for (GLObject sprite : sprites) {
			sprite.doAction(cm);
		}
	}

	public void advance() {
		if (cm.gameState == GameState.TITLE_SCREEN) {
			cm.plane.reset();
			cm.startTime = System.currentTimeMillis();
			cm.gameState = GameState.GAME_PLAY;
		}

		if (cm.gameState == GameState.GAME_OVER_SCREEN ||
			cm.gameState == GameState.WINNER) {
			resetGame();
		}
	}

	public void mouseClicked(MouseEvent e) {
		advance();
	}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void displayChanged(GLAutoDrawable glad, boolean bln, boolean bln1) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}