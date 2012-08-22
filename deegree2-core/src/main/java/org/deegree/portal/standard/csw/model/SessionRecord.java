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

package org.deegree.portal.standard.csw.model;

import java.io.Serializable;

/**
 * A <code>${type_name}</code> class.<br/> TODO class description
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SessionRecord implements Serializable {

    private static final long serialVersionUID = 5434705327143566827L;

    private String identifier;

    private String catalogName;

    private String title;

    /**
     * @param identifier
     * @param catalogName
     * @param title
     */
    public SessionRecord( String identifier, String catalogName, String title ) {

        this.identifier = identifier;
        this.catalogName = catalogName;
        this.title = title;
    }

    /**
     * @param sr
     */
    public SessionRecord( SessionRecord sr ) {
        this.identifier = sr.getIdentifier();
        this.catalogName = sr.getCatalogName();
        this.title = sr.getTitle();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object o ) {

        if ( o == null || !( o instanceof SessionRecord ) ) {
            return false;
        }
        SessionRecord sr = (SessionRecord) o;

        if ( this.identifier.equals( sr.getIdentifier() )
             && this.catalogName.equals( sr.getCatalogName() )
             && this.title.equals( sr.getTitle() ) ) {

            return true;
        }
        return false;
    }

    /**
     * @return Returns the catalogName.
     */
    public String getCatalogName() {
        return catalogName;
    }

    /**
     * @param catalogName
     *            The catalogName to set.
     */
    public void setCatalogName( String catalogName ) {
        this.catalogName = catalogName;
    }

    /**
     * @return Returns the identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     *            The identifier to set.
     */
    public void setIdentifier( String identifier ) {
        this.identifier = identifier;
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

}
