//$HeadURL$
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
package org.deegree.tools.crs.georeferencing.application;

import java.util.List;

import javax.swing.ButtonModel;
import javax.swing.JToggleButton;
import javax.vecmath.Point2d;

import org.deegree.commons.utils.Triple;
import org.deegree.tools.crs.georeferencing.communication.PointTableFrame;
import org.deegree.tools.crs.georeferencing.communication.checkboxlist.CheckboxListTransformation;
import org.deegree.tools.crs.georeferencing.communication.dialog.coordinatejump.CoordinateJumperTextfieldDialog;
import org.deegree.tools.crs.georeferencing.communication.dialog.menuitem.OpenWMS;
import org.deegree.tools.crs.georeferencing.communication.dialog.menuitem.WMSParameterChooser;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.GenericSettingsPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.NavigationPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.OptionDialog;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.SettingsPanel;
import org.deegree.tools.crs.georeferencing.model.CheckBoxListModel;
import org.deegree.tools.crs.georeferencing.model.ControllerModel;
import org.deegree.tools.crs.georeferencing.model.RowColumn;
import org.deegree.tools.crs.georeferencing.model.Scene2D;
import org.deegree.tools.crs.georeferencing.model.mouse.FootprintMouseModel;
import org.deegree.tools.crs.georeferencing.model.mouse.GeoReferencedMouseModel;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;
import org.deegree.tools.crs.georeferencing.model.points.PointResidual;
import org.deegree.tools.crs.georeferencing.model.textfield.CoordinateJumperModel;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ApplicationState {

    boolean isHorizontalRefGeoref, isHorizontalRefFoot, start, isControlDown, selectedGeoref, selectedFoot,
                            isZoomInGeoref, isZoomInFoot, isZoomOutGeoref, isZoomOutFoot, isInitGeoref, isInitFoot;

    JToggleButton buttonZoomInGeoref, buttonZoominFoot, buttonZoomoutGeoref, buttonZoomoutFoot, buttonCoord,
                            buttonPanGeoref;

    ButtonModel buttonModel;

    Scene2D model;

    Scene2DValues sceneValues;

    PointTableFrame tablePanel;

    ParameterStore store;

    CoordinateJumperModel textFieldModel;

    GeoReferencedMouseModel mouseGeoRef;

    FootprintMouseModel mouseFootprint;

    Point2d changePoint;

    List<Triple<Point4Values, Point4Values, PointResidual>> mappedPoints;

    ControllerModel conModel;

    NavigationPanel optionNavPanel;

    SettingsPanel optionSettPanel;

    OptionDialog optionDialog;

    // private CoordinateJumperSpinnerDialog jumperDialog;
    CoordinateJumperTextfieldDialog jumperDialog;

    OpenWMS wmsStartDialog;

    WMSParameterChooser wmsParameter;

    GenericSettingsPanel optionSettingPanel;

    JToggleButton buttonPanFoot;

    CheckboxListTransformation checkBoxListTransform;

    CheckBoxListModel modelTransformation;

    RowColumn rc;

}
