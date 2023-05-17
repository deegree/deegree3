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

package org.deegree.tools.rendering.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;

import org.deegree.rendering.r3d.opengl.display.OpenGLEventHandler;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;

/**
 * The <code>GLViewer</code> uses the jogl engine to render dataobjects.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
public class GLViewer extends JFrame implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 7698388852544865855L;

	private Preferences prefs;

	public final static String OPEN_KEY = "lastOpenLocation";

	public final static String LAST_EXTENSION = "lastFileExtension";

	private final static String WIN_TITLE = "Deegree 3D Object viewer: ";

	private List<ViewerFileFilter> supportedOpenFilter = new ArrayList<ViewerFileFilter>();

	/**
	 * A panel showing some key stroke helps
	 */
	JPanel helpLister;

	GLCanvas canvas = null;

	private OpenGLEventHandler openGLEventListener;

	/**
	 * Creates a new frame with the menus and the canvas3d set.
	 * @param testSphere true if a sphere should be displayed.
	 */
	public GLViewer(boolean testSphere) {
		super(WIN_TITLE);
		prefs = Preferences.userNodeForPackage(GLViewer.class);
		setupGUI();

		// openFileChooser();

		setupOpenGL(testSphere);
		ArrayList<String> extensions = new ArrayList<String>();

		extensions.add("gml");
		extensions.add("xml");
		supportedOpenFilter.add(new ViewerFileFilter(extensions, "(*.gml, *.xml) GML or CityGML-Files"));

		extensions.clear();
		extensions.add("shp");
		supportedOpenFilter.add(new ViewerFileFilter(extensions, "(*.shp) Esri ShapeFiles"));

		extensions.clear();
		extensions.add("vrml");
		extensions.add("wrl");
		supportedOpenFilter
			.add(new ViewerFileFilter(extensions, "(*.vrml, *.wrl) VRML97 - Virtual Reality Modelling Language"));

		pack();

	}

	private void addGeometries(WorldRenderableObject model, boolean remove) {
		if (remove) {
			openGLEventListener.removeAllData();
		}

		openGLEventListener.addDataObjectToScene(model);

	}

	/**
	 * GUI stuff
	 */
	private void setupGUI() {
		// add listener for closing the frame/application
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(600, 600));
		setPreferredSize(new Dimension(600, 600));

		// Adding the button panel
		JPanel totalPanel = new JPanel(new BorderLayout());
		totalPanel.add(createButtons(), BorderLayout.NORTH);
		helpLister = new JPanel(new GridBagLayout());
		Border border = BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),
				"Instant help");
		helpLister.setBorder(border);
		GridBagConstraints gb = new GridBagConstraints();
		gb.ipadx = 10;
		gb.gridx = 0;
		gb.gridy = 0;
		JLabel tmp = new JLabel("x: move postive X-axis");
		helpLister.add(tmp, gb);

		gb.gridx++;
		tmp = new JLabel("X: move negative X-axis");
		helpLister.add(tmp, gb);

		gb.gridx = 0;
		gb.gridy++;
		tmp = new JLabel("y: move positve Y-axis");
		helpLister.add(tmp, gb);

		gb.gridx++;
		tmp = new JLabel("Y: move negative Y-axis");
		helpLister.add(tmp, gb);

		gb.gridy++;
		gb.gridx = 0;
		tmp = new JLabel("z: move positve Z-axis");
		helpLister.add(tmp, gb);
		gb.gridx++;

		tmp = new JLabel("Z: move negative Z-axis");
		helpLister.add(tmp, gb);
		helpLister.setVisible(false);

		totalPanel.add(helpLister, BorderLayout.SOUTH);
		getContentPane().add(totalPanel, BorderLayout.SOUTH);

	}

	private void setupOpenGL(boolean testSphere) {
		GLCapabilities caps = new GLCapabilities();
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);
		caps.setAlphaBits(8);
		caps.setAccumAlphaBits(8);
		openGLEventListener = new OpenGLEventHandler(testSphere);

		canvas = new GLCanvas(caps);
		canvas.addGLEventListener(openGLEventListener);
		canvas.addMouseListener(openGLEventListener.getTrackBall());
		canvas.addMouseWheelListener(openGLEventListener.getTrackBall());
		canvas.addMouseMotionListener(openGLEventListener.getTrackBall());

		getContentPane().add(canvas, BorderLayout.CENTER);
	}

	private JPanel createButtons() {
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gb = new GridBagConstraints();
		gb.gridx = 0;
		gb.gridy = 0;

		JRadioButton help = new JRadioButton("Activate help");
		help.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (helpLister.isVisible()) {
					helpLister.setVisible(false);
					((JRadioButton) e.getSource()).setText("Activate help");
				}
				else {
					helpLister.setVisible(true);
					((JRadioButton) e.getSource()).setText("De-Activate help");

				}
			}
		});
		buttonPanel.add(help, gb);

		gb.insets = new Insets(10, 10, 10, 10);
		gb.gridx++;
		JButton button = new JButton("Open File");
		button.setMnemonic(KeyEvent.VK_O);
		button.addActionListener(this);
		buttonPanel.add(button, gb);

		gb.gridx++;
		button = new JButton("Export File");
		button.setMnemonic(KeyEvent.VK_O);
		button.addActionListener(this);
		buttonPanel.add(button, gb);

		return buttonPanel;
	}

	/**
	 * @param errorMessage to display
	 */
	public void showExceptionDialog(String errorMessage) {
		JOptionPane.showMessageDialog(this, errorMessage);
	}

	public Preferences getPreferences() {
		return prefs;
	}

	public JFileChooser createFileChooser(List<ViewerFileFilter> fileFilter) {
		// Setting up the fileChooser.

		String lastLoc = prefs.get(OPEN_KEY, System.getProperty("user.home"));

		File lastFile = new File(lastLoc);
		if (!lastFile.exists()) {
			lastFile = new File(System.getProperty("user.home"));
		}
		JFileChooser fileChooser = new JFileChooser(lastFile);
		fileChooser.setMultiSelectionEnabled(false);
		if (fileFilter != null && fileFilter.size() > 0) {
			// the *.* file filter is off
			fileChooser.setAcceptAllFileFilterUsed(false);
			String lastExtension = prefs.get(LAST_EXTENSION, "*");
			FileFilter selected = fileFilter.get(0);
			for (ViewerFileFilter filter : fileFilter) {
				fileChooser.setFileFilter(filter);
				if (filter.accepts(lastExtension)) {
					selected = filter;
				}
			}

			fileChooser.setFileFilter(selected);
		}
		return fileChooser;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source instanceof JButton) {
			JButton clicked = (JButton) source;
			if (clicked.getText().startsWith("Export")) {
				File3dExporter.save(this, null);
			}
			else {
				JFileChooser fileChooser = createFileChooser(supportedOpenFilter);
				int result = fileChooser.showOpenDialog(this);
				if (JFileChooser.APPROVE_OPTION == result) {
					File f = fileChooser.getSelectedFile();
					if (f != null) {
						String path = f.getAbsolutePath();
						prefs.put(LAST_EXTENSION, ((ViewerFileFilter) fileChooser.getFileFilter()).getExtension(f));
						prefs.put(OPEN_KEY, f.getParent());
						List<WorldRenderableObject> rese = File3dImporter.open(this, path);
						// add res to scene.
						for (WorldRenderableObject res : rese) {
							addGeometries(res, true);
						}
					}

				}
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// Tesselator t = new Tesselator();
		// ArrayList<SimpleAccessGeometry> simpleAccessGeometries = new
		// ArrayList<SimpleAccessGeometry>();
		// simpleAccessGeometries.add( GLViewer.createStar() );
		// simpleAccessGeometries.add( GLViewer.createTexturedConcav() );
		// simpleAccessGeometries.add( GLViewer.createGeometryWithRing() );
		// GeometryQualityModel gqm = new GeometryQualityModel( simpleAccessGeometries );
		// RenderableQualityModel rqm = t.createRenderableQM( gqm );

		// CityGMLImporter importer = new CityGMLImporter( null, new float[] { -2568000,
		// -5615600, 0 }, null, false );
		// // List<WorldRenderableObject> objects = importer.importFromFile(
		// "/tmp/building.gml", 6, 2 );
		// List<WorldRenderableObject> objects = importer.importFromFile(
		// "/home/rutger/workspace/bonn_3doptimierung/resources/data/520706.gml",
		// 6, 2 );

		// String file = write( rqm );

		// RenderableQualityModel loadedModel = (RenderableQualityModel) read( file );
		// rqm.addGeometryData( createBillboard() );

		GLViewer viewer = new GLViewer(false);
		// for ( WorldRenderableObject wro : objects ) {
		// viewer.openGLEventListener.addDataObjectToScene( wro );
		// }
		// viewer.addGeometries( rqm, true );
		// rqm = new BillBoard( "4", new float[] { -1, -2.6f, 0 }, 2, 2 );
		// file = write( rqm );
		// loadedModel = (RenderableQualityModel) read( file );
		// viewer.addGeometries( rqm, false );
		// rqm2 = new BillBoard( "3", new float[] { 1, 0, 1f }, new float[] { 3, 1 } );
		// viewer.addGeometries( rqm2, false );
		viewer.toFront();
	}

}
