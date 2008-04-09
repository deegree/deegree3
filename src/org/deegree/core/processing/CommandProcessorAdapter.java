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
 * The CommandProcessorAdapter is the default implementation of the {@link CommandProcessorListener} interface. 
 *
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author last edited by: $Author: kiehle$
 *
 * @version $Revision: $, $Date: 08.04.2008 16:38:07$
 */
public class CommandProcessorAdapter implements CommandProcessorListener {

    /* (non-Javadoc)
     * @see org.deegree.core.processing.CommandProcessorListener#commandCancelled(org.deegree.core.processing.CommandProcessorEvent)
     */
    public void commandCancelled( CommandProcessorEvent event ) {        
    }

    /* (non-Javadoc)
     * @see org.deegree.core.processing.CommandProcessorListener#commandFinished(org.deegree.core.processing.CommandProcessorEvent)
     */
    public void commandFinished( CommandProcessorEvent event ) {
   
    }

    /* (non-Javadoc)
     * @see org.deegree.core.processing.CommandProcessorListener#commandResumed(org.deegree.core.processing.CommandProcessorEvent)
     */
    public void commandResumed( CommandProcessorEvent event ) {

        
    }

    /* (non-Javadoc)
     * @see org.deegree.core.processing.CommandProcessorListener#commandStarted(org.deegree.core.processing.CommandProcessorEvent)
     */
    public void commandStarted( CommandProcessorEvent event ) {
  
        
    }

    /* (non-Javadoc)
     * @see org.deegree.core.processing.CommandProcessorListener#commandStopped(org.deegree.core.processing.CommandProcessorEvent)
     */
    public void commandStopped( CommandProcessorEvent event ) {

        
    }

 

}
