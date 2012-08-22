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
package org.deegree.model.metadata.iso19115;

import java.io.Serializable;
import java.net.URL;

/**
 * Linkage
 *
 * @author <a href="mailto:schaefer@lat-lon.de">Axel Schaefer </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Linkage implements Serializable {

    private static final long serialVersionUID = 3516862501859879157L;

    /**
     *
     */
    public static final String SIMPLE = "simple";

    private URL href = null;

    private String type = null;

    /**
     * Creates a default instance of Linkage with default xlink namespace.
     *
     * @see "org.deegree.ogcbase.CommonNamespace#XLNNS"
     */
    public Linkage() {
        this.type = SIMPLE;
    }

    /**
     *
     * @param href
     */
    public Linkage( URL href ) {
        this.setHref( href );
    }

    /**
     * Creates a new instance of Linkage
     *
     * @param href
     * @param type
     *
     */
    public Linkage( URL href, String type ) {
        setHref( href );
        setType( type );
    }

    /**
     * use="required"
     *
     * @return the href-attribute
     *
     */
    public URL getHref() {
        return href;
    }

    /**
     * @param href
     * @see #getHref()
     *
     */
    public void setHref( URL href ) {
        this.href = href;
    }

    /**
     * fixed="simple"
     *
     * @return the type-attribute
     *
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     * @see #getType()
     *
     */
    public void setType( String type ) {
        this.type = type;
    }

    @Override
    public String toString() {
        String ret = null;
        ret = "href = " + href + "\n";
        ret += "type = " + type + "\n";
        return ret;
    }

}
