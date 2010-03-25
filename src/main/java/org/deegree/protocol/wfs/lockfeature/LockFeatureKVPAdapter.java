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

package org.deegree.protocol.wfs.lockfeature;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.CRS;
import org.deegree.filter.Filter;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.protocol.i18n.Messages;
import org.deegree.protocol.wfs.AbstractWFSRequestKVPAdapter;
import org.deegree.protocol.wfs.getfeature.TypeName;

/**
 * Adapter between KVP <code>LockFeature</code> requests and {@link LockFeature} objects.
 * <p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class LockFeatureKVPAdapter extends AbstractWFSRequestKVPAdapter {

    /**
     * Parses a normalized KVP-map as a WFS {@link LockFeature} request.
     * <p>
     * Supported versions:
     * <ul>
     * <li>WFS 1.0.0</li>
     * <li>WFS 1.1.0</li>
     * </ul>
     * 
     * @param kvpParams
     *            normalized KVP-map; keys must be uppercase, each key only has one associated value
     * @return parsed {@link LockFeature} request
     * @throws Exception
     */
    public static LockFeature parse( Map<String, String> kvpParams )
                            throws Exception {

        Version version = Version.parseVersion( KVPUtils.getRequired( kvpParams, "VERSION" ) );

        LockFeature result = null;
        // if ( VERSION_100.equals( version ) ) {
        // result = parse100( kvpParams );
        // } else
        if ( VERSION_110.equals( version ) ) {
            result = parse110( kvpParams );
            // } else if ( VERSION_200.equals( version ) ) {
            // result = parse200( kvpParams );
        } else {
            String msg = Messages.get( "UNSUPPORTED_VERSION", version, Version.getVersionsString( VERSION_110 ) );
            throw new InvalidParameterValueException( msg );
        }
        return result;
    }

    @SuppressWarnings("boxing")
    private static LockFeature parse110( Map<String, String> kvpParams )
                            throws Exception {
        // optional: 'NAMESPACE'
        Map<String, String> nsBindings = extractNamespaceBindings( kvpParams );

        NamespaceContext nsContext = new NamespaceContext();
        if ( nsBindings != null ) {
            for ( String key : nsBindings.keySet() ) {
                nsContext.addNamespace( key, nsBindings.get( key ) );
            }
        }

        // optional: EXPIRY
        String expiryStr = kvpParams.get( "EXPIRY" );
        Integer expiry = null;
        if ( expiryStr != null ) {
            expiry = Integer.parseInt( expiryStr );
        }

        // optional: LOCKACTION
        Boolean lockAll = true;
        String lockStr = kvpParams.get( "LOCKACTION" );
        if ( lockStr != null ) {
            lockAll = !lockStr.equals( "SOME" );
        }

        // mandatory: TYPENAME, but optional if FEATUREID is specified
        String typeStrList = kvpParams.get( "TYPENAME" );
        TypeName[] typeNames = getTypeNames( typeStrList, nsBindings );

        // optional: FEATUREID
        String featureIdStr = kvpParams.get( "FEATUREID" );
        String[] featureIds = null;
        if ( featureIdStr != null ) {
            featureIds = featureIdStr.split( "," );
        }
        // optional: BBOX
        String bboxStr = kvpParams.get( "BBOX" );

        // optional: FILTER
        String filterStr = kvpParams.get( "FILTER" );

        if ( ( featureIdStr != null && bboxStr != null ) || ( featureIdStr != null && filterStr != null )
             || ( bboxStr != null && filterStr != null ) ) {
            // TODO make new exception
            throw new Exception( "The FEATUREID, BBOX and FILTER keywords are mutually exclusive!" );
        }

        if ( featureIdStr != null ) {

            FeatureIdLock[] featureIdLocks = new FeatureIdLock[1];
            featureIdLocks[0] = new FeatureIdLock( featureIds, typeNames );

            return new LockFeature( VERSION_110, null, featureIdLocks, expiry, lockAll );
        }

        if ( bboxStr != null ) {
            if ( typeNames == null ) {
                // TODO make new exception
                throw new Exception( "The TYPENAME keyword is mandatory if BBOX is present!" );
            }

            String[] coordList = bboxStr.split( "," );
            CRS srs = null; // TODO should this be EPSG:4326 or WGS:84 by default ??
            if ( coordList.length % 2 == 1 ) {
                srs = new CRS( coordList[coordList.length - 1] );
            }

            Envelope bbox = createEnvelope( bboxStr, srs );

            BBoxLock[] bboxLocks = new BBoxLock[1];
            bboxLocks[0] = new BBoxLock( bbox, typeNames );
            return new LockFeature( VERSION_110, null, bboxLocks, expiry, lockAll );
        }

        if ( filterStr != null || typeNames != null ) {
            if ( typeNames == null ) {
                // TODO make new exception
                throw new Exception( "The FILTER element requires the TYPENAME element" );
            }

            int length = typeNames.length;

            String[] filters = getFilters( filterStr );

            FilterLock[] filterLocks = new FilterLock[length];

            for ( int i = 0; i < length; i++ ) {
                Filter filter = null;
                if ( filters != null ) {

                    StringReader sr = new StringReader( filters[i] );
                    XMLAdapter adapter = new XMLAdapter( sr );
                    XMLStreamReaderWrapper streamWrapper = new XMLStreamReaderWrapper(
                                                                                       adapter.getRootElement().getXMLStreamReaderWithoutCaching(),
                                                                                       adapter.getSystemId() );
                    try {
                        streamWrapper.nextTag();
                        filter = Filter110XMLDecoder.parse( streamWrapper );
                    } catch ( XMLParsingException e ) {
                        e.printStackTrace();
                        // TODO raise exception
                    } catch ( XMLStreamException e ) {
                        e.printStackTrace();
                        // TODO raise exception
                    }
                }
                filterLocks[i] = new FilterLock( null, typeNames[i], filter );
            }
            return new LockFeature( VERSION_110, null, filterLocks, expiry, lockAll );

        }
        return null;
    }

    private static String[] getFilters( String filterStr ) {
        String[] filters = null;
        if ( filterStr != null ) {
            filters = filterStr.split( "[)][(]" );
            if ( filters[0].startsWith( "(" ) ) {
                filters[0] = filters[0].substring( 1 );
            }

            String last = filters[filters.length - 1];
            if ( last.endsWith( ")" ) ) {
                filters[filters.length - 1] = last.substring( 0, last.length() - 1 );
            }
        }
        return filters;
    }

    @SuppressWarnings("boxing")
    private static Envelope createEnvelope( String bboxStr, CRS srs ) {
        String[] coordList = bboxStr.split( "," );

        int n = coordList.length / 2;
        List<Double> lowerCorner = new ArrayList<Double>();
        for ( int i = 0; i < n; i++ ) {
            lowerCorner.add( Double.parseDouble( coordList[i] ) );
        }
        List<Double> upperCorner = new ArrayList<Double>();
        for ( int i = n; i < 2 * n; i++ ) {
            upperCorner.add( Double.parseDouble( coordList[i] ) );
        }

        GeometryFactory gf = new GeometryFactory();

        return gf.createEnvelope( lowerCorner, upperCorner, srs );
    }

    private static TypeName[] getTypeNames( String typeStrList, Map<String, String> nsBindings ) {
        TypeName[] result = null;
        if ( typeStrList != null ) {

            String[] typeList = typeStrList.split( "," );
            result = new TypeName[typeList.length];

            for ( int i = 0; i < typeList.length; i++ ) {
                String[] typeParts = typeList[i].split( ":" );
                if ( typeParts.length == 2 ) {

                    // check if it has an alias
                    int equalSign;
                    if ( ( equalSign = typeParts[1].indexOf( "=" ) ) != -1 ) {
                        result[i] = new TypeName(
                                                  new QName( nsBindings.get( typeParts[0] ), typeParts[1], typeParts[0] ),
                                                  typeParts[1].substring( equalSign + 1 ) );
                    } else {
                        result[i] = new TypeName(
                                                  new QName( nsBindings.get( typeParts[0] ), typeParts[1], typeParts[0] ),
                                                  null );
                    }
                } else {
                    result[i] = new TypeName( new QName( typeParts[0] ), null );
                }
            }
        }
        return result;
    }
}
