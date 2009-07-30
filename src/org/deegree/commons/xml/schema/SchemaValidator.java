//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.commons.xml.schema;

import java.util.LinkedList;
import java.util.List;

import org.apache.xerces.parsers.XIncludeAwareParserConfiguration;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utility methods for the easy validation of XML documents against schemas.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class SchemaValidator {

    private static final Logger LOG = LoggerFactory.getLogger( SchemaValidator.class );

    /** Namespaces feature id (http://xml.org/sax/features/namespaces). */
    private static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";

    /** Validation feature id (http://xml.org/sax/features/validation). */
    private static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";

    /** Schema validation feature id (http://apache.org/xml/features/validation/schema). */
    private static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";

    /** Schema full checking feature id (http://apache.org/xml/features/validation/schema-full-checking). */
    private static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";

    /** Honour all schema locations feature id (http://apache.org/xml/features/honour-all-schemaLocations). */
    private static final String HONOUR_ALL_SCHEMA_LOCATIONS_ID = "http://apache.org/xml/features/honour-all-schemaLocations";

    private static XMLParserConfiguration createValidatingParser( XMLEntityResolver entityResolver )
                            throws XNIException {

        XMLParserConfiguration parserConfiguration = new XIncludeAwareParserConfiguration();
        parserConfiguration.setFeature( NAMESPACES_FEATURE_ID, true );
        parserConfiguration.setFeature( VALIDATION_FEATURE_ID, true );
        parserConfiguration.setFeature( SCHEMA_VALIDATION_FEATURE_ID, true );
        parserConfiguration.setFeature( SCHEMA_FULL_CHECKING_FEATURE_ID, true );
        parserConfiguration.setFeature( HONOUR_ALL_SCHEMA_LOCATIONS_ID, true );
        if ( entityResolver != null ) {
            parserConfiguration.setEntityResolver( entityResolver );
        }
        return parserConfiguration;
    }

    /**
     * Validates the specified XML input document according to the contained schema references (
     * <code>xsi:schemaLocation</code> attribute).
     * 
     * @param source
     *            document to be validated
     * @return list of validation events (errors/warnings) that occured, never null, size of 0 means valid document
     */
    public static List<String> validate( XMLInputSource source ) {
        final List<String> errors = new LinkedList<String>();

        try {
            XMLParserConfiguration parserConfig = createValidatingParser( new RedirectingEntityResolver() );
            parserConfig.setErrorHandler( new XMLErrorHandler() {
                @Override
                public void error( String domain, String key, XMLParseException e )
                                        throws XNIException {
                    LOG.debug( "Encountered error: " + toString( e ) );
                    errors.add( "Error: " + toString( e ) );
                }

                @Override
                public void fatalError( String domain, String key, XMLParseException e )
                                        throws XNIException {
                    LOG.debug( "Encountered fatal error: " + toString( e ) );
                    errors.add( "Fatal error: " + toString( e ) );
                }

                @Override
                public void warning( String domain, String key, XMLParseException e )
                                        throws XNIException {
                    LOG.debug( "Encountered warning: " + toString( e ) );
                    errors.add( "Warning: " + toString( e ) );
                }

                private String toString( XMLParseException e ) {
                    String s = e.getLocalizedMessage();
                    s += " (line: " + e.getLineNumber() + ", column: " + e.getColumnNumber();
                    s += e.getExpandedSystemId() != null ? ", SystemID: '" + e.getExpandedSystemId() + "')" : ")";
                    return s;
                }
            } );
            parserConfig.parse( source );
        } catch ( Exception e ) {
            LOG.debug( e.getMessage(), e );
            errors.add( "Fatal error: " + e.getMessage() );
        }
        return errors;
    }
}
