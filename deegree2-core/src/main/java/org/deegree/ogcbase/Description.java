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
package org.deegree.ogcbase;

import java.io.Serializable;

import org.deegree.ogcwebservices.MetadataLink;

/**
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class Description extends DescriptionBase implements Cloneable, Serializable {

    private static final long serialVersionUID = -5160350013572694132L;

    private String label = null;

    /**
     * @param name
     * @param label
     * @throws OGCException
     */
    public Description( String name, String label ) throws OGCException {
        super( name );
        setLabel( label );
    }

    /**
     * @param description
     * @param name
     * @param label
     * @param metadataLink
     * @throws OGCException
     */
    public Description( String name, String label, String description, MetadataLink metadataLink ) throws OGCException {
        super( name, description, metadataLink );
        setLabel( label );
    }

    /**
     * @return Returns the label.
     *
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     *            The label to set.
     * @throws OGCException
     *
     */
    public void setLabel( String label )
                            throws OGCException {
        if ( label == null ) {
            throw new OGCException( "label must be <> null for Description" );
        }
        this.label = label;
    }

    @Override
    public Object clone() {
        try {
            MetadataLink metadataLink = getMetadataLink();
            if ( metadataLink != null ) {
                metadataLink = (MetadataLink) metadataLink.clone();
            }
            return new Description( getName(), label, getDescription(), metadataLink );

        } catch ( Exception e ) {
            // just return null
        }
        return null;
    }

}
