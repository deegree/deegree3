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

package org.deegree.tools.rendering.manager.buildings.importers;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.deegree.rendering.r3d.opengl.JOGLUtils.convertColorGLColor;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Leaf;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TriangleArray;
import javax.media.j3d.TriangleFanArray;
import javax.media.j3d.TriangleStripArray;
import javax.media.opengl.GL;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.math.MathUtils;
import org.deegree.commons.utils.math.Vectors3d;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r3d.model.geometry.SimpleGeometryStyle;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableGeometry;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableQualityModel;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableQualityModelPart;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableTexturedGeometry;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.jdesktop.j3d.loaders.vrml97.VrmlLoader;

import com.sun.j3d.loaders.Scene;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;

/**
 * The <code>J3DToCityGMLExporter</code> exports a J3D scene to citygml level 1.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 */
public class VRMLImporter implements ModelImporter {

	private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(VRMLImporter.class);

	/** Key for the rotation angle key, 4 comma separated values, x,y,z,a */
	public static final String ROT_ANGLE = "rotationAngle";

	/** Key for the translation of the x (first) axis */
	public static final String XTRANS = "xtranslation";

	/** Key for the translation of the y (second) axis */
	public static final String YTRANS = "ytranslation";

	/** Key for the translation of the z (third) axis */
	public static final String ZTRANS = "ztranslation";

	/** Key to flip the y(second) and z (third) axis */
	public static final String INV_YZ = "inverseyz";

	/** Key for the texture directory */
	public static final String TEX_DIR = "texdir";

	/** Key for the max texture size */
	public static final String MAX_TEX_DIM = "maxtexturedimension";

	private static int textureCount = 0;

	// Instance variables.
	// private String name = null;

	// Instance variables.
	private String id = null;

	private String textureimportDir;

	private Transform3D userMatrix = null;

	private Transform3D userRotationMatrix = null;

	private final boolean doUserTransform;

	private final boolean doUserRotation;

	private HashMap<BufferedImage, String> cachedTextures = new HashMap<BufferedImage, String>();

	private int maxTextureDimension = -1;

	/**
	 * A default constructor allows the instantiation of this exported to supply the
	 * getParameterList method.
	 * <p>
	 * <b>NOTE</b> this constructor is only convenient, it should not be used carelessly.
	 * Instead use one of the other constructors for correct instantiation of this class.
	 * </p>
	 */
	public VRMLImporter() {
		// nothing
		doUserRotation = false;
		doUserTransform = false;
	}

	/**
	 * @param params a hashmap to extract values from. For a list of supported parameters
	 * @throws IOException if the key 'texdir' points to a file which is not a directory
	 * or is not writable
	 */
	public VRMLImporter(Map<String, String> params) throws IOException {
		String texDir = params.get(TEX_DIR);
		if (texDir == null || "".equals(texDir.trim())) {
			LOG.info("Setting tex dir to " + System.getProperty("user.home") + "/j3dTextures/");
			texDir = System.getProperty("user.home") + "/j3dTextures/";
			File t = new File(texDir);
			t.mkdir();
		}
		File tmp = new File(texDir);

		if (!tmp.isDirectory()) {
			throw new IOException("Given file: " + tmp.getAbsoluteFile()
					+ " exists, but is not a directory, please specify a valid directory.");
		}
		if (!tmp.canWrite()) {
			throw new IOException("Can not write to specified directory: " + tmp.getAbsoluteFile()
					+ ", please specify directory which can be written to.");
		}
		textureimportDir = texDir;
		if (!textureimportDir.endsWith(File.separator)) {
			// just make sure the given path ends with the separator.
			textureimportDir += File.separator;
		}

		id = params.get("id");

		fillUserMatrix(params);

		fillUserRotationMatrix(params);

		maxTextureDimension = -1;
		String t = params.get(MAX_TEX_DIM);
		if (t != null) {
			try {
				int mtd = Integer.parseInt(t);
				if (mtd > 0) {
					maxTextureDimension = MathUtils.nextPowerOfTwoValue(mtd);
				}
			}
			catch (NumberFormatException e) {
				// nothing
			}
		}
		doUserTransform = (userMatrix != null && userMatrix.getBestType() != Transform3D.IDENTITY);
		doUserRotation = (userRotationMatrix != null && userRotationMatrix.getBestType() != Transform3D.IDENTITY);

	}

