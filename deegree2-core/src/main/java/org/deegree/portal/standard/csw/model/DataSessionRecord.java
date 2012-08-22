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

import org.deegree.model.spatialschema.Envelope;

/**
 * A <code>${type_name}</code> class.<br/> TODO class description
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class DataSessionRecord extends SessionRecord implements Serializable {

    private static final long serialVersionUID = -1816508996710579386L;

    private ServiceSessionRecord[] services;

    private Envelope bbox;

    /**
     * @param identifier
     * @param catalogName
     * @param title
     */
    public DataSessionRecord( String identifier, String catalogName, String title ) {
        super( identifier, catalogName, title );
        this.services = null;
        this.bbox = null;
    }

    /**
     * @param identifier
     * @param catalogName
     * @param title
     * @param services
     * @param bbox
     */
    public DataSessionRecord( String identifier, String catalogName, String title,
                             ServiceSessionRecord[] services, Envelope bbox ) {
        super( identifier, catalogName, title );
        this.services = services;
        this.bbox = bbox;
    }

    /**
     * @param dsr
     */
    public DataSessionRecord( DataSessionRecord dsr ) {
        super( dsr );
        this.services = dsr.getServices();
        this.bbox = dsr.getBoundingBox();
    }

    /**
     * @return Returns the bbox.
     */
    public Envelope getBoundingBox() {
        return bbox;
    }

    /**
     * @param bbox
     *            The bbox to set.
     */
    public void setBoundingBox( Envelope bbox ) {
        this.bbox = bbox;
    }

    /**
     * @return Returns the services.
     */
    public ServiceSessionRecord[] getServices() {
        return services;
    }

    /**
     * @param services
     *            The services to set.
     */
    public void setServices( ServiceSessionRecord[] services ) {
        this.services = services;
    }

}
