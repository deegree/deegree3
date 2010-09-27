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
package org.deegree.feature;

import java.util.ArrayList;
import java.util.List;

import org.deegree.feature.types.FeatureCollectionType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.geometry.Envelope;

/**
 * Abstract base class for common {@link FeatureCollection} implementations.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public abstract class AbstractFeatureCollection extends AbstractFeature implements FeatureCollection {

    private boolean envelopeCalculated = false;

    /**
     * Creates a new {@link AbstractFeatureCollection} instance.
     * 
     * @param fid
     *            feature id or <code>null</code> if the feature is anonymous (discouraged for most use cases)
     * @param ft
     *            feature type, must not be <code>null</code>
     */
    protected AbstractFeatureCollection( String fid, FeatureCollectionType ft ) {
        super( fid, ft );
    }

    @Override
    public FeatureCollectionType getType() {
        return (FeatureCollectionType) ft;
    }

    @Override
    public FeatureCollection getMembers( Filter filter, XPathEvaluator<Feature> evaluator )
                            throws FilterEvaluationException {

        List<Feature> matchingFeatures = new ArrayList<Feature>();
        for ( Feature feature : this ) {
            if ( filter.evaluate( feature, evaluator ) ) {
                matchingFeatures.add( feature );
            }
        }
        return new GenericFeatureCollection( null, matchingFeatures );
    }

    @Override
    public Envelope getEnvelope() {
        if ( envelopeCalculated ) {
            return standardProps.getBoundedBy();
        }
        envelopeCalculated = true;
        return super.getEnvelope();
    }

    @Override
    protected Envelope calcEnvelope() {
        Envelope fcBBox = null;
        for ( Feature feature : this ) {
            Envelope memberBBox = feature.getEnvelope();
            if ( memberBBox != null ) {
                if ( fcBBox != null ) {
                    fcBBox = fcBBox.merge( memberBBox );
                } else {
                    fcBBox = memberBBox;
                }
            }
        }
        return fcBBox;
    }
}