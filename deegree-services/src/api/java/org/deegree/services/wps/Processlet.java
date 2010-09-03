//$HeadURL$
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

package org.deegree.services.wps;

/**
 * Implementations of this interface are (usually GIS-related) processes that can be registered in the deegree WPS, so
 * they can be accessed over the web by the means of the OpenGIS Web Processing Service protocol.
 * <p>
 * <h4>Processes, Processlets and the WPS</h4>
 * </p>
 * <p>
 * A {@link Processlet} may offer calculations as simple as subtracting one set of spatially referenced numbers from
 * another (e.g., determining the difference in influenza cases between two different seasons), or as complicated as a
 * global climate change model.
 * </p>
 * <p>
 * The deegree WPS is a feature-complete, efficient and scalable container for processlets (Java classes implementing
 * the {@link Processlet} interface). The WPS handles the protocol as defined by the OGC Web Processing Service
 * specification while a {@link Processlet} implements the computational logic of a concrete process.
 * </p>
 * <p>
 * A deegree WPS process consists of a Java class implementing the {@link Processlet} interface and an XML configuration
 * file that has to validate against <a href="http://schemas.deegree.org/wps/0.5.0/process_definition.xsd">
 * http://schemas.deegree.org/wps/0.5.0/process_definition.xsd</a>). Besides the definition of metadata, the XML
 * configuration file references the implementation class with its fully qualified class name.
 * </p>
 * <p>
 * <h4>Accessing the input and output parameters of the Processlet</h4>
 * </p>
 * <p>
 * <b>NOTE</b>: The {@link Processlet} code must never create the input and output parameter objects manually -- they
 * are instantiated by the WPS automatically and given as parameters to the
 * {@link #process(ProcessletInputs, ProcessletOutputs, ProcessletExecutionInfo)} method. A {@link Processlet} just
 * needs to retrieve the parameter objects that it expects and read/set their values.
 * <p>
 * It is essential to know the relevant interfaces for dealing with input and output data. For input data, please refer
 * to:
 * <ul>
 * <li>{@link org.deegree.services.wps.input.BoundingBoxInput}</li>
 * <li>{@link org.deegree.services.wps.input.ComplexInput}</li>
 * <li>{@link org.deegree.services.wps.input.LiteralInput}</li>
 * </ul>
 * </p>
 * For output data, please refer to:
 * <ul>
 * <li>{@link org.deegree.services.wps.output.BoundingBoxOutput}</li>
 * <li>{@link org.deegree.services.wps.output.ComplexOutput}</li>
 * <li>{@link org.deegree.services.wps.output.LiteralOutput}</li>
 * </ul>
 * </p>
 * <p>
 * <h4>Notes on thread-safety</h4>
 * </p>
 * <p>
 * As the {@link Processlet} lifecycle concept is analogous to that of Java <code>Servlet</code>s, the implementer is
 * responsible of ensuring thread-safety: The container only creates one instance of a specific {@link Processlet},
 * regardless of the number of simultaneous executions. This implies that the implementation of the
 * {@link #process(ProcessletInputs, ProcessletOutputs, ProcessletExecutionInfo)} method <b>must not</b> change any
 * static or member variables to perform the computation. If you're unsure about this, please check out documentation on
 * the thread-safe implementation of <code>Servlet</code>s.
 * </p>
 * <p>
 * For a more detailed tutorial on implementing your own process please refer to the <a
 * href="https://wiki.deegree.org/deegreeWiki/HowToCreateWPSProcesses">deegree wiki<a>.
 * </p>
 * 
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface Processlet {

    /**
     * Called by the {@link ProcessManager} to perform the execution of this {@link Processlet}.
     * <p>
     * The typical workflow is:
     * <ol>
     * <li>Get inputs from <code>in</code> parameter</li>
     * <li>Parse inputs into the required format (e.g. GML)</li>
     * <li>Do computation.</li>
     * <li>Transform computational results into required format (e.g. GML)</li>
     * <li>Write results to <code>out</code> parameter</li>
     * </ol>
     * Please consider the corresponding <a
     * href="http://wiki.deegree.org/deegreeWiki/deegree3/HowToCreateWPSProcesses">wiki howto</a>.
     * 
     * @param in
     *            input arguments to be processed, never <code>null</code>
     * @param out
     *            used to store the process outputs, never <code>null</code>
     * @param info
     *            can be used to provide execution information, i.e. percentage completed and start/success messages
     *            that it wants to make known to clients, never <code>null</code>
     * @throws ProcessletException
     *             may be thrown by the processlet to indicate a processing exception
     */
    public void process( ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info )
                            throws ProcessletException;

    /**
     * Called by the {@link ProcessManager} to indicate to a {@link Processlet} that it is being placed into service.
     */
    public void init();

    /**
     * Called by the {@link ProcessManager} to indicate to a {@link Processlet} that it is being taken out of service.
     * <p>
     * This method gives the {@link Processlet} an opportunity to clean up any resources that are being held (for
     * example, memory, file handles, threads) and make sure that any persistent state is synchronized with the
     * {@link Processlet}'s current state in memory.
     * </p>
     */
    public void destroy();
}