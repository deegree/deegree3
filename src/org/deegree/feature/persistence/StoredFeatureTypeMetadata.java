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

package org.deegree.feature.persistence;

import org.deegree.crs.CRS;
import org.deegree.feature.types.FeatureType;

/**
 * Metadata for {@link FeatureType}s that are associated with a {@link FeatureStore}.
 * 
 * TODO missing information for wfs:FeatureTypeType elements: MetadataURL, Keywords, ...
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class StoredFeatureTypeMetadata {

    private final FeatureType ft;

    private final FeatureStore fs;

    private final String title;

    private final String desc;

    private final CRS nativeCRS;

    /**
     * Creates a new instance of {@link StoredFeatureTypeMetadata}.
     * 
     * @param ft
     *            feature type, never null
     * @param fs
     *            store, never null
     * @param title
     *            an optional title intended, to be used for display to a human, never null
     * @param desc
     *            an optional brief narrative description, intended to be used for display to a human, can be null
     * @param nativeCRS
     *            the native {@link CRS} for the geometry properties, can be null
     */
    public StoredFeatureTypeMetadata( FeatureType ft, FeatureStore fs, String title, String desc, CRS nativeCRS ) {
        this.ft = ft;
        this.fs = fs;
        this.title = title;
        this.desc = desc;
        this.nativeCRS = nativeCRS;
    }

    /**
     * Returns the associated {@link FeatureType} instance.
     * 
     * @return the associated {@link FeatureType} instance, never null
     */
    public FeatureType getType() {
        return ft;
    }

    /**
     * Returns the associated {@link FeatureStore} instance.
     * 
     * @return the associated {@link FeatureStore} instance, never null
     */
    public FeatureStore getStore() {
        return fs;
    }

    /**
     * Returns a title for this {@link FeatureType}, intended to be used for display to a human.
     * 
     * @return title, never null
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns a brief narrative description for this {@link FeatureType}, intended to be used for display to a human.
     * 
     * @return brief narrative description, can be null
     */
    public String getAbstract() {
        return desc;
    }

    public String getKeywords() {
        // TODO Auto-generated method stub
        return null;
    }    
    
    /**
     * Returns the native {@link CRS} used by the {@link FeatureStore} for storing the geometry properties of this
     * {@link FeatureType}.
     * 
     * @return the native CRS, never null
     */
    public CRS getDefaultCRS() {
        return nativeCRS;
    }
}
