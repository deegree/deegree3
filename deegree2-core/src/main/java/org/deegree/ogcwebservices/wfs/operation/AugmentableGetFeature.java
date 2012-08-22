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
package org.deegree.ogcwebservices.wfs.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.model.filterencoding.FeatureFilter;
import org.deegree.model.filterencoding.FeatureId;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathFactory;
import org.deegree.ogcwebservices.InconsistentRequestException;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.wfs.XMLFactory;
import org.deegree.ogcwebservices.wfs.configuration.WFSConfiguration;

/**
 * "Proxy" GetFeature request object, only created by {@link GetFeature#create(Map)} if the FEATUREID parameter is used
 * without TYPENAME parameter.
 * <p>
 * It is augmented with information from the {@link WFSConfiguration} to retrieve the names of the associated feature
 * types.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class AugmentableGetFeature extends GetFeature {

    private static final long serialVersionUID = -3001702206522611997L;

    private static final ILogger LOG = LoggerFactory.getLogger( AugmentableGetFeature.class );

    private Map<String, String> kvp;

    /**
     * Creates a new <code>AugmentableGetFeature</code> instance.
     *
     * @param version
     *            request version
     * @param id
     *            id of the request
     * @param handle
     * @param resultType
     *            desired result type (results | hits)
     * @param outputFormat
     *            requested result format
     * @param maxFeatures
     * @param startPosition
     *            deegree specific parameter defining where to start considering features
     * @param traverseXLinkDepth
     * @param traverseXLinkExpiry
     * @param queries
     * @param vendorSpecificParam
     */
    AugmentableGetFeature( String version, String id, String handle, RESULT_TYPE resultType, String outputFormat,
                           int maxFeatures, int startPosition, int traverseXLinkDepth, int traverseXLinkExpiry,
                           Query[] queries, Map<String, String> vendorSpecificParam ) {
        super( version, id, handle, resultType, outputFormat, maxFeatures, startPosition, traverseXLinkDepth,
               traverseXLinkExpiry, queries, vendorSpecificParam );
        this.kvp = vendorSpecificParam;
    }

    /**
     * Augments the KVP request with the needed information from the {@link WFSConfiguration}.
     *
     * @param config
     * @throws InconsistentRequestException
     * @throws InvalidParameterValueException
     */
    public void augment( WFSConfiguration config )
                            throws InconsistentRequestException, InvalidParameterValueException {

        // SRSNAME
        String srsName = kvp.get( "SRSNAME" );

        // FEATUREVERSION
        String featureVersion = kvp.get( "FEATUREVERSION" );

        // TYPENAME
        QualifiedName[] typeNames = extractTypeNames( kvp );
        if ( typeNames.length == 0 ) {
            // FEATUREID must be given
            String featureId = kvp.get( "FEATUREID" );
            if ( featureId == null ) {
                String msg = Messages.getMessage( "WFS_TYPENAME+FID_PARAMS_MISSING" );
                throw new InvalidParameterValueException( msg );
            }

            // ensure that the WFS has unique feature prefixes
            if ( !config.hasUniquePrefixMapping() ) {
                String msg = Messages.get( "WFS_CONF_FT_PREFICES_NOT_UNIQUE" );
                throw new InconsistentRequestException( msg );
            }

            // maps feature type to ids that must be fetched for it
            Map<MappedFeatureType, ArrayList<FeatureId>> ftToFids = new HashMap<MappedFeatureType, ArrayList<FeatureId>>();
            String[] featureIds = featureId.split( "," );
            for ( String fid : featureIds ) {
                MappedFeatureType ft = config.getFeatureType( fid );
                if ( ft == null ) {
                    String msg = Messages.get( "WFS_CONF_FT_PREFIX_UNKNOWN", fid );
                    throw new InconsistentRequestException( msg );
                }
                ArrayList<FeatureId> fidList = ftToFids.get( ft );
                if ( fidList == null ) {
                    fidList = new ArrayList<FeatureId>();
                }
                fidList.add( new FeatureId( fid ) );
                ftToFids.put( ft, fidList );
            }

            // build a Query instance for each requested feature type
            queries = new ArrayList<Query>( ftToFids.size() );
            for ( MappedFeatureType ft : ftToFids.keySet() ) {
                QualifiedName ftName = ft.getName();
                ArrayList<FeatureId> fids = ftToFids.get( ft );
                Filter filter = new FeatureFilter( fids );
                QualifiedName[] ftNames = new QualifiedName[] { ftName };
                PropertyPath path = PropertyPathFactory.createPropertyPath( ftName );
                PropertyPath[] properties = new PropertyPath[] { path };
                queries.add( new Query( properties, null, null, null, featureVersion, ftNames, null, srsName, filter,
                                        resultType, maxFeatures, startPosition ) );
            }
        }

        if ( LOG.isDebug() ) {
            try {
                GetFeatureDocument doc = XMLFactory.export( this );
                doc.prettyPrint( System.out );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }
}
