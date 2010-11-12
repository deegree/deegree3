//$HeadURL$
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
package org.deegree.tools.crs.georeferencing.model;

import org.deegree.tools.crs.georeferencing.application.transformation.TransformationMethod;
import org.deegree.tools.crs.georeferencing.application.transformation.TransformationMethod.TransformationType;
import org.deegree.tools.crs.georeferencing.communication.GRViewerGUI;
import org.deegree.tools.crs.georeferencing.communication.panel2D.BuildingFootprintPanel;
import org.deegree.tools.crs.georeferencing.communication.panel2D.Scene2DPanel;
import org.deegree.tools.crs.georeferencing.model.dialog.OptionDialogModel;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ControllerModel {
    private TransformationType transformationType;

    private TransformationMethod transform;

    private int order;

    private final GRViewerGUI view;

    private final BuildingFootprintPanel footPanel;

    private final Scene2DPanel panel;

    private final OptionDialogModel dialogModel;

    public ControllerModel( GRViewerGUI view, BuildingFootprintPanel footPanel, Scene2DPanel panel,
                            OptionDialogModel dialogModel ) {
        this.view = view;
        this.footPanel = footPanel;
        this.panel = panel;
        this.dialogModel = dialogModel;
    }

    public GRViewerGUI getView() {
        return view;
    }

    public BuildingFootprintPanel getFootPanel() {
        return footPanel;
    }

    public Scene2DPanel getPanel() {
        return panel;
    }

    public int getOrder() {
        return order;
    }

    public OptionDialogModel getDialogModel() {
        return dialogModel;
    }

    public void setOrder( int order ) {
        this.order = order;
    }

    public TransformationType getTransformationType() {
        return transformationType;
    }

    public void setTransformationType( TransformationType transformationType ) {
        this.transformationType = transformationType;
    }

    public TransformationMethod getTransform() {
        return transform;
    }

    public void setTransform( TransformationMethod transform ) {
        this.transform = transform;
    }

}
