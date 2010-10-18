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

package org.deegree.services.wps.annotations.output;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.deegree.services.wps.annotations.ProcessDescription;
import org.deegree.services.wps.annotations.commons.ComplexFormat;
import org.deegree.services.wps.annotations.commons.Type;

/**
 * The <code>ComplexOutput</code> annotates an output parameter of {@link Type#Complex}.
 * <p>
 * Note: The name of this annotation is slightly 'un-Java', this way it won't interfere with
 * {@link org.deegree.services.wps.output.ComplexOutput}
 * <p>
 * See {@link ProcessDescription} for a brief introduction to assigning values to annotations.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CmplxOutput {
    /**
     * The possible formats this process can output complex data to, example: <code>
     * 
     * @CmplxOutput( formats = { @ComplexFormat( mimeType="image/png", encoding="LATIN1",
     *               schema="http://schema.url.org"} ) ); </code>
     */
    public ComplexFormat[] formats();

}
