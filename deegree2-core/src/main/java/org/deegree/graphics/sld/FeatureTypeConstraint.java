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

import static org.deegree.framework.xml.XMLTools.escape;

import java.util.ArrayList;
import java.util.List;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.Marshallable;
import org.deegree.model.filterencoding.Filter;

/**
 * A FeatureTypeConstraint element is used to identify a feature type by well-known name, using the FeatureTypeName
 * element.
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @version $Revision$ $Date$
 */

public class FeatureTypeConstraint implements Marshallable {

    private List<Extent> extents = null;

    private Filter filter = null;

    private QualifiedName featureTypeName = null;

    /**
     * constructor initializing the class with the <FeatureTypeConstraint>
     *
     * @param featureTypeName
     * @param filter
     * @param extents
     */
    public FeatureTypeConstraint( QualifiedName featureTypeName, Filter filter, Extent[] extents ) {
        if ( extents != null ) {
            this.extents = new ArrayList<Extent>( extents.length );
            setExtents( extents );
        }
        setFeatureTypeName( featureTypeName );
        setFilter( filter );

    }

    /**
     * returns the name of the feature type
     *
     * @return the name of the feature type
     */
    public QualifiedName getFeatureTypeName() {
        return featureTypeName;
    }

    /**
     * sets the name of the feature type
     *
     * @param featureTypeName
     *            the name of the feature type
     */
    public void setFeatureTypeName( QualifiedName featureTypeName ) {
        this.featureTypeName = featureTypeName;
    }

    /**
     * returns a feature-filter as defined in WFS specifications.
     *
     * @return the filter of the FeatureTypeConstraints
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * sets a feature-filter as defined in WFS specifications.
     *
     * @param filter
     *            the filter of the FeatureTypeConstraints
     */
    public void setFilter( Filter filter ) {
        this.filter = filter;
    }

    /**
     * returns the extent for filtering the feature type
     *
     * @return the extent for filtering the feature type
     */
    public Extent[] getExtents() {
        if ( this.extents != null ) {
            return extents.toArray( new Extent[extents.size()] );
        }
        return new Extent[0];
    }

    /**
     * sets the extent for filtering the feature type
     *
     * @param extents
     *            extents for filtering the feature type
     */
    public void setExtents( Extent[] extents ) {
        if ( this.extents != null ) {
            this.extents.clear();
        }

        if ( extents != null ) {
            for ( int i = 0; i < extents.length; i++ ) {
                addExtent( extents[i] );
            }
        }
    }

    /**
     * Adds an Extent to the Extent-List of a FeatureTypeConstraint
     *
     * @param extent
     *            an extent to add
     */
    public void addExtent( Extent extent ) {
        if ( this.extents != null ) {
            extents = new ArrayList<Extent>();
        }
        extents.add( extent );
    }

    /**
     * Removes an Extent from the Extent-List of a FeatureTypeConstraint
     *
     * @param extent
     *            an extent to remove
     */
    public void removeExtent( Extent extent ) {
        extents.remove( extents.indexOf( extent ) );
    }

    /**
     * @return the FeatureTypeConstraint as String
     */
    @Override
    public String toString() {
        String ret = getClass().getName() + "\n";
        ret = "featureTypeName = " + featureTypeName + "\n";
        ret += ( "filter = " + filter + "\n" );
        ret += ( "extents = " + extents + "\n" );

        return ret;
    }

    /**
     * exports the content of the FeatureTypeConstraint as XML formated String
     *
     * @return xml representation of the FeatureTypeConstraint
     */
    public String exportAsXML() {

        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "<FeatureTypeConstraint>" );
        if ( featureTypeName != null ) {
            if ( featureTypeName.getNamespace() == null ) {
                sb.append( "<FeatureTypeName>" );
                sb.append( escape( featureTypeName.getLocalName() ) );
            } else {
                sb.append( "<FeatureTypeName xmlns:" ).append( featureTypeName.getPrefix() );
                sb.append( "='" ).append( featureTypeName.getNamespace().toASCIIString() ).append( "'>" );
                sb.append( featureTypeName.getPrefixedName() );
            }
            sb.append( "</FeatureTypeName>" );
        }
        if ( filter != null ) {
            sb.append( filter.to110XML() );
        }
        if ( extents != null ) {
            for ( int i = 0; i < extents.size(); i++ ) {
                sb.append( ( (Marshallable) extents.get( i ) ).exportAsXML() );
            }
        }
        sb.append( "</FeatureTypeConstraint>" );

        return sb.toString();
    }

}
