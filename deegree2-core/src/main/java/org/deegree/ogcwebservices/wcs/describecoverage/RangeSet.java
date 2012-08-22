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
package org.deegree.ogcwebservices.wcs.describecoverage;

import java.net.URI;

import org.deegree.datatypes.values.ValueEnum;
import org.deegree.ogcbase.Description;
import org.deegree.ogcbase.OGCException;
import org.deegree.ogcwebservices.MetadataLink;

/**
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */

public class RangeSet extends Description implements Cloneable {

    private URI semantic = null;

    private URI refSys = null;

    private String refSysLabel = null;

    private ValueEnum nullValues = null;

    private AxisDescription[] axisDescription = new AxisDescription[0];

    /**
     * @param name
     * @param label
     * @throws OGCException
     */
    public RangeSet( String name, String label ) throws OGCException {
        super( name, label );
    }

    /**
     * @param name
     * @param label
     * @param nullValues
     * @param axisDescription
     * @throws OGCException
     */
    public RangeSet( String name, String label, ValueEnum nullValues, AxisDescription[] axisDescription )
                            throws OGCException {
        super( name, label );
        this.nullValues = nullValues;
        setAxisDescription( axisDescription );
    }

    /**
     * @param name
     * @param label
     * @param description
     * @param metadataLink
     * @param semantic
     * @param refSys
     * @param refSysLabel
     * @param nullValues
     * @param axisDescription
     * @throws OGCException
     */
    public RangeSet( String name, String label, String description, MetadataLink metadataLink, URI semantic,
                     URI refSys, String refSysLabel, ValueEnum nullValues, AxisDescription[] axisDescription )
                            throws OGCException {
        super( name, label, description, metadataLink );
        this.semantic = semantic;
        this.refSys = refSys;
        this.refSysLabel = refSysLabel;
        this.nullValues = nullValues;
        if ( axisDescription != null ) {
            this.axisDescription = axisDescription;
        }
    }

    /**
     * @return Returns the axisDescription.
     */
    public AxisDescription[] getAxisDescription() {
        return axisDescription;
    }

    /**
     * @param axisDescription
     *            The axisDescription to set.
     */
    public void setAxisDescription( AxisDescription[] axisDescription ) {
        if ( axisDescription != null ) {
            this.axisDescription = axisDescription;
        }
    }

    /**
     * @return Returns the nullValues.
     */
    public ValueEnum getNullValues() {
        return nullValues;
    }

    /**
     * @param nullValues
     *            The nullValues to set.
     */
    public void setNullValues( ValueEnum nullValues ) {
        this.nullValues = nullValues;
    }

    /**
     * @return Returns the refSys.
     */
    public URI getRefSys() {
        return refSys;
    }

    /**
     * @param refSys
     *            The refSys to set.
     */
    public void setRefSys( URI refSys ) {
        this.refSys = refSys;
    }

    /**
     * @return Returns the refSysLabel.
     */
    public String getRefSysLabel() {
        return refSysLabel;
    }

    /**
     * @param refSysLabel
     *            The refSysLabel to set.
     */
    public void setRefSysLabel( String refSysLabel ) {
        this.refSysLabel = refSysLabel;
    }

    /**
     * @return Returns the semantic.
     */
    public URI getSemantic() {
        return semantic;
    }

    /**
     * @param semantic
     *            The semantic to set.
     */
    public void setSemantic( URI semantic ) {
        this.semantic = semantic;
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        ValueEnum nullValues_ = null;
        if ( nullValues_ != null ) {
            nullValues_ = (ValueEnum) nullValues.clone();
        }

        AxisDescription[] ad = new AxisDescription[axisDescription.length];
        for ( int i = 0; i < ad.length; i++ ) {
            ad[i] = (AxisDescription) axisDescription[i].clone();
        }
        Description des = (Description) super.clone();
        try {
            return new RangeSet( des.getName(), des.getLabel(), des.getDescription(), des.getMetadataLink(), semantic,
                                 refSys, refSysLabel, nullValues_, ad );
        } catch ( Exception e ) {
        }
        return null;
    }

}
