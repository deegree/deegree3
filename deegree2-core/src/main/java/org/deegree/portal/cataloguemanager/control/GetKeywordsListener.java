//$HeadURL: 
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
package org.deegree.portal.cataloguemanager.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GetKeywordsListener extends AbstractMetadataListener {

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.portal.cataloguemanager.control.AbstractMetadataListener#actionPerformed(
     * org.deegree.enterprise.control.ajax.WebEvent, org.deegree.portal.cataloguemanager.control.ResponseHandler)
     */
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        List<String> list = new ArrayList<String>( 1000 );

        HttpServletRequest req = (HttpServletRequest)getRequest();
        Locale loc = req.getLocale();
        if ( loc == null ) {
            loc = Locale.getDefault();
        }
        URL url = getClass().getResource( "/org/deegree/portal/cataloguemanager/control/resources/keywords_" + loc.getLanguage() + ".txt" );
        BufferedReader br = new BufferedReader( new InputStreamReader( url.openStream() ) );
        String line = null;
        while ( ( line = br.readLine() ) != null ) {
            list.add( line );
        }
        br.close();

        responseHandler.writeAndClose( false, list );
    }

}