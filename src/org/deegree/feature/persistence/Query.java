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
package org.deegree.feature.persistence;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.crs.CRS;
import org.deegree.filter.Filter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.wfs.getfeature.TypeName;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Query {

    public static final String HINT_LOOSE_BBOX = "HINT_LOOSE_BBOX";

    public static final String HINT_NO_GEOMETRIES = "HINT_NO_GEOMETRIES";

    private final TypeName[] typeNames;

    private final Filter filter;

    private final String featureVersion;

    private final CRS srsName;

    private final SortProperty[] sortBy;

    private final Map<String, Object> hints = new HashMap<String, Object>();

    public Query( QName ftName, Envelope looseBbox, Filter filter, boolean withGeometries ) {
        this.typeNames = new TypeName[] { new TypeName( ftName, null ) };
        this.filter = filter;
        this.featureVersion = null;
        this.srsName = null;
        this.sortBy = null;
        hints.put( HINT_LOOSE_BBOX, looseBbox );
        if ( !withGeometries ) {
            hints.put( HINT_NO_GEOMETRIES, Boolean.TRUE );
        }
    }

    public Query( TypeName[] typeNames, Filter filter, String featureVersion, CRS srsName, SortProperty[] sortBy ) {
        this.typeNames = typeNames;
        this.filter = filter;
        this.featureVersion = featureVersion;
        this.srsName = srsName;
        this.sortBy = sortBy;
    }

    public Object getHint( String code ) {
        return hints.get( code );
    }

    public TypeName[] getTypeNames() {
        return typeNames;
    }
    
    public Filter getFilter () {
        return filter;
    }
    
    public SortProperty[] getSortProperties () {
        return sortBy;
    }
}
