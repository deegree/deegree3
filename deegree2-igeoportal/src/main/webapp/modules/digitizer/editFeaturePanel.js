
function removeAllPropertyFields() {
	Ext.getCmp( 'propertyPanel' ).removeAll( true );
}

function setPropertyFields(featureType, feature) {	
	removeAllPropertyFields();
	
	if ( feature.attributes == null || !feature.attributes.length ) {	
		// this will be invoked if a feature is selected first time after it has been created
		feature.attributes = new Array();
		feature.attributes[0] = new Object();
		// assign feature type
		feature.attributes[0]['$FEATURETYPE$'] = currentFeatureType;
		// create and set feature ID
		feature.attributes[0]['$FEATUREID$'] = 'ID_' + Math.random();
		// mark feature to be inserted
		feature.attributes[0]['$ACTION$'] = 'INSERT';
		for ( var i = 0; i < featureType.properties.length; i++ ) {			
			var property = featureType.properties[i];			
			// create feature properties and set their values to null
			feature.attributes[0]['{' + property.namespace + '}:' + property.name] = null;
		}
	}
   
    var pp = Ext.getCmp( 'propertyPanel' );
    for ( var i = 0; i < featureType.properties.length; i++ ) {
	   var property = featureType.properties[i];
	   var tf = new Ext.form.TextField({
		   fieldLabel: property.name,
           name: '{' + property.namespace + '}:' + property.name,
           value: feature.attributes[0]['{' + property.namespace + '}:' + property.name],
           anchor:'100%'
	   });
	   pp.add( tf );
    }
   
     var buttonPanel = new Ext.Panel( {
		   bodyBorder: false,
		   border: false,
		   layout:'hbox',		   
		   layoutConfig: {
	   			padding: '10 0 0 0',
			    pack: 'start'
			}
    });
  
    buttonPanel.add( new Ext.Button({                          
       tooltip: 'OK',
       text: 'OK',
       handler: takeAttributes
    }) );
   
    buttonPanel.add( { bodyBorder: false, border: false, width: 10 } );
   
    buttonPanel.add( new Ext.Button({                          
       tooltip: 'cancel',
       text: 'cancel',
       handler: function(toggled){ }
    }) );
   
    pp.add( buttonPanel );
    pp.doLayout(); 
}

function takeAttributes(toggle) {
	var pp = Ext.getCmp( 'propertyPanel' );	
	for ( var i = 0; i < pp.items.getCount(); i++ ) {
		if ( pp.items.get(i) instanceof Ext.form.TextField ) {
			currentFeature.attributes[0][pp.items.get(i).getName()] = pp.items.get(i).getValue(); 
		}
	}
}