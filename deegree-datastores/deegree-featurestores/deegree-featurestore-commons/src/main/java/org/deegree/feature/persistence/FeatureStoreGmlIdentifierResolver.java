//$HeadURL: svn+ssh://svn.wald.intevation.org/deegree/deegree3/trunk/deegree-datastores/deegree-featurestores/deegree-featurestore-commons/src/main/java/org/deegree/feature/persistence/FeatureStoreGMLIdResolver.java $
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
package org.deegree.feature.persistence;

import static org.deegree.filter.MatchAction.ANY;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.GMLReferenceResolver;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.filter.Filter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.protocol.wfs.getfeature.TypeName;

/**
 * {@link GMLReferenceResolver} that uses a {@link FeatureStore} for resolving local object references by using the
 * gml:identifier (GML 3.2 only) property instead of the GML id. Currently supports urn:uuid: identifiers only.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31641 $, $Date: 2011-08-24 21:24:55 +0200 (Wed, 24 Aug 2011) $
 */
public class FeatureStoreGmlIdentifierResolver implements GMLReferenceResolver {

    private final FeatureStore fs;

    private final Pattern pattern = Pattern.compile( "urn\\:uuid\\:(.+)" );

    private final QName identifierName = new QName( "http://www.opengis.net/gml/3.2", "identifier" );

    /**
     * Creates a new {@link FeatureStoreGmlIdentifierResolver} instance.
     * 
     * @param fs
     *            feature store to be used for retrieving local features, must not be <code>null</code>
     */
    public FeatureStoreGmlIdentifierResolver( final FeatureStore fs ) {
        this.fs = fs;
    }

    @Override
    public GMLObject getObject( String uri, String baseURL ) {
        final Matcher m = pattern.matcher( uri );
        if ( m.find() ) {
            final String id = m.group( 1 );
            final ValueReference ref = new ValueReference( identifierName );
            final PropertyIsEqualTo eq = new PropertyIsEqualTo( ref, new Literal<PrimitiveValue>( id ), true, ANY );
            final Filter filter = new OperatorFilter( eq );
            final Query query = new Query( new TypeName[] {}, filter, null, null, null );
            FeatureInputStream rs = null;
            try {
                rs = fs.query( query );
                return rs.iterator().next();
            } catch ( Throwable e ) {
                // then it's not resolvable (?)
            } finally {
                try {
                    rs.close();
                } catch ( Throwable e ) {
                    // not closable
                }
            }
        }
        return null;
    }
}
