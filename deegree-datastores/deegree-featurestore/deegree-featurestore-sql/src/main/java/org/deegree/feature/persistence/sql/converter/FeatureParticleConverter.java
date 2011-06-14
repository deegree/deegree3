//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-base/src/main/java/org/deegree/geometry/utils/GeometryParticleConverter.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

package org.deegree.feature.persistence.sql.converter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.feature.Feature;
import org.deegree.gml.GMLReferenceResolver;
import org.deegree.gml.feature.FeatureReference;

/**
 * {@link ParticleConverter} for {@link Feature} particles.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 30976 $, $Date: 2011-05-31 11:09:40 +0200 (Di, 31. Mai 2011) $
 */
public class FeatureParticleConverter implements ParticleConverter<Feature> {

    private final String fkColumn;

    private final String hrefColumn;

    private final GMLReferenceResolver resolver;

    public FeatureParticleConverter( String fkColumn, String hrefColumn, GMLReferenceResolver resolver ) {
        this.fkColumn = fkColumn;
        this.hrefColumn = hrefColumn;
        this.resolver = resolver;
    }

    @Override
    public String getSelectSnippet( String tableAlias ) {
        return tableAlias + "." + fkColumn;
    }

    @Override
    public Feature toParticle( ResultSet rs, int colIndex )
                            throws SQLException {
        Object value = rs.getObject( colIndex );
        if ( value != null ) {
            // TODO
            String ref;
            if ( value.toString().startsWith( "http" ) ) {
                ref = value.toString();
            } else {
                ref = "#" + value;
            }
            return new FeatureReference( resolver, ref, null );
        }

        // value = rs.getObject( colIndex + 1);
        // if ( value != null ) {
        // return new FeatureReference( resolver, value.toString(), null );
        // }
        return null;
    }

    @Override
    public String getSetSnippet( Feature particle ) {
        return "?,?";
    }

    @Override
    public void setParticle( PreparedStatement stmt, Feature particle, int paramIndex )
                            throws SQLException {
        // TODO Auto-generated method stub

    }
}