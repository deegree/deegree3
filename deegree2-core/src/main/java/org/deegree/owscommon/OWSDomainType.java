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
package org.deegree.owscommon;

/**
 * Class representation of the type <code>ows:DomainType</code> defined in
 * <code>owsOperationsMetadata.xsd</code> from the <code>OWS Common Implementation
 * Specification 0.3</code>.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class OWSDomainType {

    private String name;

    private String[] values;

    private OWSMetadata[] metadata;

    /**
     *
     * @param name
     * @param metadata
     */
    public OWSDomainType( String name, OWSMetadata[] metadata ) {
        this.name = name;
        this.metadata = metadata;
        this.values = new String[0];
    }

    /**
     *
     * @param name
     * @param values
     * @param metadata
     */
    public OWSDomainType( String name, String[] values, OWSMetadata[] metadata ) {
        this.name = name;
        this.values = values;
        this.metadata = metadata;
    }

    /**
     * @return Returns the metadata.
     */
    public OWSMetadata[] getMetadata() {
        return metadata;
    }

    /**
     * @param metadata
     *            The metadata to set.
     */
    public void setMetadata( OWSMetadata[] metadata ) {
        this.metadata = metadata;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * @return Returns the values.
     */
    public String[] getValues() {
        return values;
    }

    /**
     * @param values
     *            The values to set.
     */
    public void setValues( String[] values ) {
        this.values = values;
    }
}
