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
package org.deegree.protocol.wps.client.output.type;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.protocol.wps.client.param.ValueWithRef;

/**
 * {@link OutputType} that defines a literal output.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class LiteralOutputType extends OutputType {

    private ValueWithRef dataType;

    private ValueWithRef defaultUom;

    private ValueWithRef[] supportedUoms;

    public LiteralOutputType( CodeType id, LanguageString outputTitle, LanguageString outputAbstract,
                              ValueWithRef dataType, ValueWithRef defaultUom, ValueWithRef[] supportedUoms ) {
        super( id, outputTitle, outputAbstract );
        this.dataType = dataType;
        this.defaultUom = defaultUom;
        this.supportedUoms = supportedUoms;
    }

    @Override
    public Type getType() {
        return Type.LITERAL;
    }

    /**
     * 
     * @return the data type as {@link ValueWithRef} for this literal
     */
    public ValueWithRef getDataType() {
        return dataType;
    }

    /**
     * 
     * @return the default unit-of-measure used, as {@link ValueWithRef}
     */
    public ValueWithRef getDefaultUom() {
        return defaultUom;
    }

    /**
     * 
     * @return the supported units-of-measure used, as an array of {@link ValueWithRef}
     */
    public ValueWithRef[] getSupportedUoms() {
        return supportedUoms;
    }
}
