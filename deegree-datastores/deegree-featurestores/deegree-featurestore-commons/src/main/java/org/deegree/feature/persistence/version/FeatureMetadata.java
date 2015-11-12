//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.feature.persistence.version;

/**
 * Encapsulates the feature id and version (if versioning is enabled).
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class FeatureMetadata {

    private final String fid;

    private final String version;

    /**
     * @param fid
     *            id of the feature, never <code>null</code>
     */
    public FeatureMetadata( String fid ) {
        this( fid, null );
    }

    /**
     * @param fid
     *            id of the feature, never <code>null</code>
     * @param version
     *            current version of this feature, may be <code>null</code> if versioning is not supported
     */
    public FeatureMetadata( String fid, String version ) {
        this.fid = fid;
        this.version = version;
    }

    /**
     * @return the id of the feature, never <code>null</code>
     */
    public String getFid() {
        return fid;
    }

    /**
     * @return the current version of this feature, may be <code>null</code> if versioning is not supported
     */
    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( fid == null ) ? 0 : fid.hashCode() );
        result = prime * result + ( ( version == null ) ? 0 : version.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        FeatureMetadata other = (FeatureMetadata) obj;
        if ( fid == null ) {
            if ( other.fid != null )
                return false;
        } else if ( !fid.equals( other.fid ) )
            return false;
        if ( version == null ) {
            if ( other.version != null )
                return false;
        } else if ( !version.equals( other.version ) )
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "FeatureMetadata [fid=" + fid + ", version=" + version + "]";
    }

}