function addMeasureControl() {
    // style the sketch fancy
    var sketchSymbolizers = getSketchSymbolizers();
    var style = new OpenLayers.Style();
    style.addRules([ new OpenLayers.Rule({
        symbolizer : sketchSymbolizers
    }) ]);
    var styleMap = new OpenLayers.StyleMap({
        "default" : style
    });

    var line = new OpenLayers.Control.Measure(OpenLayers.Handler.Path, {
        persist : true,
        handlerOptions : {
            layerOptions : {
                styleMap : styleMap
            }
        },
        measureComplete : stopMeasuring
    });
    line.events.on({
        "measure" : handleMeasurements,
        "measurepartial" : handleMeasurements
    });
    line.deactivate();
    map.addControl(line);

    var polygon = new OpenLayers.Control.Measure(OpenLayers.Handler.Polygon, {
        persist : true,
        handlerOptions : {
            layerOptions : {
                styleMap : styleMap
            }
        },
        measureComplete : stopMeasuring
    });
    polygon.events.on({
        "measure" : handleMeasurements,
        "measurepartial" : handleMeasurements
    });
    polygon.deactivate();
    map.addControl(polygon);
}

function getSketchSymbolizers() {
    // style the sketch fancy
    return {
        "Point" : {
            pointRadius : 4,
            graphicName : "square",
            fillColor : "white",
            fillOpacity : 1,
            strokeWidth : 1,
            strokeOpacity : 1,
            strokeColor : "#333333"
        },
        "Line" : {
            strokeWidth : 3,
            strokeOpacity : 1,
            strokeColor : "#FF0000",
            strokeDashstyle : "dash"
        },
        "Polygon" : {
            strokeWidth : 2,
            strokeOpacity : 1,
            strokeColor : "#666666",
            fillColor : "white",
            fillOpacity : 0.3
        }
    };
}

function handleMeasurements(e) {
    parent.controller.vMeasurement.handleMeasurements(e);
}

function stopMeasuring(e) {
    parent.controller.vMeasurement.stopMeasurement(e);
}