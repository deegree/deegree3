function initDigitizerStyles() {
    return new OpenLayers.StyleMap({
        "default" : new OpenLayers.Style(null, {
            rules : [ new OpenLayers.Rule({
                symbolizer : {
                    "Point" : {
                        pointRadius : 5,
                        graphicName : "circle",
                        fillColor : "white",
                        fillOpacity : 0.25,
                        strokeWidth : 1,
                        strokeOpacity : 1,
                        strokeColor : "#3333aa"
                    },
                    "Line" : {
                        strokeWidth : 3,
                        strokeOpacity : 1,
                        strokeColor : "#6666aa"
                    },
                    "Polygon" : {
                        strokeWidth : 1,
                        strokeOpacity : 1,
                        fillColor : "#9999aa",
                        strokeColor : "#6666aa"
                    }
                }
            }) ]
        }),
        "select" : new OpenLayers.Style(null, {
            rules : [ new OpenLayers.Rule({
                symbolizer : {
                    "Point" : {
                        pointRadius : 5,
                        graphicName : "circle",
                        fillColor : "white",
                        fillOpacity : 0.25,
                        strokeWidth : 2,
                        strokeOpacity : 1,
                        strokeColor : "#00ffff"
                    },
                    "Line" : {
                        strokeWidth : 3,
                        strokeOpacity : 1,
                        strokeColor : "#ff0000"
                    },
                    "Polygon" : {
                        strokeWidth : 2,
                        strokeOpacity : 1,
                        fillColor : "#ffffff",
                        strokeColor : "#ff0000"
                    }
                }
            }) ]
        }),
        "temporary" : new OpenLayers.Style(null, {
            rules : [ new OpenLayers.Rule({
                symbolizer : {
                    "Point" : {
                        graphicName : "circle",
                        pointRadius : 5,
                        fillColor : "white",
                        fillOpacity : 0.25,
                        strokeWidth : 2,
                        strokeColor : "#00ffff"
                    },
                    "Line" : {
                        strokeWidth : 3,
                        strokeOpacity : 1,
                        strokeColor : "#ff0000"
                    },
                    "Polygon" : {
                        strokeWidth : 2,
                        strokeOpacity : 1,
                        strokeColor : "#ff0000",
                        fillColor : "#ffffff"
                    }
                }
            }) ]
        })
    });
}

/**
 * initializes a drawing pane for digitizing
 */
function initDrawinPane() {
    var drawingPane = null;
    var tmp = map.getLayersByName("_DIGITIZE_FEATURES_");
    if (tmp.length == 0) {
        // create new drawing pane for digitizing
        drawingPane = new OpenLayers.Layer.Vector("_DIGITIZE_FEATURES_", {
            styleMap : initDigitizerStyles()
        });
        map.addLayer(drawingPane);
    } else {
        // use already existing drawingpane (layer)
        drawingPane = tmp[0];
    }
    drawingPane.setMap(map);
    return drawingPane;
}

function resetDigitizeController() {
    var controls = map.getControlsByClass('OpenLayers.Control.DrawFeature');
    if (controls != null) {
        for (key in controls) {
            controls[key].deactivate();
            map.removeControl(controls[key]);
        }
    }

    controls = map.getControlsByClass('OpenLayers.Control.DragFeature');
    if (controls != null) {
        for (key in controls) {
            controls[key].deactivate();
            map.removeControl(controls[key]);
        }
    }

    controls = map.getControlsByClass('OpenLayers.Control.SelectFeature');
    if (controls != null) {
        for (key in controls) {
            controls[key].unselectAll();
            controls[key].deactivate();
            map.removeControl(controls[key]);
        }
    }

    controls = map.getControlsByClass('OpenLayers.Control.ModifyFeature');
    if (controls != null) {
        for (key in controls) {
            controls[key].selectControl.unselectAll();
            controls[key].deactivate();
            map.removeControl(controls[key]);
        }
    }
}

function initDrawPoint(btns) {
    resetDigitizeController();
    var drawingPane = initDrawinPane();
    var control = new OpenLayers.Control.DrawFeature(drawingPane, OpenLayers.Handler.Point, {
        featureAdded : function(f) {
            drawingPane.removeAllFeatures();
            drawingPane.addFeatures(f);
            btns[0].toggle()
            btns[1].toggle()
            btns[0].handler.call(btns[0])
            var sel = map.getControlsByClass('OpenLayers.Control.SelectFeature')[0]
            sel.select(f)
        }
    });
    map.addControl(control);
    control.activate();
}

function initDrawCurve(callback) {
    resetDigitizeController();
    var drawingPane = initDrawinPane();
    var control = new OpenLayers.Control.DrawFeature(drawingPane, OpenLayers.Handler.Path);
    map.addControl(control);
    control.activate();
}

function initDrawPolygon(btns) {
    resetDigitizeController();
    var drawingPane = initDrawinPane();
    var control = new OpenLayers.Control.DrawFeature(drawingPane, OpenLayers.Handler.Polygon, {
        featureAdded : function(f) {
            drawingPane.removeAllFeatures();
            drawingPane.addFeatures(f);
            btns[0].toggle()
            btns[1].toggle()
            btns[0].handler.call(btns[0])
            var sel = map.getControlsByClass('OpenLayers.Control.SelectFeature')[0]
            sel.select(f)
        }
    });
    map.addControl(control);
    control.activate();
}

function initModifyFeature(callback) {
    resetDigitizeController();
    var drawingPane = initDrawinPane();
    var control = new OpenLayers.Control.ModifyFeature(drawingPane, {
        beforefeaturemodified : function(feature) {
            callback(feature);
        }
    });
    map.addControl(control);
    control.activate();
}

function initSelectFeature(callback) {
    resetDigitizeController();
    var drawingPane = initDrawinPane();
    var control = new OpenLayers.Control.SelectFeature(drawingPane, {
        onSelect : function(feature) {
            callback(feature);
        }
    });
    map.addControl(control);
    control.activate();
}

function initFeatureDragging() {
    resetDigitizeController();
    var drawingPane = initDrawinPane();
    var control = new OpenLayers.Control.DragFeature(drawingPane);
    map.addControl(control);
    control.activate();
}

function resetDigitizer() {
    resetDigitizeController();
    var tmp = map.getLayersByName("_DIGITIZE_FEATURES_");
    for ( var i = 0; i < tmp.length; i++) {
        map.removeLayer(tmp[i]);
    }
}
