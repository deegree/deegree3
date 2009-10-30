//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.feature.gml;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.gml.GMLVersion;
import org.deegree.feature.Feature;
import org.deegree.feature.Property;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.expression.PropertyName;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.jaxen.JaxenException;

/**
 * Represents a reference to the GML representation of a feature, which is usually expressed using an
 * <code>xlink:href</code> attribute in GML (may be document-local or remote).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class FeatureReference implements Feature {

    private String href;

    private String fid;

    private Feature feature;

    private FeatureType ft;

    public FeatureReference( String href ) {
        this.href = href;
        int pos = href.lastIndexOf( '#' );
        if ( pos < 0 ) {
            String msg = "Reference string (='" + href + "') does not contain a '#' character.";
            throw new IllegalArgumentException( msg );
        }
        fid = href.substring( pos + 1 );
    }

    public FeatureReference( String href, FeatureType ft ) {
        this( href );
        this.ft = ft;
    }

    public void resolve( Feature feature ) {
        if ( this.feature != null ) {
            String msg = "Internal error: Feature reference (" + href + ") has already been resolved.";
            throw new RuntimeException( msg );
        }
        this.feature = feature;
    }

    @Override
    public Envelope getEnvelope() {
        return feature.getEnvelope();
    }

    @Override
    public Property<Geometry>[] getGeometryProperties() {
        return feature.getGeometryProperties();
    }

    @Override
    public String getId() {
        // TODO remove hack (only necessary because ID generation in store does not update reference objects)
        if ( feature != null ) {
            return feature.getId();
        }
        return fid;
    }

    @Override
    public QName getName() {
        return feature.getName();
    }

    @Override
    public Property<?>[] getProperties() {
        return feature.getProperties();
    }

    @Override
    public Property<?>[] getProperties( QName propName ) {
        return feature.getProperties( propName );
    }

    @Override
    public Property<?> getProperty( QName propName ) {
        return feature.getProperty( propName );
    }

    @Override
    public Object[] getPropertyValues( PropertyName propName, GMLVersion version )
                            throws JaxenException {
        return feature.getPropertyValues( propName, version );
    }

    @Override
    public Object getPropertyValue( QName propName ) {
        return feature.getPropertyValue( propName );
    }

    @Override
    public Object[] getPropertyValues( QName propName ) {
        return feature.getPropertyValues( propName );
    }

    @Override
    public FeatureType getType() {
        if ( feature == null ) {
            return ft;
        }
        return feature.getType();
    }

    @Override
    public void setId( String id ) {
        feature.setId( id );
    }

    @Override
    public void setProperties( List<Property<?>> props )
                            throws IllegalArgumentException {
        feature.setProperties( props );
    }

    @Override
    public void setPropertyValue( QName propName, int occurence, Object value ) {
        feature.setPropertyValue( propName, occurence, value );
    }

    public String getHref() {
        return href;
    }

    /**
     * Returns whether the reference is local or remote.
     * 
     * @return true, if the reference is local, false otherwise
     */
    public boolean isLocal() {
        return href.startsWith( "#" );
    }

    @Override
    public Property<?>[] getProperties( GMLVersion version ) {
        return feature.getProperties( version );
    }

    @Override
    public Property<?>[] getProperties( QName propName, GMLVersion version ) {
        return feature.getProperties( propName, version );
    }

    @Override
    public Property<?> getProperty( QName propName, GMLVersion version ) {
        return feature.getProperty( propName, version );
    }

    @Override
    public Object getPropertyValue( QName propName, GMLVersion version ) {
        return feature.getPropertyValue( propName, version );
    }

    @Override
    public Object[] getPropertyValues( QName propName, GMLVersion version ) {
        return feature.getPropertyValues( propName, version );
    }

    @Override
    public void setProperties( List<Property<?>> props, GMLVersion version )
                            throws IllegalArgumentException {
        feature.setProperties( props, version );
    }

    @Override
    public void setPropertyValue( QName propName, int occurence, Object value, GMLVersion version ) {
        feature.setPropertyValue( propName, occurence, value, version );
    }
}
