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
import org.deegree.model.types.Identifier;

/**
 * The CommandProcessor interface encapsulates the {@link Command} execution.
 * 
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author last edited by: $Author: kiehle$
 * 
 * @version $Revision: $, $Date: 08.04.2008 16:38:07$
 */
public interface CommandProcessor {

    /**
     * executes a {@link Command} asynchronoulsy
     * 
     * @param command
     *            command to perform; if {@link Command} is an instance of {@link PersistableCommand}
     *            it will be persited into the configured backend before executing
     * @param timeout
     *            timeout (millis) after processing will be canceled
     * @param executionStart
     *            date/time when execution shall start or <code>null</code> if it shall start
     *            directly
     * @param cyclic
     *            true if a {@link Command} shall be executed in a cycle
     * @param interval
     *            time length of the cycle (millis)
     * @param repeatOnFailture
     *            nummber of tries to execute a {@link Command} if execution failed. If
     *            repeatOnFailture > 0 the interval will be used to determine the time lags between
     *            tries.
     */
    public void executeAsynchronously( Command command, long timeout, Calendar executionStart, boolean cyclic,
                                       long interval, int repeatOnFailture );

    /**
     * Executes a {@link Command} synchronously. If the pass {@link Command} can not be processed in
     * passed timeout an {@link CommandProcessorException} will be thrown
     * 
     * @param command
     * @param timeout
     */
    public CommandResult executeSychronously( Command command, long timeout )
                            throws CommandProcessorException;

    /**
     * executes a {@link Command} asynchronoulsy. Additionall a (@link CommandProcessorListener) may
     * be registered to be informed about events
     * 
     * @param command
     * @param timeout
     * @param executionStart
     * @param cyclic
     * @param interval
     * @param repeatOnFailture
     * @param registeredListener
     */
    public void executeAsynchronoously( Command command, long timeout, Calendar executionStart, boolean cyclic,
                                        long interval, int repeatOnFailture, CommandProcessorListener registeredListener );

    /**
     * 
     * Executes a {@link Command} synchronously. Additionall a (@link CommandProcessorListener) may
     * be registered to be informed about events
     * 
     * @param command
     * @param timeout
     * @param registeredListener
     * @return
     * @throws CommandProcessorException
     */
    public CommandResult executeSychronously( Command command, long timeout, CommandProcessorListener registeredListener )
                            throws CommandProcessorException;

    /**
     * 
     * @param identifier
     */
    public void cancelCommand( Identifier identifier );

    /**
     * 
     * @param identifier
     */
    public void pauseCommand( Identifier identifier );

    /**
     * 
     * @param idenditfier
     */
    public void restartCommand( Identifier idenditfier );

    /**
     * 
     * @param identifier
     */
    public CommandState getStatus( Identifier identifier );

    /**
     * registers listeners that will be informed if processing of a {@link Command} has been
     * finished.
     * 
     * @param listener
     */
    public void addCommandProcessedListener( CommandProcessorListener listener );

    /**
     * 
     * @param listener
     */
    public void removeCommandProcessedListener( CommandProcessorListener listener );

}