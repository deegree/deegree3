//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/framework/log/LoggerServiceTest.java $
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
package org.deegree.framework.log;

import alltests.AllTests;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This test case executes the underlying logging service.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 *
 * @author last edited by: $Author: mschneider $
 *
 * @version 2.0, $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 *
 * @since 2.0
 */

public class LoggerServiceTest extends TestCase {


    private ILogger LOG = LoggerFactory.getLogger(LoggerService.class);

    /**
     * Constructor for LoggerTest.
     *
     * @param arg0
     */
    public LoggerServiceTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return new TestSuite(LoggerServiceTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        LOG.logInfo("Testing logger: " + this.LOG.toString());

    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testLogException() {
        this.LOG.logDebug("This is a test debug message with exception, it MUST NOT appear in the log file",
                          new Exception("This is NOT an error, only a test exception"));
        this.LOG.logInfo("This is a test info message with exception", new Exception(
        "This is NOT an error, only a test exception"));
        this.LOG.logWarning("This is a test warning message with exception", new Exception(
        "This is NOT an error, only a test exception"));
        this.LOG.logError("This is a test error message with exception", new Exception(
        "This is NOT an error, only a test exception"));
    }

    public void testLog() {
        this.LOG.logDebug("invisible debug message");
        this.LOG.logInfo("visible info message");
        this.LOG.logWarning("visible warning message");
        this.LOG.logError("visible error message");
    }

    public void testToString() {
        assertNotNull("Logger is null", LOG);
        assertTrue(LOG.toString().length()>0);
    }

    public void testIsDebug() {
        assert(LOG.getLevel() == ILogger.LOG_INFO);
        assertFalse( LOG.isDebug() );
    }

}
