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

package org.deegree.ogcwebservices.wps.describeprocess;

import org.deegree.owscommon.OWSMetadata;

/**
 * LiteralOutput.java
 *
 * Created on 09.03.2006. 22:50:41h
 *
 * Description of a literal output (or input).
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @author last edited by: $Author:wanhoff$
 *
 * @version $Revision$, $Date:20.03.2007$
 */
public class LiteralOutput {

    /**
     * Data type of this set of values (e.g. integer, real, etc). This data type metadata should be
     * included for each quantity whose data type is not a string.
     */
    protected OWSMetadata dataType;

    /**
     * List of supported units of measure for this input or output. This element should be included
     * when this literal has a unit of measure (e.g., "meters", without a more complete reference
     * system). Not necessary for a count, which has no units.
     */
    protected SupportedUOMs supportedUOMs;

    /**
     *
     * @param domainMetadataType
     * @param supportedUOMsType
     */
    public LiteralOutput( OWSMetadata domainMetadataType, SupportedUOMs supportedUOMsType ) {

        dataType = domainMetadataType;
        supportedUOMs = supportedUOMsType;
    }

    /**
     * @return Returns the dataType.
     */
    public OWSMetadata getDataType() {
        return dataType;
    }

    /**
     * @param value
     *            The dataType to set.
     */
    public void setDataType( OWSMetadata value ) {
        this.dataType = value;
    }

    /**
     * @return the supportedUOMs.
     */
    public SupportedUOMs getSupportedUOMs() {
        return supportedUOMs;
    }

    /**
     * @param value
     *            The supportedUOMs to set.
     */
    public void setSupportedUOMs( SupportedUOMs value ) {
        this.supportedUOMs = value;
    }
}
