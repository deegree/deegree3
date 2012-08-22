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

import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.util.StringTools;
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
 * The mandatory GetRecordById request retrieves the default representation of catalogue records using their identifier.
 * The GetRecordById operation is an implementation of the Present operation from the general model. This operation
 * presumes that a previous query has been performed in order to obtain the identifiers that may be used with this
 * operation. For example, records returned by a GetRecords operation may contain references to other records in the
 * catalogue that may be retrieved using the GetRecordById operation. This operation is also a subset of the GetRecords
 * operation, and is included as a convenient short form for retrieving and linking to records in a catalogue.
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class GetRecordById extends AbstractCSWRequest {

    private static final long serialVersionUID = -3602776884510160189L;

    private static final ILogger LOG = LoggerFactory.getLogger( GetRecordById.class );

    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    private String[] ids = null;

    private String elementSetName = null;

    private String outputSchema = null;

    /**
     * creates a <code>GetRecordById</code> request from the XML fragment passed. The passed element must be valid
     * against the OGC CSW 2.0 GetRecordById schema.
     * 
     * @param id
     *            unique ID of the request
     * @param root
     *            root element of the GetRecors request
     * @return a GetRecordById bean representation created from the xml fragment.
     * @throws MissingParameterValueException
     * @throws InvalidParameterValueException
     * @throws OGCWebServiceException
     */
    public static GetRecordById create( String id, Element root )
                            throws MissingParameterValueException, InvalidParameterValueException,
                            OGCWebServiceException {

        String version = null;
        try {
            // first try to read verdsion attribute which is optional for CSW 2.0.0 and 2.0.1
            version = XMLTools.getNodeAsString( root, "./@version", nsContext, null );
        } catch ( XMLParsingException e ) {
            // default version?
        }
        if ( version == null ) {
            // if no version attribute has been set try mapping namespace URI to a version;
            // this is not well defined for 2.0.0 and 2.0.1 which uses the same namespace.
            // in this case 2.0.0 will be returned!
            version = CSWPropertiesAccess.getString( root.getNamespaceURI() );
        }

        // read class for version depenging parsing of GetRecords request from properties
        String className = CSWPropertiesAccess.getString( "GetRecordById" + version );
        Class<?> clzz = null;
        try {
            clzz = Class.forName( className );
        } catch ( ClassNotFoundException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage(), e );
        }
        GetRecordByIdDocument document = null;
        try {
            document = (GetRecordByIdDocument) clzz.newInstance();
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
     * Creates a new <code>GetRecordById</code> instance from the values stored in the submitted Map. Keys (parameter
     * names) in the Map must be uppercase.
     * 
     * @TODO evaluate vendorSpecificParameter
     * 
     * @param kvp
     *            Map containing the parameters
     * @return a GetRecordById bean representation, the internal request id will be set to Long.toString(
     *         IDGenerator.getInstance().generateUniqueID() ).
     */
    public static GetRecordById create( Map<String, String> kvp ) {

        String version = kvp.remove( "VERSION" );
        String elementSetName = kvp.remove( "ELEMENTSETNAME" );
        String outputSchema = null;
        if ( "2.0.2".equals( version ) ) {
            outputSchema = kvp.remove( "OUTPUTSCHEMA" );
        }
        String tmp = kvp.remove( "ID" );
        String[] ids = StringTools.toArray( tmp, ",", true );

        return new GetRecordById( Long.toString( IDGenerator.getInstance().generateUniqueID() ), version, kvp, ids,
                                  elementSetName, outputSchema );
    }

    /**
     * Creates a new <code>GetRecordById</code> instance from the values stored in the submitted Map. Keys (parameter
     * names) in the Map must be uppercase.
     * 
     * @param id
     *            of the request, and not the requested ids.
     * 
     * @TODO evaluate vendorSpecificParameter
     * 
     * @param kvp
     *            Map containing the parameters
     * @return a GetRecordById bean representation.
     */
    public static GetRecordById create( String id, Map<String, String> kvp ) {

        String version = kvp.remove( "VERSION" );
        String elementSetName = kvp.remove( "ELEMENTSETNAME" );
        String outputSchema = null;
        if ( "2.0.2".equals( version ) ) {
            outputSchema = kvp.remove( "OUTPUTSCHEMA" );
        }
        String tmp = kvp.remove( "ID" );
        String[] ids = StringTools.toArray( tmp, ",", true );

        return new GetRecordById( id, version, kvp, ids, elementSetName, outputSchema );
    }

    /**
     * 
     * @param ids
     *            identifiers of the requested catalogue entries
     * @param elementSetName
     *            requested element set (brief|summary|full). Can be <code>null</code>; will be treaded as full.
     */
    GetRecordById( String id, String version, Map<String, String> vendorSpecificParameters, String[] ids,
                   String elementSetName ) {
        super( version, id, vendorSpecificParameters );
        this.ids = ids;
        this.elementSetName = elementSetName;
    }

    /**
     * 
     * @param ids
     *            identifiers of the requested catalogue entries
     * @param elementSetName
     *            requested element set (brief|summary|full). Can be <code>null</code>; will be treaded as full.
     */
    GetRecordById( String id, String version, Map<String, String> vendorSpecificParameters, String[] ids,
                   String elementSetName, String outputSchema ) {
        super( version, id, vendorSpecificParameters );
        this.ids = ids;
        this.elementSetName = elementSetName;
        this.outputSchema = outputSchema;
    }

    /**
     * @return the requested element set name. If the returned value equals <code>null</code> a 'summary' request shall
     *         be performed. possible values are:
     *         <ul>
     *         <li>brief</li>
     *         <li>summary</li>
     *         <li>full</li>
     *         </ul>
     * 
     */
    public String getElementSetName() {
        return elementSetName;
    }

    /**
     * @return the requested outputSchema. If the returned value equals <code>null</code> dublin core
     *         (http://www.opengis.net/cat/csw/2.0.2) should be returned by the the CSW 2.02.
     */
    public String getOutputSchema() {
        return outputSchema;
    }

    /**
     * @return the requested ids as an array of strings
     */
    public String[] getIds() {
        return ids;
    }
}
