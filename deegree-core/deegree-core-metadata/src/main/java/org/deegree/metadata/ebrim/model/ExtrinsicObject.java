//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.metadata.ebrim.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.axiom.om.OMElement;

/**
 * Represents an additional ExtrinsicObject types
 * 
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class ExtrinsicObject extends RegistryObject {

    private final boolean isOpaque;

    private Object resource;

    private Map<String, Object> slotValues = new HashMap<String, Object>();

    public ExtrinsicObject( RegistryObject ro, Boolean isOpaque ) {
        super( ro );
        this.isOpaque = isOpaque;
    }

    public ExtrinsicObject( String id, String home, String lid, String status, String name, String desc,
                            String versionInfo, String extId, String objectType, boolean isOpaque, OMElement element ) {
        super( id, home, lid, status, name, desc, versionInfo, extId, objectType, element );
        this.isOpaque = isOpaque;
    }

    public void addSlot( String slot, Object value ) {
        slotValues.put( slot, value );
    }

    public Object getSlotValue( String slot ) {
        return slotValues.get( slot );
    }

    /**
     * @return the isOpaque
     */
    public boolean isOpaque() {
        return isOpaque;
    }

    /**
     * @return
     */
    public Object getResource() {
        return resource;
    }

    /**
     * @param resource
     *            the resource to set
     */
    public void setResource( Object resource ) {
        this.resource = resource;
    }

}
