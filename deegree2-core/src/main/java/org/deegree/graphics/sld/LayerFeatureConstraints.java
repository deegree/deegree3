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
package org.deegree.graphics.sld;

import java.util.ArrayList;
import java.util.List;

import org.deegree.framework.xml.Marshallable;

/**
 * The LayerFeatureConstraints element is optional in a NamedLayer and allows the user to specify
 * constraints on what features of what feature types are to be selected by the named-layer
 * reference. It is essentially a filter that allows the selection of fewer features than are
 * present in the named layer.
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class LayerFeatureConstraints implements Marshallable {
    private List<FeatureTypeConstraint> featureTypeConstraint = null;

    /**
     * constructor initializing the class with the <LayerFeatureConstraints>
     * @param featureTypeConstraint
     */
    public LayerFeatureConstraints( FeatureTypeConstraint[] featureTypeConstraint ) {
        this.featureTypeConstraint = new ArrayList<FeatureTypeConstraint>();
        setFeatureTypeConstraint( featureTypeConstraint );
    }

    /**
     * A FeatureTypeConstraint element is used to identify a feature type by a well-known name,
     * using the FeatureTypeName element.
     *
     * @return the FeatureTypeConstraints as Array
     *
     */
    public FeatureTypeConstraint[] getFeatureTypeConstraint() {
        return featureTypeConstraint.toArray( new FeatureTypeConstraint[featureTypeConstraint.size()] );
    }

    /**
     * sets the <FeatureTypeConstraint>
     *
     * @param featureTypeConstraint
     *            the <FeatureTypeConstraint>
     */
    public void setFeatureTypeConstraint( FeatureTypeConstraint[] featureTypeConstraint ) {
        this.featureTypeConstraint.clear();

        if ( featureTypeConstraint != null ) {
            for ( int i = 0; i < featureTypeConstraint.length; i++ ) {
                this.featureTypeConstraint.add( featureTypeConstraint[i] );
            }
        }
    }

    /**
     * adds the <FeatureTypeConstraint>
     *
     * @param featureTypeConstraint
     *            the <FeatureTypeConstraint>
     */
    public void addFeatureTypeConstraint( FeatureTypeConstraint featureTypeConstraint ) {
        this.featureTypeConstraint.add( featureTypeConstraint );
    }

    /**
     * Removes a FeatureTypeConstraint.
     *
     * @param featureTypeConstraint
     *            the <FeatureTypeConstraint>
     */
    public void removeFeatureTypeConstraint( FeatureTypeConstraint featureTypeConstraint ) {
        this.featureTypeConstraint.remove( this.featureTypeConstraint.indexOf( featureTypeConstraint ) );
    }

    /**
     * returns the LayerFeatureConstraints as String.
     *
     * @return the LayerFeatureConstraints as String
     */
    @Override
    public String toString() {
        String ret = getClass().getName() + "\n";
        ret = "featureTypeConstraint = " + featureTypeConstraint + "\n";

        return ret;
    }

    /**
     * exports the content of the Font as XML formated String
     *
     * @return xml representation of the Font
     */
    public String exportAsXML() {

        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "<LayerFeatureConstraints>" );
        for ( int i = 0; i < featureTypeConstraint.size(); i++ ) {
            sb.append( ( (Marshallable) featureTypeConstraint.get( i ) ).exportAsXML() );
        }
        sb.append( "</LayerFeatureConstraints>" );

        return sb.toString();
    }

}
