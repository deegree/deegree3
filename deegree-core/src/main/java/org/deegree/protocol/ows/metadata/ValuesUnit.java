//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.protocol.ows.metadata;

/**
 * The <code>ValuesUnit</code> bean encapsulates the corresponding GetCapabilities response metadata element.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class ValuesUnit {

    private String uomName;

    private String uomURI;

    private String referenceSystemName;

    private String referenceSystemURL;

    /**
     * @param uomName
     */
    public void setUomName( String uomName ) {
        this.uomName = uomName;
    }

    /**
     * @return uomName, may be <code>null</code>.
     */
    public String getUomName() {
        return uomName;
    }

    /**
     * @param uomURI
     */
    public void setUomURI( String uomURI ) {
        this.uomURI = uomURI;
    }

    /**
     * @return uomURI, may be <code>null</code>.
     */
    public String getUomURI() {
        return uomURI;
    }

    /**
     * @param referenceSystemName
     */
    public void setReferenceSystemName( String referenceSystemName ) {
        this.referenceSystemName = referenceSystemName;
    }

    /**
     * @return referenceSystemName, may be <code>null</code>.
     */
    public String getReferenceSystemName() {
        return referenceSystemName;
    }

    /**
     * @param referenceSystemURI
     */
    public void setReferenceSystemURL( String referenceSystemURI ) {
        this.referenceSystemURL = referenceSystemURI;
    }

    /**
     * @return referenceSystemURL, may be <code>null</code>.
     */
    public String getReferenceSystemURL() {
        return referenceSystemURL;
    }

}
