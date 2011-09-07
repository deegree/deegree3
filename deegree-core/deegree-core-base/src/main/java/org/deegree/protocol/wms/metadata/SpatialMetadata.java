//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.wms.metadata;

import java.util.List;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class SpatialMetadata {

    private Envelope envelope;

    private List<ICRS> coordinateSystems;

    /**
     * @param envelope
     * @param coordinateSystems
     */
    public SpatialMetadata( Envelope envelope, List<ICRS> coordinateSystems ) {
        this.envelope = envelope;
        this.coordinateSystems = coordinateSystems;
    }

    /**
     * @return the envelope
     */
    public Envelope getEnvelope() {
        return envelope;
    }

    /**
     * @param envelope
     *            the envelope to set
     */
    public void setEnvelope( Envelope envelope ) {
        this.envelope = envelope;
    }

    /**
     * @return the coordinateSystems, never null
     */
    public List<ICRS> getCoordinateSystems() {
        return coordinateSystems;
    }

    /**
     * @param coordinateSystems
     *            the coordinateSystems to set, may not be null
     */
    public void setCoordinateSystems( List<ICRS> coordinateSystems ) {
        this.coordinateSystems = coordinateSystems;
    }

}
