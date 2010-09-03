//$Header: /deegreerepository/deegree/resources/eclipse/svn_classfile_header_template.xml,v 1.2 2007/03/06 09:44:09 bezema Exp $
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

package org.deegree.services.wps.execute;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.wps.WPSRequest;
import org.deegree.services.jaxb.wps.ProcessDefinition;
import org.deegree.services.wps.ProcessletInputs;

/**
 * Represents a WPS <code>Execute</code> request.
 *
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: padberg$
 *
 * @version $Revision$, $Date: 08.05.2008 13:54:31$
 */
public class ExecuteRequest extends WPSRequest {

    private ProcessDefinition processDef;

    private ProcessletInputs dataInputs;

    private ResponseForm responseForm;

    /**
     * Creates a new {@link ExecuteRequest} instance.
     *
     * @param version
     *            WPS protocol version
     * @param language
     *            RFC 4646 language code of the human-readable text, may be null
     * @param processDef
     *            identifier of the process to be executed
     * @param dataInputs
     *            input (or parameter) values provided to the process, may be null
     * @param responseForm
     *            defines the response type of the WPS, may be null
     */
    public ExecuteRequest( Version version, String language, ProcessDefinition processDef, ProcessletInputs dataInputs,
                           ResponseForm responseForm ) {
        super( version, language );
        this.processDef = processDef;
        this.dataInputs = dataInputs;
        this.responseForm = responseForm;
    }

    /**
     * Returns the identifier of the process to be executed.
     *
     * @return the identifier of the process to be executed
     */
    public CodeType getProcessId() {
        return new CodeType(processDef.getIdentifier().getValue(), processDef.getIdentifier().getCodeSpace());
    }

    /**
     * Returns the definition of the process to be executed.
     *
     * @return the definition of the process to be executed
     */
    public ProcessDefinition getProcessDefinition() {
        return processDef;
    }

    /**
     * Returns the input parameter values provided to the process.
     *
     * @return the input parameter values provided to the process, may be null
     */
    public ProcessletInputs getDataInputs() {
        return dataInputs;
    }

    /**
     * Returns the requested response type.
     *
     * @return the requested response type, or null for default response type
     */
    public ResponseForm getResponseForm() {
        return responseForm;
    }

    @Override
    public String toString() {
        return ( "Request: Execute " + super.toString() + ", identifier: " + processDef.getIdentifier()
                 + ", DataInputs: " + dataInputs + ", ResponseForm: " + responseForm );
    }
}
