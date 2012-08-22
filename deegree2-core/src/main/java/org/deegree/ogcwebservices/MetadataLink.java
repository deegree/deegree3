// $HeadURL$
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
package org.deegree.ogcwebservices;

import java.net.URI;
import java.net.URL;

/**
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */

public class MetadataLink implements Cloneable {
    private URL reference = null;

    private String title = null;

    private URI about = null;

    private MetadataType metadataType = null;

    /**
     * @param reference
     * @param title
     * @param about
     * @param metadataType
     */
    public MetadataLink( URL reference, String title, URI about, MetadataType metadataType ) {
        this.reference = reference;
        this.title = title;
        this.about = about;
        this.metadataType = metadataType;
    }

    /**
     * @return Returns the about.
     *
     */
    public URI getAbout() {
        return about;
    }

    /**
     * @param about
     *            The about to set.
     *
     */
    public void setAbout( URI about ) {
        this.about = about;
    }

    /**
     * @return Returns the metadataType.
     *
     */
    public MetadataType getMetadataType() {
        return metadataType;
    }

    /**
     * @param metadataType
     *            The metadataType to set.
     */
    public void setMetadataType( MetadataType metadataType ) {
        this.metadataType = metadataType;
    }

    /**
     * @return Returns the reference.
     *
     */
    public URL getReference() {
        return reference;
    }

    /**
     * @param reference
     *            The reference to set.
     */
    public void setReference( URL reference ) {
        this.reference = reference;
    }

    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            The title to set.
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        return new MetadataLink( reference, title, about, new MetadataType( metadataType.value ) );
    }

}