	/**
	 * @param params
	 */
	private void fillUserRotationMatrix(Map<String, String> params) {
		String get = params.get(ROT_ANGLE);
		if (get != null) {
			String[] vals = get.split(",");
			if (vals.length == 4) {
				try {
					double x = Double.parseDouble(vals[0]);
					double y = Double.parseDouble(vals[1]);
					double z = Double.parseDouble(vals[2]);
					double a = Double.parseDouble(vals[3]);
					userRotationMatrix = new Transform3D();
					userRotationMatrix.set(new AxisAngle4d(x, y, z, a));
				}
				catch (NumberFormatException e) {
					LOG.error(
							"The rotation axis values were not correctly defined, please use a comma separated list: x,y,z,a: "
									+ e.getLocalizedMessage());
				}

			}
			else {
				LOG.error(
						"The rotation axis values were not correctly defined, please use a comma separated list: x,y,z,a.");
			}
		}
	}

	private void fillUserMatrix(Map<String, String> params) {
		double xTranslation = 0;
		String t = params.get(XTRANS);
		if (t != null && !"".equals(t)) {
			try {
				xTranslation = Double.parseDouble(t);
			}
			catch (NumberFormatException nfe) {
				// nottin
			}
		}
		double yTranslation = 0;
		t = params.get(YTRANS);
		if (t != null && !"".equals(t)) {
			try {
				yTranslation = Double.parseDouble(t);
			}
			catch (NumberFormatException nfe) {
				// nottin
			}
		}
		double zTranslation = 0;
		t = params.get(ZTRANS);
		if (t != null && !"".equals(t)) {
			try {
				zTranslation = Double.parseDouble(t);
			}
			catch (NumberFormatException nfe) {
				// nottin
			}
		}

		boolean flipYZ = false;
		t = params.get(INV_YZ);
		if (t != null && !"".equals(t.trim())) {
			flipYZ = t.equalsIgnoreCase("y") || t.equalsIgnoreCase("yes");
		}
		userMatrix = new Transform3D(new double[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 });
		userMatrix.setTranslation(new Vector3d(xTranslation, yTranslation, zTranslation));
		if (flipYZ) {
			double[] d = new double[] { 1, 0, 0, 0, 0, 0, -1, 0, 0, 1, 0, 0, 0, 0, 0, 1 };
			Transform3D skip = new Transform3D(d);
			userMatrix.mul(skip);
		}

	}

