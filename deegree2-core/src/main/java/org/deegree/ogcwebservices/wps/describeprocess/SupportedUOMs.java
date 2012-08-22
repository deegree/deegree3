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

import java.util.ArrayList;
import java.util.List;

import org.deegree.owscommon.OWSMetadata;

/**
 * SupportedUOMs.java
 *
 * Created on 09.03.2006. 22:55:34h
 *
 * List of supported units of measure for a process input or output.
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SupportedUOMs {

    /**
     * Unordered list of references to the Units of Measure supported for this input or output. This element shall not
     * be included if there is only one (i.e., the default) UOM supported.
     */
    protected List<OWSMetadata> uom;

    /**
     * eference to the default UOM supported for this input or output, if any. The process shall expect input in or
     * produce output in this UOM unless the Execute request specifies another supported UOM.
     */
    protected OWSMetadata defaultUOM;

    /**
     * @param defaultuom
     * @param uom
     */
    public SupportedUOMs( OWSMetadata defaultuom, List<OWSMetadata> uom ) {
        defaultUOM = defaultuom;
        this.uom = uom;
    }

    /**
     *
     * @return UOM
     */
    public List<OWSMetadata> getUOM() {
        if ( uom == null ) {
            uom = new ArrayList<OWSMetadata>();
        }
        return this.uom;
    }

    /**
     * Gets the value of the defaultUOM property.
     *
     * @return possible object is {@link String  }
     */
    public OWSMetadata getDefaultUOM() {
        return defaultUOM;
    }

    /**
     * Sets the value of the defaultUOM property.
     *
     * @param defaultUOM
     */
    public void setDefaultUOM( OWSMetadata defaultUOM ) {
        this.defaultUOM = defaultUOM;
    }

}
