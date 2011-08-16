//$HeadURL$
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
package org.deegree.protocol.wfs.client;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.geometry.Envelope;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WFSFeatureType {

    private final QName name;

    private final String title;

    private final String ftAbstract;

    private final String keywords;

    private final CRSRef defaultSrs;

    private final List<Envelope> wgs84BBoxes;

    private final Envelope wgs84BBox;

    public WFSFeatureType( QName name, String title, String ftAbstract, String keywords, CRSRef srs,
                           List<Envelope> ftBboxes, Envelope ftBBox ) {
        this.name = name;
        this.title = title;
        this.ftAbstract = ftAbstract;
        this.keywords = keywords;
        this.defaultSrs = srs;
        this.wgs84BBoxes = ftBboxes;
        this.wgs84BBox = ftBBox;
    }

    public QName getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getAbstract() {
        return ftAbstract;
    }
    
    public CRSRef getDefaultSrs() {
        return defaultSrs;
    }

    public CRSRef getOtherSrs() {
        return null;
    }

    public Envelope getWGS84BoundingBox() {
        return wgs84BBox;
    }

    public List<String> getOutputFormats() {
        return null;
    }

    @Override
    public String toString() {
        return "WFSFeatureType [name=" + name + ", title=" + title + ", ftAbstract=" + ftAbstract + ", keywords="
               + keywords + ", defaultSrs=" + defaultSrs + ", wgs84BBoxes=" + wgs84BBoxes + ", wgs84BBox=" + wgs84BBox
               + "]";
    }
}