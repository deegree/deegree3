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

/**
 * CommandGroup interface extends a single {@link Command} and defines Methods for adding and removing
 * {@link Command}s from a CommandGroup as well as setting an ExecutionPlan for CommandGroups.
 * 
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author last edited by: $Author: kiehle$
 * 
 * @version $Revision: $, $Date: 08.04.2008 16:38:07$
 */
public interface CommandGroup extends Command {

    /**
     * 
     * @param command
     */
    public void addCommand( Command command );

    /**
     * 
     * @param command
     */
    public void removeCommand( Command command );

    /**
     * 
     * @param executionPlan
     */
    public void setExecutionPlan( ExecutionPlan executionPlan );

}