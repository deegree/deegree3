//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.console.styles;

import java.net.MalformedURLException;
import java.net.URL;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.deegree.console.XMLConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class StyleConfigManager extends XMLConfigManager<StyleConfig> {

    private static final Logger LOG = LoggerFactory.getLogger( StyleConfigManager.class );

    private static final String SE_NAMESPACE = "http://www.opengis.net/se";

    private static final String SE_SCHEMA = "http://schemas.opengis.net/se/1.1.0/FeatureStyle.xsd";

    private static final String SLD_NAMESPACE = "http://www.opengis.net/sld";

    private static final String SLD_SCHEMA = "http://schemas.opengis.net/sld/1.1.0/StyledLayerDescriptor.xsd";

    @Override
    protected void add( String id, String namespace, boolean ignore ) {

        URL schema = null;
//        if ( namespace.equals( SE_NAMESPACE ) ) {
//            schema = SE_SCHEMA;
//        } else if ( namespace.equals( SLD_NAMESPACE ) ) {
//            schema = SLD_SCHEMA;
//        }

        // TODO still using null
        
//        if ( schema != null ) {
            StyleConfig config = new StyleConfig( id, !ignore, ignore, this, schema );
            idToConfig.put( id, config );
//        } else {
//            LOG.warn( "Skipping style file with id '" + id + "' -- neither in SLD nor in SE namespace." );
//        }
    }

    @Override
    public String getBaseDir() {
        return "styles";
    }
}
