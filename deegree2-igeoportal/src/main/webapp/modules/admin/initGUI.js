var selectedModulesTree;
var availableModulesTree;
var availableUsersStore; 
var selectedUsers;
var availableUsers;
var messages;

function initExtJs() {
	
	 // create bean containing all required messages
    var bean = JSON.stringify( new function() {
        this.action = 'getMessages';
        this.keys = ['IGEO_STD_ADMIN_AV_MODULES', 'IGEO_STD_ADMIN_SEL_MODULES', 'IGEO_STD_ADMIN_SELM_GUI', 
                     'IGEO_STD_ADMIN_SELM_CENTER', 'IGEO_STD_ADMIN_SELM_EAST', 'IGEO_STD_ADMIN_SELM_WEST', 
                     'IGEO_STD_ADMIN_SELM_SOUTH', 'IGEO_STD_ADMIN_SELM_NORTH', 'IGEO_STD_ADMIN_SELUSERS', 
                     'IGEO_STD_ADMIN_SELUSERS_NAME', 'IGEO_STD_ADMIN_SELUSERS_FNAME', 'IGEO_STD_ADMIN_SELUSERS_LNAME', 
                     'IGEO_STD_ADMIN_AVUSERS', 'IGEO_STD_ADMIN_AVUSERS_NAME', 'IGEO_STD_ADMIN_AVUSERS_FNAME', 
                     'IGEO_STD_ADMIN_AVUSERS_LNAME', 'IGEO_STD_ADMIN_WMC_NAME', 'IGEO_STD_ADMIN_BT_PUBLISH_TOOLTIP', 
                     'IGEO_STD_ADMIN_BT_PUBLISH', 'IGEO_STD_ADMIN_BT_CANCEL_TOOLTIP', 'IGEO_STD_ADMIN_BT_CANCEL'];
    }, null, false );
    

    // send request/bean to the server part of the module
    try {
         submitPostRequest( url, function( result ) { 
            messages = JSON.parse( result );
             } , bean, null, false );                    
    } catch(e) {
        alert( 22+ JSON.stringify( e ) );
    }
	
	initSelectedModules();
	initAvailableModules();
	initGroups();
	initSelectedUsersList();
	initAvailableUsersList();
	initContextName();
	initButtons();

	 
     // Setup Drop Targets
     // This will make sure we only drop to the  view scroller element
     var firstGridDropTargetEl =  availableUsers.getView().scroller.dom;
     var firstGridDropTarget = new Ext.dd.DropTarget(firstGridDropTargetEl, {
             ddGroup    : 'firstGridDDGroup',
             notifyDrop : function(ddSource, e, data){
                     var records =  ddSource.dragData.selections;
                     Ext.each(records, ddSource.grid.store.remove, ddSource.grid.store);
                     availableUsers.store.add(records);
                     availableUsers.store.sort('name', 'ASC');
                     return true
             }
     });

     // This will make sure we only drop to the view scroller element
     var secondGridDropTargetEl = selectedUsers.getView().scroller.dom;
     var secondGridDropTarget = new Ext.dd.DropTarget(secondGridDropTargetEl, {
             ddGroup    : 'secondGridDDGroup',
             notifyDrop : function(ddSource, e, data){
                     var records =  ddSource.dragData.selections;
                     Ext.each(records, ddSource.grid.store.remove, ddSource.grid.store);
                     selectedUsers.store.add(records);
                     selectedUsers.store.sort('name', 'ASC');
                     return true
             }
     });
         	

}

function initAvailableModules() {
    var Tree = Ext.tree;
    
    availableModulesTree = new Tree.TreePanel({
        animate:true, 
        autoScroll:true,
        loader: new Tree.TreeLoader({
        	 	dataUrl: url + '?action=getModules'        	 	
        	 } ),
        listeners: { 'load': fillSelectedModules },
        enableDD:true,
        containerScroll: true,
        border: false,
        width: 285,
        height: 200
    });
    
    // set the root node
    var root = new Tree.AsyncTreeNode({
        text: messages['IGEO_STD_ADMIN_AV_MODULES'], 
        draggable:false, // disable root node dragging
        id:'src'
    });
    availableModulesTree.setRootNode( root );
    
    // render the tree
    availableModulesTree.render('AvailableModules');
    
    root.expand( true, false );
}

