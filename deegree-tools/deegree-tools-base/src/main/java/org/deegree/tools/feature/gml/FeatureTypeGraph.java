/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.tools.feature.gml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.utils.GraphvizDot;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;

/**
 * Creates a Graphviz dot representation of a feature type application schema.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 */
public class FeatureTypeGraph {

	/**
	 * Write a dot file to the given file. The resulting file can be viewed / converted
	 * with the 'dot' application, for example call $> dot -Tpng -o out.png your/file.dot
	 * to create a png from the the 'dot' file.
	 * @param schema to get the graph for.
	 * @param fileName
	 * @throws IOException
	 */
	public static void createDotGraph(AppSchema schema, String fileName) throws IOException {
		File f = new File(fileName);
		BufferedWriter out = new BufferedWriter(new FileWriter(f));
		List<String> attribs = new ArrayList<String>();
		GraphvizDot.addRankDirLeftToRight(attribs);
		GraphvizDot.startDiGraph(out, attribs);
		FeatureType[] roots = schema.getRootFeatureTypes();
		if (roots != null && roots.length > 0) {
			String defaultRoot = null;
			if (roots.length > 1) {
				defaultRoot = "feature";
				attribs.clear();
				attribs.add(GraphvizDot.getShapeDef("diamond"));
				attribs.add(GraphvizDot.getFillColorDef("green"));
				GraphvizDot.writeVertex("feature", attribs, out);
			}
			for (FeatureType ft : roots) {
				if (defaultRoot != null) {
					GraphvizDot.writeEdge(defaultRoot, dotName(ft), null, out);
				}
				createGraph(out, schema, ft);
			}
		}
		GraphvizDot.endGraph(out);
		out.close();
	}

	private static void createGraph(BufferedWriter out, AppSchema schema, FeatureType root) throws IOException {
		if (root != null) {
			addNode(out, root);
		}
		FeatureType[] directSubtypes = schema.getDirectSubtypes(root);
		if (directSubtypes != null && directSubtypes.length > 0) {
			for (FeatureType dft : directSubtypes) {
				if (dft != null) {
					GraphvizDot.writeEdge(dotName(root), dotName(dft), null, out);
					createGraph(out, schema, dft);
				}
			}
		}
	}

	/**
	 * @param root
	 * @throws IOException
	 */
	private static void addNode(BufferedWriter out, FeatureType ft) throws IOException {
		if (ft != null) {
			String dotName = dotName(ft);
			List<String> attributes = createAttributes(ft);

			List<PropertyType> pd = ft.getPropertyDeclarations();
			StringBuilder sb = new StringBuilder("tooltip=\"");
			if (pd != null && !pd.isEmpty()) {
				Iterator<PropertyType> it = pd.iterator();
				while (it.hasNext()) {
					sb.append(dotName(it.next().getName()));
					if (it.hasNext()) {
						sb.append(",<br/>");
					}
				}
			}
			sb.append("\"");
			attributes.add(sb.toString());
			GraphvizDot.writeVertex(dotName, attributes, out);
		}

	}

	private static List<String> createAttributes(FeatureType ft) {
		List<String> result = new LinkedList<String>();
		if (ft.isAbstract()) {
			result.add(GraphvizDot.getFillColorDef("red"));
			result.add(GraphvizDot.getShapeDef("triangle"));
		}
		else {
			result.add(GraphvizDot.getFillColorDef("cyan"));
		}
		return result;
	}

	private static String dotName(FeatureType t) {
		return dotName(t.getName());
	}

	private static String dotName(QName qName) {
		return qName.getLocalPart();
	}

}
