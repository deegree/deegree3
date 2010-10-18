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
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.schema.SchemaValidator;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates the inserted Metadata.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MetadataValidation implements RecordInspector {
    private static Logger LOG = LoggerFactory.getLogger( MetadataValidation.class );

    private final boolean isValidate;

    private MetadataValidation( boolean isValidate ) {
        this.isValidate = isValidate;
    }

    public static MetadataValidation newInstance( boolean isValidate ) {
        return new MetadataValidation( isValidate );
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
                            throws MetadataStoreException {
        StringWriter s = new StringWriter();
        if ( isValidate ) {
            try {
                elem.serialize( s );
            } catch ( XMLStreamException e ) {

                LOG.debug( "error: " + e.getMessage(), e );
                throw new MetadataStoreException( e.getMessage() );
            }
            InputStream is = new ByteArrayInputStream( s.toString().getBytes() );
            if ( elem.getLocalName().equals( "MD_Metadata" ) ) {
                // TODO use local copy of schema
                return SchemaValidator.validate( is, "http://www.isotc211.org/2005/gmd/metadataEntity.xsd" );

            }
            return SchemaValidator.validate( is, "http://schemas.opengis.net/csw/2.0.2/record.xsd" );
        }
        return new ArrayList<String>();
    }

    @Override
    public OMElement inspect( OMElement record )
                            throws MetadataStoreException {
        List<String> errors = validate( record );
        if ( errors.isEmpty() ) {
            return record;
        }
        return null;
    }
}
