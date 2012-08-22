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
package org.deegree.model.filterencoding;

import java.util.ArrayList;

import org.deegree.model.feature.Feature;

/**
 * Encapsulates the information of a <Filter>element that consists of a number of FeatureId
 * constraints (only) (as defined in the FeatureId DTD).
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class FeatureFilter extends AbstractFilter {

    /** FeatureIds the FeatureFilter is based on */
    private ArrayList<FeatureId> featureIds = new ArrayList<FeatureId>();

    /**
     *
     */
    public FeatureFilter() {
        // it's empty
    }

    /**
     * @param fids
     */
    public FeatureFilter( ArrayList<FeatureId> fids ) {
        this.featureIds = fids;
    }

    /**
     * Adds a FeatureId constraint.
     *
     * @param featureId
     *
     */
    public void addFeatureId( FeatureId featureId ) {
        featureIds.add( featureId );
    }

    /**
     * @return the contained FeatureIds.
     *
     */
    public ArrayList<FeatureId> getFeatureIds() {
        return featureIds;
    }

    /**
     * Calculates the <tt>FeatureFilter</tt>'s logical value based on the ID of the given
     * <tt>Feature</tt>. FIXME!!! Use a TreeSet (or something) to speed up comparison.
     *
     * @param feature
     *            that determines the Id
     * @return true, if the <tt>FeatureFilter</tt> evaluates to true, else false
     * @throws FilterEvaluationException
     *             if the evaluation fails
     */
    public boolean evaluate( Feature feature )
                            throws FilterEvaluationException {
        String id = feature.getId();
        for ( int i = 0; i < featureIds.size(); i++ ) {
            FeatureId featureId = featureIds.get( i );
            if ( id.equals( featureId.getValue() ) )
                return true;
        }
        return false;
    }

    public StringBuffer toXML() {
        StringBuffer sb = new StringBuffer( 500 );
        sb.append( "<ogc:Filter xmlns:ogc='http://www.opengis.net/ogc'>" );
        for ( int i = 0; i < featureIds.size(); i++ ) {
            FeatureId fid = featureIds.get( i );
            sb.append( fid.toXML() );
        }
        sb.append( "</ogc:Filter>" );
        return sb;
    }

    public StringBuffer to100XML() {
        return toXML();
    }

    public StringBuffer to110XML() {
        return toXML();
    }
}
