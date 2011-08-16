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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.protocol.wfs.WFSVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WFSCapabilitiesAdapter extends XMLAdapter {

    private static Logger LOG = LoggerFactory.getLogger( WFSCapabilitiesAdapter.class );

    private final WFSVersion version;

    private final NamespaceBindings nsContext = new NamespaceBindings();

    public WFSCapabilitiesAdapter( WFSVersion version ) {
        this.version = version;
        nsContext.addNamespace( "wfs", version.getNamespaceUri() );
    }

    public List<WFSFeatureType> getFeatureTypes() {
        switch ( version ) {
        case WFS_100:
            return getFeatureTypes100();
        case WFS_110:
            return getFeatureTypes110();
        case WFS_200:
            return getFeatureTypes200();
        }
        return null;
    }

    public List<WFSFeatureType> getFeatureTypes100() {
        List<OMElement> ftEls = getElements( rootElement, new XPath( "wfs:FeatureTypeList/wfs:FeatureType", nsContext ) );
        List<WFSFeatureType> fts = new ArrayList<WFSFeatureType>( ftEls.size() );
        for ( OMElement ftEl : ftEls ) {
            fts.add( parseFeatureType100( ftEl ) );
        }
        return fts;
    }

    private List<WFSFeatureType> getFeatureTypes110() {
        // TODO Auto-generated method stub
        return null;
    }

    private List<WFSFeatureType> getFeatureTypes200() {
        // TODO Auto-generated method stub
        return null;
    }

    private WFSFeatureType parseFeatureType100( OMElement ftEl ) {

        // <xsd:element name="Name" type="xsd:QName"/>
        OMElement nameEl = getRequiredElement( ftEl, new XPath( "wfs:Name", nsContext ) );
        String prefixedName = nameEl.getText().trim();
        QName ftName = parseQName( prefixedName, nameEl );

        // <xsd:element ref="wfs:Title" minOccurs="0"/>
        String title = getNodeAsString( ftEl, new XPath( "wfs:Title", nsContext ), null );

        // <xsd:element ref="wfs:Abstract" minOccurs="0"/>
        String ftAbstract = getNodeAsString( ftEl, new XPath( "wfs:Abstract", nsContext ), null );

        // <xsd:element ref="wfs:Keywords" minOccurs="0"/>
        String keywords = getNodeAsString( ftEl, new XPath( "wfs:Keywords", nsContext ), null );

        // <xsd:element ref="wfs:SRS"/>
        String srs = getNodeAsString( ftEl, new XPath( "wfs:SRS", nsContext ), null );
        CRSRef defaultSrs = CRSManager.getCRSRef( srs );

        // <xsd:element name="Operations" type="wfs:OperationsType" minOccurs="0"/>
        // TODO

        // <xsd:element name="LatLongBoundingBox" type="wfs:LatLongBoundingBoxType" minOccurs="0"
        // maxOccurs="unbounded"/>
        List<OMElement> bboxEls = getElements( ftEl, new XPath( "wfs:LatLongBoundingBox", nsContext ) );
        List<Envelope> ftBboxes = new ArrayList<Envelope>( bboxEls.size() );
        for ( OMElement bboxEl : bboxEls ) {
            double minX = getRequiredNodeAsDouble( bboxEl, new XPath( "@minx" ) );
            double minY = getRequiredNodeAsDouble( bboxEl, new XPath( "@miny" ) );
            double maxX = getRequiredNodeAsDouble( bboxEl, new XPath( "@maxx" ) );
            double maxY = getRequiredNodeAsDouble( bboxEl, new XPath( "@maxy" ) );
            CRSRef wgs84 = CRSManager.getCRSRef( "EPSG:4326", true );
            ftBboxes.add( new GeometryFactory().createEnvelope( minX, minY, maxX, maxY, wgs84 ) );
        }

        Envelope ftBBox = ftBboxes.isEmpty() ? null : ftBboxes.get( 0 );
        for ( int i = 1; i < ftBboxes.size(); i++ ) {
            ftBBox = ftBBox.merge( ftBboxes.get( i ) );
        }

        // <xsd:element name="MetadataURL" type="wfs:MetadataURLType" minOccurs="0" maxOccurs="unbounded"/>
        // TODO

        return new WFSFeatureType( ftName, title, ftAbstract, keywords, defaultSrs, ftBboxes, ftBBox );
    }
}
