//$HeadURL: http://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

import es.unex.sextante.core.ITaskMonitor;
import es.unex.sextante.core.OutputFactory;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.rasterWrappers.GridExtent;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
import javax.swing.JDialog;

/**
 * Factory to create output objects.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class OutputFactoryImpl extends OutputFactory {

    @Override
    public Object getDefaultCRS() {
        return "EPSG:4326";
    }

    @Override
    public IRasterLayer getNewRasterLayer( String sName, int iDataType, GridExtent extent, int iBands,
                                           IOutputChannel channel, Object crs )
                            throws UnsupportedOutputChannelException {
        throw new UnsupportedOperationException();
        // return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ITable getNewTable( String sName, Class[] types, String[] sFields, IOutputChannel channe )
                            throws UnsupportedOutputChannelException {
        throw new UnsupportedOperationException();
        // return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IVectorLayer getNewVectorLayer( String iName, int iShapeType, Class[] types, String[] sFields,
                                           IOutputChannel channel, Object crs )
                            throws UnsupportedOutputChannelException {

        // LOG.info(channel.getAsCommandLineParameter());

        return new VectorLayerImpl( iName, crs.toString(), Field.createFieldArray( sFields, types ) );
    }

    @SuppressWarnings("unchecked")
    @Override
    public IVectorLayer getNewVectorLayer( String iName, int iShapeType, Class[] types, String[] sFields,
                                           IOutputChannel channel, Object crs, int[] fieldSize )
                            throws UnsupportedOutputChannelException {

        // LOG.info(channel.getAsCommandLineParameter());

        return new VectorLayerImpl( iName, crs.toString(), Field.createFieldArray( sFields, types ) );
    }

    @Override
    public String[] getRasterLayerOutputExtensions() {
        throw new UnsupportedOperationException();
        // return null;
    }

    @Override
    public String[] getTableOutputExtensions() {
        throw new UnsupportedOperationException();
        // return null;
    }

    @Override
    public ITaskMonitor getTaskMonitor( String sTitle, boolean bDeterminate, JDialog parent ) {
        throw new UnsupportedOperationException();
        // return null;
    }

    @Override
    protected String getTempFolder() {
        return System.getProperty( "java.io.tmpdir" );
    }

    @Override
    public String[] getVectorLayerOutputExtensions() {
        return new String[] { "gml" };
    }

}