function fillSelectedModules(e) {
	try {
		var sRoot = selectedModulesTree.getRootNode();
		var sGui = sRoot.childNodes[0];
		var sCenter = sGui.childNodes[0];
		var sSouth = sGui.childNodes[3];
		
		var aRoot = availableModulesTree.getRootNode();
		var aGui = aRoot.childNodes[0];
		var aCenter = null; 
		var aSouth =null;
		for ( var i = 0; i < aGui.childNodes.length; i++) {
			if ( aGui.childNodes[i].id == 'center' ) {
				aCenter = aGui.childNodes[i];
			} else if ( aGui.childNodes[i].id == 'south' ) {
				aSouth = aGui.childNodes[i];
			} 
		}
		
		while ( aCenter.childNodes.length > 0 ) {
			sCenter.appendChild( aCenter.childNodes[0] );		
		}		
		while ( aSouth.childNodes.length > 0 ) {
			sSouth.appendChild( aSouth.childNodes[0] );		
		}
	} catch(e) {
	}	
}

function initSelectedModules() {
	var Tree = Ext.tree;
    // second tree
    selectedModulesTree = new Tree.TreePanel({
        animate:true,
        autoScroll:true,       
        containerScroll: true,
        border: false,
        width: 285,
        height: 200,
        enableDD:true
    });
    
    // add the root node
    var root2 = new Tree.TreeNode({
        text: messages['IGEO_STD_ADMIN_SEL_MODULES'], 
        expanded: true,
        draggable:false, 
        id:'ux'
    });
    var gui = new Tree.TreeNode({
        text: messages['IGEO_STD_ADMIN_SELM_GUI'], 
        draggable:false, 
        expanded: true,
        id:'gui'
    });
    var center = new Tree.TreeNode({
        text: messages['IGEO_STD_ADMIN_SELM_CENTER'], 
        draggable:false, 
        id:'center'
    });
    gui.appendChild( center );
    var east = new Tree.TreeNode({
        text: messages['IGEO_STD_ADMIN_SELM_EAST'], 
        draggable:false, 
        id:'east'
    });
    gui.appendChild( east );
    var north = new Tree.TreeNode({
        text: messages['IGEO_STD_ADMIN_SELM_NORTH'], 
        draggable:false, 
        id:'north'
    });
    gui.appendChild( north );
    var south = new Tree.TreeNode({
        text: messages['IGEO_STD_ADMIN_SELM_SOUTH'],
        draggable:false, 
        id:'south'
    });
    gui.appendChild( south );
    var west = new Tree.TreeNode({
        text: messages['IGEO_STD_ADMIN_SELM_WEST'],
        draggable:false, 
        id:'west'
    });
    gui.appendChild( west );
    
    root2.appendChild( gui );
    selectedModulesTree.setRootNode(root2);
    selectedModulesTree.render('SelectedModules');
    
    selectedModulesTree.expand(false, /*no anim*/ false);
}

function initGroups() {

	var store = new Ext.data.JsonStore({
	    // store configs
	    url: url + '?action=getUserGroups',
	    // reader configs
	    root: 'options',
	    fields: [ 'name', 'title' ]
	});
	store.load();

    
	// create the combo instance
	var combo = new Ext.form.ComboBox({
		listeners:{ 'select': loadUsers },
	    typeAhead: true,
	    triggerAction: 'all',
	    mode: 'local',
	    store: store,
	    valueField: 'name',
	    width: 285,
	    displayField: 'title'                	    
	});
	combo.render( 'AvailableUserGroups' );  
}

function loadUsers(cb, record, index) {
	if ( index >= 0 ) {                                                  
        availableUsersStore.reload( { params:{ GROUP: record.data['name'] } } );
		//availableUsersStore.load();
    }
}

function handleLoadUsers(result) {
    alert( result );
}

