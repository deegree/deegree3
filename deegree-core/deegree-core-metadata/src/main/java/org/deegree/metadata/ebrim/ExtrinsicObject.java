//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.metadata.ebrim;

import static org.slf4j.LoggerFactory.getLogger;

import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XPath;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class ExtrinsicObject extends RegistryObject {

    private static final Logger LOG = getLogger( ExtrinsicObject.class );

    public ExtrinsicObject( XMLStreamReader xmlReader ) {
        super( xmlReader );
    }

    public ExtrinsicObject( OMElement eoElement ) {
        super( eoElement );
    }

    public Geometry getGeometrySlotValue( String slotName ) {
        OMElement geomElem = adapter.getElement( adapter.getRootElement(),
                                                 new XPath( "./rim:Slot[@name='" + slotName
                                                            + "']/wrs:ValueList/wrs:AnyValue[1]/*", ns ) );
        if ( geomElem != null ) {
            try {
                GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31,
                                                                                   geomElem.getXMLStreamReader() );
                return gmlReader.readGeometry();
            } catch ( Exception e ) {
                String msg = "Could not parse geometry " + geomElem;
                LOG.debug( msg, e );
                e.printStackTrace();
                throw new IllegalArgumentException( msg );
            }
        }
        return null;
    }

    public String[] getSlotValueList( String slotName ) {
        return adapter.getNodesAsStrings( adapter.getRootElement(), new XPath( "./rim:Slot[@name='" + slotName
                                                                               + "']/rim:ValueList/rim:Value", ns ) );
    }

    public String getSlotValue( String slotName ) {
        return adapter.getNodeAsString( adapter.getRootElement(), new XPath( "./rim:Slot[@name='" + slotName
                                                                             + "']/rim:ValueList/rim:Value[1]", ns ),
                                        null );
    }

    /**
     * @return the isOpaque
     */
    public boolean isOpaque() {
        return false;
    }

    /**
     * @return
     */
    public Object getResource() {
        return null;
    }

}
