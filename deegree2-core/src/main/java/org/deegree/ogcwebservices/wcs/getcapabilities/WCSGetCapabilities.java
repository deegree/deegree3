// $HeadURL$
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
package org.deegree.ogcwebservices.wcs.getcapabilities;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.KVP2Map;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.getcapabilities.GetCapabilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Each Web Coverage Server must describe its capabilities. This clause defines the structure
 * intended to convey general information about the service itself, and summary information about
 * the available data collections from which coverages may be requested.<p/> An instance of
 * <tt>WCSGetCapabilities</tt> encapsulates a GetCapabilites request against a WCS and offeres two
 * factory methods inherited from <tT>GetCapabilities</tt> for request creation using KVP and one
 * own method for request creation from a DOM object.
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class WCSGetCapabilities extends GetCapabilities {

    private static final ILogger LOG = LoggerFactory.getLogger( WCSGetCapabilities.class );

    /**
     * creates a GetCapabilities request from its KVP representation
     *
     * @param id
     *            unique ID of the request
     * @param kvp
     *            request
     * @return created <tt>DescribeCoverage</tt>
     * @throws OGCWebServiceException
     *             will be thrown if something general is wrong
     * @throws InvalidParameterValueException
     * @throws MissingParameterValueException
     */
    public static GetCapabilities create( String id, String kvp )
                            throws OGCWebServiceException, InvalidParameterValueException,
                            MissingParameterValueException {
        Map<String, String> map = KVP2Map.toMap( kvp );
        map.put( "ID", id );
        return create( map );
    }

    /**
     * creates a GetCapabilities request from its KVP representation
     *
     * @param map
     *            request
     * @return created <tt>DescribeCoverage</tt>
     * @throws OGCWebServiceException
     *             will be thrown if something general is wrong
     * @throws InvalidParameterValueException
     * @throws MissingParameterValueException
     */
    public static GetCapabilities create( Map<String, String> map )
                            throws OGCWebServiceException, InvalidParameterValueException,
                            MissingParameterValueException {

        String version = getParam( "VERSION", map, "1.0.0" );
        /*
         * if ( version == null ) { version = "1.0.0"; } if ( version.compareTo("1.0.0") < 0 ) {
         * ExceptionCode code = ExceptionCode.INVALIDPARAMETERVALUE; throw new
         * InvalidParameterValueException( "WCSGetCapabilities", "version must be equal to " + "or
         * greater than 1.0.0", code ); } else { version = "1.0.0"; }
         */

        String service = getRequiredParam( "SERVICE", map );
        if ( !service.equals( "WCS" ) ) {
            ExceptionCode code = ExceptionCode.INVALIDPARAMETERVALUE;
            throw new InvalidParameterValueException( "WCSGetCapabilities", "'service' must be 'WCS'", code );
        }
        String updateSeq = getParam( "UPDATESEQUENCE", map, null );
        String tmp = getParam( "SECTION", map, null );
        String[] sections = null;
        if ( tmp != null ) {
            sections = StringTools.toArray( tmp, ",", true );
            if ( !validateSection( sections ) ) {
                ExceptionCode code = ExceptionCode.INVALIDPARAMETERVALUE;
                throw new InvalidParameterValueException( "WCSGetCapabilities", "invalid value for section parameter",
                                                          code );
            }
        }
        String id = getParam( "ID", map, "" + System.currentTimeMillis() );

        return new WCSGetCapabilities( id, version, updateSeq, sections, map );
    }

    /**
     * creates a GetCapabilities request from its XML representation
     *
     * @param id
     *            unique ID of the request
     * @param doc
     *            XML representation of the request
     * @return created <tt>DescribeCoverage</tt>
     * @throws OGCWebServiceException
     *             will be thrown if something general is wrong
     * @throws InvalidParameterValueException
     * @throws MissingParameterValueException
     */
    public static GetCapabilities create( String id, Document doc )
                            throws OGCWebServiceException, InvalidParameterValueException,
                            MissingParameterValueException {
        String version = null;
        String service = null;
        String updateSeq = null;
        String[] sections = null;
        try {
            Element root = XMLTools.getRequiredChildElement( "GetCapabilities", CommonNamespaces.WCSNS, doc );

            version = XMLTools.getAttrValue( root, null, "version", "1.0.0" );

            service = XMLTools.getAttrValue( root, null, "service", null );
            if ( service == null ) {
                ExceptionCode code = ExceptionCode.MISSINGPARAMETERVALUE;
                throw new MissingParameterValueException( "WCSGetCapabilities", "'service' is missing", code );
            } else if ( !service.equals( "WCS" ) ) {
                ExceptionCode code = ExceptionCode.INVALIDPARAMETERVALUE;
                throw new InvalidParameterValueException( "WCSGetCapabilities", "'service' must be 'WCS'", code );
            }

            updateSeq = XMLTools.getAttrValue( root, null, "updateSequence", null );

            String tmp = XMLTools.getStringValue( "section", CommonNamespaces.WCSNS, root, "/" );
            if ( tmp != null ) {
                sections = StringTools.toArray( tmp, ",", true );
                if ( !validateSection( sections ) ) {
                    ExceptionCode code = ExceptionCode.INVALIDPARAMETERVALUE;
                    throw new InvalidParameterValueException( "WCSGetCapabilities",
                                                              "invalid value for section parameter", code );
                }
            }

        } catch ( XMLParsingException e ) {
            LOG.logError( "WCSGetCapabilities", e );
            ExceptionCode code = ExceptionCode.INVALID_FORMAT;
            throw new OGCWebServiceException( "WCSGetCapabilities", e.getMessage(), code );
        }

        return new WCSGetCapabilities( id, version, updateSeq, sections, null );
    }

    /**
     * valid values are:
     * <ul>
     * <li>null
     * <li>/
     * <li>/WCS_Capabilities/CapabilitiesService
     * <li>/WCS_Capabilities/Capabilitiy
     * <li>/WCS_Capabilities/ContentMetadata
     * </ul>
     *
     * @param sections
     */
    private static boolean validateSection( String[] sections ) {
        if ( sections == null )
            return false;
        for ( int i = 0; i < sections.length; i++ ) {
            try {
                sections[i] = URLDecoder.decode( sections[i], CharsetUtils.getSystemCharset() );
            } catch ( UnsupportedEncodingException e ) {
                e.printStackTrace();
            }
            if ( sections[i] != null && !"/".equals( sections[i] ) && !"/WCS_Capabilities/Service".equals( sections[i] )
                 && !"/WCS_Capabilities/Capability".equals( sections[i] )
                 && !"/WCS_Capabilities/ContentMetadata".equals( sections[i] ) ) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param id
     * @param version
     * @param updateSequence
     * @param sections
     * @param vendoreSpec
     */
    public WCSGetCapabilities( String id, String version, String updateSequence, String[] sections,
                               Map<String, String> vendoreSpec ) {
        super( id, version, updateSequence, null, sections, null, vendoreSpec );
    }

    /**
     * returns WCS as service name
     */
    public String getServiceName() {
        return "WCS";
    }
}
