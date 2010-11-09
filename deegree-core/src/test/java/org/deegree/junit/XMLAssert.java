//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.junit;

import static org.junit.Assert.fail;

import java.io.Reader;
import java.util.List;

import org.apache.xerces.xni.parser.XMLInputSource;
import org.deegree.commons.xml.schema.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains static assert methods for using XML validation results in JUnit test cases.
 * 
 * @see SchemaValidator
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class XMLAssert {

    private static final Logger LOG = LoggerFactory.getLogger( XMLAssert.class );

    /**
     * Asserts that the specified XML document is valid with respect to the schemas that it references (using
     * <code>xsi:schemaLocation</code> attributes) and/or the specified schema documents.
     * 
     * @param reader
     *            provides the XML document to be validated
     * @param schemaLocations
     *            optional locations of schema documents to be considered in the validation
     */
    public static void assertValidity( Reader reader, String... schemaLocations ) {
        XMLInputSource source = new XMLInputSource( null, null, null, reader, null );
        List<String> messages = SchemaValidator.validate( source, schemaLocations );
        if ( messages.size() > 0 ) {
            fail( messages.get( 0 ) );
        }
        if ( LOG.isErrorEnabled() ) {
            for ( String msg : messages ) {
                LOG.error( msg );
            }
        }
    }    
    
    /**
     * Asserts that the specified XML document is valid with respect to the schemas that it references (using
     * <code>xsi:schemaLocation</code> attributes) and/or the specified schema documents.
     * 
     * @param source
     *            provides the document to be validated
     * @param schemaLocations
     *            optional locations of schema documents to be considered in the validation
     */
    public static void assertValidity( XMLInputSource source, String... schemaLocations ) {
        List<String> messages = SchemaValidator.validate( source, schemaLocations );
        if ( messages.size() > 0 ) {
            fail( messages.get( 0 ) );
        }
        if ( LOG.isErrorEnabled() ) {
            for ( String msg : messages ) {
                LOG.error( msg );
            }
        }
    }
}