function initSelectedUsersList() {

	 // create the data store
    var store = new Ext.data.ArrayStore({
        fields: [
           {name: 'name'},
           {name: 'first name'},
           {name: 'last name'}                       
        ]
    });
	                
	selectedUsers = new Ext.grid.GridPanel({
	        store: store,
	        enableDragDrop   : true,
	        ddGroup          : 'firstGridDDGroup',
	        columns: [
	            {
	                id       :'name',
	                header   : messages['IGEO_STD_ADMIN_SELUSERS_NAME'], 
	                width    : 80, 
	                sortable : true, 
	                dataIndex: 'name'
	            },
	            {
	            	id       :'firstName',
	                header   : messages['IGEO_STD_ADMIN_SELUSERS_FNAME'], 
	                width    : 90, 
	                sortable : true,  
	                dataIndex: 'firstName'
	            },
                {
                    id       :'lastName',
                    header   : messages['IGEO_STD_ADMIN_SELUSERS_LNAME'], 
                    width    : 90, 
                    sortable : true,  
                    dataIndex: 'lastName'
                }
	        ],
	        stripeRows: true,
	        height: 250,
	        width: 285,
	        title: messages['IGEO_STD_ADMIN_SELUSERS'],
	    });

	selectedUsers.render('SelectedUsers');           	   
	                
}

function initAvailableUsersList() {

	 // create the data store
    availableUsersStore = new Ext.data.JsonStore({
        // store configs
        url: url + '?action=getUsersForGroup',
        baseParams : { 'GROUP': 'default' }, 
        // reader configs
        root: 'options',
        fields: [ 'name', 'firstName', 'lastName'  ]
    });
                    
    availableUsers = new Ext.grid.GridPanel({
           store: availableUsersStore,
           enableDragDrop   : true,            
           ddGroup          : 'secondGridDDGroup',                                  
           columns: [
                     {
                         id       :'name',
                         header   : messages['IGEO_STD_ADMIN_AVUSERS_NAME'], 
                         width    : 80, 
                         sortable : true, 
                         dataIndex: 'name'
                     },
                     {
                         id       :'firstName',
                         header   : messages['IGEO_STD_ADMIN_AVUSERS_FNAME'], 
                         width    : 90, 
                         sortable : true,  
                         dataIndex: 'firstName'
                     },
                     {
                         id       :'lastName',
                         header   : messages['IGEO_STD_ADMIN_AVUSERS_LNAME'], 
                         width    : 90, 
                         sortable : true,  
                         dataIndex: 'lastName'
                     }
                 ],
           stripeRows: true,
           height: 200,
           width: 285,
           title: messages['IGEO_STD_ADMIN_AVUSERS'],
       });

    availableUsers.render('AvailableUsers');                 
                   
}

function initContextName() {

	var items = new Array();
	var label = new Ext.form.Label( {
		x: 10,
        y: 15,
        width: 250,
        height:20,
        text: messages['IGEO_STD_ADMIN_WMC_NAME'],
	});
	items.push( label );
	
	var field = new Ext.form.TextField( {
    	id: 'contextNameAreaField',
        x: 160,
        y: 10,
        width: 400,
        height:20                    
    });
    items.push( field );
    
    var panel  = new Ext.FormPanel({
        bodyBorder: false,
        border: true,
        layout:'absolute', 
        x: 10,
        y: 10,
        width: 580,                    
        height: 40,
        items: items
    });
    panel.render('contextNameArea');
    
}

function initButtons() { 

    var items = new Array();
    var btOK = new Ext.Button({                          
        x: 10,
        y: 10,
        tooltip: messages['IGEO_STD_ADMIN_BT_PUBLISH_TOOLTIP'],
        text: messages['IGEO_STD_ADMIN_BT_PUBLISH'],
        handler: function(toggled){
    	   saveContext();
        }
    });
    items.push(btOK);  

    var btCancel = new Ext.Button({                          
        x: 70,
        y: 10,
        tooltip: messages['IGEO_STD_ADMIN_BT_CANCEL_TOOLTIP'],
        text: messages['IGEO_STD_ADMIN_BT_CANCEL'],
        handler: function(toggled){
    	parent.Ext.getCmp( 'winAdminConsole' ).hide();
        }
    });
    items.push(btCancel);

    var panel  = new Ext.Panel({
        bodyBorder: false,
        border: true,
        layout:'absolute',                    
        x: 10,
        y: 10,
        width: 580,                    
        height: 40,
        items: items
    });
    panel.render('buttonArea');
}