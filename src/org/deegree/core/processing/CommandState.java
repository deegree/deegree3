//$Header: /deegreerepository/deegree/resources/eclipse/svn_classfile_header_template.xml$
/*----------------    FILE HEADER ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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

package org.deegree.core.processing;

import java.util.Calendar;

/**
 * A CommandState encapsulates an enumeration of possible CommandStates (i.e. finished, cancelled,
 * paused, processing) as well as getters to the most central portions of (@link STATE) information
 * (e.g. timestamps, duration, etc.)
 * 
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author last edited by: $Author: kiehle$
 * 
 * @version $Revision: $, $Date: 08.04.2008 16:38:07$
 */
public interface CommandState {

    /**
     * 
     * Enumeration holding potential states of a (@link Command).
     * 
     * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
     * @author last edited by: $Author: kiehle$
     * 
     * @version $Revision: $, $Date: 08.04.2008 17:06:47$
     */
    public enum STATE {
        started, stopped, resumed, cancelled, finished
    };

    /**
     * 
     * @return current command execution (@link STATE)
     */
    public STATE getState();

    /**
     * 
     * @return detailed descrition of the current (@link STATE) (should return an empty string
     *         instead of <code>null</code> if no description is available
     */
    public String getDescription();

    /**
     * 
     * @return timestamp of commiting a {@link Command} to the {@link CommandProcessor}
     */
    public Calendar getIncomingOrderTimestamp();

    /**
     * 
     * @return timestamp of starting a {@link Command}
     */
    public Calendar getStartExecutionTimestamp();

    /**
     * 
     * @return timestamp when execution of a {@link Command} has been finished
     */
    public Calendar getExecutionFinishedTimestamp();

    /**
     * Returns the real duration required for processing a {@link Command}. Real execution time
     * means the time needed for execution without possible idle time.
     * 
     * @return duration (millis) of a {@link Command} execution.
     */
    public long getRealExecutionDuration();

}