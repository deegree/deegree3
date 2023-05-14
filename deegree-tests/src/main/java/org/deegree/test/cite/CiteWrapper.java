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
package org.deegree.test.cite;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class CiteWrapper {

	private final String citeScript;

	private final PrintStream oldSysOut;

	private final PrintStream oldSysErr;

	private String out;

	private String err;

	CiteWrapper(String citeScript) {
		this.citeScript = citeScript;
		this.oldSysOut = System.out;
		this.oldSysErr = System.err;
	}

	void execute() throws Exception {

		String[] args = new String[] { "-cmd=-mode=test", "-mode=test", "-source=" + citeScript, "-workdir=/tmp" };
		try {
			ByteArrayOutputStream sysOut = new ByteArrayOutputStream();
			ByteArrayOutputStream sysErr = new ByteArrayOutputStream();
			System.setOut(new PrintStream(sysOut));
			System.setErr(new PrintStream(sysErr));

			// TODO what about the build path?
			com.occamlab.te.Test.main(args);
			out = sysOut.toString("UTF-8");
			err = sysErr.toString("UTF-8");
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			System.setOut(oldSysOut);
			System.setErr(oldSysErr);
		}
	}

	String getOutput() {
		return out;
	}

	String getError() {
		return err;
	}

}
