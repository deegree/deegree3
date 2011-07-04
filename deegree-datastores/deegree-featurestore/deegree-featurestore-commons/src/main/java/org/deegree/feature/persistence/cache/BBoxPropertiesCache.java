//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-datastores/deegree-featurestore/deegree-featurestore-commons/src/main/java/org/deegree/feature/persistence/cache/FeatureStoreCache.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.feature.persistence.cache;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.utils.StringUtils;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Point;
import org.slf4j.Logger;

/**
 * {@link BBoxCache} based on a Java properties file.
 * 
 * @see BBoxCache
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision$
 */
public class BBoxPropertiesCache implements BBoxCache {

    private static final Logger LOG = getLogger( BBoxPropertiesCache.class );
	
	private final File propsFile;
	
	private final Map<String,Envelope> ftNameToEnvelope = new TreeMap<String,Envelope>();

	public BBoxPropertiesCache(File propsFile) throws IOException {
		this.propsFile = propsFile;
		if (!propsFile.exists()) {
			LOG.info ("File '" + propsFile.getCanonicalPath() + "' does not exist. Will be created as needed.");
		}
		if (!propsFile.isFile()) {
			LOG.error ("File '" + propsFile.getCanonicalPath() + "' does not denote a standard file.");
		}
		
		Properties props = new Properties();
		InputStream is = new FileInputStream (propsFile);
		try {
			props.load(is);
		} finally {
			IOUtils.closeQuietly(is);
		}
		
		Enumeration<?> propNames = props.propertyNames();
		
	}

	@Override
	public Envelope get(QName ftName) {
//		String propValue = props.getProperty(ftName.toString());
//		if (propValue == null) {
//			throw new IllegalArgumentException();
//		}
//		if (propValue.isEmpty()) {
//			return null;
//		}
//		double [] coords = decodePropValue(propValue);
//		return new GeometryFactory().createEnvelope(min, max, null);
		return null;
	}

	@Override
	public boolean contains(QName ftName) {
//		return props.getProperty(ftName.toString()) != null;
		return false;
	}

	@Override
	public void update(QName ftName, Envelope bbox) {
	}

	@Override
	public void persist() {
		// TODO Auto-generated method stub
	}

	private final double[] decodePropValue(String s) {
		String[] parts = StringUtils.split(s, ",");
		double[] coords = new double[parts.length];
		for (int i = 0; i < parts.length; i++) {
			coords[i] = Double.parseDouble(parts[i].trim());
		}
		return coords;
	}

	private final String encodePropValue(Envelope env) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		Point p = env.getMin();
		for (double d : p.getAsArray()) {
			if (!first ){
				sb.append (',');
			}
			sb.append(d);
			first = false;
		}
		p = env.getMax();
		for (double d : p.getAsArray()) {
			if (!first ){
				sb.append (',');
			}
			sb.append(d);
			first = false;
		}				
		return sb.toString();
	}
}
