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
 * menubar event handler method
 * @param item 
 */
function onItemClick(item) {
	if ( item.id == 'help' ) {
		controller.vMenuBarTop.openHelp();
	}  else  if ( item.id == 'lc' ) {
		controller.vMenuBarTop.openLoadContext();
	}  else  if ( item.id == 'sc' ) {
		controller.vMenuBarTop.openSaveContext();
	}  else  if ( item.id == 'login' ) {
		controller.vMenuBarTop.openLogin();
	}  else  if ( item.id == 'logout' ) {
		controller.vMenuBarTop.performLogout();
	}  else  if ( item.id == 'gotoHome' ) {
		window.location.href = 'http://www.deegree.org';
	}  else  if ( item.id == 'licence' ) {
		controller.vMenuBarTop.openLegal();
	} else if ( item.id == 'openAdminConsole' ) {
		Ext.getCmp( 'winAdminConsole' ).show();
	}  else if ( item.id == 'black' ) {
		currentStyle = 'black'; 
		setActiveStyleSheet();
	} else if ( item.id == 'blue' ) {
		currentStyle = 'blue';
		setActiveStyleSheet();
	} else if ( item.id == 'gray' ) {
		currentStyle = 'gray';
		setActiveStyleSheet();
	} else if ( item.id == 'wpt' ) {
		currentStyle = 'wpt';
		setActiveStyleSheet();
	} else if ( item.id == 'nds' ) {
		currentStyle = 'nds';
		setActiveStyleSheet();
	}  
}

/**
 * create menubar GUI
 */
function initMenubar(isAdmin) {
	var items = new Array();
	items.push({
        text: 'Load & manage context',
        id: 'lc',
        icon: './images/mt_load.gif',  
        handler: onItemClick
    } );
	items.push( {
        text: 'Save context',
        id: 'sc',
        icon: './images/mt_save.gif',  
        handler: onItemClick
    } );
	
	var fileMenu = new Ext.menu.Menu({
        id: 'fileMenu',
        style: {
            overflow: 'visible'    
        },
        items: items
    });
    
    var loginMenu = new Ext.menu.Menu({
        id: 'loginMenu',
        style: {
            overflow: 'visible'    
        },
        items: [
            {
                text: 'Login',
                id: 'login',
                icon: './images/mt_login.gif',  
                handler: onItemClick
            },{
                text: 'Logout',
                id: 'logout',
                icon: './images/mt_logout.gif',  
                handler: onItemClick
            }
        ]
    });
    
    items = new Array();
    items.push({
        text: 'Search metadata',
        id: 'searchMetadata',
        icon: './images/mt_metadata.gif',  
        handler: onItemClick
    });
    if ( isAdmin ) {
    	// just add to menu if admin console module is available
	    items.push({
	        text: 'Open admin console',
	        id: 'openAdminConsole',
	        icon: './images/mt_metadata.gif',  
	        handler: onItemClick
	    });
    }
    var operationMenu = new Ext.menu.Menu({
        id: 'operationMenu',
        style: {
            overflow: 'visible'    
        },
        items: items
    });
    
    var styleMenu = new Ext.menu.Menu({
        id: 'styleMenu',
        style: {
            overflow: 'visible'    
        },
        items: [
            {
                text: 'Black',
                id: 'black',
                handler: onItemClick
            },
            {
                text: 'Blue',
                id: 'blue',
                handler: onItemClick
            },
            {
                text: 'Gray',
                id: 'gray',
                handler: onItemClick
            },
            {
                text: 'WPT',
                id: 'wpt',
                handler: onItemClick
            },
            {
                text: 'NDS',
                id: 'nds',
                handler: onItemClick
            }
        ]
    });
    
    var helpMenu = new Ext.menu.Menu({
        id: 'helpMenu',
        style: {
            overflow: 'visible'
        },
        items: [
            {
                text: 'Help',
                id: 'help',
                icon: './images/mt_help.gif',  
                handler: onItemClick
            },                       
            {
                text: 'Legal note',
                id: 'licence',
                icon: 'images/copyright.gif',  
                handler: onItemClick
            },                       
             {
                text: 'deegree home',
                id: 'gotoHome',
                icon: './images/house.png',  
                handler: onItemClick
            }
        ]
    });

    var tb = new Ext.Toolbar();
    tb.height = 30; 
    tb.render( 'menubar' );

    tb.add({
        text:'File',
        icon: './images/folder_star.png',  
        menu: fileMenu 
    });
    
    tb.add({
        text:'User management',
        icon: './images/user.png',  
        menu: loginMenu 
    });
    
    tb.add({
        text:'Tools',
        icon: './images/cog.png',  
        menu: operationMenu
    });
    
    tb.add({
        text:'Styles',
        icon: './images/palette.png',  
        menu: styleMenu
    });
    
    tb.add({
        text:'Help',
        icon: './images/mt_help.gif',  
        menu: helpMenu
    });

    tb.doLayout();
}