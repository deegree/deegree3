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
/**
 * encapsulating openlayers map for enabling communication with iGeoPortal controller
 */
function OLMap(mapModel) {
    // variables
    this.mapModel = mapModel;
    this.map;
    this.eventType = -1;
    this.div;
    this.click;
    this.targetDocument;

    // methods
    this.paint = paint;
    this.repaint = repaint;
    this.setMap = setMap
    this.getMap = getMap
    this.mapEvent = mapEvent;
    this.initOpenLayers = initOpenLayers;
    this.updateLayers = updateLayers;
    this.onDbClick = onDbClick;
    this.onClick = onClick;
    this.deactivateClickHandler = deactivateClickHandler;
    this.activateClickHandler = activateClickHandler;
    this.initFence = initFence;
    this.initDrawPoint = initDrawPoint;
    this.initDrawCurve = initDrawCurve;
    this.initDrawPolygon = initDrawPolygon;
    this.initModifyFeature = initModifyFeature;
    this.resetDigitizer = resetDigitizer;
    this.initFeatureDragging = initFeatureDragging;
    this.initSelectFeature = initSelectFeature;
    this.pushLayers = pushLayers;

    /**
     * implements mandatory paint method; will be invoke first time a module will be displayed
     */
    function paint(targetDocument, parentNode) {
        this.targetDocument = targetDocument;
        this.div = targetDocument.getElementById('map');
        this.repaint();
    }

    /**
     * implements mandatory repaint method; will be invoked every time repaint-method of the controller will be invoked
     */
    function repaint() {
        this.div.style.width = (controller.mapModel.getWidth()) + "px";
        this.div.style.height = (controller.mapModel.getHeight()) + "px";

        if (this.map != null && this.eventType == -1) {
            var bbox = controller.mapModel.getBoundingBox();
            this.map.zoomToExtent(new OpenLayers.Bounds(bbox.minx, bbox.miny, bbox.maxx, bbox.maxy));
        }

        if (this.map != null && this.eventType != 0 && controller.mapModel.isChanged()) {
            this.updateLayers();
        }
        this.eventType = -1;
    }

    /**
     * updates the list of visible layers within openlayers map
     * 
     * @return
     */
    function updateLayers() {
        var num = this.map.getNumLayers();
        var cp = new Array();
        for ( var i = 0; i < num; i++) {
            cp[i] = this.map.layers[i];
        }
        for ( var i = num - 1; i >= 0; i--) {
            if (cp[i].name != '_BOXESMARKER_') {
                this.map.removeLayer(cp[i]);
            }
        }

        // var wfslayer = new OpenLayers.Layer.Vector( "WFS", {
        // strategies : [ new OpenLayers.Strategy.BBOX() ],
        // protocol : new OpenLayers.Protocol.WFS( {
        // url : "http://demo.deegree.org:80/deegree-wfs/services",
        // featureType : "CountyBoundaries_edited",
        // featureNS : "http://www.deegree.org/app",
        // srsName : "EPSG:26912",
        // version : "1.1.0"
        // })
        // } );
        // this.map.addLayer( wfslayer );

        // the repeated re-adding of layers is not a good thing, it causes a whole lot of this.map == null problems
        this.pushLayers();
    }

    function pushLayers() {
        var layerGroups = controller.mapModel.getLayerList().getLayerGroups();

        var self = this

        // dummy layer will be used to enable all WMS based layers not to be a base layer
        // and so can be used with transparent = true
        var dummy = new OpenLayers.Layer("_DUMMY_BASE_", {
            isBaseLayer : true
        });
        dummy.setMap(self.map)
        self.map.addLayer(dummy);

        // get session id if user has logged in
        var sid = "";
        if (controller.vSessionKeeper != null) {
            sid = controller.vSessionKeeper.id;
        }

        // create OL layers for each available layergroup. As far a possible layers from one group
        // will be collected and gathered within on GetMap request to reduce network loading overhead
        for ( var j = layerGroups.length - 1; j >= 0; j--) {
            var layers = layerGroups[j].getLayers();
            var time = layerGroups[j].getTime();
            // find all visible layers in a group
            var vLayers = new Array();
            for ( var i = layers.length - 1; i >= 0; i--) {
                if (layers[i].isVisible()) {
                    vLayers.push(layers[i]);
                }
            }

            // create OL layer by collecting all following up layers that use the same
            // tiling strategy
            var l = "";
            for ( var i = 0; i < vLayers.length; i++) {
                if (i > 0 && vLayers[i].isTiled() != vLayers[i - 1].isTiled()) {
                    l = l.substring(0, l.length - 1);
                    var layer = new OpenLayers.Layer.WMS(layerGroups[j].getServiceName() + j + " " + i, layerGroups[j]
                            .getServiceURL(), {
                        layers : l,
                        transparent : true,
                        format : 'image/png',
                        sessionID : sid
                    }, {
                        // using singleTile=true here causes the this.map == null problems in the console
                        singleTile : !vLayers[i - 1].isTiled()
                    });
                    if (time != null) {
                        layer.mergeNewParams({
                            'time' : time
                        });
                    }
                    layer.setMap(this.map)
                    this.map.addLayer(layer);
                    l = "";
                }
                l += vLayers[i].getName() + ',';
            }

            // create OL layer for the last set of layers having the same tiling strategy
            if (l.length > 1) {
                l = l.substring(0, l.length - 1);
                var layer = new OpenLayers.Layer.WMS(layerGroups[j].getServiceName() + j, layerGroups[j]
                        .getServiceURL(), {
                    layers : l,
                    transparent : true,
                    format : 'image/png',
                    sessionID : sid
                }, {
                    // using singleTile=true here causes the this.map == null problems in the console
                    singleTile : !vLayers[vLayers.length - 1].isTiled()
                });
                if (time != null) {
                    layer.mergeNewParams({
                        'time' : time
                    });
                }
                layer.setMap(this.map)
                this.map.addLayer(layer);
            }
        }
    }

    /**
     * sets the openlayers map object used by this class
     * 
     * @param map
     * @return
     */
    function setMap(map) {
        this.map = map;
        this.initOpenLayers();

        this.map.events.register("moveend", null, this.mapEvent);
        this.map.events.register("zoomend", null, this.mapEvent);
    }

    /**
     * @return openlayers map
     */
    function getMap() {
        return this.map;
    }

    /**
     * initializes openlayers map by adding visible layers and zooming to current bbox
     * 
     * @return
     */
    function initOpenLayers() {
        var self = this;

        var btn = window.controller.vToolbar.buttonGroups[0].buttons[0].targetDocument.getElementById('refresh');
        $(btn).click(function() {
            $.each(self.map.layers, function(idx, val) {
                val.redraw(true);
            });
        });

        this.pushLayers();

        // zoom to current BBOX
        var bbox = controller.mapModel.getBoundingBox();
        this.map.zoomToExtent(new OpenLayers.Bounds(bbox.minx, bbox.miny, bbox.maxx, bbox.maxy));

        // define a click handler
        OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {
            defaultHandlerOptions : {
                'single' : true,
                'double' : true,
                'pixelTolerance' : 2,
                'stopSingle' : true,
                'stopDouble' : true
            },

            initialize : function(options) {
                this.handlerOptions = OpenLayers.Util.extend({}, this.defaultHandlerOptions);
                OpenLayers.Control.prototype.initialize.apply(this, arguments);
                this.handler = new OpenLayers.Handler.Click(this, {
                    'click' : controller.vOLMap.onClick,
                    'dblclick' : controller.vOLMap.onDbClick
                }, this.handlerOptions);
            }

        });

        // add Click Control to map
        this.click = new OpenLayers.Control.Click();
        this.map.addControl(this.click);
        this.map.addControl(new OpenLayers.Control.NavigationHistory());

        this.activateClickHandler();
    }

    /**
     * handle click events
     */
    function onClick(e) {
        if (controller.mode != null) {
            controller.mode.onClick(e);
        }
    }

    /**
     * handle double click events
     */
    function onDbClick(e) {
        if (controller.mode != null) {
            controller.mode.onDbClick(e);
        }
    }

    /**
     * activate click control
     */
    function activateClickHandler() {
        this.click.activate();
    }

    /**
     * activate click control
     */
    function deactivateClickHandler() {
        this.click.deactivate();
    }

    /**
     * will be invoke by zoom or pan events on openlayers map
     * 
     * @param e
     * @return
     */
    function mapEvent(e) {
        controller.vOLMap.eventType = 0;
        var bb = controller.vOLMap.map.getExtent().toArray();
        var event = new Event(this, "BBOX", new Envelope(bb[0], bb[1], bb[2], bb[3]));
        controller.actionPerformed(event);
    }

    /**
     * adds a layer with a box that can be dragged to the map
     */
    function initFence(bbox, callback) {
        controller.getFrame('IDOLMap').initFence(bbox, callback);
    }

    function initDrawPoint(callback) {
        controller.getFrame('IDOLMap').initDrawPoint(callback);
    }

    function initDrawCurve(callback) {
        controller.getFrame('IDOLMap').initDrawCurve(callback);
    }

    function initDrawPolygon(callback) {
        controller.getFrame('IDOLMap').initDrawPolygon(callback);
    }

    function initModifyFeature(callback) {
        controller.getFrame('IDOLMap').initModifyFeature(callback);
    }

    function initSelectFeature(callback) {
        controller.getFrame('IDOLMap').initSelectFeature(callback);
    }

    function resetDigitizer() {
        controller.getFrame('IDOLMap').resetDigitizer();
    }

    function initFeatureDragging() {
        controller.getFrame('IDOLMap').initFeatureDragging();
    }
}
