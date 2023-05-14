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
package org.deegree.services.wps.provider;

import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.output.LiteralOutput;

/**
 * Parametrizable {@link Processlet} used by the {@link ExampleProcessProvider}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
class ConstantProcesslet implements Processlet {

	private String returnValue;

	/**
	 * Creates a new {@link ConstantProcesslet} instance that will always return the given
	 * value in the
	 * {@link #process(ProcessletInputs, ProcessletOutputs, ProcessletExecutionInfo)}
	 * method.
	 * @param returnValue value to be returned by the execution
	 */
	ConstantProcesslet(String returnValue) {
		this.returnValue = returnValue;
	}

	@Override
	public void destroy() {
		// nothing to do
	}

	@Override
	public void init() {
		// nothing to do
	}

	@Override
	public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info)
			throws ProcessletException {
		LiteralOutput literalOutput = (LiteralOutput) out.getParameter("LiteralOutput");
		literalOutput.setValue(returnValue);
	}

}
