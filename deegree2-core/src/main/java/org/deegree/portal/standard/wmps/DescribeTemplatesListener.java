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
package org.deegree.portal.standard.wmps;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DescribeTemplatesListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( DescribeTemplatesListener.class );

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        String template = (String) event.getParameter().get( "template" );
        InputStream is = HttpUtils.performHttpGet( getInitParameter( "WMPS" ),
                                                   "request=DescribeTemplate&version=1.0.0&template=" + template,
                                                   timeout, null, null, null ).getResponseBodyAsStream();
        XMLFragment xml = new XMLFragment();
        List<String[]> parameter = new ArrayList<String[]>( 10 );        
        try {
            xml.load( is, getInitParameter( "WMPS" ) );
            List<Node> nodes = XMLTools.getRequiredNodes( xml.getRootElement(), "deegreewmps:Parameter",
                                                          CommonNamespaces.getNamespaceContext() );
            for ( Node node : nodes ) {
                String[] param = new String[2];
                param[0] = ( (Element) node ).getAttribute( "name" );
                param[1] = ( (Element) node ).getAttribute( "type" );
                parameter.add( param );
            }
        } catch ( Exception e ) {
            LOG.logError( e );
            String s = getInitParameter( "WMPS" ) + "request=DescribeTemplate&version=1.0.0&template=" + template;
            throw new IOException( "can not get description of a template from WMPS: " + s );
        }
        responseHandler.writeAndClose( false, parameter );
    }

}
