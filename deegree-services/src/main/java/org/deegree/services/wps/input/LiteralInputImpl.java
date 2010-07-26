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
import org.deegree.services.jaxb.wps.LiteralInputDefinition;
import org.deegree.services.jaxb.wps.LiteralInputDefinition.DataType;

/**
 * A literal data {@link ProcessletInput} parameter of a simple quantity (e.g., one number) with optional UOM
 * (unit-of-measure) information.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class LiteralInputImpl extends ProcessletInputImpl implements LiteralInput {

    private String value;

    private String uom;

    /**
     * Creates a new {@link LiteralInputImpl} instance.
     *
     * @param definition
     *            corresponding input definition from process description
     * @param title
     *            optional title supplied with the input parameter, may be null
     * @param summary
     *            optional narrative description supplied with the input parameter, may be null
     * @param value
     *            literal value
     * @param uom
     *            optional unit-of-measure, must be one of the UOMs values from the definition, or null if no UOMs are
     *            defined there
     */
    public LiteralInputImpl( LiteralInputDefinition definition, LanguageString title, LanguageString summary, String value,
                         String uom ) {
        super( definition, title, summary );
        this.value = value;
        this.uom = uom;
    }

    /**
     * Returns the literal value.
     *
     * @see #getUOM()
     * @return the literal value (has to be in the correct UOM)
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the UOM (unit-of-measure) for the literal value, it is guaranteed that the returned UOM is supported for
     * this parameter (according to the process description).
     *
     * @return the requested UOM (unit-of-measure) for the literal value, may be null if no UOM is specified in the
     *         process description
     */
    public String getUOM() {
        return uom;
    }

    /**
     * Returns the (human-readable) literal data type from the process definition, e.g. <code>integer</code>,
     * <code>real</code>, etc).
     *
     * @return the data type, or null if not specified in the process definition
     */
    public String getDataType() {
        DataType dataType = ( (LiteralInputDefinition) definition ).getDataType();
        return dataType != null ? dataType.getValue() : null;
    }

    @Override
    public LiteralInputDefinition getDefinition() {
        return (LiteralInputDefinition) definition;
    }

    @Override
    public String toString() {
        return super.toString() + " (LiteralData), value='" + value + "'" + ", dataType='" + getDataType() + "', uom='"
               + uom + "'";
    }
}
