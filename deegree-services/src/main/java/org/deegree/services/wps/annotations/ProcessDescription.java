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

package org.deegree.services.wps.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.deegree.services.wps.annotations.commons.Metadata;
import org.deegree.services.wps.annotations.input.InputParameter;
import org.deegree.services.wps.annotations.output.OutputParameter;

/**
 * The <code>ProcessDescription</code> annotation can be used to annotate a process with a description. For a live
 * example usage see. Basically it looks like this:
 * <ul>
 * <li>Assigning a String or native type to an annotation-method: <code>methodName = "value"</code></li>
 * <li>Assigning a native type/String array to an annotation-method: <code>methodName= { 1, 2, 3, 4}</code></li>
 * <li>Assigning another Annotation to annotation-method:
 * <code>methodName = @RequiredAnnotation( intMethodName = 2, secondMethod= "value" )</code></li>
 * <li>Assigning Annotation array to an annotation-method:
 * <code>methodName= { @RequiredAnnotation( method= "value" ), @RequiredAnnotation( method= "value2" ) }</code></li>
 * <li>Assigning an empty array to an annotation-method: <code>methodName= { }</code></li>
 * <li>Assigning a <code>null</code> value is <b>not</b> allowed, instead you can use {@link ProcessDescription#NOT_SET}
 * for String values which were not set.</li>
 * </ul>
 * 
 * 
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProcessDescription {

    /** Indicating the String was not set, but is optional. */
    public final static String NOT_SET = "_NS_";

    /** process version */
    public String version();

    /** true if store is supported, default = false */
    public boolean storeSupported() default false;

    /** true if status updates are supported, default = false */
    public boolean statusSupported() default false;

    /** id (identifier) of the process */
    public String id();

    /** title of the process */
    public String title();

    /** optional abstract of the process */
    public String abs() default NOT_SET;

    /** optional metadatas of the process */
    public Metadata[] metadata() default {};

    /** profiles the process implements */
    public String[] profile() default {};

    /** url to a wsdl file the process describes */
    public String wsdl() default NOT_SET;

    /** input parameters */
    public InputParameter[] input();

    /** output parameters */
    public OutputParameter[] output();
}
