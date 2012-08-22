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
package org.deegree.portal.standard.digitizer.model;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureTypeDescription {

    private String name;

    private String namespace;

    private String geomPropertyName;

    private String geomPropertyNamespace;

    private String wfsURL;

    private String sourceType;

    private FeatureTypeProperty[] properties;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @param namespace
     *            the namespace to set
     */
    public void setNamespace( String namespace ) {
        this.namespace = namespace;
    }

    /**
     * @return the properties
     */
    public FeatureTypeProperty[] getProperties() {
        return properties;
    }

    /**
     * @param properties
     *            the properties to set
     */
    public void setProperties( FeatureTypeProperty[] properties ) {
        this.properties = properties;
    }

    /**
     * @return the wfsURL
     */
    public String getWfsURL() {
        return wfsURL;
    }

    /**
     * @param wfsURL
     *            the wfsURL to set
     */
    public void setWfsURL( String wfsURL ) {
        this.wfsURL = wfsURL;
    }

    /**
     * @return the geomPropertyName
     */
    public String getGeomPropertyName() {
        return geomPropertyName;
    }

    /**
     * @param geomPropertyName
     *            the geomPropertyName to set
     */
    public void setGeomPropertyName( String geomPropertyName ) {
        this.geomPropertyName = geomPropertyName;
    }

    /**
     * @return the geomPropertyNamespace
     */
    public String getGeomPropertyNamespace() {
        return geomPropertyNamespace;
    }

    /**
     * @param geomPropertyNamespace
     *            the geomPropertyNamespace to set
     */
    public void setGeomPropertyNamespace( String geomPropertyNamespace ) {
        this.geomPropertyNamespace = geomPropertyNamespace;
    }

    /**
     * @return the sourceType
     */
    public String getSourceType() {
        return sourceType;
    }

    /**
     * @param sourceType
     *            the sourceType to set
     */
    public void setSourceType( String sourceType ) {
        this.sourceType = sourceType;
    }

}
