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

package org.deegree.ogcwebservices.wcts.operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.deegree.i18n.Messages;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.OGCWebServiceException;

/**
 * <code>WCTSGetCapabilities</code> bean representation of a GetCapabilities request, xml-dom or kvp encoded.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class WCTSGetCapabilities extends WCTSRequestBase {

    private static final long serialVersionUID = 6951749192378539154L;

    private final String updateSequence;

    private final List<String> acceptedVersions;

    private final List<String> sections;

    private final List<String> acceptedFormats;

    /**
     * @param id
     *            of the request
     * @param updateSequence
     * @param acceptedVersions
     * @param sections
     * @param acceptedFormats
     */
    public WCTSGetCapabilities( String id, String updateSequence, List<String> acceptedVersions, List<String> sections,
                                List<String> acceptedFormats ) {
        super( id, null );
        if ( updateSequence == null ) {
            updateSequence = new String();
        }
        if ( acceptedVersions == null ) {
            acceptedVersions = new ArrayList<String>();
        }
        if ( sections == null ) {
            sections = new ArrayList<String>();
        } else {
            String[] secs = new String[sections.size()];
            sections.toArray( secs );
            for( int i = 0; i< secs.length; ++i ){
                if( secs[i] != null ){
                    secs[i] = secs[i].toLowerCase();
                }
            }
            sections.clear();
            sections = Arrays.asList( secs );
        }
        if ( acceptedFormats == null ) {
            acceptedFormats = new ArrayList<String>();
        }
        this.updateSequence = updateSequence;
        this.acceptedVersions = acceptedVersions;
        this.sections = sections;

        this.acceptedFormats = acceptedFormats;
    }

    /**
     * @return the updateSequence, can be empty but never <code>null</code>
     */
    public final String getUpdateSequence() {
        return updateSequence;
    }

    /**
     * @return the acceptedVersions, can be empty but never <code>null</code>
     */
    public final List<String> getAcceptedVersions() {
        return acceptedVersions;
    }

    /**
     * @return the sections, can be empty but never <code>null</code>, all sections are lower cased.
     */
    public final List<String> getSections() {
        return sections;
    }

    /**
     * @return the acceptedFormats, can be empty but never <code>null</code>
     */
    public final List<String> getAcceptedFormats() {
        return acceptedFormats;
    }

    /**
     * Create a {@link WCTSGetCapabilities}-request by extracting the values from the map, and calling the constructor
     * with these values.
     *
     * @param requestID
     *            service internal id for this request.
     * @param map
     *            to extract requested values from.
     * @return the bean representation
     * @throws OGCWebServiceException
     *             if the map is <code>null</code> or has size==0, or the service,request parameters have none
     *             accepted values.
     */
    public static WCTSGetCapabilities create( String requestID, Map<String, String> map )
                                                                                         throws OGCWebServiceException {
        if ( map == null || map.size() == 0 ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_REQUESTMAP_NULL" ),
                                              ExceptionCode.MISSINGPARAMETERVALUE );
        }
        String service = map.get( "SERVICE" );
        if ( service == null || !"WCTS".equals( service ) ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_NO_VERSION_KVP", service ),
                                              ExceptionCode.MISSINGPARAMETERVALUE );
        }
        String request = map.get( "REQUEST" );
        if ( request == null || !"getcapabilities".equalsIgnoreCase( request ) ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_NO_REQUEST_KVP", "GetCapabilities" ),
                                              ( request == null ? ExceptionCode.MISSINGPARAMETERVALUE
                                                               : ExceptionCode.OPERATIONNOTSUPPORTED ) );
        }

        String tmp = map.get( "ACCEPTVERSIONS" );
        List<String> acceptedVersions = new ArrayList<String>(10);
        if( tmp != null && !"".equals( tmp.trim() ) ){
            String[] splitter = tmp.split( ",");
            for( String split : splitter ){
                if( split != null && !"".equals( split.trim() ) ){
                    acceptedVersions.add( split.trim() );
                }
            }
        }

        tmp = map.get( "SECTIONS" );
        List<String> sections = new ArrayList<String>(10);
        if( tmp != null && !"".equals( tmp.trim() ) ){
            String[] splitter = tmp.split( ",");
            for( String split : splitter ){
                if( split != null && !"".equals( split.trim() ) ){
                    sections.add( split.trim() );
                }
            }
        }

        tmp = map.get( "ACCEPTFORMATS" );
        List<String> acceptedFormats = new ArrayList<String>(10);
        if( tmp != null && !"".equals( tmp.trim() ) ){
            String[] splitter = tmp.split( ",");
            for( String split : splitter ){
                if( split != null && !"".equals( split.trim() ) ){
                    acceptedFormats.add( split.trim() );
                }
            }

        }

        String updateSequence = map.get( "UPDATESEQUENCE" );

        return new WCTSGetCapabilities( requestID, updateSequence, acceptedVersions, sections, acceptedFormats );
    }

}
