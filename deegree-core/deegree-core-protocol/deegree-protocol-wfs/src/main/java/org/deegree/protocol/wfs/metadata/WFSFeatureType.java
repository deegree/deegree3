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
package org.deegree.protocol.wfs.metadata;

import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Envelope;

/**
 * {@link FeatureType} metadata announced by a <code>WFS</code>.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class WFSFeatureType {

	private final QName name;

	private final List<LanguageString> titles;

	private final List<LanguageString> abstracts;

	private final List<String> outputFormats;

	private final List<Object> keywords;

	private final CRSRef defaultCrs;

	private final List<CRSRef> otherCrs;

	private Envelope wgs84BBox;

	private final List<Envelope> wgs84BBoxes;

	private final List<Object> mdReferences;

	// private final Object extendedDescription;

	public WFSFeatureType(QName name, List<LanguageString> titles, List<LanguageString> abstracts,
			List<String> outputFormats, List<Object> keywords, CRSRef defaultCrs, List<CRSRef> otherCrs,
			List<Envelope> wgs84BBoxes, List<Object> mdReferences, Object extendedDescription) {
		this.name = name;
		this.titles = titles;
		this.abstracts = abstracts;
		this.outputFormats = outputFormats;
		this.keywords = keywords;
		this.defaultCrs = defaultCrs;
		this.otherCrs = otherCrs;
		this.wgs84BBoxes = wgs84BBoxes;
		wgs84BBox = wgs84BBoxes.isEmpty() ? null : wgs84BBoxes.get(0);
		for (int i = 1; i < wgs84BBoxes.size(); i++) {
			wgs84BBox = wgs84BBox.merge(wgs84BBoxes.get(i));
		}
		this.mdReferences = Collections.emptyList();
		// this.extendedDescription = extendedDescription;
	}

	public QName getName() {
		return name;
	}

	public List<LanguageString> getTitles() {
		return titles;
	}

	public List<LanguageString> getAbstracts() {
		return abstracts;
	}

	public List<String> getOutputFormats() {
		return outputFormats;
	}

	public List<Object> getKeywords() {
		return keywords;
	}

	public CRSRef getDefaultCrs() {
		return defaultCrs;
	}

	public List<CRSRef> getOtherCrs() {
		return otherCrs;
	}

	public Envelope getWGS84BoundingBox() {
		return wgs84BBox;
	}

	public List<Envelope> getWGS84BoundingBoxes() {
		return wgs84BBoxes;
	}

	public List<Object> getMetadataReferences() {
		return mdReferences;
	}

}
