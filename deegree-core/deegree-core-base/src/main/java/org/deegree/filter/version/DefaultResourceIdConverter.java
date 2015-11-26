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
package org.deegree.filter.version;

/**
 * A default ResourceIdConverter, pattern: &lt;fid&gt;_&lt;version&gt;
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class DefaultResourceIdConverter implements ResourceIdConverter {

    private static final String DELIMITER = "_version";

    @Override
    public String generateResourceId( FeatureMetadata featureMetadata ) {
        if ( featureMetadata == null )
            throw new NullPointerException( "FeatureMetadata must never be null!" );
        if ( featureMetadata.getVersion() == null )
            return featureMetadata.getFid();
        return featureMetadata.getFid() + DELIMITER + featureMetadata.getVersion();
    }

    @Override
    public boolean hasVersion( String id ) {
        if ( id == null )
            throw new NullPointerException( "id must never be null!" );
        return id.contains( DELIMITER );
    }

    @Override
    public FeatureMetadata convertToFeatureMetadata( String id ) {
        if ( id == null )
            throw new NullPointerException( "id must never be null!" );
        if ( hasVersion( id ) ) {
            int indexOf = id.indexOf( DELIMITER );
            String fid = id.substring( 0, indexOf );
            String version = id.substring( indexOf + DELIMITER.length(), id.length() );
            return new FeatureMetadata( fid, version );
        }
        return new FeatureMetadata( id );
    }

}