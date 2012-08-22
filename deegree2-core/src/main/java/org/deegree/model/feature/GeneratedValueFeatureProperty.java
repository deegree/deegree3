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
package org.deegree.model.feature;

import org.deegree.datatypes.QualifiedName;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GeneratedValueFeatureProperty extends DefaultFeatureProperty {

    private static final long serialVersionUID = 4550233124091558045L;

    private ValueGenerator valueGenerator;

    /**
     * @param name
     * @param valueGenerator
     */
    public GeneratedValueFeatureProperty( QualifiedName name, ValueGenerator valueGenerator ) {
        super( name, null );
        this.valueGenerator = valueGenerator;
        super.setValue( this.valueGenerator.generate() );
    }
    
    /**
     * 
     * @return {@link ValueGenerator}
     */
    public ValueGenerator getValueGenerator() {
       return valueGenerator; 
    }

    /**
     * changes behavior of super class {@link #setValue(Object)} method. Passed value will be ignored; instead of this @see
     * {@link ValueGenerator#generate()} will be invoked and generated value will be passed
     */
    @Override
    public void setValue( Object value ) {
        if ( this.valueGenerator != null ) {
            super.setValue( this.valueGenerator.generate() );
        }
    }
}
