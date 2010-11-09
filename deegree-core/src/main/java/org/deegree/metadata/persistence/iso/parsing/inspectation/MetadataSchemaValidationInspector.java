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
package org.deegree.metadata.persistence.iso.parsing.inspectation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.iso19115.jaxb.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates the inserted Metadata.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * @param <MSVInspector>
 */
public class MetadataSchemaValidationInspector implements RecordInspector {
    private static Logger LOG = LoggerFactory.getLogger( MetadataSchemaValidationInspector.class );

    private boolean isValidate = false;

    public MetadataSchemaValidationInspector( SchemaValidator config ) {
        if ( config != null ) {
            this.isValidate = true;
        }

    }

    /**
     * Before any transaction operation is possible there should be an evaluation of the record. The response of the
     * full ISO record has to be valid. With this method this is guaranteed.
     * 
     * @param elem
     *            that has to be evaluated before there is any transaction operation possible.
     * @return a list of error-strings, or empty list if there is no validation needed.
     * @throws MetadataStoreException
     */
    private List<String> validate( OMElement elem )
                            throws MetadataInspectorException {
        StringWriter s = new StringWriter();
        if ( isValidate ) {
            try {
                elem.serialize( s );
            } catch ( XMLStreamException e ) {
                LOG.debug( "error: " + e.getMessage(), e );
                throw new MetadataInspectorException( e.getMessage() );
            }
            InputStream is = new ByteArrayInputStream( s.toString().getBytes() );
            if ( elem.getLocalName().equals( "MD_Metadata" ) ) {
                // TODO use local copy of schema
                return org.deegree.commons.xml.schema.SchemaValidator.validate( is,
                                                                                "http://www.isotc211.org/2005/gmd/metadataEntity.xsd" );

            }
            return org.deegree.commons.xml.schema.SchemaValidator.validate( is,
                                                                            "http://schemas.opengis.net/csw/2.0.2/record.xsd" );
        }
        return new ArrayList<String>();
    }

    @Override
    public OMElement inspect( OMElement record, Connection conn )
                            throws MetadataInspectorException {
        List<String> errors = validate( record );
        if ( errors.isEmpty() ) {
            return record;
        } else {
            String msg = Messages.getMessage( "ERROR_VALIDATE" );
            LOG.debug( msg );
            throw new MetadataInspectorException( msg );
        }

    }

}
