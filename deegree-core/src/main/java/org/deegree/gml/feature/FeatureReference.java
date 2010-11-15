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

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.feature.Feature;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Envelope;
import org.deegree.gml.GMLReference;
import org.deegree.gml.GMLReferenceResolver;
import org.deegree.gml.GMLVersion;

/**
 * Represents a reference to a feature, which is usually expressed using an <code>xlink:href</code> attribute in GML
 * (may be document-local or remote).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class FeatureReference extends GMLReference<Feature> implements Feature {

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
    public FeatureReference( GMLReferenceResolver resolver, String uri, String baseURL ) {
        super( resolver, uri, baseURL );
    }

    @Override
    public Envelope getEnvelope() {
        return getReferencedObject().getEnvelope();
    }

    @Override
    public Property[] getGeometryProperties() {
        return getReferencedObject().getGeometryProperties();
    }

    @Override
    public QName getName() {
        return getReferencedObject().getName();
    }

    @Override
    public Property[] getProperties() {
        return getReferencedObject().getProperties();
    }

    @Override
    public Property[] getProperties( QName propName ) {
        return getReferencedObject().getProperties( propName );
    }

    @Override
    public Property getProperty( QName propName ) {
        return getReferencedObject().getProperty( propName );
    }

    @Override
    public FeatureType getType() {
        return getReferencedObject().getType();
    }

    @Override
    public void setId( String id ) {
        getReferencedObject().setId( id );
    }

    @Override
    public void setProperties( List<Property> props )
                            throws IllegalArgumentException {
        getReferencedObject().setProperties( props );
    }

    @Override
    public void setPropertyValue( QName propName, int occurence, TypedObjectNode value ) {
        getReferencedObject().setPropertyValue( propName, occurence, value );
    }

    @Override
    public Property[] getProperties( GMLVersion version ) {
        return getReferencedObject().getProperties( version );
    }

    @Override
    public Property[] getProperties( QName propName, GMLVersion version ) {
        return getReferencedObject().getProperties( propName, version );
    }

    @Override
    public Property getProperty( QName propName, GMLVersion version ) {
        return getReferencedObject().getProperty( propName, version );
    }

    @Override
    public void setProperties( List<Property> props, GMLVersion version )
                            throws IllegalArgumentException {
        getReferencedObject().setProperties( props, version );
    }

    @Override
    public void setPropertyValue( QName propName, int occurence, TypedObjectNode value, GMLVersion version ) {
        getReferencedObject().setPropertyValue( propName, occurence, value, version );
    }

    @Override
    public StandardGMLFeatureProps getGMLProperties() {
        return getReferencedObject().getGMLProperties();
    }
}