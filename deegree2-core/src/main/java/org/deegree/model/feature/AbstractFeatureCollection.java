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
package org.deegree.model.feature;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcbase.CommonNamespaces;

/**
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractFeatureCollection extends AbstractFeature implements FeatureCollection {

    private static final ILogger LOG = LoggerFactory.getLogger( AbstractFeatureCollection.class );

    /**
     * @param id
     */
    public AbstractFeatureCollection( String id ) {
        this( id, null );
    }

    /**
     * @param id
     * @param name
     *            of the feature collection if <code>null</code> it will be set to wfs:FeatureCollection
     */
    public AbstractFeatureCollection( String id, QualifiedName name ) {
        super( id, null );

        PropertyType[] ftp = new PropertyType[1];
        if ( name == null ) {
            name = new QualifiedName( CommonNamespaces.WFS_PREFIX, "FeatureCollection", CommonNamespaces.WFSNS );
        }
        try {
            ftp[0] = FeatureFactory.createPropertyType( new QualifiedName( "features" ),
                                                        Types.FEATURE_ARRAY_PROPERTY_NAME, true );
        } catch ( UnknownTypeException e ) {
            LOG.logError( "Unreachable point reached.", e );
        }
        this.featureType = FeatureFactory.createFeatureType( name, false, ftp );

    }

    /**
     * returns a Point with position 0/0 and no CRS
     * 
     * @return a geometry
     */
    public Geometry getDefaultGeometryPropertyValue() {
        return GeometryFactory.createPoint( 0, 0, null );
    }

    /**
     * returns the value of a feature collection geometry properties
     * 
     * @return array of all geometry property values
     */
    public Geometry[] getGeometryPropertyValues() {
        return new Geometry[0];
    }

    /**
     * returns all properties of a feature collection
     * 
     * @return all properties of a feature
     */
    public FeatureProperty[] getProperties() {
        return new FeatureProperty[0];
    }

    /**
     * returns the properties of a feature collection at the passed index position
     * 
     * @param index
     * @return properties at the passed index position
     */
    public FeatureProperty[] getProperties( int index ) {
        // TODO
        // a FeatureCollection may also have properties?
        return null;
    }

    /**
     * returns the default property of a feature collection with the passed name
     * 
     * @param name
     * @return named default property
     */
    public FeatureProperty getDefaultProperty( QualifiedName name ) {
        // TODO
        // a FeatureCollection may also have properties?
        return null;
    }

    /**
     * returns the named properties of a feature collection
     * 
     * @param name
     * @return named properties
     */
    public FeatureProperty[] getProperties( QualifiedName name ) {
        // TODO
        // a FeatureCollection may also have properties?
        return null;
    }

    /**
     * sets a property of a feature collection.<br>
     * !!! this method is not implemented yet !!!
     * 
     * @param property
     */
    public void setProperty( FeatureProperty property ) {
        // TODO
        // a FeatureCollection may also have properties?
    }

  

    public void addAllUncontained( Feature[] feature ) {
        for ( int i = 0; i < feature.length; i++ ) {
            Feature f = feature[i];
            Feature f2 = getFeature( f.getId() );
            if ( f2 == null || !f2.getId().equals( f.getId() ) ) {
                add( feature[i] );
            }
        }
    }

   

    public void addAllUncontained( FeatureCollection fc ) {
        int size = fc.size();
        for ( int i = 0; i < size; i++ ) {
            Feature f = fc.getFeature( i );
            Feature f2 = getFeature( f.getId() );
            if ( f2 == null || !f2.getId().equals( f.getId() ) ) {
                add( f );
            }
        }
    }

    /**
     * removes a feature identified by its ID from the feature collection. If no feature with the passed ID is available
     * nothing happens and <tt>null</tt> will be returned
     * 
     * @param id
     * @return the removed feature
     */
    public Feature remove( String id ) {
        Feature feat = getFeature( id );
        return remove( feat );
    }

}
