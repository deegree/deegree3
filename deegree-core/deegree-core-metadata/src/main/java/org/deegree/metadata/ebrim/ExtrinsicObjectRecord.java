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

import static org.deegree.metadata.ebrim.model.Slot.SLOTTYPE._date;
import static org.deegree.metadata.ebrim.model.Slot.SLOTTYPE._double;
import static org.deegree.metadata.ebrim.model.Slot.SLOTTYPE._geom;
import static org.deegree.metadata.ebrim.model.Slot.SLOTTYPE._int;
import static org.deegree.metadata.ebrim.model.Slot.SLOTTYPE._multiple;
import static org.deegree.metadata.ebrim.model.Slot.SLOTTYPE._string;
import static org.slf4j.LoggerFactory.getLogger;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.commons.xml.XPath;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.metadata.ebrim.model.ExtrinsicObject;
import org.deegree.metadata.ebrim.model.RegistryObject;
import org.deegree.metadata.ebrim.model.SlotMapper;
import org.deegree.metadata.ebrim.model.SlotMapper.EOTYPE;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class ExtrinsicObjectRecord extends RegistryObjectRecord {

    private static final Logger LOG = getLogger( ExtrinsicObjectRecord.class );

    public ExtrinsicObjectRecord( XMLStreamReader xmlReader ) {
        super( xmlReader );
    }

    public ExtrinsicObjectRecord( OMElement eoElement ) {
        super( eoElement );
    }

    @Override
    public ExtrinsicObject getParsedRecord() {
        RegistryObject ro = super.getParsedRecord();

        // not yet supported
        Boolean isOpaque = false;

        ExtrinsicObject ext = new ExtrinsicObject( ro, isOpaque );
        
        EOTYPE type = EOTYPE.valueOfType( ro.getObjectType() );
        OMElement record = adapter.getRootElement();
        
        parseStringSlots( type, SlotMapper.getSlots( type, _string ), record, ext );
        parseStringListSlots( type, SlotMapper.getSlots( type, _multiple ), record, ext );
        parseIntSlots( type, SlotMapper.getSlots( type, _int ), record, ext );
        parseDoubleSlots( type, SlotMapper.getSlots( type, _double ), record, ext );
        parseDateSlots( type, SlotMapper.getSlots( type, _date ), record, ext );
        parseGeometrySlots( type, SlotMapper.getSlots( type, _geom ), record, ext );

        return ext;
    }

    private void parseStringSlots( EOTYPE eoType, List<String> slotNames, OMElement node, ExtrinsicObject elements ) {
        for ( String slotName : slotNames ) {
            String s = adapter.getNodeAsString( node, new XPath( "./rim:Slot[@name='" + SlotMapper.SLOTURN + slotName
                                                                 + "']/rim:ValueList/rim:Value[1]", ns ), null );
            LOG.debug( "Slot name " + SlotMapper.SLOTURN + slotName + " value " + s );
            elements.addSlot( slotName, s );
        }
    }

    private void parseStringListSlots( EOTYPE eoType, List<String> slotNames, OMElement node, ExtrinsicObject elements ) {
        for ( String slotName : slotNames ) {
            String[] s = adapter.getNodesAsStrings( node, new XPath( "./rim:Slot[@name='" + SlotMapper.SLOTURN
                                                                     + slotName + "']/rim:ValueList/rim:Value", ns ) );
            elements.addSlot( slotName, Arrays.asList( s ) );
        }
    }

    private void parseIntSlots( EOTYPE eoType, List<String> slotNames, OMElement node, ExtrinsicObject elements ) {
        for ( String slotName : slotNames ) {
            int s = adapter.getNodeAsInt( node, new XPath( "./rim:Slot[@name='" + SlotMapper.SLOTURN + slotName
                                                           + "']/rim:ValueList/rim:Value[1]", ns ), 0 );
            elements.addSlot( slotName, s );
        }
    }

    private void parseDoubleSlots( EOTYPE eoType, List<String> slotNames, OMElement node, ExtrinsicObject elements ) {
        for ( String slotName : slotNames ) {
            double s = adapter.getNodeAsDouble( node, new XPath( "./rim:Slot[@name='" + SlotMapper.SLOTURN + slotName
                                                                 + "']/rim:ValueList/rim:Value[1]", ns ), Double.NaN );
            elements.addSlot( slotName, s );
        }
    }

    private void parseDateSlots( EOTYPE eoType, List<String> slotNames, OMElement node, ExtrinsicObject elements ) {
        for ( String slotName : slotNames ) {
            String s = adapter.getNodeAsString( node, new XPath( "./rim:Slot[@name='" + SlotMapper.SLOTURN + slotName
                                                                 + "']/rim:ValueList/rim:Value[1]", ns ), null );
            if ( s != null ) {
                try {
                    elements.addSlot( slotName, DateUtils.parseISO8601Date( s ) );
                } catch ( ParseException e ) {
                    String msg = "Could not parse as Date:" + s;
                    LOG.debug( msg, e );
                    throw new IllegalArgumentException( msg );
                }
            }
        }
    }

    private void parseGeometrySlots( EOTYPE eoType, List<String> slotNames, OMElement node, ExtrinsicObject elements ) {
        for ( String slotName : slotNames ) {
            OMElement geomElem = adapter.getElement( node,
                                                     new XPath( "./rim:Slot[@name='" + SlotMapper.SLOTURN + slotName
                                                                + "']/wrs:ValueList/wrs:AnyValue[1]/*", ns ) );
            if ( geomElem != null ) {
                try {
                    GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31,
                                                                                       geomElem.getXMLStreamReader() );
                    elements.addSlot( slotName, gmlReader.readGeometry() );
                } catch ( Exception e ) {
                    String msg = "Could not parse geometry " + geomElem;
                    LOG.debug( msg, e );
                    e.printStackTrace();
                    throw new IllegalArgumentException( msg );
                }
            }
        }

    }

}
