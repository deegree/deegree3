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
package org.deegree.tools.binding;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.feature.AbstractFeature;
import org.deegree.feature.types.FeatureType;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 */
public class RootFeature extends FeatureClass {

	protected final static String FEAT = AbstractFeature.class.getSimpleName();

	protected final static String FTYPE = FeatureType.class.getSimpleName();

	public RootFeature(FeatureType ft) {
		super(ft, null);
	}

	/**
	 * @param geomClasses
	 * @param featClasses
	 * @return a list of imports needed for this feature class.
	 */
	@Override
	public List<String> getImports(Map<QName, FeatureClass> featClasses) {
		Set<String> imports = new HashSet<String>(super.getImports(featClasses));
		FeatureInstanceWriter.addImports(imports);
		FeatureTypeInstanceWriter.addImports(imports);

		return new ArrayList<String>(imports);
	}

	@Override
	public void writeClassStart(Writer out) throws IOException {
		StringBuilder sb = new StringBuilder("public ");
		if (isAbstract()) {
			sb.append("abstract ");
		}
		if (isInterface()) {
			sb.append("interface");
		}
		else {
			sb.append("class ");
		}
		sb.append(getClassName());
		sb.append(" extends ").append(FEAT);
		sb.append(" implements ").append(FTYPE);
		sb.append(" {\n");
		out.write(sb.toString());
	}

	/**
	 * @param out
	 * @throws IOException
	 */
	@Override
	public void writeFields(Writer out) throws IOException {
		super.writeFields(out);
		// generate fields for property..
		// FeatureInstanceWriter.writeFields( out );
		// FeatureTypeInstanceWriter.writeFields( out );
	}

	/**
	 * @param out
	 */
	@Override
	public void writeMethods(Writer out, HashMap<QName, FeatureClass> featClasses) throws IOException {
		super.writeMethods(out, featClasses);
		FeatureInstanceWriter.writeFeatureMethods(out, featClasses);
		FeatureTypeInstanceWriter.writeFeatureTypeMethods(out, featClasses);
	}

}
