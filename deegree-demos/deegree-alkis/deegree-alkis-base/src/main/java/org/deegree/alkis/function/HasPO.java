//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.alkis.function;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashSet;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.feature.persistence.memory.MemoryFeatureStore;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.Function;
import org.deegree.filter.function.FunctionProvider;
import org.deegree.gml.feature.FeatureReference;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class HasPO implements FunctionProvider {

    private static final Logger LOG = getLogger( HasPO.class );

    private static final String NAME = "HasPO";

    static final HashSet<Feature> featuresWithPO = new HashSet<Feature>();

    private static String ns511 = "http://www.adv-online.de/namespaces/adv/gid/5.1.1";

    private static String ns601 = "http://www.adv-online.de/namespaces/adv/gid/6.0";

    private static MemoryFeatureStore featureStore;

    private static boolean is511;

    private static TypeName[] interestingProps511 = new TypeName[] {
                                                                    new TypeName( new QName( ns511, "AP_Darstellung" ),
                                                                                  null ),
                                                                    new TypeName( new QName( ns511, "AP_LTO" ), null ),
                                                                    new TypeName( new QName( ns511, "AP_PTO" ), null ),
                                                                    new TypeName( new QName( ns511, "AP_FTO" ), null ),
                                                                    new TypeName( new QName( ns511, "AP_LTO" ), null ),
                                                                    new TypeName( new QName( ns511, "AP_PPO" ), null ) };

    // use the same as for 511, 3d stuff is not supported anyway
    private static TypeName[] interestingProps601 = new TypeName[] {
                                                                    new TypeName( new QName( ns601, "AP_Darstellung" ),
                                                                                  null ),
                                                                    new TypeName( new QName( ns601, "AP_LTO" ), null ),
                                                                    new TypeName( new QName( ns601, "AP_PTO" ), null ),
                                                                    new TypeName( new QName( ns601, "AP_FTO" ), null ),
                                                                    new TypeName( new QName( ns601, "AP_LTO" ), null ),
                                                                    new TypeName( new QName( ns601, "AP_PPO" ), null ) };

    static {
        QName gemeinde511 = new QName( ns511, "AX_Gemeinde" );
        QName gemeinde601 = new QName( ns601, "AX_Gemeinde" );
        for ( FeatureStore store : FeatureStoreManager.getAll() ) {
            ApplicationSchema schema = store.getSchema();
            if ( store instanceof MemoryFeatureStore ) {
                is511 = schema.getFeatureType( gemeinde511 ) != null;
                if ( is511 || schema.getFeatureType( gemeinde601 ) != null ) {
                    featureStore = (MemoryFeatureStore) store;
                }

            }
        }
    }

    /**
     * Update index. Must be called after each transaction.
     */
    public static void update() {
        if ( featureStore == null ) {
            return;
        }

        featuresWithPO.clear();
        
        try {
            FeatureResultSet col = featureStore.query( new Query( is511 ? interestingProps511 : interestingProps601,
                                                                  null, null, null, null ) );
            QName name = new QName( is511 ? ns511 : ns601, "dientZurDarstellungVon" );
            for ( Feature f : col ) {
                for ( Property p : f.getProperties( name ) ) {
                    if ( p != null && p.getType() instanceof FeaturePropertyType ) {
                        FeatureReference ref = (FeatureReference) p.getValue();
                        if ( ref.isResolved() ) {
                            featuresWithPO.add( ref.getReferencedObject() );
                        }
                    }
                }
            }
        } catch ( FeatureStoreException e ) {
            LOG.warn( "Could not update the HasPO index: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } catch ( FilterEvaluationException e ) {
            LOG.warn( "Could not update the HasPO index: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Function create( List<Expression> params ) {
        return new Function( NAME, params ) {
            @Override
            public <T> TypedObjectNode[] evaluate( T obj, XPathEvaluator<T> xpathEvaluator )
                                    throws FilterEvaluationException {
                return new TypedObjectNode[] { new PrimitiveValue( "" + featuresWithPO.contains( obj ) ) };
            }
        };
    }

}
