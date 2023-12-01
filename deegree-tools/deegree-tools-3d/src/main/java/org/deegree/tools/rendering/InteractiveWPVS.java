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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.xml.bind.JAXBException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.SunInfo;
import org.deegree.commons.utils.math.Vectors3f;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderUtils;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.multiresolution.MultiresolutionMesh;
import org.deegree.rendering.r3d.opengl.JOGLChecker;
import org.deegree.rendering.r3d.opengl.JOGLUtils;
import org.deegree.rendering.r3d.opengl.display.LODAnalyzer;
import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
import org.deegree.rendering.r3d.opengl.rendering.dem.Colormap;
import org.deegree.rendering.r3d.opengl.rendering.dem.manager.RenderFragmentManager;
import org.deegree.rendering.r3d.opengl.rendering.dem.manager.TerrainRenderingManager;
import org.deegree.rendering.r3d.opengl.rendering.dem.manager.TextureManager;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.BuildingRenderer;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.RenderableManager;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.TreeRenderer;
import org.deegree.rendering.r3d.opengl.rendering.model.texture.TexturePool;
import org.deegree.services.OWS;
import org.deegree.services.OwsManager;
import org.deegree.services.exception.ServiceInitException;
import org.deegree.services.jaxb.wpvs.SkyImages;
import org.deegree.services.jaxb.wpvs.SkyImages.SkyImage;
import org.deegree.services.wpvs.PerspectiveViewService;
import org.deegree.services.wpvs.config.ColormapDataset;
import org.deegree.services.wpvs.config.DEMDataset;
import org.deegree.services.wpvs.config.DEMTextureDataset;
import org.deegree.services.wpvs.controller.WPVSController;
import org.deegree.services.wpvs.controller.getview.GetView;
import org.deegree.services.wpvs.controller.getview.GetViewKVPAdapter;
import org.deegree.services.wpvs.rendering.jogl.ConfiguredOpenGLInitValues;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.texture.Texture;

/**
 * Reads in a configuration document for the {@link WPVSController} and lets the user
 * navigate interactively through the scene.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus ls /Schneider</a>
 */
@Tool("Reads in a configuration document for the deegree WPVS and allows the user to interactively navigate through the scene.")
public class InteractiveWPVS extends GLCanvas implements GLEventListener, KeyListener {

	private static final Logger LOG = LoggerFactory.getLogger(InteractiveWPVS.class);

	private static final String OPT_WPVS_CONFIG_FILE = "wpvsconfig";

	private static final String OPT_WPVS_DB_CONNECTION = "dburl";

	private static final String OPT_WPVS_DB_USER = "db_user";

	private static final String OPT_WPVS_DB_PASS = "db_pass";

	private static final String OPT_WPVS_DB_ID = "connection_id";

	private static final long serialVersionUID = 7634444161374573563L;

	private final static double zNear = 1.0;

	private final static double zFar = 300000.0;

	private static Workspace workspace;

	private final GLU glu = new GLU();

	private final GLUT glut = new GLUT();

	private final ViewParams params;

	// private final FlightControls flightControls;

	private TerrainRenderingManager demRenderer;

	private int lastFrameMs = 0;

	private int maxFrameMs = 0;

	private long allFramesMs = 0;

	private int numFrames = 0;

	// should the elevation model be rendered
	private boolean disableElevationModel;

	private TextureManager[] textureManagers;

	private boolean[] activeTextureManagers;

	private boolean renderTrees;

	private boolean renderBuildings;

	private boolean getImage;

	private final JFrame lodAnalyzerFrame = new JFrame("Terrain fragment structure");

	private final LODAnalyzer lodAnalyzer;

	private String copyrightID;

	private String skyImageID;

	private ConfiguredOpenGLInitValues initValues;

	private RenderContext glRenderContext;

	private float zScale;

	private float[] light_position;

	private PerspectiveViewService perspectiveViewService;

	private ArrayList<String> currentDatasets = new ArrayList<String>(20);

	private ArrayList<String> availableDatasets = new ArrayList<String>(20);

	private ArrayList<String> availableColorMaps = new ArrayList<String>(20);

	private Pair<String, Colormap> activeColormap;

	private int currentColormap = -1;

	private boolean updateLODStructure = true;

	/**
	 * Creates a new {@link InteractiveWPVS} instance.
	 * @param params
	 * @param zScale
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 * @throws ServiceInitException
	 */
	private InteractiveWPVS(Workspace workspace, ViewParams params, float zScale)
			throws IOException, UnsupportedOperationException, ServiceInitException {

		this.zScale = zScale;
		setMinimumSize(new Dimension(0, 0));
		initValues = new ConfiguredOpenGLInitValues(8);
		addGLEventListener(this);
		addKeyListener(this);
		// used to create the shader programs.
		lodAnalyzerFrame.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		lodAnalyzerFrame.setSize(600, 600);
		lodAnalyzerFrame.setLocationByPlatform(true);

		OwsManager wsConfig = workspace.getResourceManager(OwsManager.class);
		List<OWS> wpvsControllers = wsConfig.getByOWSClass(WPVSController.class);
		if (wpvsControllers.isEmpty()) {
			throw new ServiceInitException("No active WPVS found in workspace.");
		}

		this.perspectiveViewService = ((WPVSController) wpvsControllers.get(0)).getService();

		this.demRenderer = this.perspectiveViewService.getDefaultDEMRenderer();
		DEMDataset dDW = this.perspectiveViewService.getDEMDatasets();
		Set<String> datasetNames = dDW.datasetTitles();
		if (datasetNames.isEmpty()) {
			LOG.warn("No elevation model dataset was detected, this is strange.");
		}
		else {
			Iterator<String> iterator = datasetNames.iterator();
			String title = "dem";
			if (iterator.hasNext()) {
				title = iterator.next();
			}
			else {
				LOG.warn("The elevation model dataset has no title, using 'dem' as default.");
			}
			title += "(E)";
			this.availableDatasets.add(title);
			this.currentDatasets.add(title);
		}

		double[][] bbox = demRenderer.getFragmentManager().getMultiresolutionMesh().getBBox();
		double minX = bbox[0][0];
		double maxX = bbox[1][0];
		double minY = bbox[0][1];
		double maxY = bbox[1][1];
		lodAnalyzer = new LODAnalyzer((float) (maxX - minX), (float) (maxY - minY));
		lodAnalyzerFrame.getContentPane().add(lodAnalyzer, BorderLayout.CENTER);

		DEMTextureDataset tDS = this.perspectiveViewService.getTextureDataSets();
		for (String tN : tDS.datasetTitles()) {
			this.availableDatasets.add(tN + "(T)");
		}

		ColormapDataset cDS = this.perspectiveViewService.getColormapDatasets();

		for (String cN : cDS.datasetTitles()) {
			this.availableDatasets.add(cN + "(C)");
			this.availableColorMaps.add(cN);
		}

		this.textureManagers = this.perspectiveViewService.getAllTextureManagers();
		activeTextureManagers = new boolean[textureManagers.length];

		// initTerrain( configAdapter, sc );
		// initTerrainTextures( configAdapter, sc );

		if (params != null) {
			this.params = params;
		}
		else {
			this.params = getViewParams(demRenderer);
		}

		// add listener for motion controls
		FlightControls flightControls = new FlightControls(this, this.params);
		addKeyListener(flightControls);
		addMouseWheelListener(flightControls);
		addMouseMotionListener(flightControls);

		initSkyImage(perspectiveViewService.getServiceConfiguration().getSkyImages());
		this.copyrightID = this.perspectiveViewService.getCopyrightKey();
		initModels();
	}

