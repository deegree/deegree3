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

import org.deegree.model.types.Identifier;
import org.deegree.security.model.User;

/**
 * Declaration of a Command. All Commands submitted to the Command Processor must implement this
 * interface.
 * 
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author last edited by: $Author: kiehle$
 * 
 * @version $Revision: $, $Date: 08.04.2008 16:38:07$
 */
public interface Command {

    /**
     * 
     * @return unique {@link Command} identifier
     */
    public Identifier getIdentifier();

    /**
     * 
     * @return {@link Command} processing result
     */
    public CommandResult getResult();

    /**
     * 
     * @return current status of a {@link Command}
     */
    public CommandState getStatus();

    /**
     * executes a {@link Command}
     * 
     */
    public void execute();

    /**
     * cancels {@link Command} execution
     * 
     */
    public void cancel();

    /**
     * pauses {@link Command} execution
     * 
     */
    public void pause();

    /**
     * restarts {@link Command} execution if it has been paused
     * 
     */
    public void resume();

    /**
     * 
     * @param priority
     */
    public void setPriority( int priority );

    /**
     * 
     * @return owner of a {@link Command} (<code>null</code> not owned by anyone except deegree)
     */
    public User getOwner();

    /**
     * sets a listener to be informed if {@link Command} execution has been finished or canceled
     * 
     * @param listener
     */
    public void setCommandProcessorListener( CommandProcessorListener listener );

    /**
     * 
     * @param processMonitor
     */
    public void setProcessMonitor( ProcessMonitor processMonitor );

}