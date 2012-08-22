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
package org.deegree.security.owsproxy;

import java.util.ArrayList;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.trigger.Trigger;
import org.deegree.framework.util.StringTools;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueGetCapabilities;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSGetCapabilities;
import org.deegree.ogcwebservices.wfs.operation.WFSGetCapabilities;
import org.deegree.ogcwebservices.wmps.operation.WMPSGetCapabilities;
import org.deegree.ogcwebservices.wms.operation.WMSGetCapabilities;
import org.deegree.ogcwebservices.wps.capabilities.WPSGetCapabilities;
import org.deegree.ogcwebservices.wpvs.operation.WPVSGetCapabilities;

/**
 * Trigger for replacing string within requests and responses to OGC Web Service packed behind a
 * owsProxy. At the moment replacements are just supported for responses to GetCapabilities
 * requests.
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class StringReplacementTrigger implements Trigger {

    private static final ILogger LOG = LoggerFactory.getLogger( StringReplacementTrigger.class );

    private String name;

    private String consideredRequests;

    private List<String> replacementsKeys;

    private List<String> replacementsVals;

    /**
     *
     * @param replacements
     *            list of replacement rules "{key1|value1}{key2|value2} ... {keyX|valueX}"
     * @param consideredRequests
     *            comma seperated list of request names to be considerded; e.g.
     *            "WMS:GetFeatureInfo,WMS:GetCapabilities,WFS:GetFeature"
     */
    public StringReplacementTrigger( String replacements, String consideredRequests ) {

        this.consideredRequests = consideredRequests;
        String[] tmp = StringTools.extractStrings( replacements, "{", "}" );
        this.replacementsKeys = new ArrayList<String>( tmp.length );
        this.replacementsVals = new ArrayList<String>( tmp.length );
        for ( int i = 0; i < tmp.length; i++ ) {
            int pos = tmp[i].lastIndexOf( '|' );
            this.replacementsKeys.add( tmp[i].substring( 0, pos ) );
            this.replacementsVals.add( tmp[i].substring( pos + 1, tmp[i].length() ) );
        }
    }

    /**
     *
     * @param caller
     *            instance of calling class
     * @param values
     *            values passed from the calling method
     */
    public Object[] doTrigger( Object caller, Object... values ) {

        if ( values.length == 2 ) {
            values = handleRequestValidation( values );
        } else {
            values = handleResponseValidation( values );
        }
        return values;
    }

    /**
     *
     * @param values
     * @return an array of objects
     */
    private Object[] handleResponseValidation( Object[] values ) {

        // TODO
        // consider personal user rights

        OGCWebServiceRequest request = (OGCWebServiceRequest) values[0];
        byte[] data = (byte[]) values[1];

        if ( ( request instanceof WMSGetCapabilities && consideredRequests.indexOf( "WMS:GetCapabilities" ) > -1 )
             || ( request instanceof WFSGetCapabilities && consideredRequests.indexOf( "WFS:GetCapabilities" ) > -1 )
             || ( request instanceof CatalogueGetCapabilities && consideredRequests.indexOf( "CSW:GetCapabilities" ) > -1 )
             || ( request instanceof WCSGetCapabilities && consideredRequests.indexOf( "WCS:GetCapabilities" ) > -1 )
             || ( request instanceof WMPSGetCapabilities && consideredRequests.indexOf( "WMPS:GetCapabilities" ) > -1 )
             || ( request instanceof WPVSGetCapabilities && consideredRequests.indexOf( "WPVS:GetCapabilities" ) > -1 )
             || ( request instanceof WPSGetCapabilities && consideredRequests.indexOf( "WPS:GetCapabilities" ) > -1 ) ) {
            String tmp = new String( data );
            for ( int i = 0; i < replacementsKeys.size(); i++ ) {
                LOG.logDebug( "response replacement: ", replacementsKeys.get( i ) + " - " + replacementsVals.get( i ) );
                tmp = tmp.replaceAll( replacementsKeys.get( i ), replacementsVals.get( i ) );
            }
            values[1] = tmp.getBytes();
        } else {
            // TODO
            // support replacements within responses to other requests
            LOG.logInfo( "No string replacement implemented for request: ", request.getClass().getName() );
        }

        return values;
    }

    /**
     *
     * @param values
     * @return the values.
     */
    private Object[] handleRequestValidation( Object[] values ) {
        // OGCWebServiceRequest request = (OGCWebServiceRequest)values[0];
        // User user = (User)values[1];

        // TODO
        // consider personal user rights

        // TODO
        // support replacements within requests

        return values;
    }

    /**
     * returns the name of the trigger
     *
     * @return name of the trigger
     */
    public String getName() {
        return name;
    }

    /**
     * sets the name of the trigger
     *
     * @param name
     *            name of the trigger
     */
    public void setName( String name ) {
        this.name = name;
    }

}
