//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.services.csw.exporthandling;

import static org.deegree.protocol.csw.CSWConstants.CSW_202_DISCOVERY_SCHEMA;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.GMD_NS;
import static org.deegree.protocol.csw.CSWConstants.VERSION_202;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ows.Version;
import org.deegree.metadata.DCRecord;
import org.deegree.metadata.MetadataRecord;
import org.deegree.protocol.csw.CSWConstants.OutputSchema;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.services.csw.CSWController;
import org.deegree.services.csw.getrecords.GetRecords;

/**
 * Defines the export functionality for a {@link GetRecords} request
 * 
 * @see CSWController
 * 
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetRecordsHandler extends AbstractGetRecordsHandler {

    @Override
    protected void writeRecord( XMLStreamWriter writer, GetRecords getRec, MetadataRecord m, boolean isElementName )
                            throws MetadataStoreException, XMLStreamException {
        if ( isElementName == false ) {
            if ( getRec.getOutputSchema().equals( OutputSchema.determineOutputSchema( OutputSchema.ISO_19115 ) ) ) {
                m.serialize( writer, getRec.getQuery().getElementSetName() );
            } else {
                DCRecord dc = m.toDublinCore();
                dc.serialize( writer, getRec.getQuery().getElementSetName() );
            }
        } else {
            if ( getRec.getOutputSchema().equals( OutputSchema.determineOutputSchema( OutputSchema.ISO_19115 ) ) ) {
                m.serialize( writer, getRec.getQuery().getElementName() );
            } else {
                DCRecord dc = m.toDublinCore();
                dc.serialize( writer, getRec.getQuery().getElementName() );
            }
        }
    }

    @Override
    protected String getSchemaLocation( Version version ) {
        if ( version == VERSION_202 ) {
            return CSW_202_NS + " " + CSW_202_DISCOVERY_SCHEMA + " " + GMD_NS + " "
                   + "http://schemas.opengis.net/iso/19139/20070417/gmd/gmd.xsd";
        }
        return "";
    }

}
