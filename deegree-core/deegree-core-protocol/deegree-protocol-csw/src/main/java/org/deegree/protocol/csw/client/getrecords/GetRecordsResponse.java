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
package org.deegree.protocol.csw.client.getrecords;

import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_PREFIX;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.commons.xml.XPath;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.MetadataRecordFactory;
import org.deegree.protocol.ows.client.OWSResponse;
import org.deegree.protocol.ows.exception.OWSExceptionReport;

/**
 * Represents a <code>GetRecords</code> response of a CSW.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetRecordsResponse extends XMLAdapter {

    private final OWSResponse response;

    static {
        nsContext.addNamespace( CSW_202_PREFIX, CSW_202_NS );
    }

    public GetRecordsResponse( OWSResponse response ) throws XMLProcessingException, OWSExceptionReport,
                            XMLStreamException {
        this.response = response;
        this.load( response.getAsXMLStream() );
    }

    public OWSResponse getResponse() {
        return response;
    }

    public List<MetadataRecord> getRecords() {
        List<MetadataRecord> records = new ArrayList<MetadataRecord>();
        for ( OMElement element : getElements( getRootElement(),
                                               new XPath( "/csw:GetRecordsResponse/csw:SearchResults/child::*",
                                                          nsContext ) ) ) {
            records.add( MetadataRecordFactory.create( element ) );
        }
        return records;
    }

    @Deprecated
    public List<OMElement> getElements( XPath xpath ) {
        return getElements( getRootElement(), xpath );
    }

    public int getNumberOfRecordsMatched() {
        return getNodeAsInt( getRootElement(), getXPath( "numberOfRecordsMatched" ), 0 );
    }

    public int getNumberOfRecordsReturned() {
        return getNodeAsInt( getRootElement(), getXPath( "numberOfRecordsReturned" ), 0 );
    }

    public int getNextRecord() {
        return getNodeAsInt( getRootElement(), getXPath( "nextRecord" ), 0 );
    }

    private XPath getXPath( String attribute ) {
        return new XPath( "//" + CSW_202_PREFIX + ":GetRecordsResponse/" + CSW_202_PREFIX + ":SearchResults/@"
                          + attribute, nsContext );
    }

    public void close()
                            throws IOException {
        response.close();
    }

}