	private void initSkyImage(SkyImages skyImages) {
		if (skyImages != null) {
			List<SkyImage> images = skyImages.getSkyImage();
			if (!images.isEmpty()) {
				skyImageID = images.get(0).getName();
			}
		}
		if (skyImageID == null) {
			LOG.info("Not rendering a sky image, because it could not be read from the configuration.");
		}
	}

	private void initModels() {
		if (this.perspectiveViewService.getAllRenderableRenderers() != null
				&& !this.perspectiveViewService.getAllRenderableRenderers().isEmpty()) {
			availableDatasets.add("buildings");
			this.currentDatasets.add("buildings");
			this.renderBuildings = true;
			LOG.info("- Key 't' toggles tree dataset");
			LOG.info("- Key 'b' toggles building dataset");
		}
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		LOG.trace("display( GLAutoDrawable ) called");
		LOG.debug("view params: " + params);

		long begin = System.currentTimeMillis();
		GL gl = drawable.getGL();
		glRenderContext.setContext(gl);
		glRenderContext.setUpdateLOD(updateLODStructure);

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();

		setBackground();

		Point3d eyePos = params.getViewFrustum().getEyePos();
		Point3d lookingAt = params.getViewFrustum().getLookingAt();
		Vector3d viewerUp = params.getViewFrustum().getUp();
		glu.gluLookAt(eyePos.x, eyePos.y, eyePos.z, lookingAt.x, lookingAt.y, lookingAt.z, viewerUp.x, viewerUp.y,
				viewerUp.z);

		// render scene objects
		List<TextureManager> activeTextureManagers = new ArrayList<TextureManager>(textureManagers.length);
		for (int i = 0; i < this.activeTextureManagers.length; i++) {
			if (this.activeTextureManagers[i]) {
				activeTextureManagers.add(textureManagers[i]);
			}
		}
		demRenderer.render(glRenderContext, disableElevationModel,
				activeColormap != null ? activeColormap.second : null,
				activeTextureManagers.toArray(new TextureManager[activeTextureManagers.size()]));
		// outputMV( gl );
		List<RenderableManager<?>> renderableRenders = perspectiveViewService.getRenderableRenderers(this.params);
		if (renderBuildings) {
			for (RenderableManager<?> br : renderableRenders) {
				if (br != null && br instanceof BuildingRenderer) {
					br.render(glRenderContext);
				}
			}
		}

		if (renderTrees) {
			for (RenderableManager<?> tr : renderableRenders) {
				if (tr != null && tr instanceof TreeRenderer) {
					tr.render(glRenderContext);
				}
			}
		}
		gl.glFlush();

		// update and render time stats
		lastFrameMs = (int) (System.currentTimeMillis() - begin);
		numFrames++;
		allFramesMs += lastFrameMs;
		if (maxFrameMs < lastFrameMs) {
			maxFrameMs = lastFrameMs;
		}
		displayStats(gl);

		if (lodAnalyzerFrame.isVisible()) {
			lodAnalyzer.updateParameters(demRenderer.getCurrentLOD(), params.getViewFrustum());
			lodAnalyzer.repaint();
		}
		renderCopyright();
		if (getImage) {
			gl.glFinish();
			writeResult(gl);
			getImage = false;
		}

	}

	private void renderCopyright() {
		Texture copyImage = TexturePool.getTexture(glRenderContext, copyrightID);
		if (copyImage != null) {
			float tH = copyImage.getHeight();
			float tW = copyImage.getWidth();

			float quadWidth = tW;
			float quadHeight = tH;
			glRenderContext.getContext().glEnable(GL.GL_ALPHA_TEST);
			glRenderContext.getContext().glAlphaFunc(GL.GL_GREATER, 0.4f);
			draw2D(0, 0, quadWidth, quadHeight, copyImage, true);
			glRenderContext.getContext().glDisable(GL.GL_ALPHA_TEST);
			glRenderContext.getContext().glAlphaFunc(GL.GL_ALWAYS, 1);

		}
	}

	private void setBackground() {
		Texture skyImage = TexturePool.getTexture(glRenderContext, skyImageID);
		if (skyImage != null) {
			draw2D(0, 0, params.getScreenPixelsX(), params.getScreenPixelsY(), skyImage, false);
		}
	}

