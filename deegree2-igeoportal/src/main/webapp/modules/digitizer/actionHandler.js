function handleException( result ) {
	var exception = JSON.parse( result );
	parent.Ext.Msg.show({
        title: 'ERROR',
        msg: exception.message,
        buttons: Ext.MessageBox.OK,
        icon: Ext.MessageBox.ERROR
     });
}

/**
* will invoked when selecting a layer for editing. Reads featureType definition
* of the data source assigned to selected layer
*/
function selectLayer(cb, record, index) {
     var bean = JSON.stringify( new function() {
    	                     this.action = 'getFeatureType';
    	                     this.name = record.data['name'];
    	                     this.url = record.data['url'];
                           }, null, false );
      try {
          submitPostRequest( url, handleGetFeatureType, bean );                    
      } catch(e) {
          alert( "selectLayer: " + JSON.stringify( e ) );
      }
}

function handleGetFeatureType(result) {	
    if ( result.indexOf( 'ExceptionBean' ) > -1 ) {
    	handleException( result );
    } else {                    
        removeAllPropertyFields();
        currentFeatureType = JSON.parse( result );
        var vp = Ext.getCmp( 'VIEWPORT' );
        for ( var i = 0; i < vp.items.getCount(); i++ ) {
        	// just if a feature type has been selected digitizer actions are allowed
            if ( vp.items.get(i) instanceof Ext.Button ) {                            
                vp.items.get(i).enable();
            } 
        }
    }                
}

/**
* will be invoked if a feature is selected
*/
function selectFeatureCallback(feature) {
    currentFeature = feature;
    setPropertyFields( currentFeatureType, feature );
}

/**
* will invoked on reset dialog 
*/
function processReset(btn, text) {
	if ( btn == 'yes' ) {
		parent.controller.vOLMap.resetDigitizer();
    } 
}            

/**
 * will invoked on save dialog 
 */            
function processSave(btn, text) {
    var features = parent.controller.vOLMap.getMap().getLayersByName( '_DIGITIZE_FEATURES_' )[0].features;
    var map = new Array();
    for ( var i = 0; i < features.length; i ++ ) {
    	map[i] = new Object();
        map[i]['attributes'] = features[i].attributes;
        map[i]['geometry'] =  '' + features[i].geometry;
    }             
    
    var bean = JSON.stringify( new function() {
        this.action = 'saveFeature';
        this.id = 'ID';
        this.featureMap = map;
      }, null, false );
    
    //alert( JSON.stringify( map ) ); 
    try {
        submitPostRequest( url, handleSaveFeature, bean );                    
    } catch(e) {
        alert( "processSave: " + JSON.stringify( e ) );
    }
}

function handleSaveFeature(result) {
	if ( result.indexOf( 'ExceptionBean' ) > -1 ) {
		handleException( result );
    }
}