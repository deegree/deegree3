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
package org.deegree.ogcwebservices.csw.capabilities;

import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.csw.CSWPropertiesAccess;
import org.deegree.ogcwebservices.getcapabilities.GetCapabilities;
import org.w3c.dom.Element;

/**
 * Class representation of an <code>OGC-GetCapabilities</code> request in <code>CSW</code>
 * flavour.
 * <p>
 * Special to the <code>CSW</code> version of the <code>GetCapabilities</code> request are these
 * two additional parameters: <table border="1">
 * <tr>
 * <th>Name</th>
 * <th>Occurences</th>
 * <th>Function</th>
 * </tr>
 * <tr>
 * <td>AcceptVersions</td>
 * <td align="center">0|1</td>
 * <td>Protocol versions supported by this service.</td>
 * </tr>
 * <tr>
 * <td>AcceptFormats</td>
 * <td align="center">0|1</td>
 * <td>Formats accepted by this service.</td>
 * </tr>
 * </table>
 *
 * @since 2.0
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$
 */
public class CatalogueGetCapabilities extends GetCapabilities {

    private static final long serialVersionUID = 7690283041658363481L;

    private static final ILogger LOG = LoggerFactory.getLogger( CatalogueGetCapabilities.class );

    /**
     * Creates a new <code>CatalogueGetCapabilities</code> instance.
     *
     * @param id
     *            request identifier
     * @param updateSequence
     * @param version
     * @param acceptVersions
     * @param acceptFormats
     * @param sections
     * @param vendoreSpec
     */
    CatalogueGetCapabilities( String id, String updateSequence, String version, String[] acceptVersions,
                              String[] acceptFormats, String[] sections, Map<String, String> vendoreSpec ) {
        super( id, version, updateSequence, acceptVersions, sections, acceptFormats, vendoreSpec );
    }

    /**
     * Creates a <code>CatalogGetCapabilities</code> request from its KVP representation.
     *
     * @param kvp
     *            Map containing the key-value pairs
     * @return created <code>CatalogGetCapabilities</code> object
     * @throws InvalidParameterValueException
     */
    public static CatalogueGetCapabilities create( Map<String, String> kvp )
                            throws InvalidParameterValueException {

        String id = getParam( "ID", kvp, null );
        // use 2.0.0 as default version
        String version = getParam( "VERSION", kvp, CSWPropertiesAccess.getString( "DEFAULTVERSION" ) );
        if ( !"2.0.0".equals( version ) && !"2.0.1".equals( version ) && !"2.0.2".equals( version ) ) {
            throw new InvalidParameterValueException( Messages.get( "CSW_UNSUPPORTED_VERSION" ) );
        }
        String updateSequence = getParam( "UPDATESEQUENCE", kvp, null );
        String[] acceptVersions = null;
        if ( kvp.get( "ACCEPTVERSIONS" ) != null ) {
            String tmp = getParam( "ACCEPTVERSIONS", kvp, null );

            acceptVersions = StringTools.toArray( tmp, ",", false );
            version = CatalogueGetCapabilities.validateVersion( acceptVersions );
            if ( version == null ) {
                throw new InvalidParameterValueException( Messages.get( "CSW_UNSUPPORTED_VERSION" ) );
            }
        }
        LOG.logInfo( "process with version:", version );
        String[] acceptFormats = null;
        if ( kvp.get( "OUTPUTFORMAT" ) != null ) {
            String tmp = getParam( "OUTPUTFORMAT", kvp, null );
            acceptFormats = StringTools.toArray( tmp, ",", false );
        }
        String[] sections = null;
        if ( kvp.get( "SECTIONS" ) != null ) {
            String tmp = getParam( "SECTIONS", kvp, null );
            sections = StringTools.toArray( tmp, ",", false );
        }
        return new CatalogueGetCapabilities( id, updateSequence, version, acceptVersions, acceptFormats, sections, kvp );

    }

    /**
     * Creates a <code>CatalogGetCapabilities</code> request from its XML representation.
     *
     * @param id
     *            unique ID of the request
     * @param root
     *            XML representation of the request
     * @return created <code>CatalogGetCapabilities</code> object
     * @throws OGCWebServiceException
     *             thrown if something in the request is wrong
     */
    public static CatalogueGetCapabilities create( String id, Element root )
                            throws OGCWebServiceException {

        CatalogueGetCapabilitiesDocument doc = new CatalogueGetCapabilitiesDocument();
        doc.setRootElement( root );
        CatalogueGetCapabilities request;
        try {
            request = doc.parse( id );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            ExceptionCode code = ExceptionCode.INVALID_FORMAT;
            throw new OGCWebServiceException( "CatalogGetCapabilities", e.getMessage(), code );
        }
        return request;
    }

    /**
     * returns WCS as service name
     */
    public String getServiceName() {
        return "CSW";
    }

    /**
     *
     * @param acceptVersions
     * @return the highst supported version or <code>null</code> if none of the passed versions is
     *         supported
     */
    static String validateVersion( String[] acceptVersions ) {
        String version = null;
        for ( int i = 0; i < acceptVersions.length; i++ ) {
            if ( acceptVersions[i].equals( "2.0.0" ) || acceptVersions[i].equals( "2.0.1" )
                 || acceptVersions[i].equals( "2.0.2" ) ) {
                if ( version == null ) {
                    version = acceptVersions[i];
                }
                if ( acceptVersions[i].compareTo( version ) > 0 ) {
                    version = acceptVersions[i];
                }
            }
        }

        return version;
    }
}
