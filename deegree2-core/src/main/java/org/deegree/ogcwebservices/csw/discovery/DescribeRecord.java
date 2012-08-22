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

package org.deegree.ogcwebservices.csw.discovery;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.csw.AbstractCSWRequest;
import org.deegree.ogcwebservices.csw.CSWPropertiesAccess;
import org.w3c.dom.Element;

/**
 * The mandatory DescribeRecord operation allows a client to discover elements of the information
 * model supported by the target catalogue service. The operation allows some or all of the
 * information model to be described.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class DescribeRecord extends AbstractCSWRequest {

    private static final long serialVersionUID = 6554937884331546780L;

    private static final ILogger LOG = LoggerFactory.getLogger( DescribeRecord.class );

    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    private Map<String, URI> namespaceMappings;

    private String[] typeNames;

    private String outputFormat;

    private URI schemaLanguage;

    /**
     * creates a GetRecords request from the XML fragment passed. The passed element must be valid
     * against the OGC CSW 2.0 GetRecords schema.
     *
     * @param id
     *            unique ID of the request
     * @param root
     *            root element of the GetRecors request
     * @return the new instance
     * @throws MissingParameterValueException
     * @throws InvalidParameterValueException
     * @throws OGCWebServiceException
     */
    public static DescribeRecord create( String id, Element root )
                            throws MissingParameterValueException, InvalidParameterValueException,
                            OGCWebServiceException {

        String version = null;
        try {
            // first try to read verdsion attribute which is optional for CSW 2.0.0 and 2.0.1
            version = XMLTools.getNodeAsString( root, "./@version", nsContext, null );
        } catch ( XMLParsingException e ) {
            // a default version will be used
        }
        if ( version == null ) {
            // if no version attribute has been set try mapping namespace URI to a version;
            // this is not well defined for 2.0.0 and 2.0.1 which uses the same namespace.
            // in this case 2.0.0 will be returned!
            version = CSWPropertiesAccess.getString( root.getNamespaceURI() );
        }

        // read class for version depenging parsing of DescribeRecord request from properties
        String className = CSWPropertiesAccess.getString( "DescribeRecord" + version );
        Class<?> clzz = null;
        try {
            clzz = Class.forName( className );
        } catch ( ClassNotFoundException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage(), e );
        }
        DescribeRecordDocument document = null;
        try {
            document = (DescribeRecordDocument) clzz.newInstance();
        } catch ( InstantiationException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage(), e );
        } catch ( IllegalAccessException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage(), e );
        }

        document.setRootElement( root );
        return document.parse( id );

    }

    /**
     * Creates a new <code>DecribeRecord</code> instance from the values stored in the submitted
     * Map. Keys (parameter names) in the Map must be uppercase.
     *
     * @TODO evaluate vendorSpecificParameter
     *
     * @param kvp
     *            Map containing the parameters
     * @return the new instance
     * @exception InvalidParameterValueException
     * @throws MissingParameterValueException
     */
    public static DescribeRecord create( Map<String, String> kvp )
                            throws InvalidParameterValueException, MissingParameterValueException {

        String id;
        String version;
        Map<String, String> vendorSpecificParameter = new HashMap<String, String>();
        Map<String, URI> namespaceMappings;
        String[] typeNames = new String[0];
        String outputFormat;
        URI schemaLanguage;

        // 'ID'-attribute (optional)
        id = getParam( "ID", kvp, "" );

        // 'VERSION'-attribute (mandatory)
        version = getRequiredParam( "VERSION", kvp );

        // 'NAMESPACE'-attribute (optional)
        namespaceMappings = getNSMappings( getParam( "NAMESPACE", kvp, null ) );

        // 'TYPENAME'-attribute (optional)
        String typeNamesString = getParam( "TYPENAME", kvp, null );
        if ( typeNamesString != null ) {
            typeNames = typeNamesString.split( "," );
        }

        // 'OUTPUTFORMAT'-attribute (optional)
        if ( "2.0.2".equals( version ) ) {
            outputFormat = getParam( "OUTPUTFORMAT", kvp, "application/xml" );
        } else {
            outputFormat = getParam( "OUTPUTFORMAT", kvp, "text/xml" );
        }

        // 'SCHEMALANGUAGE'-attribute (optional)
        String schemaLanguageString = getParam( "SCHEMALANGUAGE", kvp, "XMLSCHEMA" );
        try {
            schemaLanguage = new URI( schemaLanguageString );
        } catch ( URISyntaxException e ) {
            String msg = "Value '" + schemaLanguageString
                         + "' for parameter 'SCHEMALANGUAGE' is invalid. Must denote a valid URI.";
            throw new InvalidParameterValueException( msg );
        }

        return new DescribeRecord( id, version, vendorSpecificParameter, namespaceMappings, typeNames, outputFormat,
                                   schemaLanguage );
    }

    /**
     * Creates a new <code>DescribeRecord</code> instance.
     *
     * @param id
     * @param version
     * @param vendorSpecificParameter
     */
    DescribeRecord( String id, String version, Map<String, String> vendorSpecificParameter ) {
        super( version, id, vendorSpecificParameter );
    }

    /**
     * Creates a new <code>DescribeRecord</code> instance.
     *
     * @param id
     * @param version
     * @param vendorSpecificParameter
     * @param namespaceMappings
     * @param typeNames
     * @param outputFormat
     * @param schemaLanguage
     */
    DescribeRecord( String id, String version, Map<String, String> vendorSpecificParameter,
                    Map<String, URI> namespaceMappings, String[] typeNames, String outputFormat, URI schemaLanguage ) {
        this( id, version, vendorSpecificParameter );
        this.namespaceMappings = namespaceMappings;
        this.typeNames = typeNames;
        this.outputFormat = outputFormat;
        this.schemaLanguage = schemaLanguage;
    }

    /**
     * Used to specify namespace(s) and their prefix(es). Format is [prefix:]uri. If prefix is not
     * specified, then this is the default namespace.
     * <p>
     * Zero or one (Optional). Include value for each namespace used by a TypeName. If not included,
     * all qualified names are in the default namespace
     *
     * @return the mappings
     */
    public Map<String, URI> getNamespaces() {
        return this.namespaceMappings;
    }

    /**
     * One or more qualified type names to be described.
     * <p>
     * Zero or one (Optional). Default action is to describe all types known to server.
     *
     * @return the type names
     *
     */
    public String[] getTypeNames() {
        return this.typeNames;
    }

    /**
     * A MIME type indicating the format that the output document should have.
     * <p>
     * Zero or one (Optional). Default value is text/xml
     *
     * @return the format
     *
     */
    public String getOutputFormat() {
        return this.outputFormat;
    }

    /**
     * Default value is 'XMLSCHEMA'.
     *
     * @return the language
     *
     */
    public URI getSchemaLanguage() {
        return this.schemaLanguage;
    }
}
