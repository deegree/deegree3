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

function History(controller, maxSteps) {

    // variable declaration
    this.controller = controller;
    this.maxSteps = 0; // TODO: implement maxSteps

    this.message1 = "It is not possible to move to the previous map, because you are at the initial map."
    this.message2 = "It is not possible to move to the next map, because you are at the final map."

    // method declaration
    this.init = init;
    this.addEnvelope = addEnvelope;
    this.moveBack = moveBack;
    this.moveForward = moveForward;

    // method implementation

    /*
     * this method is called when the portal is loaded, in Controller.init(), in viewcontext.xml
     */
    function init(initialEnvelope) {
        // obsolete, using OpenLayers history control
    }

    /*
     * this method is called when following events occur:
     * 
     * PAN (use hand icon to move map) BOX (mode = zoomin, zoomout, recenter) BBOX (use mouse to open new bbox) MOVE
     * (use arrows to navigate)
     */
    function addEnvelope(envelope) {
        // obsolete, using OpenLayers history control
    }

    function moveBack() {
        var ctrl = this.controller.vOLMap.map.getControlsByClass('OpenLayers.Control.NavigationHistory')[0];

        if (ctrl.previousStack.length === 1) {
            alert(this.message1);
        } else {
            ctrl.previousTrigger();
        }
    }

    function moveForward() {
        var ctrl = this.controller.vOLMap.map.getControlsByClass('OpenLayers.Control.NavigationHistory')[0];

        if (ctrl.nextStack.length === 0) {
            alert(this.message2);
        } else {
            ctrl.nextTrigger();
        }
    }
}
