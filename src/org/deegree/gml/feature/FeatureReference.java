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

package org.deegree.gml.feature;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.feature.Feature;
import org.deegree.feature.Property;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.expression.PropertyName;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLObjectResolver;
import org.deegree.gml.ReferenceResolvingException;
import org.deegree.gml.GMLVersion;
import org.jaxen.JaxenException;

/**
 * Represents a reference to a feature, which is usually expressed using an <code>xlink:href</code> attribute in GML
 * (may be document-local or remote).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class FeatureReference implements Feature {

    private final GMLObjectResolver resolver;

    private final String uri;

    private final String baseURL;

    private Feature feature;

    /**
     * Creates a new {@link FeatureReference} instance.
     * 
     * @param resolver
     *            used for resolving the reference, must not be <code>null</code>
     * @param uri
     *            the feature's uri, must not be <code>null</code>
     * @param baseURL
     *            base URL for resolving the uri, may be <code>null</code> (no resolving of relative URLs)
     */
    public FeatureReference( GMLObjectResolver resolver, String uri, String baseURL ) {
        this.resolver = resolver;
        this.uri = uri;
        this.baseURL = baseURL;
    }

    /**
     * Returns the URI of the feature.
     * 
     * @return the URI of the feature, never <code>null</code>
     */
    public String getURI() {
        return uri;
    }

    /**
     * Returns whether the URI is local, i.e. if it starts with the <code>#</code> character.
     * 
     * @return true, if the URI is local, false otherwise
     */
    public boolean isLocal() {
        return uri.startsWith( "#" );
    }

    /**
     * Returns the referenced {@link Feature} instance (may trigger resolving and fetching it).
     * 
     * @return the referenced {@link Feature} instance
     * @throws ReferenceResolvingException
     *             if the reference cannot be resolved
     */
    public Feature getReferencedFeature()
                            throws ReferenceResolvingException {
        if ( this.feature == null ) {
            feature = resolver.getFeature( uri, baseURL );
            if ( feature == null ) {
                String msg = "Unable to resolve feature reference '" + uri + "'.";
                throw new ReferenceResolvingException( msg );
            }
        }
        return feature;
    }

    @Override
    public Envelope getEnvelope() {
        return getReferencedFeature().getEnvelope();
    }

    @Override
    public Property<Geometry>[] getGeometryProperties() {
        return getReferencedFeature().getGeometryProperties();
    }

    @Override
    public String getId() {
        return getReferencedFeature().getId();
    }

    @Override
    public QName getName() {
        return getReferencedFeature().getName();
    }

    @Override
    public Property<?>[] getProperties() {
        return getReferencedFeature().getProperties();
    }

    @Override
    public Property<?>[] getProperties( QName propName ) {
        return getReferencedFeature().getProperties( propName );
    }

    @Override
    public Property<?> getProperty( QName propName ) {
        return getReferencedFeature().getProperty( propName );
    }

    @Override
    public Object[] getPropertyValues( PropertyName propName, GMLVersion version )
                            throws JaxenException {
        return getReferencedFeature().getPropertyValues( propName, version );
    }

    @Override
    public Object getPropertyValue( QName propName ) {
        return getReferencedFeature().getPropertyValue( propName );
    }

    @Override
    public Object[] getPropertyValues( QName propName ) {
        return getReferencedFeature().getPropertyValues( propName );
    }

    @Override
    public FeatureType getType() {
        return getReferencedFeature().getType();
    }

    @Override
    public void setId( String id ) {
        getReferencedFeature().setId( id );
    }

    @Override
    public void setProperties( List<Property<?>> props )
                            throws IllegalArgumentException {
        getReferencedFeature().setProperties( props );
    }

    @Override
    public void setPropertyValue( QName propName, int occurence, Object value ) {
        getReferencedFeature().setPropertyValue( propName, occurence, value );
    }

    @Override
    public Property<?>[] getProperties( GMLVersion version ) {
        return getReferencedFeature().getProperties( version );
    }

    @Override
    public Property<?>[] getProperties( QName propName, GMLVersion version ) {
        return getReferencedFeature().getProperties( propName, version );
    }

    @Override
    public Property<?> getProperty( QName propName, GMLVersion version ) {
        return getReferencedFeature().getProperty( propName, version );
    }

    @Override
    public Object getPropertyValue( QName propName, GMLVersion version ) {
        return getReferencedFeature().getPropertyValue( propName, version );
    }

    @Override
    public Object[] getPropertyValues( QName propName, GMLVersion version ) {
        return getReferencedFeature().getPropertyValues( propName, version );
    }

    @Override
    public void setProperties( List<Property<?>> props, GMLVersion version )
                            throws IllegalArgumentException {
        getReferencedFeature().setProperties( props, version );
    }

    @Override
    public void setPropertyValue( QName propName, int occurence, Object value, GMLVersion version ) {
        getReferencedFeature().setPropertyValue( propName, occurence, value, version );
    }
}
