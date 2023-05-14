/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.tools.rendering;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.media.opengl.GLCanvas;

import org.deegree.rendering.r3d.ViewFrustum;
import org.deegree.rendering.r3d.ViewParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code></code> class TODO add class documentation here.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class FlightControls implements KeyListener, MouseMotionListener, MouseWheelListener {

	private static final Logger LOG = LoggerFactory.getLogger(FlightControls.class);

	private final ViewFrustum vf;

	// fields needed by the MouseMotionListener
	private boolean mouseLook = false;

	private int oldX = -1;

	private int oldY = -1;

	private GLCanvas master;

	public FlightControls(GLCanvas master, ViewParams viewParams) {
		this.master = master;
		this.vf = viewParams.getViewFrustum();
	}

	@Override
	public void keyPressed(KeyEvent ev) {

		int k = ev.getKeyCode();
		ev.getModifiers();

		double rotationStep = 0.01;
		double moveStep = calcMove();

		if ((ev.getModifiersEx()
				& (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) == InputEvent.SHIFT_DOWN_MASK) {
			// SHIFT (and not CTRL)
			rotationStep *= 10.0;
			moveStep *= 10.0;
		}
		else if ((ev.getModifiersEx()
				& (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) == InputEvent.CTRL_DOWN_MASK) {
			// CTRL (and not SHIFT)
			rotationStep /= 10.0;
			moveStep /= 10.0;
		}

		switch (k) {
			case KeyEvent.VK_Q: {
				vf.rotateZ(rotationStep);
				break;
			}
			case KeyEvent.VK_E: {
				vf.rotateZ(-rotationStep);
				break;
			}
			case KeyEvent.VK_A: {
				vf.moveRight(-moveStep);
				break;
			}
			case KeyEvent.VK_D: {
				vf.moveRight(moveStep);
				break;
			}
			case KeyEvent.VK_W: {
				vf.moveForward(moveStep);
				break;
			}
			case KeyEvent.VK_S: {
				vf.moveForward(-moveStep);
				break;
			}
			case KeyEvent.VK_R: {
				vf.moveUp(moveStep);
				break;
			}
			case KeyEvent.VK_F: {
				vf.moveUp(-moveStep);
				break;
			}
			case KeyEvent.VK_P: {
				LOG.info(this.vf.toString());
				LOG.info("View parameters:\n" + this.vf.toInitString());
				break;
			}
			// arrow keys
			case KeyEvent.VK_UP: {
				vf.moveUp(moveStep);
				break;
			}
			case KeyEvent.VK_DOWN: {
				vf.moveUp(-moveStep);
				break;
			}
			case KeyEvent.VK_LEFT: {
				vf.moveRight(-moveStep);
				break;
			}
			case KeyEvent.VK_RIGHT: {
				vf.moveRight(moveStep);
				break;
			}
			case KeyEvent.VK_ESCAPE:
				LOG.info("Last view:\n" + this.vf.toInitString());
				System.exit(1);
				break;
		}
		master.repaint();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent event) {
		int x = event.getX();
		int y = event.getY();

		if (!mouseLook) {
			mouseLook = true;
			oldX = x;
			oldY = y;
			return;
		}

		int dx = oldX - x;
		int dy = oldY - y;

		oldX = x;
		oldY = y;

		if (dx * dx > 100 * 100 || dy * dy > 100 * 100) {
			return;
		}

		vf.rotateX(0.003f * dy);
		vf.rotateY(0.003f * dx);

		oldX = x;
		oldY = y;
		master.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent ev) {

		double moveStep = calcMove();

		if ((ev.getModifiersEx()
				& (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) == InputEvent.SHIFT_DOWN_MASK) {
			// SHIFT (and not CTRL)
			moveStep *= 10.0;
		}
		else if ((ev.getModifiersEx()
				& (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) == InputEvent.CTRL_DOWN_MASK) {
			// CTRL (and not SHIFT)
			moveStep /= 10.0;
		}

		vf.moveForward(-ev.getWheelRotation() * moveStep);
		master.repaint();
	}

	private double calcMove() {
		double moveStep = vf.getEyePos().z / 50.0;
		if (moveStep < 5) {
			moveStep = 5;
		}
		return moveStep;
	}

}
