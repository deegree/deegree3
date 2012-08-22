//$HeadURL$
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
package org.deegree.ogcwebservices.wms.capabilities;

import java.net.URL;

import org.deegree.ogcbase.BaseURL;

/**
 * AuthorityURL encloses an <OnlineResource>element which states the URL of a document defining the
 * meaning of the Identifier values.
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$
 */
public class AuthorityURL extends BaseURL {

    private String name = null;

    /**
     * constructor initializing the class with the authorityURL
     * @param name
     * @param onlineResource
     */
    public AuthorityURL( String name, URL onlineResource ) {
        super( null, onlineResource );
        setName( name );
    }

    /**
     * @return the name of the authority
     */
    public String getName() {
        return name;
    }

    /**
     * sets the name of the authority
     * @param name
     */
    public void setName( String name ) {
        this.name = name;
    }

    @Override
    public String toString() {
        String ret = null;
        ret = "name = "
            + name + "\n";
        return ret;
    }

}
