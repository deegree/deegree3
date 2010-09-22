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
package org.deegree.feature.persistence.query;

import static org.deegree.gml.GMLVersion.GML_31;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.xpath.FeatureXPathEvaluator;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;

/**
 * {@link FeatureResultSet} that is derived by filtering another {@link FeatureCollection}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FilteredFeatureResultSet implements FeatureResultSet {

    private FeatureResultSet rs;

    private Filter filter;

    /**
     * Creates a new {@link FilteredFeatureResultSet} that is backed by the given {@link FeatureResultSet}.
     * 
     * @param rs
     *            FeatureResultSet to back the result set, must not be <code>null</code>
     * @param filter
     *            filter, must not be <code>null</code>
     */
    public FilteredFeatureResultSet( FeatureResultSet rs, Filter filter ) {
        this.rs = rs;
        this.filter = filter;
    }

    @Override
    public void close() {
        rs.close();
    }

    @Override
    public FeatureCollection toCollection() {
        List<Feature> members = new ArrayList<Feature>();
        for ( Feature feature : this ) {
            members.add( feature );
        }
        close();
        return new GenericFeatureCollection( null, members );
    }

    @Override
    public Iterator<Feature> iterator() {
        return new Iterator<Feature>() {

            // TODO
            FeatureXPathEvaluator evaluator = new FeatureXPathEvaluator( GML_31 );

            Iterator<Feature> iter = rs.iterator();

            boolean nextCalled = true;

            Feature next = null;

            @Override
            public boolean hasNext() {
                if ( !nextCalled ) {
                    return next != null;
                }
                next = null;
                while ( iter.hasNext() ) {
                    Feature candidate = iter.next();
                    try {
                        if ( filter.evaluate( candidate, evaluator ) ) {
                            nextCalled = false;
                            next = candidate;
                            break;
                        }
                    } catch ( FilterEvaluationException e ) {
                        throw new RuntimeException( e.getMessage(), e );
                    }
                }
                return next != null;
            }

            @Override
            public Feature next() {
                if ( !hasNext() ) {
                    throw new NoSuchElementException();
                }
                nextCalled = true;
                return next;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
