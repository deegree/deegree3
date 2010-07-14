//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.services.wps.annotations.input;

import static org.deegree.services.wps.annotations.ProcessDescription.NOT_SET;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.deegree.services.wps.annotations.ProcessDescription;
import org.deegree.services.wps.annotations.commons.ReferenceType;
import org.deegree.services.wps.annotations.commons.Type;

/**
 * The <code>LiteralOutput</code> annotates an input parameter of {@link Type#Literal}.
 * <p>
 * Note: The name of this annotation is slightly 'un-Java', this way it won't interfere with
 * {@link org.deegree.services.wps.input.LiteralInput}
 * <p>
 * See {@link ProcessDescription} for a brief introduction to assigning values to annotations.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface LitInput {

    /** The datatype of the literal input */
    public ReferenceType dataType();

    /** optional The units-of-measure of the literal input, uoms[0] will be the default uom */
    public ReferenceType[] uoms() default {};

    /** optional the default value of the literal input */
    public String defaultValue() default NOT_SET;

    /** allowed values of the literal input, if not set, the {@link #validValueReference()} will be tried */
    public ValueType[] allowedValues() default {};

    /** optional annotation, pointing to an external list of allowed values */
    public ValueReference validValueReference() default @ValueReference;

}