	/**
	 * Draw a 2d quad width given width height and location, and use given texture.
	 * @param x
	 * @param y
	 * @param quadWidth
	 * @param quadHeight
	 * @param texture
	 * @param useDepth true if the depth buffer should be enabled.
	 */
	private void draw2D(float x, float y, float quadWidth, float quadHeight, Texture texture, boolean useDepth) {
		GL gl = glRenderContext.getContext();
		if (!useDepth) {
			gl.glDisable(GL.GL_DEPTH_TEST);
		}

		gl.glDisable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_TEXTURE_2D);
		//
		// gl.glViewport( 0, 0, width, height );

		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		glu.gluOrtho2D(0, params.getScreenPixelsX(), 0, params.getScreenPixelsY());

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		texture.bind();
		gl.glBegin(GL.GL_QUADS);
		{
			gl.glTexCoord2f(0, 0);
			gl.glVertex2f(x, quadHeight);

			gl.glTexCoord2f(1f, 0f);
			gl.glVertex2f(quadWidth, quadHeight);

			gl.glTexCoord2f(1f, 1f);
			gl.glVertex2f(quadWidth, y);

			gl.glTexCoord2f(0f, 1f);
			gl.glVertex2f(x, y);

		}
		gl.glEnd();
		texture.disable();
		gl.glPopMatrix();

		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);

		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glEnable(GL.GL_LIGHTING);
		if (!useDepth) {
			gl.glEnable(GL.GL_DEPTH_TEST);
		}
	}

	private void writeResult(GL gl) {
		int width = params.getScreenPixelsX();
		int height = params.getScreenPixelsY();

		BufferedImage resultImage = JOGLUtils.getFrameBufferRGB(gl, null, 0, 0, width, height, null);
		try {
			File f = File.createTempFile("wpvs_", ".jpg");
			ImageIO.write(resultImage, "jpg", f);
			System.out.println("Wrote file to: " + f.getAbsolutePath());
		}
		catch (IOException e) {
			// nottin
		}
	}

	@Override
	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
		LOG.trace("displayChanged( GLAutoDrawable, boolean, boolean ) called");
	}

	@Override
	public void init(GLAutoDrawable drawable) {

		LOG.trace("init( GLAutoDrawable ) called");

		GL gl = drawable.getGL();
		gl.glClearColor(0.1f, 0.2f, 0.8f, 0.0f);

		// int[] values = new int[3];
		// gl.glGetIntegerv( GL.GL_COMPRESSED_TEXTURE_FORMATS, values, 0 );
		// for ( int v : values ) {
		// System.out.println( "v: " + v );
		// }
		// System.out.println( GL.GL_COMPRESSED_RGB_S3TC_DXT1_EXT );
		// System.out.println( GL.GL_COMPRESSED_RGBA_S3TC_DXT1_EXT );
		// System.out.println( GL.GL_COMPRESSED_RGBA_S3TC_DXT3_EXT );
		// System.out.println( GL.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT );

		// gl.glEnable( GL.GL_CULL_FACE );
		// gl.glCullFace( GL.GL_BACK );
		gl.glShadeModel(GL.GL_SMOOTH);
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
		// gl.glPolygonMode( GL.GL_FRONT, GL.GL_LINE );
		// gl.glPolygonMode( GL.GL_BACK, GL.GL_LINE );
		// -0.9099613,-0.10755372,-0.41469324
		// float[] light_position = { -0.9099613f, -0.10755372f, 0.41469324f, 0.0f };

		// float[] light_position = { 1f, 1f, 1.0f, 0.0f };
		GregorianCalendar cal = new GregorianCalendar(2009, 4, 19, 3, 0);
		SunInfo pos = new SunInfo(cal);
		light_position = pos.getEucledianPosition(51.7);
		Vectors3f.scale(-1, light_position);

		float[] ambientAndDiffuse = pos.calculateSunlight(51.7);
		float intens = pos.calcSunlightIntensity(ambientAndDiffuse, 0.5f);

		// LOG.info( "Sun pos for time: " + cal.getTime().toGMTString() );
		LOG.debug("Using sun's direction: " + Vectors3f.asString(light_position));
		LOG.debug("Using sun's color: " + Vectors3f.asString(ambientAndDiffuse));
		LOG.debug("Using sun's intensity: " + intens);
		// light_position[0] += 15000;
		// light_position[1] += 15000;
		// light_position[2] += 550;
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION,
				new float[] { light_position[0], light_position[1], light_position[2], 1 }, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT,
				new float[] { ambientAndDiffuse[0], ambientAndDiffuse[1], ambientAndDiffuse[2], 1 }, 0);

		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT,
				new float[] { ambientAndDiffuse[0], ambientAndDiffuse[1], ambientAndDiffuse[2], 1 }, 0);
		// gl.glLightfv( GL.GL_LIGHT0, GL.GL_AMBIENT, new float[] { 0.1f, 0.1f, 0.1f, 1 },
		// 0 );
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE,
				new float[] { ambientAndDiffuse[0], ambientAndDiffuse[1], ambientAndDiffuse[2], 1 }, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, new float[] { ambientAndDiffuse[0] - 0.2f,
				ambientAndDiffuse[1] - 0.2f, ambientAndDiffuse[2] - 0.2f, 0 }, 0);

		/* enable extreme lighting */
		// ambientAndDiffuse = new float[] { 1f, 1f, 1f };
		// gl.glLightfv( GL.GL_LIGHT0, GL.GL_POSITION, new float[] { light_position[0],
		// light_position[1],
		// light_position[2], 0 }, 0 );
		// gl.glLightfv( GL.GL_LIGHT0, GL.GL_AMBIENT, new float[] { ambientAndDiffuse[0] -
		// 0.5f,
		// ambientAndDiffuse[1] - 0.5f,
		// ambientAndDiffuse[2] - 0.5f, 1 }, 0 );
		//
		// gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, new float[] {
		// ambientAndDiffuse[0] - 0.8f,
		// ambientAndDiffuse[1] - 0.8f,
		// ambientAndDiffuse[2] - 0.8f, 1 }, 0 );
		// gl.glLightfv( GL.GL_LIGHT0, GL.GL_DIFFUSE, new float[] { ambientAndDiffuse[0],
		// ambientAndDiffuse[1],
		// ambientAndDiffuse[2], 1 }, 0 );
		// gl.glLightfv( GL.GL_LIGHT0, GL.GL_SPECULAR, new float[] { ambientAndDiffuse[0],
		// ambientAndDiffuse[1],
		// ambientAndDiffuse[2], 0 }, 0 );

		// gl.glLightfv( GL.GL_LIGHT1, GL.GL_POSITION, new float[] { 0, 1, -1, 0 }, 0 );
		// gl.glLightfv( GL.GL_LIGHT1, GL.GL_DIFFUSE, new float[] { ambientAndDiffuse[0],
		// ambientAndDiffuse[1],
		// ambientAndDiffuse[2], 1 }, 0 );
		// gl.glLightfv( GL.GL_LIGHT1, GL.GL_SPECULAR, new float[] { ambientAndDiffuse[0],
		// ambientAndDiffuse[1],
		// ambientAndDiffuse[2], 0 }, 0 );
		// gl.glLightfv( GL.GL_LIGHT1, GL.GL_AMBIENT, new float[] { ambientAndDiffuse[0] -
		// 0.5f,
		// ambientAndDiffuse[1] - 0.5f,
		// ambientAndDiffuse[2] - 0.5f, 1 }, 0 );
		// gl.glEnable( GL.GL_LIGHT1 );
		// gl.glLightf( GL.GL_LIGHT0, GL.GL_SHININESS, intens );

		gl.glLightModelf(GL.GL_LIGHT_MODEL_LOCAL_VIEWER, GL.GL_TRUE);

		// make glColor set the material properties
		// gl.glEnable( GL.GL_COLOR_MATERIAL );
		// gl.glColorMaterial( GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT );

		gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_LIGHT0);

		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glEnable(GL.GL_DEPTH_TEST);

		// enable vertex arrays
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL.GL_NORMAL_ARRAY);

		// make sure the initvalues are initialized.
		initValues.createCompositingTextureShaderPrograms(gl);
		this.glRenderContext = new RenderContext(params, zScale, JOGLUtils.getMaxTextureSize(gl),
				initValues.getCompositingTextureShaderPrograms());
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		float aspect = (float) width / (float) height;
		LOG.info("reshape( GLAutoDrawable, " + x + ", " + y + ", " + width + ", " + height + " ) called, aspect: "
				+ aspect);

		params.setProjectionPlaneDimensions(width, height);

		GL gl = drawable.getGL();

		gl.glViewport(x, y, width, height);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(params.getViewFrustum().getFOVY(), aspect, params.getViewFrustum().getZNear(),
				params.getViewFrustum().getZFar());
		// glu.gluPerspective( params.getViewFrustum().getFOVY(), aspect, 0.01, 1000 );
		gl.glMatrixMode(GL.GL_MODELVIEW);
	}

	private void displayStats(GL gl) {
		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		gl.glWindowPos2d(20, 30);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "frame [ms]: " + lastFrameMs + " (max=" + maxFrameMs + ")");
		gl.glWindowPos2d(20, 44);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "avg [ms]: " + allFramesMs / numFrames);
		gl.glWindowPos2d(20, 58);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "num frames: " + numFrames);
		gl.glWindowPos2d(20, 72);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "enabled datasets: " + currentDatasets);
		gl.glWindowPos2d(20, 86);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "available datasets: " + availableDatasets);
		gl.glRasterPos2d(0, 0);
		gl.glColor3f(1.0f, 1.0f, 1.0f);

	}

	@Override
	public void keyPressed(KeyEvent ev) {

		int k = ev.getKeyCode();

		float scale = 1.01f;
		if ((ev.getModifiersEx()
				& (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) == InputEvent.SHIFT_DOWN_MASK) {
			// SHIFT (and not CTRL)
			scale = 1.10f;
		}
		else if ((ev.getModifiersEx()
				& (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) == InputEvent.CTRL_DOWN_MASK) {
			// CTRL (and not SHIFT)
			scale /= 1.001;
		}

		switch (k) {
			case KeyEvent.VK_U: {
				this.updateLODStructure = !updateLODStructure;
				break;
			}
			case KeyEvent.VK_F11: {
				lodAnalyzerFrame.setVisible(!lodAnalyzerFrame.isVisible());
				this.setEnabled(true);
				break;
			}
			case KeyEvent.VK_T: {
				if (this.availableDatasets.contains("trees")) {
					renderTrees = !renderTrees;
					if (renderTrees) {
						this.currentDatasets.add(currentDatasets.size() - 1, "trees");
					}
					else {
						this.currentDatasets.remove("trees");
					}
				}
				break;
			}
			case KeyEvent.VK_C: {
				if (!this.availableColorMaps.isEmpty()) {
					this.currentColormap++;
					if (this.currentColormap >= this.availableColorMaps.size()) {
						this.currentColormap = -1;
					}
					if (this.currentColormap >= 0) {
						String title = this.availableColorMaps.get(currentColormap);
						List<String> cm = new ArrayList<String>(1);
						cm.add(title);
						this.activeColormap = new Pair<String, Colormap>(title,
								this.perspectiveViewService.getColormap(cm, null));
						this.currentDatasets.add(title);
					}
					else {
						if (this.activeColormap != null) {
							this.currentDatasets.remove(this.activeColormap.first);
						}
						this.activeColormap = null;
					}

				}
				break;
			}
			case KeyEvent.VK_B: {
				if (this.availableDatasets.contains("buildings")) {
					renderBuildings = !renderBuildings;
					renderTrees = !renderTrees;
					if (renderBuildings) {
						this.currentDatasets.add(currentDatasets.size() - 1, "buildings");
					}
					else {
						this.currentDatasets.remove("buildings");
					}
				}
				break;
			}
			case KeyEvent.VK_G: {
				getImage = true;
				break;
			}
			case KeyEvent.VK_1: {
				disableElevationModel = !disableElevationModel;
				if (disableElevationModel) {
					this.currentDatasets.remove("dem");
				}
				else {
					this.currentDatasets.add("dem");
				}
				break;
			}
			case KeyEvent.VK_2: {
				if (activeTextureManagers.length >= 1) {
					activeTextureManagers[0] = !activeTextureManagers[0];
					if (activeTextureManagers[0]) {
						this.currentDatasets.add(Math.min(currentDatasets.size(), 1), this.availableDatasets.get(1));
					}
					else {
						this.currentDatasets.remove(this.availableDatasets.get(1));
					}
				}
				break;
			}
			case KeyEvent.VK_3: {
				if (activeTextureManagers.length >= 2) {
					activeTextureManagers[1] = !activeTextureManagers[1];
					if (activeTextureManagers[1]) {
						this.currentDatasets.add(Math.min(currentDatasets.size(), 2), this.availableDatasets.get(2));
					}
					else {
						this.currentDatasets.remove(this.availableDatasets.get(2));
					}
				}
				break;
			}
			case KeyEvent.VK_4: {
				if (activeTextureManagers.length >= 3) {
					activeTextureManagers[2] = !activeTextureManagers[2];
					if (activeTextureManagers[2]) {
						this.currentDatasets.add(Math.min(currentDatasets.size(), 3), this.availableDatasets.get(3));
					}
					else {
						this.currentDatasets.remove(this.availableDatasets.get(3));
					}
				}
				break;
			}
			case KeyEvent.VK_5: {
				if (activeTextureManagers.length >= 4) {
					activeTextureManagers[3] = !activeTextureManagers[3];
					if (activeTextureManagers[3]) {
						this.currentDatasets.add(Math.min(currentDatasets.size(), 4), this.availableDatasets.get(4));
					}
					else {
						this.currentDatasets.remove(this.availableDatasets.get(4));
					}

				}
				break;
			}
			case KeyEvent.VK_6: {
				if (activeTextureManagers.length >= 5) {
					activeTextureManagers[4] = !activeTextureManagers[4];
					if (activeTextureManagers[4]) {
						this.currentDatasets.add(Math.min(currentDatasets.size(), 5), this.availableDatasets.get(5));
					}
					else {
						this.currentDatasets.remove(this.availableDatasets.get(5));
					}

				}
				break;
			}
			case KeyEvent.VK_7: {
				if (activeTextureManagers.length >= 6) {
					activeTextureManagers[5] = !activeTextureManagers[5];
					if (activeTextureManagers[5]) {
						this.currentDatasets.add(Math.min(currentDatasets.size(), 6), this.availableDatasets.get(6));
					}
					else {
						this.currentDatasets.remove(this.availableDatasets.get(6));
					}
				}
				break;
			}
			case KeyEvent.VK_8: {
				if (activeTextureManagers.length >= 7) {
					activeTextureManagers[6] = !activeTextureManagers[6];
					if (activeTextureManagers[6]) {
						this.currentDatasets.add(Math.min(currentDatasets.size(), 7), this.availableDatasets.get(7));
					}
					else {
						this.currentDatasets.remove(this.availableDatasets.get(7));
					}

				}
				break;
			}
			case KeyEvent.VK_9: {
				if (activeTextureManagers.length >= 8) {
					activeTextureManagers[7] = !activeTextureManagers[7];
					if (activeTextureManagers[7]) {
						this.currentDatasets.add(Math.min(currentDatasets.size(), 8), this.availableDatasets.get(8));
					}
					else {
						this.currentDatasets.remove(this.availableDatasets.get(8));
					}

				}
				break;
			}
			case KeyEvent.VK_PAGE_DOWN: {
				glRenderContext.setTerrainScale(glRenderContext.getTerrainScale() / scale);
				break;
			}
			case KeyEvent.VK_PAGE_UP: {
				glRenderContext.setTerrainScale(glRenderContext.getTerrainScale() * scale);
				break;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyTyped(KeyEvent ev) {
		// TODO Auto-generated method stub
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 * @throws JAXBException
	 * @throws OWSException
	 * @throws ServiceInitException
	 */
	public static void main(String[] args)
			throws IOException, UnsupportedOperationException, JAXBException, OWSException, ServiceInitException {

		Options options = initOptions();

		// for the moment, using the CLI API there is no way to respond to a help
		// argument; see
		// https://issues.apache.org/jira/browse/CLI-179
		if (args != null && args.length > 0) {
			for (String a : args) {
				if (a != null && a.toLowerCase().contains("help") || "-?".equals(a)) {
					printHelp(options);
					System.exit(1);
				}
			}
		}

		try {
			CommandLine line = new PosixParser().parse(options, args);

			String request = null;
			// request =
			// "http://localhost:8080/services/services?service=WPVS&request=GetView&version=0.4.0&crs=epsg:31466&ELEVATIONMODEL=Elevation&OUTPUTFORMAT=image%2Fjpeg&EXCEPTIONS=application/vnd.ogc.se_xml&ROLL=0&Boundingbox=2579816.5%2C5616304.5%2C2582519.5%2C5619007.5&DATETIME=2006-06-21T12:30:00&AOV=60&SCALE=1.0&BACKGROUND=cirrus&WIDTH=800&HEIGHT=600&BACKGROUNDCOLOR=0xc6d6e5&datasets=Buildings,Trees,aerophoto-2007&POI=2579778,5620865,50&YAW=273&PITCH=25&DISTANCE=842";

			String configFile = line.getOptionValue(OPT_WPVS_CONFIG_FILE);

			LOG.info("Checking for JOGL.");
			JOGLChecker.check();
			LOG.info("JOGL check ok.");

			InteractiveWPVS app = createWPVSInstance(configFile, request, line);

			JFrame frame = new JFrame("deegree interactive WPVS");
			frame.getContentPane().add(app, BorderLayout.CENTER);
			frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
			frame.setSize(800, 600);
			frame.setLocationByPlatform(true);
			frame.setVisible(true);
			frame.pack();
		}
		catch (ParseException exp) {
			System.err.println("ERROR: Invalid command line: " + exp.getMessage());
		}

		// System.exit( 0 );
	}

	private static InteractiveWPVS createWPVSInstance(String configFile, String getUrl, CommandLine line)
			throws UnsupportedOperationException, IOException, OWSException, ServiceInitException {
		File baseDir = new File(configFile);
		if (!baseDir.exists()) {
			throw new FileNotFoundException("Given location: " + configFile
					+ " does not exist, please supply a valid configuration file / directory.");
		}
		if (baseDir.isFile()) {
			// let's assume it is a configuration file, let's test for service as parent.
			File parent = baseDir.getParentFile();
			if (parent == null || !parent.exists()) {
				throw new FileNotFoundException("The parent of configuration location: " + configFile
						+ " does not exist, no way to load the datasource, please supply a valid configuration directory.");
			}
			if (!"services".equalsIgnoreCase(parent.getName())) {
				System.out.println("The parent ( " + parent.getName() + " of file: " + baseDir.getAbsolutePath()
						+ " is not a deegree 3 'services' directory, trying to find the datasource directory.");
			}
			File p2 = parent.getParentFile();
			if (p2 == null || !p2.exists()) {
				throw new FileNotFoundException("The root of your wpvs configuration location: "
						+ parent.getAbsolutePath() + "../"
						+ " does not exist, no way to load the configuration/datasources, please supply a valid configuration directory.");
			}
			baseDir = p2;

		}

		// TODO adapt to workspace concept
		workspace = new DefaultWorkspace(baseDir);

		String dbURL = line.getOptionValue(OPT_WPVS_DB_CONNECTION);
		if (dbURL != null && !"".equals(dbURL)) {
			String user = line.getOptionValue(OPT_WPVS_DB_USER);
			String pass = line.getOptionValue(OPT_WPVS_DB_PASS);
			String id = line.getOptionValue(OPT_WPVS_DB_ID);
			ResourceLocation<ConnectionProvider> loc = ConnectionProviderUtils.getSyntheticProvider(id, dbURL, user,
					pass);
			workspace.getLocationHandler().addExtraResource(loc);
		}

		File dsDir = new File(baseDir, "/datasources/");
		if (!dsDir.exists()) {
			throw new FileNotFoundException("The expected datasource location " + dsDir.getAbsolutePath()
					+ ", does not exists, please supply a valid configuration directory.");
		}
		try {
			workspace.initAll();
		}
		catch (ResourceInitException e) {
			LOG.error("Initialization failed: {}", e.getLocalizedMessage());
			LOG.trace("Stack trace: ", e);
			return null;
		}

		if (workspace.getResourceManager(OwsManager.class) == null) {
			throw new FileNotFoundException("No web service configurations were found in the workspace.");
		}
		if (workspace.getResourceManager(OwsManager.class).getByOWSClass(WPVSController.class) == null) {
			throw new FileNotFoundException("No WPVS configuration was found in the workspace.");
		}

		float zScale = 1;
		ViewParams params = null;
		if (getUrl != null) {
			GetView gv = createFromRequest(getUrl);
			zScale = gv.getSceneParameters().getScale();
			params = gv.getViewParameters();
		}
		return new InteractiveWPVS(workspace, params, zScale);
	}

	private ViewParams getViewParams(TerrainRenderingManager demRenderer) {
		// // setup viewer position and direction
		// // Point3d eye = new Point3d( 1169.4413, 989.9026, 331.31174 );
		// // Point3d eye = new Point3d( 15927.5, 7689, 300 );
		//
		// // Vector3d viewDirection = new Vector3d( 0.72272307, 0.64145917, -0.25741702
		// );
		// // Point3d lookingAt = new Point3d( viewDirection );
		// // lookingAt.add( eye );
		// // Vector3d viewerUp = new Vector3d( 0.2185852, 0.14120139, 0.96556276 );
		// //
		// // Point3d eye = new Point3d( 12962.94836753434, 12593.593984246898,
		// 90.38167030764276 );
		// // Point3d lookingAt = new Point3d( 12962.613589134678, 12594.53064243064,
		// 90.48460169230239 );
		// // Vector3d viewerUp = new Vector3d( 0.017654201204610157,
		// -0.10298316020337722, 0.9945408808038575 );
		//
		// // looking up the godesberger hill
		// // Point3d eye = new Point3d( 13451.07327279907, 11069.148746358578,
		// 102.0725860674174 );
		// // Point3d lookingAt = new Point3d( 13450.82937390088, 11070.108252492108,
		// 102.21351621742897 );
		// // Vector3d viewerUp = new Vector3d( 0.06727614458805162, -0.12823156824233176,
		// 0.9894742800958647 );

		// Point3d eye = new Point3d( 17986.5718025168, 10445.807745848066,
		// 29013.44152491993 );
		// Point3d lookingAt = new Point3d( 17986.5314521542, 10445.81549881379,
		// 29012.4423694065 );
		// Vector3d viewerUp = new Vector3d( -0.0042550244648288235, 0.9999738721136294,
		// 0.007931932463392117 );

		// hochwasser blick
		// Point3d eye = new Point3d( 12540.065339366225, 14825.06184789524,
		// 405.8445763856689 );
		// Point3d lookingAt = new Point3d( 11778.0, 14865.0, 50.0 );
		// Vector3d viewerUp = new Vector3d( -0.42203907810090174, 0.022118130853929614,
		// 0.9063077870366498 );

		// ls_ns_gebiet
		// Point3d eye = new Point3d( 7911.181456483806, 11214.239820576904,
		// 298.4662832984199 );
		// Point3d lookingAt = new Point3d( 7992.0, 12138.0, 50.0 );
		// Vector3d viewerUp = new Vector3d( 0.022557566113149727, 0.2578341604962995,
		// 0.9659258262890682 );

		// umgebungs
		// Point3d eye = new Point3d( 11646.815747933482, 18449.506280218528,
		// 212.34068114402103 );
		// Point3d lookingAt = new Point3d( 11429.0, 18087.0, 50.0 );
		// Vector3d viewerUp = new Vector3d( -0.1845731388432743, -0.3071812879698955,
		// 0.9335804264972021 );

		// jens
		// Point3d eye = new Point3d( 10913.308304137812, 15021.543377732793,
		// 90.34890557577096 );
		// Point3d lookingAt = new Point3d( 10914.25310107993, 15021.218113244397,
		// 90.30938658346824 );
		// Vector3d viewerUp = new Vector3d( 0.030261091918976406, -0.03347475349449462,
		// 0.9989813347577329 );

		// Point3d eye = new Point3d( -73047.70146472409, 38512.120946986484,
		// 26970.205157368593 );
		// Point3d lookingAt = new Point3d( -73046.78120268782, 38511.863489770185,
		// 26969.910482010924 );
		// Vector3d viewerUp = new Vector3d( 0.2747903722504608, -0.11093253999688812,
		// 0.9550833591306462 );

		// Point3d eye = new Point3d( 11795.310126271357, 14458.153190013969,
		// 128.14202719026218 );
		// Point3d lookingAt = new Point3d( 11795.526694264807, 14459.094383967173,
		// 127.88269680896333 );
		// Vector3d viewerUp = new Vector3d( 0.06763746981420082, 0.2505311057936102,
		// 0.9657428942047335 );

		// // sunposition
		// Point3d eye = new Point3d( 0, 0, 7 );
		// Point3d lookingAt = new Point3d( 0, 0, 0 );
		// Vector3d viewerUp = new Vector3d( 0, 1, 0 );

		// Point3d eye = new Point3d( 14863.581689508377, 14874.257693700007,
		// 146.73991301323542 );
		// Point3d lookingAt = new Point3d( 14864.08371028227, 14873.40329772973,
		// 146.8740124858517 );
		// Vector3d viewerUp = new Vector3d( -0.0579411289423698, 0.12147962354586098,
		// 0.9909013707932913 );

		// Point3d eye = new Point3d( 17183.96902170159, 11104.651411049092,
		// 5252.299959766186 );
		// Point3d lookingAt = new Point3d( 17183.87383467913, 11104.540544537771,
		// 5251.310693347357 );
		// Vector3d viewerUp = new Vector3d( 0.005618138084521037, -0.9938227673681405,
		// 0.1108365624924782 );

		// schloss
		// Point3d eye = new Point3d( 9215.695465194443, 15321.819352516042,
		// 177.70466056621308 );
		// Point3d lookingAt = new Point3d( 9215.868711190713, 15322.726799941856,
		// 177.32187473293513 );
		// Vector3d viewerUp = new Vector3d( 0.08839715708976609, 0.37276722064380496,
		// 0.9237047914955354 );

		// rhein
		// Point3d eye = new Point3d( 13925.753788033839, 14431.125891954574,
		// 85.04428646818577 );
		// Point3d lookingAt = new Point3d( 13926.097992769744, 14430.235130354195,
		// 84.74752583894274 );
		// Vector3d viewerUp = new Vector3d( 0.09598128880808184, -0.28103511759282995,
		// 0.9548857810638757 );

		// tree_posttower
		// Point3d eye = new Point3d( 12402.045997461932, 14793.69142817672,
		// 70.4937199211793 );
		// Point3d lookingAt = new Point3d( 12401.10002391475, 14793.382817636224,
		// 70.39425343271608 );
		// Vector3d viewerUp = new Vector3d( -0.1033479271492392, -0.0037871342018632754,
		// 0.9946380565655442 );

		// Point3d eye = new Point3d( 6448.260268219342, 17363.50304721038,
		// 84.29050865858129 );
		// Point3d lookingAt = new Point3d( 6447.634642666272, 17364.28204533543,
		// 84.33239646721954 );
		// Vector3d viewerUp = new Vector3d( 0.06425052852314768, -0.002059566706843565,
		// 0.9979316749003633 );

		// Point3d eye = new Point3d( 350, -127, 23.7 );
		// Point3d lookingAt = new Point3d( 349.37437444693, -126.221001874950,
		// 23.65811219136174 );
		// Vector3d viewerUp = new Vector3d( 0.06425052852314768, -0.002059566706843565,
		// 0.9979316749003633 );

		// Point3d eye = new Point3d( 6442.004012688637, 17371.293028460885,
		// 84.7093867449643 );
		// Point3d lookingAt = new Point3d( 6441.378387135567, 17372.072026585935,
		// 84.75127455360256 );
		// Vector3d viewerUp = new Vector3d( 0.06425052852314768, -0.002059566706843565,
		// 0.9979316749003633 );

		// essen gebäude
		// Point3d eye = new Point3d( 5441.872284653714, 7803.240170850542,
		// 150.8541731262849 );
		// Point3d lookingAt = new Point3d( 5441.621465393425, 7804.178715235103,
		// 150.61705683228917 );
		// Vector3d viewerUp = new Vector3d( 0.030150645228390737, 0.2524015253189707,
		// 0.9671527328239826 );

		// demo
		// Point3d eye = new Point3d( 7751.4274632595325, 7738.632168131135,
		// 1700.8178651653407 );
		// Point3d lookingAt = new Point3d( 7751.506737376979, 7739.419748747671,
		// 1700.2067740209447 );
		// Vector3d viewerUp = new Vector3d( -0.08199978679939689, 0.6160949046469247,
		// 0.7833920496359404 );

		// Point3d eye = new Point3d( 4842.663403926125, -21159.654750781716,
		// 24123.268156063317 );
		// Point3d lookingAt = new Point3d( 4842.742678043572, -21158.86717016518,
		// 24122.65706491892 );
		// Vector3d viewerUp = new Vector3d( -0.081999.78679939689, 0.6160949046469247,
		// 0.7833920496359404 );

		// Point3d eye = new Point3d( 13360.610583075291, 13759.692891781559,
		// 8636.598222159204 );
		// Point3d lookingAt = new Point3d( 13360.078832161853, 13759.276659426903,
		// 8635.860664301814 );
		// Vector3d viewerUp = new Vector3d( -0.7073708715804196, -0.2605954677793352,
		// 0.6570513314895419 );

		// Point3d eye = new Point3d( 6081.839091011266, 384.75124442199444,
		// 6432.359365945068 );
		// Point3d lookingAt = new Point3d( 6081.859440809533, 385.502735808828,
		// 6431.699936963884 );
		// Vector3d viewerUp = new Vector3d( -0.006707700408040963, 0.6596533401191206,
		// 0.7515400705382526 );

		// Kennedy
		// Point3d eye = new Point3d( 10394.591904069532, 17177.24388643706,
		// 82.62293305712367 );
		// Point3d lookingAt = new Point3d( 10394.961152650601, 17176.37017540717,
		// 82.30625574245802 );
		// Vector3d viewerUp = new Vector3d( 0.12756699054896722, -0.2898805092310912,
		// 0.9485230378278534 );
		// Point3d eye = new Point3d( 10394.591904069532, 17177.24388643706,
		// 82.62293305712367 );
		// Point3d lookingAt = new Point3d( 10394.961152650601, 17176.37017540717,
		// 82.30625574245802 );
		// Vector3d viewerUp = new Vector3d( 0.12756699054896722, -0.2898805092310912,
		// 0.9485230378278534 );

		// start a position that works for any other datasets (not Bonn) as well
		// Point3d eye = new Point3d( 0.0, 0.0, 500.0 );
		// Point3d lookingAt = new Point3d( 0.0, 0.0, 0.0 );
		// Vector3d viewerUp = new Vector3d( 0, 1, 0 );

		Point3d eye = new Point3d(200, 200, 200);
		Point3d lookingAt = new Point3d(200, 200, 40);
		Vector3d viewerUp = new Vector3d(0, 1, 0);

		// Point3d eye = new Point3d( 10048.734288083613, 16641.184387034107,
		// 102.67907642130088 );
		// Point3d lookingAt = new Point3d( 10049.187423499783, 16642.04973827599,
		// 102.89316874434968 );
		// Vector3d viewerUp = new Vector3d( -0.12084130245951936, -0.17831934773102942,
		// 0.9765242392509327 );

		// Point3d eye = new Point3d( -32.39931707896916, 159.80418924642078,
		// 1126.912059107212 );
		// Point3d lookingAt = new Point3d( -32.214625131242, 159.68821181918537,
		// 1125.9361297445957 );
		// Vector3d viewerUp = new Vector3d( 0.043593521862963565, -0.9910685786306717,
		// 0.12602649444650582 );

		// Point3d eye = new Point3d( 16384.0, 16384.0, 23449.175586768335 );
		// Point3d lookingAt = new Point3d( 16384.0, 16384.0, 23295.975589820093 );
		// Vector3d viewerUp = new Vector3d( 0.0, 1.0, 0.0 );

		// Point3d eye = new Point3d( 275.9620120805319, 64.46718597043534,
		// 562.4719914099325 );
		// Point3d lookingAt = new Point3d( 275.53842374511527, 64.61744308424116,
		// 561.5786853471244 );
		// Vector3d viewerUp = new Vector3d( -0.8988362258828301, 0.05280297809393711,
		// 0.43509227130010486 );

		// Point3d eye = new Point3d( 12918.670266734358, 11638.312310974536,
		// 150.9617994096138 );
		// Point3d lookingAt = new Point3d( 12918.923183515637, 11637.442084650263,
		// 150.53902376427538 );
		// Vector3d viewerUp = new Vector3d( 0.0768278457638342, -0.4175378808700864,
		// 0.9054057654741021 );

		// demRenderer = null;

		// geglättet?
		// Point3d eye = new Point3d( 7510.179944980607, 18132.55247159876,
		// 68.67045685404854 );
		// Point3d lookingAt = new Point3d( 7511.084219286393, 18132.97794378385,
		// 68.63494066607761 );
		// Vector3d viewerUp = new Vector3d( 0.027791438877521564, 0.024351414382101418,
		// 0.9993170890876871 );

		// b9
		// Point3d eye = new Point3d( 10979.19258508588, 14968.466515333152,
		// 64.72044690627314 );
		// Point3d lookingAt = new Point3d( 10979.980566257613, 14967.850816271703,
		// 64.72102890882154 );
		// Vector3d viewerUp = new Vector3d( -0.013955210675769197, -0.016915050429668607,
		// 0.9997595376708944 );

		// Point3d eye = new Point3d( 9828.47264469345, 16798.85145159197,
		// 60.203111000265814 );
		// Point3d lookingAt = new Point3d( 9827.530665218446, 16799.169395749544,
		// 60.09547187476404 );
		// Vector3d viewerUp = new Vector3d( -0.09913125830807487, 0.04286879256498191,
		// 0.994150521928221 );

		// Point3d eye = new Point3d( 15576.6342518542, 12838.569167080923,
		// 68.48134435085872 );
		// Point3d lookingAt = new Point3d( 15576.117083773124, 12839.42435008561,
		// 68.44671481997439 );
		// Vector3d viewerUp = new Vector3d( -0.06343873856618466, 0.0020477656389633015,
		// 0.9979836336858363 );

		// Point3d eye = new Point3d( 12812.052257831021, 14217.479020702382,
		// 107.09873254245474 );
		// Point3d lookingAt = new Point3d( 12811.188477648411, 14217.920063628097,
		// 106.85508334010474 );
		// Vector3d viewerUp = new Vector3d( -0.22985946417393577, 0.08539437677143473,
		// 0.9694701785744898 );

		// Point3d eye = new Point3d( 12981.026537448855, 14614.67522876718,
		// 64.11014757850944 );
		// Point3d lookingAt = new Point3d( 12981.038842815053, 14614.578292510932,
		// 63.11493305882678 );
		// Vector3d viewerUp = new Vector3d( -0.9531604427744198, 0.29967690949927844,
		// -0.04097462925758576 );

		// Point3d eye = new Point3d( 12981.149591110829, 14613.705866204698,
		// 54.15800238168214 );
		// Point3d lookingAt = new Point3d( 12981.161896477026, 14613.60892994845,
		// 53.162787861999476 );
		// Vector3d viewerUp = new Vector3d( -0.9531604427744198, 0.29967690949927844,
		// -0.04097462925758576 );

		// Point3d eye = new Point3d( 15194.79953395015, 14758.685598863745,
		// 260.46604235461416 );
		// Point3d lookingAt = new Point3d( 15193.928403030075, 14758.526682033795,
		// 260.00141738622966 );
		// Vector3d viewerUp = new Vector3d( -0.46922710289487696, -0.009549895584917189,
		// 0.8830258916945521 );

		// Point3d eye = new Point3d( 14044.001468902079, 14493.412095321868,
		// 99.7017835888749 );
		// Point3d lookingAt = new Point3d( 14043.100411829999, 14493.217441627952,
		// 99.31421928156796 );
		// Vector3d viewerUp = new Vector3d( -0.38859083784627274, -0.034487121403220806,
		// 0.9207647903775463 );

		// Point3d eye = new Point3d( 13941.009894736511, 14468.932309482483,
		// 54.79817692426634 );
		// Point3d lookingAt = new Point3d( 13940.629520838034, 14468.899319115404,
		// 53.873932728700424 );
		// Vector3d viewerUp = new Vector3d( -0.9095971676519867, -0.16730620974684152,
		// 0.3803177944553526 );

		// Point3d eye = new Point3d( 13830.390906966933, 14466.7982625299,
		// 52.331671222522104 );
		// Point3d lookingAt = new Point3d( 13829.983666405678, 14466.903710390307,
		// 51.42445797636429 );
		// Vector3d viewerUp = new Vector3d( -0.8982101223665114, 0.13370044237873424,
		// 0.4187394987169722 );

		// Point3d eye = new Point3d( 11672.849103687942, 14869.854300744528,
		// 69.884234719344 );
		// Point3d lookingAt = new Point3d( 11671.965328114737, 14870.006960868699,
		// 69.44192756058229 );
		// Vector3d viewerUp = new Vector3d( -0.4447948753649359, 0.01934610579968591,
		// 0.8954235015003251 );

		// Point3d eye = new Point3d( 11608.37185230881, 14960.25622251627,
		// 57.303409872107316 );
		// Point3d lookingAt = new Point3d( 11607.966165915617, 14960.942471044413,
		// 56.69968683295827 );
		// Vector3d viewerUp = new Vector3d( -0.49208113043558477, 0.3926383915255517,
		// 0.7769757104112581 );

		// Point3d eye = new Point3d( 11640.826763764217, 14905.356340264812,
		// 105.60125300402316 );
		// Point3d lookingAt = new Point3d( 11640.421077371024, 14906.042588792956,
		// 104.99752996487413 );
		// Vector3d viewerUp = new Vector3d( -0.49208113043558477, 0.3926383915255517,
		// 0.7769757104112581 );

		// eye = new Point3d( 302.89945876629815, 32478.042855379656, 1107.8712218394253
		// );
		// lookingAt = new Point3d( 302.8243522721566, 32477.936366469246,
		// 1106.8797486292488 );
		// viewerUp = new Vector3d( -0.012344764703222063, 0.9943046401331957,
		// -0.10585787355701011 );
		//
		// eye = new Point3d( 255.80779269279031, 32411.27445849423, 486.2189150965908 );
		// lookingAt = new Point3d( 255.73268619864876, 32411.16796958382,
		// 485.2274418864143 );
		// viewerUp = new Vector3d( -0.012344764703222063, 0.9943046401331957,
		// -0.10585787355701011 );
		//
		// eye = new Point3d( 10620.468935048752, 14985.997719715537, 71.66828874932546 );
		// lookingAt = new Point3d( 10620.490821833564, 14985.32548833627,
		// 70.92827122157445 );
		// viewerUp = new Vector3d( 0.14028570064737583, -0.7308065630552575,
		// 0.6680132405792482 );

		double fovy = 45.0;
		if (demRenderer != null && eye.x == 200 && eye.y == 200 && eye.z == 200) {
			RenderFragmentManager fragmentManager = demRenderer.getFragmentManager();
			if (fragmentManager != null) {
				MultiresolutionMesh multiresolutionMesh = fragmentManager.getMultiresolutionMesh();
				if (multiresolutionMesh != null) {
					double[][] bBox = multiresolutionMesh.getBBox();
					if (bBox != null) {
						double centerX = bBox[0][0] + ((bBox[1][0] - bBox[0][0]) * 0.5f);
						double centerY = bBox[0][1] + ((bBox[1][1] - bBox[0][1]) * 0.5f);
						double centerZ = bBox[0][2] + ((bBox[1][2] - bBox[0][2]) * 0.5f);
						double eyeZ = Math.max(centerX, centerY) / Math.tan(Math.toRadians(fovy * 0.5));
						lookingAt = new Point3d(centerX, centerY, centerZ);
						eye = new Point3d(centerX, centerY, eyeZ);
					}
				}
			}
		}

		// texture error
		// Point3d eye = new Point3d( 4773.823341206398, 31098.005203681878,
		// 10435.788368373765 );
		// Point3d lookingAt = new Point3d( 4774.131456607485, 31097.242049024728,
		// 10435.220336799908 );
		// Vector3d viewerUp = new Vector3d( 0.22472638841595236, -0.5217978426441787,
		// 0.8229368516243384 );

		return new ViewParams(eye, lookingAt, viewerUp, fovy, zNear, zFar);
	}

	private static GetView createFromRequest(String request) throws OWSException {
		Map<String, String> parsedRequest = parseRequest(request);
		return GetViewKVPAdapter.create(parsedRequest, "UTF-8", new double[] { -2568000.0, -5606000.0 }, zNear, zFar);
	}

	/**
	 * @param request
	 */
	private static Map<String, String> parseRequest(String request) {

		Map<String, String> result = new HashMap<String, String>();
		String[] server = request.split("\\?");
		if (server == null || server.length != 2) {
			throw new IllegalArgumentException("invalid key-value request.");
		}

		String[] splitter = server[1].split("&");
		for (String t : splitter) {
			String[] split = t.trim().split("=");
			if (split != null) {
				if (split.length == 2) {
					result.put(split[0].toUpperCase(), split[1]);
				}
				else {
					StringBuilder sb = new StringBuilder(
							"The given request holds an invalid - key value pair with following values:\n");
					for (String tmp : split) {
						sb.append("- ").append(tmp).append("\n");
					}
					System.out.println(sb.toString());
				}
			}
			else {
				System.out.println("The given request holds an invalid null - key value pair.");

			}
		}
		return result;
	}

	private static Options initOptions() {

		Options opts = new Options();

		Option opt = new Option(OPT_WPVS_CONFIG_FILE, true, "URL of WPVS configuration (e.g. file:/...)");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option("host", OPT_WPVS_DB_CONNECTION, true,
				"URL to db connection (e.g. jdbc:postgres://localhost:5432/db_name)");
		opts.addOption(opt);

		opt = new Option("u", OPT_WPVS_DB_USER, true, "Database user name");
		opts.addOption(opt);

		opt = new Option("p", OPT_WPVS_DB_PASS, true, "Database password");
		opts.addOption(opt);

		opt = new Option("id", OPT_WPVS_DB_ID, true, "Database id referenced in the wpvs configuration");
		opts.addOption(opt);

		CommandUtils.addDefaultOptions(opts);

		return opts;
	}

	private static void printHelp(Options options) {
		CommandUtils.printHelp(options, InteractiveWPVS.class.getSimpleName(), null, "");
	}

}
