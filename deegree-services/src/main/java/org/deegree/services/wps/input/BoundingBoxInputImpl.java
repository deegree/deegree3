//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.services.wps.input;

import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.geometry.Envelope;
import org.deegree.services.jaxb.wps.BoundingBoxInputDefinition;

/**
 * A {@link Process} input parameter with a bounding box value.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class BoundingBoxInputImpl extends ProcessletInputImpl implements BoundingBoxInput {

    private Envelope value;

    /**
     * Creates a new {@link BoundingBoxInputImpl} instance.
     *
     * @param definition
     *            corresponding input definition from process description
     * @param title
     *            optional title supplied with the input parameter, may be null
     * @param summary
     *            optional narrative description supplied with the input parameter, may be null
     * @param value
     *            bounding box value, the srs must be null or supported according to the input definition
     */
    public BoundingBoxInputImpl( BoundingBoxInputDefinition definition, LanguageString title, LanguageString summary,
                                 Envelope value ) {
        super( definition, title, summary );
        this.value = value;
    }

    @Override
    public String getCRSName() {
        return value.getCoordinateSystem() == null ? null : value.getCoordinateSystem().getName();
    }

    @Override
    public double[] getLower() {
        return value.getMin().getAsArray();
    }

    @Override
    public double[] getUpper() {
        return value.getMax().getAsArray();
    }

    @Override
    public Envelope getValue() {
        return value;
    }

    @Override
    public BoundingBoxInputDefinition getDefinition() {
        return (BoundingBoxInputDefinition) definition;
    }

    @Override
    public String toString() {
        return super.toString() + " (BoundingBoxData), bbox='" + value + "'";
    }
}
