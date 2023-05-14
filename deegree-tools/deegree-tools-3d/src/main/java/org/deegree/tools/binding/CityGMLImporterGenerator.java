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

package org.deegree.tools.binding;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.deegree.tools.rendering.manager.buildings.importers.ModelImporter;

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
public class CityGMLImporterGenerator {

	public static void main(String[] arg) throws ClassCastException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, IOException {
		String schemaURL = "file:/home/rutger/workspace/schemas/citygml/profiles/base/1.0/CityGML.xsd";
		GMLAppSchemaReader adapter = new GMLAppSchemaReader(GMLVersion.GML_31, null, schemaURL);
		AppSchema schema = adapter.extractAppSchema();
		// FeatureType[] fts = schema.getFeatureTypes();
		// Assert.assertEquals( 54, fts.length );
		// List<QName> ftNames = new ArrayList<QName>( 54 );
		FeatureType[] roots = schema.getRootFeatureTypes();
		// HashMap<String, GeometryClass> geomClasses = new HashMap<String,
		// GeometryClass>();
		HashMap<QName, FeatureClass> featClasses = new HashMap<QName, FeatureClass>();
		if (roots != null && roots.length > 0) {
			for (FeatureType ft : roots) {
				createClassHierarchy(schema, ft, null, featClasses);
			}
		}
		// Iterator<GeometryClass> it = geomClasses.values().iterator();
		// System.out.println( "found geomtry classes: \n" );
		// while ( it.hasNext() ) {
		// GeometryClass next = it.next();
		// System.out.println( " - " + next.toString() );
		// List<GeometryPropertyType> geomProps = next.geomProps;
		// for ( GeometryPropertyType gp : geomProps ) {
		// System.out.println( " | - " + gp.getName() );
		// }
		// System.out.println();
		// }

		Iterator<FeatureClass> fit = featClasses.values().iterator();
		System.out.println("found feature classes: \n");
		while (fit.hasNext()) {
			FeatureClass next = fit.next();
			System.out.println(" - " + next.toString());
		}
		// for ( GeometryClass geomClass : geomClasses.values() ) {
		// createGeometryFile( geomClass, geomClasses, featClasses );
		// }
		if (roots != null && roots.length > 0) {
			for (FeatureType ft : roots) {
				createClasses(schema, ft, featClasses);
			}
		}

	}

	private static void createClassHierarchy(AppSchema schema, FeatureType currentType, FeatureClass parent,
			HashMap<QName, FeatureClass> featClasses) throws IOException {
		FeatureClass newClass = null;
		if (currentType != null) {
			if (parent != null) {
				newClass = new FeatureClass(currentType, parent);
			}
			else {
				newClass = new RootFeature(currentType);
			}
			featClasses.put(currentType.getName(), newClass);
		}

		FeatureType[] directSubtypes = schema.getDirectSubtypes(currentType);

		if (directSubtypes != null && directSubtypes.length > 0) {
			for (FeatureType dft : directSubtypes) {
				if (dft != null) {
					createClassHierarchy(schema, dft, newClass, featClasses);
				}
			}
		}
	}

	private static void createClasses(AppSchema schema, FeatureType root, HashMap<QName, FeatureClass> featClasses)
			throws IOException {
		if (root != null) {
			createFeatureFile(root, featClasses);
		}
		FeatureType[] directSubtypes = schema.getDirectSubtypes(root);

		if (directSubtypes != null && directSubtypes.length > 0) {
			for (FeatureType dft : directSubtypes) {
				if (dft != null) {
					createClasses(schema, dft, featClasses);
				}
			}
		}
	}

	/**
	 * @param featClasses
	 * @param geomClasses
	 * @param qn
	 * @throws IOException
	 */
	private static void createFeatureFile(FeatureType newClass, HashMap<QName, FeatureClass> featClasses)
			throws IOException {

		QName qn = newClass.getName();
		FeatureClass featureClass = featClasses.get(qn);
		File f = createFile(featureClass);
		writeNewFile(f, featureClass, featClasses);
	}

	private static File createFile(ModelClass featureClass) throws IOException {
		File dir = new File(featureClass.getPackageDir());

		if (!dir.exists()) {
			boolean mkdir = dir.mkdirs();
			if (!mkdir) {
				throw new IOException("Could not create dir: " + dir.getAbsolutePath());
			}
		}
		File f = new File(featureClass.getClassLocation());
		if (f.exists()) {
			System.out.println("File f: " + f.getAbsolutePath() + " already exists, overwriting...");
			boolean delete = f.delete();
			if (!delete) {
				throw new IOException("Could not delete file: " + f.getAbsolutePath());
			}
		}
		boolean newFile = f.createNewFile();
		if (!newFile) {
			throw new IOException("Could not create new file: " + f.getAbsolutePath());
		}
		return f;
	}

	/**
	 * @param f
	 * @param localPart
	 * @param string
	 * @param t
	 * @throws IOException
	 */
	private static void writeNewFile(File f, ModelClass newClass, HashMap<QName, FeatureClass> featClasses)
			throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(f));
		newClass.writeHeader(out);
		newClass.writePackage(out);
		out.newLine();
		newClass.writeImports(out, featClasses);
		out.newLine();
		newClass.writeClassDoc(out);
		newClass.writeClassStart(out);
		newClass.writeFields(out);
		newClass.writeMethods(out, featClasses);
		out.write("}\n");
		out.close();
	}

	/**
	 * @param namespaceURI
	 * @return
	 */
	static String getPackageName(String namespaceURI) {
		String packageName = namespaceURI.substring("http://www.opengis.net/citygml/".length());
		return packageName.replace("1.0", "");
	}

}
