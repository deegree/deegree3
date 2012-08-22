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

package org.deegree.ogcwebservices.wfs;

import static java.lang.Integer.parseInt;
import static java.util.Collections.singleton;
import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.deegree.i18n.Messages.get;

import java.util.ArrayList;
import java.util.Set;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.Datastore;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.filterencoding.FeatureFilter;
import org.deegree.model.filterencoding.FeatureId;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcwebservices.InconsistentRequestException;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wfs.capabilities.FormatType;
import org.deegree.ogcwebservices.wfs.configuration.WFSConfiguration;
import org.deegree.ogcwebservices.wfs.operation.GetGmlObject;
import org.deegree.ogcwebservices.wfs.operation.GmlResult;
import org.deegree.ogcwebservices.wfs.operation.Query;

/**
 * <code>GetGmlObjectHandler</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
class GetGmlObjectHandler {

    private static final ILogger LOG = getLogger( GetGmlObjectHandler.class );

    private WFSConfiguration config;

    GetGmlObjectHandler( WFSConfiguration config ) {
        this.config = config;
    }

    GmlResult handleRequest( GetGmlObject request ) {
        if ( !config.hasUniquePrefixMapping() ) {
            return new GmlResult( request, new InconsistentRequestException( get( "WFS_CONF_FT_PREFICES_NOT_UNIQUE2" ) ) );
        }
        String objectId = request.getObjectId();
        MappedFeatureType type = config.getFeatureType( objectId );

        if ( type == null ) {
            LOG.logDebug( "The GML type could not be determined for the incoming GMLObjectID" );
            return new GmlResult( request, new InvalidParameterValueException( "getgmlobject",
                                                                               get( "WFS_NO_SUCH_FEATURE", objectId ) ) );
        }

        if ( type.isAbstract() ) {
            String msg = Messages.getMessage( "WFS_FEATURE_TYPE_ABSTRACT", type.getName() );
            return new GmlResult( request, new OGCWebServiceException( "getgmlobject", msg ) );
        }
        if ( !type.isVisible() ) {
            String msg = Messages.getMessage( "WFS_FEATURE_TYPE_INVISIBLE", type.getName() );
            return new GmlResult( request, new OGCWebServiceException( "getgmlobject", msg ) );
        }

        int idx = objectId.indexOf( "_GEOM_" );
        int geom = -1;
        if ( idx > 0 ) {
            try {
                geom = parseInt( objectId.substring( idx + 6 ) );
            } catch ( NumberFormatException e ) {
                LOG.logDebug( "Stack trace: ", e );
                return new GmlResult( request, new InvalidParameterValueException( "getgmlobject",
                                                                                   get( "WFS_NO_SUCH_FEATURE",
                                                                                        request.getObjectId() ) ) );
            }
            objectId = objectId.substring( 0, idx );
            LOG.logDebug( "Trying to find geometry object number " + geom );
        }

        LOG.logDebug( "Feature ID: ", objectId );

        Set<FeatureId> set = singleton( new FeatureId( objectId ) );
        Filter filter = new FeatureFilter( new ArrayList<FeatureId>( set ) );

        Datastore ds = type.getGMLSchema().getDatastore();

        Query q = new Query( null, null, null, null, null, new QualifiedName[] { type.getName() }, null, null, filter,
                             null, -1, -1 );
        try {
            FeatureCollection col = ds.performQuery( q, new MappedFeatureType[] { type } );
            if ( col.size() == 0 ) {
                return new GmlResult( request, new InvalidParameterValueException( "getgmlobject",
                                                                                   get( "WFS_NO_SUCH_FEATURE",
                                                                                        request.getObjectId() ) ) );
            }
            Feature feature = col.getFeature( 0 );

            // different formats are not supported anyway, so just use the first one
            FormatType format = config.getFeatureTypeList().getFeatureType( type.getName() ).getOutputFormats()[0];

            if ( geom > -1 ) {
                try {
                    return new GmlResult( request, feature.getGeometryPropertyValues()[geom], format );
                } catch ( ArrayIndexOutOfBoundsException e ) {
                    return new GmlResult( request, new InvalidParameterValueException( "getgmlobject",
                                                                                       get( "WFS_NO_SUCH_FEATURE",
                                                                                            request.getObjectId() ) ) );
                }
            }

            return new GmlResult( request, feature, format );
        } catch ( DatastoreException e ) {
            LOG.logDebug( "Stack trace: ", e );
            return new GmlResult( request, new OGCWebServiceException( "getgmlobject", e.getMessage() ) );
        } catch ( UnknownCRSException e ) {
            LOG.logDebug( "Stack trace: ", e );
            return new GmlResult( request, new OGCWebServiceException( "getgmlobject", e.getMessage() ) );
        }
    }

}