	@Override
	public List<WorldRenderableObject> importFromFile(String fileName, int numberOfQualityLevels, int qualityLevel)
			throws IOException {
		BranchGroup group = readVRMLFile(fileName);
		// citygml creation date
		RenderableQualityModel result = null;
		double[] lower = new double[] { Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE };
		double[] upper = new double[] { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
		if (fileName != null) {
			if (group.getCapability(Group.ALLOW_CHILDREN_READ)) {
				result = new RenderableQualityModel();
				Transform3D initialMatrix = new Transform3D();
				if (doUserRotation) {
					initialMatrix = userRotationMatrix;
				}
				importGroup(result, group, initialMatrix, lower, upper);
			}
			else {
				LOG.info("The given branchgroup may not read it's children, nothing to export.");
			}
		}
		else {
			LOG.info("The given branchgroup may not be null, nothing to export.");
		}
		List<WorldRenderableObject> r = new ArrayList<WorldRenderableObject>();
		if (result != null) {

			GeometryFactory geom = new GeometryFactory();

			Envelope env = geom.createEnvelope(lower, upper, null);
			WorldRenderableObject wro = new WorldRenderableObject(id,
					new Timestamp(System.currentTimeMillis()).toString(), env, numberOfQualityLevels);
			wro.setName(fileName);
			wro.setExternalReference(fileName);

			wro.setQualityLevel(qualityLevel, result);

			r.add(wro);
		}
		return r;
	}

	/**
	 * @param fileName
	 * @return
	 * @throws IOException if the file could not be read.
	 */
	private BranchGroup readVRMLFile(String fileName) throws IOException {
		VrmlLoader loader = new VrmlLoader();
		try {
			Scene scene = loader.load(fileName);
			if (scene != null) {
				return scene.getSceneGroup();
			}
		}
		catch (Exception e) {
			throw new IOException("Could not create vrml scene from file: " + fileName + " because: " + e.getMessage());
		}
		throw new IOException("Could not create scene from file: " + fileName);
	}

	/**
	 * Iterates over all children of the given branchgroup and appends their citygml
	 * representation to the rootNode.
	 * @param result
	 * @param j3dScene to export
	 * @param rootNode to append to.
	 * @param calcBounds if true the bounds should be calculated and placed in lower and
	 * upper.
	 * @param upper bound
	 * @param lower bound
	 */
	@SuppressWarnings("unchecked")
	private void importGroup(RenderableQualityModel result, Group j3dScene, Transform3D transformation, double[] lower,
			double[] upper) {
		Enumeration<Node> en = j3dScene.getAllChildren();
		LOG.debug("importting a group.");
		while (en.hasMoreElements()) {
			Node n = en.nextElement();
			if (n != null) {
				if (n instanceof Group) {
					Transform3D tmpTrans = null;
					if (n instanceof TransformGroup) {
						TransformGroup tmp = (TransformGroup) n;
						tmpTrans = applyMatrixTransformGroup(transformation, tmp);
					}
					importGroup(result, (Group) n, transformation, lower, upper);
					if (tmpTrans != null) {
						transformation.mulInverse(tmpTrans);
						LOG.debug("After undoing the inverse of transformGroup transform:\n" + transformation);
					}
				}
				else if (n instanceof Leaf) {
					importLeaf(result, (Leaf) n, transformation, lower, upper);
				}
			}
		}
	}

	private Transform3D applyMatrixTransformGroup(Transform3D transformation, TransformGroup tg) {
		Transform3D tmpTrans = new Transform3D();
		tg.getTransform(tmpTrans);
		if (tmpTrans.getBestType() != Transform3D.IDENTITY && tmpTrans.getBestType() != Transform3D.ZERO) {
			LOG.debug("A transform group with transform:\n" + tmpTrans);
			// Matrix3d mat = new Matrix3d();
			// tmpTrans.getRotationScale( mat );
			// transformation.set( mat );

			transformation.mul(tmpTrans);

			LOG.debug("Resulting transform:\n" + transformation);
		}
		return tmpTrans;
	}

	/**
	 * Checks if the given leaf is a shape3D or background leaf, and if so, their cityGML
	 * representation will be appended to the parent node.
	 * @param l a j3d leaf.
	 * @param parent to append to.
	 * @param transformation
	 * @param calcBounds if true the bounds should be calculated and placed in lower and
	 * upper.
	 * @param upper bound
	 * @param lower bound
	 */
	private void importLeaf(RenderableQualityModel result, Leaf l, Transform3D transformation, double[] lower,
			double[] upper) {
		if (l != null) {
			if (l instanceof Shape3D) {
				importShape3D(result, (Shape3D) l, transformation, lower, upper);
			}
			else if (l instanceof Background) {
				importBackground(result, (Background) l);
			}
			else {
				LOG.info("Don't know howto import object of instance: " + l.getClass().getName());
			}
		}
	}

	/**
	 * Appends the citygml representation to the given parent node.
	 * @param shape (of the j3dScene) to append
	 * @param parent to append to.
	 * @param transformation
	 * @param calcBounds if true the bounds should be calculated and placed in lower and
	 * upper.
	 * @param upper bound
	 * @param lower bound
	 */
	@SuppressWarnings("unchecked")
	private void importShape3D(RenderableQualityModel result, Shape3D shape, Transform3D transformation, double[] lower,
			double[] upper) {
		Enumeration<Geometry> geoms = shape.getAllGeometries();
		Appearance app = shape.getAppearance();
		Color3f ambient = new Color3f(1f, 1f, 1f);
		Color3f diffuse = new Color3f(1f, 1f, 1f);
		Color3f specular = new Color3f(0.8f, 0.8f, 0.8f);
		Color3f emmisive = new Color3f();
		float shininess = 20f;

		if (app != null) {
			Material mat = app.getMaterial();
			if (mat != null) {
				mat.getAmbientColor(ambient);
				mat.getDiffuseColor(diffuse);
				mat.getSpecularColor(specular);
				mat.getEmissiveColor(emmisive);
				shininess = mat.getShininess();
			}
		}
		else {
			LOG.info("Shape3D has no material!, setting to gray!");
		}

		List<RenderableQualityModelPart> geometries = new LinkedList<RenderableQualityModelPart>();
		while (geoms.hasMoreElements()) {
			Geometry geom = geoms.nextElement();

			if (geom != null) {

				if (geom instanceof GeometryArray) {
					GeometryArray ga = (GeometryArray) geom;
					int glType = getGLType(ga);

					boolean doImport = true;

					// TRIANGLE_FANS are not supported.
					if (!(glType == GL.GL_TRIANGLE_STRIP || glType == GL.GL_QUADS || glType == GL.GL_LINE_STRIP)) {
						if (!(ga instanceof TriangleArray)) {
							LOG.info("Not a triangle Array geometry -> convert to triangles, original type is: " + ga);
							try {
								GeometryInfo inf = new GeometryInfo(ga);
								inf.recomputeIndices();
								inf.convertToIndexedTriangles();

								// regenerate the normals
								inf = new GeometryInfo(ga);
								NormalGenerator ng = new NormalGenerator();
								ng.generateNormals(inf);
								inf.recomputeIndices();

								ga = inf.getGeometryArray();
								glType = getGLType(ga);
								LOG.info("The converted type is: " + ga);
							}
							catch (IllegalArgumentException e) {
								LOG.info("Could not create a triangle array of the: " + ga);
								doImport = false;
							}
						}
					}

					if (doImport) {

						int vertexCount = ga.getVertexCount();
						if (vertexCount == 0) {
							LOG.error("No coordinates found in the geometryArray, this may not be.");
						}
						else {
							LOG.debug("Number of vertices in shape3d: " + vertexCount);

							float[] coords = exportCoords(ga, transformation, lower, upper);

							/**
							 * NORMALS
							 */
							float[] normals = exportNormals(ga, transformation);
							if (normals == null) {
								LOG.debug("VRML file: " + id + " has no normals");
							}

							/**
							 * Textures
							 */
							Pair<String, float[]> texCoordID = exportTexture(app, ga);

							float[] bb = getInterLeaved(ga);

							SimpleGeometryStyle style = new SimpleGeometryStyle(convertColorGLColor(specular.get()),
									convertColorGLColor(ambient.get()), convertColorGLColor(diffuse.get()),
									convertColorGLColor(emmisive.get()), shininess);
							if (bb == null) {
								if (texCoordID == null) {
									geometries.add(new RenderableGeometry(coords, glType, normals, style, false));
								}
								else {
									geometries.add(new RenderableTexturedGeometry(coords, glType, normals, style,
											texCoordID.first, texCoordID.second, false));
								}
							}
							else {
								LOG.warn("Interleaved is currently not supported. ");
							}
						}
					}
				}
				else {
					LOG.info("Only Shape3d Objects with geometry rasters are supported for importing");
				}

			}
		}
		result.addQualityModelParts(geometries);
	}

	/**
	 * @param ga
	 * @return
	 */
	private int getGLType(GeometryArray ga) {
		int result = GL.GL_TRIANGLES;
		if (ga instanceof TriangleFanArray) {
			result = GL.GL_TRIANGLE_FAN;
		}
		else if (ga instanceof QuadArray) {
			result = GL.GL_QUADS;
		}
		else if (ga instanceof TriangleStripArray) {
			result = GL.GL_TRIANGLE_STRIP;
		}
		else if (ga instanceof LineStripArray) {
			result = GL.GL_LINE_STRIP;
		}
		return result;
	}

	/**
	 * @param ga
	 * @return
	 */
	private float[] getInterLeaved(GeometryArray ga) {
		float[] result = null;
		if ((ga.getVertexFormat() & GeometryArray.INTERLEAVED) == GeometryArray.INTERLEAVED) {
			if ((ga.getVertexFormat() & GeometryArray.USE_NIO_BUFFER) == GeometryArray.USE_NIO_BUFFER) {
				Buffer b = ga.getInterleavedVertexBuffer().getBuffer();
				if (b instanceof ByteBuffer) {
					result = ((ByteBuffer) b).asFloatBuffer().array();
				}
				else if (b instanceof FloatBuffer) {
					result = ((FloatBuffer) b).array();
				}
			}
			else {
				result = ga.getInterleavedVertices();
			}

		}
		return result;
	}

	/**
	 * @param app
	 * @param ga
	 * @return
	 */
	private float[] exportNormals(GeometryArray ga, Transform3D transformation) {
		float[] normals = null;
		if (!((GeometryArray.NORMALS & ga.getVertexFormat()) == GeometryArray.NORMALS)) {
			GeometryInfo inf = new GeometryInfo(ga);
			NormalGenerator ng = new NormalGenerator();
			ng.generateNormals(inf);
		}
		normals = new float[ga.getVertexCount() * 3];
		ga.getNormals(ga.getInitialVertexIndex(), normals);
		double[] normal = new double[3];
		boolean userTransform = doUserTransform;
		Matrix3d rot = new Matrix3d();
		boolean doSceneTransform = (transformation != null && transformation.getBestType() != Transform3D.IDENTITY);
		Transform3D transScene = new Transform3D();
		if (doSceneTransform) {
			transformation.getRotationScale(rot);
			transScene.set(rot);
			doSceneTransform = (transScene.getBestType() != Transform3D.IDENTITY);
		}

		Transform3D transUser = new Transform3D();
		if (doUserTransform) {
			userMatrix.getRotationScale(rot);
			transUser.set(rot);
			userTransform = (transUser.getBestType() != Transform3D.IDENTITY);
		}

		for (int i = 0; (i + 2) < normals.length; i += 3) {
			Point3d coord = new Point3d(normals[i], normals[i + 1], normals[i + 2]);
			// transform (rotate/flipyz) the normal, the translation factors are removed.
			// if ( doUserRotation ) {
			// userRotationMatrix.transform( coord );
			// }
			if (doSceneTransform) {
				transScene.transform(coord);
			}
			if (userTransform) {
				transUser.transform(coord);
			}
			coord.get(normal);
			Vectors3d.normalizeInPlace(normal);
			normals[i] = (float) normal[0];
			normals[i + 1] = (float) normal[1];
			normals[i + 2] = (float) normal[2];
		}
		return normals;
	}

	private float[] exportCoords(GeometryArray ga, Transform3D transformation, double[] lower, double[] upper) {
		float[] coords = new float[ga.getVertexCount() * 3];
		ga.getCoordinates(ga.getInitialVertexIndex(), coords);
		boolean doSceneTransform = (transformation != null && transformation.getBestType() != Transform3D.IDENTITY);
		for (int i = 0; (i + 2) < coords.length; i += 3) {
			Point3d coord = new Point3d(coords[i], coords[i + 1], coords[i + 2]);
			// if ( doUserRotation ) {
			// userRotationMatrix.transform( coord );
			// }

			if (doSceneTransform) {
				transformation.transform(coord);
			}

			if (doUserTransform) {
				userMatrix.transform(coord);
			}

			coords[i] = (float) coord.x;
			coords[i + 1] = (float) coord.y;
			coords[i + 2] = (float) coord.z;
			upper[0] = max(coords[i], upper[0]);
			upper[1] = max(coords[i + 1], upper[1]);
			upper[2] = max(coords[i + 2], upper[2]);

			lower[0] = min(coords[i], lower[0]);
			lower[1] = min(coords[i + 1], lower[1]);
			lower[2] = min(coords[i + 2], lower[2]);

		}
		return coords;
	}

	private Pair<String, float[]> exportTexture(Appearance appearance, GeometryArray ga) {
		LOG.debug("The Geometry has a texture attached to it.");

		float[] texCoords = null;
		String texName = null;
		if ((GeometryArray.TEXTURE_COORDINATE_2 & ga.getVertexFormat()) == GeometryArray.TEXTURE_COORDINATE_2) {
			texCoords = new float[ga.getVertexCount() * 2];
			ga.getTextureCoordinates(0, ga.getInitialTexCoordIndex(0), texCoords);
			for (int i = 1; i < texCoords.length; i += 2) {
				texCoords[i] = 1 - texCoords[i];
			}
			if (appearance != null) {
				Texture tex = appearance.getTexture();
				if (tex != null) {
					ImageComponent ic = tex.getImage(0);
					if (ic != null) {
						if (ic instanceof ImageComponent2D) {
							BufferedImage bi = ((ImageComponent2D) ic).getImage();
							if (bi != null) {
								Pair<BufferedImage, String> cache = testEquality(bi);
								if (cache == null) {
									LOG.debug("No cached texture found, saving texture to a new file.");
									texName = saveImageToFile(bi, texName);
								}
								else {
									texName = cache.second;
									LOG.debug("Using old texture reference: " + texName);
								}
							}
						}
					}
				}
			}
		}
		if (texCoords != null) {
			return new Pair<String, float[]>(texName, texCoords);
		}
		return null;

	}

	private String saveImageToFile(BufferedImage image, String texture) {
		String texName = texture;
		if (texName == null || "".equals(texName.trim())) {
			texName = id + "_texture_" + textureCount++;
		}
		if (!texName.endsWith(".png")) {
			texName += (".png");
		}
		BufferedImage scaledImage = image;
		if (shouldScale(image)) {

			int oldWidth = image.getWidth();
			int oldHeight = image.getHeight();
			double scale = 1;
			scale = (double) maxTextureDimension / ((oldWidth > oldHeight) ? oldWidth : oldHeight);

			scaledImage = new BufferedImage((int) Math.floor(scale * oldWidth), (int) Math.floor(scale * oldHeight),
					BufferedImage.TYPE_INT_ARGB);

			Graphics2D graphics = (Graphics2D) scaledImage.getGraphics();
			// graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
			// RenderingHints.VALUE_ANTIALIAS_ON );
			graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			AffineTransform at = new AffineTransform();
			at.scale(scale, scale);
			graphics.setTransform(at);
			graphics.drawImage(image, null, 0, 0);
		}

		try {
			File f = new File(textureimportDir, texName);
			ImageIO.write(scaledImage, "png", f);
			LOG.debug("Wrote texture to: " + f.getAbsolutePath());
		}
		catch (IOException e) {
			LOG.error("Failed to write texture: " + texName, e);
		}
		cachedTextures.put(image, texName);
		return texName;
	}

	/**
	 * @param image
	 * @return
	 */
	private boolean shouldScale(BufferedImage image) {
		return (maxTextureDimension > 0)
				&& (image.getWidth() > maxTextureDimension || image.getHeight() > maxTextureDimension);
	}

	/**
	 * @param bi
	 * @return
	 */
	private Pair<BufferedImage, String> testEquality(BufferedImage bi) {

		Pair<BufferedImage, String> result = null;
		Iterator<BufferedImage> it = cachedTextures.keySet().iterator();
		while (result == null && it.hasNext()) {
			BufferedImage b = it.next();
			String file = cachedTextures.get(b);
			if (b != null) {
				if (testImages(bi, b)) {
					result = new Pair<BufferedImage, String>(b, file);
				}
			}
		}
		return result;
	}

	/**
	 * @param bi
	 * @param b
	 * @return
	 */
	private boolean testImages(BufferedImage bi, BufferedImage b) {
		boolean result = bi.getWidth() == b.getWidth() && bi.getHeight() == b.getHeight()
				&& bi.getType() == b.getType();

		if (result) {
			int width = b.getWidth();
			int height = b.getHeight();
			for (int x = 0; (x < width) && (result); ++x) {
				for (int y = 0; (y < height) && (result); ++y) {
					result = (bi.getRGB(x, y) == b.getRGB(x, y));
				}
			}
		}
		return result;
	}

	/**
	 * Don't know how to export the background yet.
	 * @param result leaf of the j3dScene
	 * @param background to append to.
	 */
	private void importBackground(RenderableQualityModel result, Background background) {
		LOG.error("Cannot import a background yet.");
	}

}
