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
package org.deegree.services.wps;

import org.deegree.services.jaxb.wps.ProcessDefinition;

/**
 * Encapsulates the components that are needed by the {@link WPService} to offer a process.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WPSProcess {

    private final ProcessDefinition description;

    private final Processlet processlet;

    private final ExceptionCustomizer exceptionCustomizer;

    /**
     * Creates a new {@link WPSProcess} instance.
     * 
     * @param description
     *            description of the process (e.g. parameter types, metadata), must not be <code>null</code>
     * @param processlet
     *            process code, must not be <code>null</code>
     * @param exceptionCustomizer
     *            exception customizer, can be <code>null</code> (use default exception generation)
     */
    public WPSProcess( ProcessDefinition description, Processlet processlet, ExceptionCustomizer exceptionCustomizer ) {
        this.description = description;
        this.processlet = processlet;
        this.exceptionCustomizer = exceptionCustomizer;
    }

    /**
     * Returns the description (e.g. parameter types, metadata) of the process.
     * 
     * @return the description, never <code>null</code>
     */
    public ProcessDefinition getDescription() {
        return description;
    }

    /**
     * Returns the {@link Processlet} (process code) instance of the process.
     * 
     * @return the processlet, never <code>null</code>
     */
    public Processlet getProcesslet() {
        return processlet;
    }

    /**
     * Returns the exception customizer for the process.
     * 
     * @return exception customizer, can be <code>null</code>
     */
    public ExceptionCustomizer getExceptionCustomizer() {
        return exceptionCustomizer;
    }
}
