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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.xpath.TypedObjectNodeXPathEvaluator;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.composite.CompositeSurface;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.deegree.rendering.r3d.model.geometry.GeometryQualityModel;
import org.deegree.rendering.r3d.model.geometry.SimpleAccessGeometry;
import org.deegree.rendering.r3d.model.geometry.SimpleGeometryStyle;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableQualityModel;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.rendering.r3d.opengl.tesselation.Tesselator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ModelImporter} that reads a CityGML element (namespace
 * <code>http://www.opengis.net/citygml/1.0</code>) file and creates a WPVS representation
 * from it.
 * <p>
 * NOTE: Currently, only <code>Building</code> elements on the first level of the
 * collection are imported. All other CityGML features else is ignored.
 * </p>
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 */
public class CityGMLImporter implements ModelImporter {

	private static final GeometryFactory geomFac = new GeometryFactory();

	private static Logger LOG = LoggerFactory.getLogger(CityGMLImporter.class);

	private static final String CITYGML_SCHEMA = CityGMLImporter.class.getResource("schema/citygml100_old/CityGML.xsd")
		.toString();

	private static final String OPENGIS_SCHEMA = "http://schemas.opengis.net/citygml/profiles/base/1.0/CityGML.xsd";

	private final String SCHEMA_URL;

	private static final String CITYGML_NS = "http://www.citygml.org/citygml/1/0/0";

	private static final String OPENGIS_NS = "http://www.opengis.net/citygml/building/1.0";

	private final String NS;

	private final String NS_BRACE;

	private final QName BUILDING_QNAME;

	private final AppSchema schema;

	private final Tesselator tesselator;

	private final float[] translation;

	private SimpleGeometryStyle defaultStyle;

	private List<GeometryQualityModel> qmList;

	/**
	 * @param schemaLocation to create the schema xsd apapter from.
	 * @param translation
	 * @param defaultStyle to be used for the buildings.
	 * @param useOpengis true if the objects are defined in the
	 * http://www.opengis.net/citygml/building/1.0. Otherwise they will be defined in
	 * http://www.citygml.org/citygml/1/0/0
	 */
	public CityGMLImporter(String schemaLocation, float[] translation, SimpleGeometryStyle defaultStyle,
			boolean useOpengis) {
		if (translation == null || translation.length == 0) {
			this.translation = new float[] { 0, 0, 0 };
		}
		else {
			this.translation = new float[3];
			this.translation[0] = (translation.length > 0) ? translation[0] : 0;
			this.translation[1] = (translation.length > 1) ? translation[1] : 0;
			this.translation[2] = (translation.length > 2) ? translation[2] : 0;
		}

		SCHEMA_URL = (useOpengis) ? OPENGIS_SCHEMA : CITYGML_SCHEMA;
		NS = (useOpengis) ? OPENGIS_NS : CITYGML_NS;
		NS_BRACE = "{" + NS + "}";
		BUILDING_QNAME = new QName(NS, "Building");
		String schemaLoc = determineSchemaLocation(schemaLocation);

		GMLAppSchemaReader adapter = null;
		try {
			LOG.info("Using schemalocation: " + schemaLoc);
			adapter = new GMLAppSchemaReader(GMLVersion.GML_31, null, schemaLoc);
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new IllegalArgumentException("Could not create an ApplicationSchemaXSDAdapter from schemaLocation: "
					+ schemaLoc + " no way to import buildings.", e);
		}
		schema = adapter.extractAppSchema();
		if (schema == null) {
			throw new NullPointerException(
					"Application schema could not be extracted from schema location: " + schemaLoc);
		}
		FeatureType buildingType = schema.getFeatureType(QName.valueOf(NS_BRACE + "Building"));
		if (buildingType == null) {
			throw new IllegalArgumentException(
					"Could not create a featuretype: " + NS_BRACE + "Building, probably your schema at location: "
							+ schemaLoc + " is invalid. Or try the opengis switch.");
		}
		tesselator = new Tesselator(true);
		if (defaultStyle != null) {
			this.defaultStyle = defaultStyle;
		}
		else {
			this.defaultStyle = new SimpleGeometryStyle();
		}

		qmList = new ArrayList<GeometryQualityModel>();
	}

	private String determineSchemaLocation(String schemaLocation) {
		String schemaLoc = schemaLocation;
		if (schemaLoc == null) {
			schemaLoc = SCHEMA_URL;
		}
		else {

			try {
				URL url = new URL(schemaLoc);
				schemaLoc = url.toExternalForm();
			}
			catch (MalformedURLException e) {
				try {
					File f = new File(schemaLoc);
					if (!f.exists()) {
						LOG.warn("Unable to read from schemaLocation, using default instead.");
						schemaLoc = SCHEMA_URL;
					}
					else {
						schemaLoc = f.toURI().toURL().toString();
					}
				}
				catch (Exception e1) {
					LOG.warn("Unable to read from schemaLocation, using default instead.");
					schemaLoc = SCHEMA_URL;
				}
			}
		}
		return schemaLoc;
	}

	private FeatureCollection readGML(String fileName) throws IOException, XMLStreamException,
			FactoryConfigurationError, XMLParsingException, UnknownCRSException, ReferenceResolvingException {

		File f = new File(fileName);
		GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_31, f.toURI().toURL());
		gmlReader.setApplicationSchema(schema);
		FeatureCollection fc = (FeatureCollection) gmlReader.readFeature();
		gmlReader.getIdContext().resolveLocalRefs();
		gmlReader.close();
		return fc;
	}

	/**
	 * Map the given geometry to a {@link GeometryQualityModel} by extracting it's
	 * 'surfacepatches'.
	 * @param geom to extract the geomtries from.
	 * @param qm to add the extracted geometries to.
	 * @param min will contain the minimum of all geometries of the given geometry
	 * @param max will contain the maximum of all geometries in the given geometry
	 */
	private void mapGeometryToSAG(Geometry geom, GeometryQualityModel qm, double[] min, double[] max) {
		if (geom instanceof Solid) {
			extractGeometries((Solid) geom, qm, min, max);
		}
		else if (geom instanceof MultiSurface) {
			extractGeometries((MultiSurface) geom, qm, min, max);
		}
		else if (geom instanceof MultiCurve) {
			extractGeometries((MultiCurve) geom, qm, min, max);
		}
		else {
			if (geom == null) {
				LOG.error("Could not map the geometry which was not instantiated");
			}
			else {
				LOG.error("Could not map the geometry: " + geom.getClass().getName());
			}
		}
	}

	/**
	 * Map the given geometry to a {@link GeometryQualityModel} by extracting it's
	 * 'surfacepatches'.
	 * @param geom to extract the geomtries from.
	 * @param qm to add the extracted geometries to.
	 * @param min will contain the minimum of all geometries of the given geometry
	 * @param max will contain the maximum of all geometries in the given geometry
	 */
	private void extractGeometries(MultiCurve<Curve> geom, GeometryQualityModel qm, double[] min, double[] max) {
		for (Curve c : geom) {
			extractGeometries(c, qm, min, max);
		}
	}

	/**
	 * @param curve
	 * @param qm
	 * @param min
	 * @param max
	 */
	private void extractGeometries(Curve curve, GeometryQualityModel qm, double[] min, double[] max) {
		if (curve != null) {
			LineString ls = curve.getAsLineString();
			float[] coordinates = extractGeometries(ls, min, max);
			SimpleAccessGeometry sag = new SimpleAccessGeometry(coordinates, (int[]) null);
			qm.addQualityModelPart(sag);
		}
	}

	/**
	 * Map the given geometry to a {@link GeometryQualityModel} by extracting it's
	 * 'surfacepatches'.
	 * @param geom to extract the geomtries from.
	 * @param qm to add the extracted geometries to.
	 * @param min will contain the minimum of all geometries of the given geometry
	 * @param max will contain the maximum of all geometries in the given geometry
	 */
	private void extractGeometries(MultiSurface<Surface> geom, GeometryQualityModel qm, double[] min, double[] max) {
		List<SurfacePatch> patches = new LinkedList<org.deegree.geometry.primitive.patches.SurfacePatch>();
		for (Surface s : geom) {
			patches.addAll(s.getPatches());
		}
		extractGeometriesFromPatches(patches, qm, min, max);
	}

	/**
	 * Map the given geometry to a {@link GeometryQualityModel} by extracting it's
	 * 'surfacepatches'.
	 * @param geom to extract the geometries from.
	 * @param qm to add the extracted geometries to.
	 * @param min will contain the minimum of all geometries of the given geometry
	 * @param max will contain the maximum of all geometries in the given geometry
	 */
	private void extractGeometries(Solid geom, GeometryQualityModel qm, double[] min, double[] max) {
		// gml 3.1: if in eucledian 3d space the exterior will be a Composite surface.
		CompositeSurface exterior = (CompositeSurface) geom.getExteriorSurface();
		extractGeometries(exterior, qm, min, max);
	}

	/**
	 * Map the given geometry to a {@link GeometryQualityModel} by extracting it's
	 * 'surfacepatches'.
	 * @param compositeSurface to extract the geomtries from.
	 * @param qm to add the extracted geometries to.
	 * @param min will contain the minimum of all geometries of the given geometry
	 * @param max will contain the maximum of all geometries in the given geometry
	 */
	private void extractGeometries(CompositeSurface compositeSurface, GeometryQualityModel qm, double[] min,
			double[] max) {
		List<? extends SurfacePatch> patches = compositeSurface.getPatches();
		extractGeometriesFromPatches(patches, qm, min, max);
	}

	/**
	 * Map the given geometry to a {@link GeometryQualityModel} by extracting it's
	 * 'surfacepatches'.
	 * @param patches to extract the geomtries from.
	 * @param qm to add the extracted geometries to.
	 * @param min will contain the minimum of all geometries of the given geometry
	 * @param max will contain the maximum of all geometries in the given geometry
	 */
	private void extractGeometriesFromPatches(List<? extends SurfacePatch> patches, GeometryQualityModel qm,
			double[] min, double[] max) {
		if (patches != null && !patches.isEmpty()) {
			for (SurfacePatch patch : patches) {
				if (patch instanceof PolygonPatch) {
					extractGeometries((PolygonPatch) patch, qm, min, max);
				}
				else {
					LOG.warn("The extractation of geometries currently only support planar surfaces.");
				}
			}
		}
	}

	/**
	 * Extract the coordinates from the given {@link PolygonPatch} rings and add them to
	 * the given {@link GeometryQualityModel}.
	 * @param patch to extract the coordinates from.
	 * @param qm to add the extracted geometries to.
	 * @param min will contain the minimum of all geometries of the given geometry
	 * @param max will contain the maximum of all geometries in the given geometry
	 */
	private void extractGeometries(PolygonPatch patch, GeometryQualityModel qm, double[] min, double[] max) {
		Ring exterior = patch.getExteriorRing();
		float[] extC = extractGeometries(exterior, min, max);
		if (extC == null) {
			LOG.warn("The exterior ring of a polygon patch did not contain any coordinates, discarding this patch.");
		}
		else {
			List<Ring> interior = patch.getInteriorRings();
			List<float[]> interiors = new ArrayList<float[]>(interior.size());
			int size = extC.length;
			for (int i = 0; i < interior.size(); ++i) {
				float[] iC = extractGeometries(interior.get(i), min, max);
				if (iC != null) {
					interiors.add(iC);
					size += iC.length;
				}
			}
			float[] coordinates = new float[size];

			int[] innerRings = interiors.isEmpty() ? null : new int[interiors.size()];
			System.arraycopy(extC, 0, coordinates, 0, extC.length);
			int index = extC.length;
			int ringCount = 0;
			for (float[] innerRing : interiors) {
				System.arraycopy(innerRing, 0, coordinates, index, innerRing.length);
				innerRings[ringCount++] = index;
				index += innerRing.length;
			}
			SimpleAccessGeometry sag = new SimpleAccessGeometry(coordinates, innerRings, defaultStyle);
			qm.addQualityModelPart(sag);
		}
	}

	/**
	 * Extracts the coordinates in the given ring by calling the get as linestring method.
	 * @param someRing to extract the geometries from.
	 * @param min will contain the minimum of all geometries of the given geometry
	 * @param max will contain the maximum of all geometries in the given geometry
	 * @return the coordinates of this ring.
	 */
	private float[] extractGeometries(Ring someRing, double[] min, double[] max) {
		float[] coordinates = null;
		if (someRing != null) {
			LineString ls = someRing.getAsLineString();
			coordinates = extractGeometries(ls, min, max);
		}
		return coordinates;
	}

	private float[] extractGeometries(LineString lineString, double[] min, double[] max) {
		float[] coordinates = null;
		if (lineString != null) {
			double[] c = lineString.getControlPoints().getAsArray();
			if (c != null && c.length > 0) {
				if (c.length % 3 == 0) {
					coordinates = new float[c.length];
					for (int i = 0; i + 2 < c.length; i += 3) {
						coordinates[i] = (float) (translation[0] + c[i]);
						coordinates[i + 1] = (float) (translation[1] + c[i + 1]);
						coordinates[i + 2] = (float) (translation[2] + c[i + 2]);
						updateMinMax(min, max, coordinates, i);
					}
				}
				else {
					LOG.warn("The coordinates in a linestring must be a multiple of 3 (3-dimensions)");
				}
			}
			else {
				LOG.warn("No coordinates found in linestring, this is strange.");
			}
		}
		return coordinates;
	}

	/**
	 * Update the min and max values according to the coordinates given at index+[0,1,2]
	 * @param min
	 * @param max
	 * @param coordinates to use.
	 * @param index pointing to the current vertex
	 */
	private void updateMinMax(double[] min, double[] max, float[] coordinates, int index) {
		if ((min != null && max != null)) {
			float val = coordinates[index];
			min[0] = (min[0] < val) ? min[0] : val;
			max[0] = (max[0] > val) ? max[0] : val;

			val = coordinates[index + 1];
			min[1] = (min[1] < val) ? min[1] : val;
			max[1] = (max[1] > val) ? max[1] : val;

			val = coordinates[index + 2];
			min[2] = (min[2] < val) ? min[2] : val;
			max[2] = (max[2] > val) ? max[2] : val;
		}
	}

	public List<GeometryQualityModel> getQmList() {
		return qmList;
	}

	/**
	 * Create a {@link WorldRenderableObject} from the given citygml building.
	 * @param qualityLevel
	 * @param fc
	 * @return the branch group
	 */
	private WorldRenderableObject createDataObjectWithMaterial(Feature building, int numberOfLevels, int qualityLevel) {

		String id = getID(building);
		// Envelope env = building.getEnvelope();
		double[] min = new double[] { Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE };
		double[] max = new double[] { Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE };

		GeometryQualityModel qm = new GeometryQualityModel();
		String externalReference = getExternalRef(building);

		for (Property props : building.getGeometryProperties()) {
			if (props != null && !(props.getValue() instanceof Envelope)) {
				mapGeometryToSAG((Geometry) props.getValue(), qm, min, max);
			}
		}
		qmList.add(qm);

		Envelope env = geomFac.createEnvelope(min, max, null);
		WorldRenderableObject rwo = new WorldRenderableObject(id, new Timestamp(System.currentTimeMillis()).toString(),
				env, numberOfLevels);
		QName name = building.getName();
		if (name != null) {
			rwo.setName(name.getLocalPart());
		}
		else {
			rwo.setName(id);
		}

		rwo.setExternalReference(externalReference);
		RenderableQualityModel rqm = null;
		try {
			rqm = tesselator.createRenderableQM(id, qm);
		}
		catch (Exception e) {
			LOG.error("Could not tesselate building with id: " + id + " because: " + e.getLocalizedMessage(), e);
		}
		if (rqm != null) {
			rwo.setQualityLevel(qualityLevel, rqm);
		}
		return rwo;
	}

	private String getExternalRef(Feature building) {

		String result = null;

		NamespaceBindings nsContext = new NamespaceBindings();
		nsContext.addNamespace("cgml", NS);
		ValueReference propName = new ValueReference("cgml:externalReference/cgml:informationSystem/text()", nsContext);

		TypedObjectNodeXPathEvaluator evaluator = new TypedObjectNodeXPathEvaluator();
		TypedObjectNode[] tons;
		try {
			tons = evaluator.eval(building, propName);
			if (tons.length > 0) {
				result = ((PrimitiveValue) tons[0]).getAsText().trim();
			}
		}
		catch (FilterEvaluationException e) {
			LOG.error("Retrieving of information system property failed: " + e.getMessage());
		}
		return result;
	}

	private String getID(Feature building) {
		String result = building.getId();
		if (result == null || "".equals(result.trim())) {
			result = "Building_" + UUID.randomUUID().toString();
			LOG.warn("Created id: " + result + " for building with envelope: " + building.getEnvelope() + " and name: "
					+ building.getName() + " because it did not supply a gml:id.");
		}
		return result;
	}

	@Override
	public List<WorldRenderableObject> importFromFile(String fileName, int numberOfQualityLevels, int qualityLevel)
			throws IOException {
		FeatureCollection fc = null;
		try {
			fc = readGML(fileName);
		}
		catch (Exception e) {
			throw new IOException("Error while importing file: " + fileName + " because: " + e.getLocalizedMessage(),
					e);
		}
		Map<String, WorldRenderableObject> bMap = new HashMap<String, WorldRenderableObject>(fc.size());
		for (Feature f : fc) {
			if (BUILDING_QNAME.equals(f.getName())) {

				WorldRenderableObject wro = createDataObjectWithMaterial(f, numberOfQualityLevels, qualityLevel);
				if (wro != null) {
					if (!bMap.containsKey(wro.getId())) {
						bMap.put(wro.getId(), wro);
					}
					else {
						LOG.warn("Duplicate building with id: " + wro.getId() + " using first building with envelope: "
								+ wro.getBbox().toString());
					}
				}
			}
			else {
				LOG.warn("Unhandled feature type '" + f.getName() + "' -- skipping.");
			}
		}
		List<WorldRenderableObject> result = new ArrayList<WorldRenderableObject>(bMap.size());
		result.addAll(bMap.values());
		return result;
	}

}
