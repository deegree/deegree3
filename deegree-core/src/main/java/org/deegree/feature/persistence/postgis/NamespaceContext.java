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
package org.deegree.feature.persistence.postgis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.deegree.feature.types.ApplicationSchema;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class NamespaceContext implements javax.xml.namespace.NamespaceContext {

    private static final Logger LOG = LoggerFactory.getLogger( NamespaceContext.class );
    
    private Map<String, String> prefixToNs = new HashMap<String, String>();

    private Map<String, String> nsToPrefix = new HashMap<String, String>();

    NamespaceContext( ApplicationSchema schema, GMLVersion version ) {
        for ( Entry<String, String> prefixAndNs : schema.getNamespaceBindings().entrySet() ) {
            LOG.debug( prefixAndNs.getKey() + " <-> " + prefixAndNs.getValue() );
            prefixToNs.put( prefixAndNs.getKey(), prefixAndNs.getValue() );
            nsToPrefix.put( prefixAndNs.getValue(), prefixAndNs.getKey() );
        }
        prefixToNs.put( "gml", version.getNamespace() );
        nsToPrefix.put( version.getNamespace(), "gml" );
    }

    @Override
    public String getNamespaceURI( String arg0 ) {
        return prefixToNs.get( arg0 );
    }

    @Override
    public String getPrefix( String arg0 ) {
        return nsToPrefix.get( arg0 );
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator getPrefixes( String arg0 ) {
        List<String> prefixes = null;
        String prefix = getPrefix( arg0 );
        if ( prefix == null ) {
            prefixes = Collections.emptyList();
        } else {
            prefixes = Collections.singletonList( prefix );
        }
        return prefixes.iterator();
    }
}