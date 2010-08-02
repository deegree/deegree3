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

package org.deegree.services.wps.output;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.input.ComplexInput;

/**
 * Represents the requested properties for an output argument (e.g. format, encoding, schema) for a {@link Processlet}
 * execution and provides a sink for the output data.
 * <p>
 * A {@link ProcessletOutput} instance is always one of the following:
 * <ul>
 * <li>{@link LiteralOutput}: Identifies the output as literal data of a simple quantity (e.g., one number), and
 * provides a storage for this data.</li>
 * <li>{@link BoundingBoxOutput}: Identifies the output as an ows:BoundingBox data structure, and provides a storage for
 * this data.</li>
 * <li>{@link ComplexInput}: Identifies the output as a complex data structure encoded in XML (e.g., using GML), and
 * provides a storage for this complex data structure.</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public interface ProcessletOutput {

    /**
     * Returns the identifier or name of the output parameter as defined in the process description.
     * 
     * @return the identifier of the output parameter
     */
    public CodeType getIdentifier();

    /**
     * Returns the title that has been supplied with the request of the output parameter, normally available for display
     * to a human.
     * 
     * @return the title provided with the output, may be null
     */
    public LanguageString getSubmittedTitle();

    /**
     * Returns the narrative description that has been supplied with the request of the output parameter, normally
     * available for display to a human.
     * 
     * @return the abstract provided with the output, may be null
     */
    public LanguageString getSubmittedAbstract();

    /**
     * Returns whether this output parameter has been requested by the client, i.e. if it will be present in the result.
     * <p>
     * NOTE: If the parameter is requested, the {@link Processlet} must set a value for this parameter, if not, it may
     * or may not do so. However, for complex output parameters that are not requested, it is advised to omit them for
     * more efficient execution of the {@link Processlet}.
     * </p>
     * 
     * @return true, if the {@link Processlet} must set the value of this parameter (in this execution), false otherwise
     */
    public boolean isRequested();

    /**
     * Sets the parameter title in the response sent to the client.
     * 
     * @param title
     *            the parameter title in the response sent to the client
     */
    public void setTitle( LanguageString title );

    /**
     * Sets the parameter abstract in the response sent to the client.
     * 
     * @param summary
     *            the parameter abstract in the response sent to the client
     */
    public void setAbstract( LanguageString summary );
}
