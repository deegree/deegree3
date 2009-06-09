//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.feature.refs;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.filter.expression.PropertyName;
import org.deegree.commons.types.gml.StandardGMLObjectProperties;
import org.deegree.feature.Feature;
import org.deegree.feature.Property;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.jaxen.JaxenException;

/**
 * The <code></code> class TODO add class documentation here.
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
    
    public FeatureReference (String href) {
        this.href = href;
        int pos = href.lastIndexOf( '#' );
        if (pos < 0) {
            String msg = "Reference string (='" + href + "') does not contain a '#' character.";
            throw new IllegalArgumentException(msg);
        }
        fid = href.substring( pos + 1 );
    }
    
    public FeatureReference (String href, FeatureType ft) {
        this (href);
        this.ft = ft;
    }
    
    public void resolve (Feature feature) {
        if (this.feature != null) {
            String msg = "Internal error: Feature reference (" + href + ") has already been resolved.";
            throw new RuntimeException(msg);
        }
        this.feature = feature;
    }
    
    public Envelope getEnvelope() {
        return feature.getEnvelope();
    }

    public Property<Geometry>[] getGeometryProperties() {
        return feature.getGeometryProperties();
    }

    public String getId() {
        return fid;
    }

    public QName getName() {
        return feature.getName();
    }

    public Property<?>[] getProperties() {
        return feature.getProperties();
    }

    public Property<?>[] getProperties( QName propName ) {
        return feature.getProperties( propName );
    }

    public Property<?> getProperty( QName propName ) {
        return feature.getProperty( propName );
    }

    public Object getPropertyValue( PropertyName propName )
                            throws JaxenException {
        return feature.getPropertyValue( propName );
    }

    public Object getPropertyValue( QName propName ) {
        return feature.getPropertyValue( propName );
    }

    public Object[] getPropertyValues( QName propName ) {
        return feature.getPropertyValues( propName );
    }

    public FeatureType getType() {
        return ft;
    }

    public void setId( String id ) {
        feature.setId( id );
    }

    public void setProperties( List<Property<?>> props )
                            throws IllegalArgumentException {
        feature.setProperties( props );
    }

    public void setPropertyValue( QName propName, int occurence, Object value ) {
        feature.setPropertyValue( propName, occurence, value );
    }

    public StandardGMLObjectProperties getStandardGMLProperties() {
        return feature.getStandardGMLProperties();
    }

    public void setStandardGMLProperties( StandardGMLObjectProperties standardProps ) {
        feature.setStandardGMLProperties( standardProps );
    }    
    
    public String getHref() {
        return href;
    }   
}
