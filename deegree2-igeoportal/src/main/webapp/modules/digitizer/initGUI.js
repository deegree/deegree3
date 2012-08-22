

var diItems = new Array();

function resetStates(button) {
	 for (var i = 0; i < diItems.length; i++ ) {
        if ( diItems[i] != button && diItems[i].toggle ) { 
       	 diItems[i].toggle( false, true );
        }
    }
	Ext.getCmp( 'propertyPanel' ).removeAll( true );
}

function initGUI() {
	   Ext.QuickTips.init();   
	   
	   // determine frame ajax controller URL
	   var tmp = window.location.pathname.split( '/' );
	   var s = '';
	   for ( var i = 0; i < tmp.length-3; i++) {
	       s += (tmp[i] + '/' ); 
	   } 
	   url = window.location.protocol + '//' + window.location.host + s + 'ajaxcontrol';

	   // add GUI elements
	   diItems.push( new Ext.Button({                          
	       enableToggle: true,
	       x: 10,
	       y: 10,
	       tooltip: 'select feature',
	       icon: './images/select.gif',
	       disabled: true,
	       handler: function(toggled){
	           if (toggled) {
	               resetStates( this );
	           }
	           parent.controller.vOLMap.initSelectFeature( selectFeatureCallback ); 
	       }
	   }) );
	   
	   diItems.push( new Ext.Button({                           
	       enableToggle: true,
	       x: 50,
	       y: 10,
	       tooltip: 'create point feature',
	       icon: './images/point_create.gif',
	       disabled: true,
	       handler: function(toggled){
	           if (toggled) {
	        	   resetStates( this );                            	  
	           }
	           parent.controller.vOLMap.initDrawPoint(null);
	       }
	   }));      

	   diItems.push( new Ext.Button({                          
	       enableToggle: true,
	       x: 80,
	       y: 10,
	       tooltip: 'create line feature',
	       icon: './images/line_create.gif',
	       disabled: true,
	       handler: function(toggled){
	           if (toggled) {
	               resetStates( this );                                   
	           } 
	           parent.controller.vOLMap.initDrawCurve(null);
	       }
	   }) );

	   diItems.push( new Ext.Button({                          
	       enableToggle: true,
	       x: 110,
	       y: 10,
	       tooltip: 'create polygon feature',
	       icon: './images/polygon_create.png',
	       disabled: true,
	       handler: function(toggled){
	           if (toggled) {
	               resetStates( this );
	           } 
	           parent.controller.vOLMap.initDrawPolygon(null);
	       }
	   }) );
	   
	   diItems.push( new Ext.Button({       
		   enableToggle: true,
	       x: 150,
	       y: 10,
	       tooltip: 'modify a feature',
	       icon: './images/move_vertex.png',
	       disabled: true,
	       handler: function(toggled){
			   if (toggled) {
		           resetStates( this );
		       } 
		       parent.controller.vOLMap.initModifyFeature( selectFeatureCallback );
		   }
	   }) );

	   diItems.push( new Ext.Button({                                 
	       x: 190,
	       y: 10,
	       tooltip: 'create delete selected features',
	       icon: './images/shape_square_delete.gif',
	       disabled: true,
	       handler: function(toggled){
		            alert( "realy want to delete all selected features?" );
	       }
	   }) );

	   diItems.push( new Ext.Button({     
		   enableToggle: true,
	       x: 220,
	       y: 10,
	       tooltip: 'move feature',
	       icon: './images/shape_square_move.gif',
	       disabled: true,
	       handler: function(toggled){                               
	           if (toggled) {
	               resetStates( this );
	           }
	           parent.controller.vOLMap.initFeatureDragging(null);
	       }
	   }) );

	   diItems.push( new Ext.Button({                          
	       x: 10,
	       y: 40,
	       tooltip: 'reset',
	       icon: '../../images/mt_save.gif',
	       disabled: true,
	       handler: function(toggled){                               
		        parent.Ext.Msg.show({
	               title:'save?',
	               msg: 'You changes you did will be sended stored in assigned backend',
	               buttons: Ext.Msg.YESNO,
	               fn: processSave,
	               animEl: 'elId',
	               icon: Ext.MessageBox.QUESTION
	            });
	       }
	   }) );

	   diItems.push( new Ext.Button({                          
	       x: 40,
	       y: 40,
	       tooltip: 'reset',
	       text: 'reset',
	       disabled: true,
	       handler: function(toggled){                               
			   parent.Ext.Msg.show({
				   title:'reset?',
				   msg: 'Reset will remove all changes you did!',
				   buttons: Ext.Msg.YESNO,
				   fn: processReset,
				   animEl: 'elId',
				   icon: Ext.MessageBox.QUESTION
				});
	       }
	   }) );

	   diItems.push( new Ext.form.Label({
	       text: 'Select editing layer',
	       width: 220,
	       x: 10,
	       y: 80
	    }) );
	   
	   
	   values = new Ext.data.Store({
	       proxy: new Ext.data.HttpProxy( { url: url + '?action=getEditableLayer' } ),       
	       autoLoad: true,
	       reader: new Ext.data.JsonReader(
	    		   {
		           id: 'ID',
		           totalProperty: 'totalCount',
		           root: 'layers'
		       	   }, 
		       	   [{ name: 'name' }, { name: 'title' }, { name: 'url' }, { name: 'description' }]
	           )
	   });
	   values.on( 'load', function onLoad(store) {
			//Ext.getCmp( 'DIGITIZER_LAYER_SELECT' ).setValue( store.getAt(0).get('title') );
			Ext.getCmp( 'DIGITIZER_LAYER_SELECT' ).setValue( 'select a layer to edit' );
	   } );

	   diItems.push( new Ext.form.ComboBox({
		   id: 'DIGITIZER_LAYER_SELECT',
		   autoLoad: true,
		   displayField:'title',
	       fieldLabel: 'Value',
	       mode: 'remote',
	       name: 'value',
	       store: values,
	       tpl: '<tpl for="."><div ext:qtip="{title}; {url}; {description}" class="x-combo-list-item">{title}</div></tpl>',
	       triggerAction: 'all',
	       typeAhead: true,
	       valueField: 'name', 
	       editable: false,
	       width: 270,
	       x: 10,
	       y: 100,
		   listeners:{
		         'select': selectLayer
		    }
		}) );
	   
	   diItems.push( new Ext.form.FormPanel({
		   id: 'propertyPanel',
		   bodyBorder: false,
		   border: false,
	       labelWidth: 80,
	       defaultType: 'textfield',
	       x: 10,
	       y: 140,
	       width: 270,
	       //height: 300,
	       //autoScroll: true,
	       autoHeight: true,
	       items: []
	   }) );

	   var viewport = new Ext.Viewport({
		     id: 'VIEWPORT',
			 layout:'fit',
       height: 600,
			 items : {
          layout: 'absolute',
          height: 600,
          items: diItems
          }
//	   ,
//			 resizeEvent: '',
//			 stateEvents: []
	       })
}
