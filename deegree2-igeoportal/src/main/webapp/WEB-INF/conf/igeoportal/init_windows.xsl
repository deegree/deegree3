<!-- This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license. -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:cntxt="http://www.opengis.net/context" xmlns:sld="http://www.opengis.net/sld" xmlns:deegree="http://www.deegree.org/context" xmlns:xlink="http://www.w3.org/1999/xlink">

    <xsl:template name="INITEXTJS">
       
       // global variables for area sizes and borders
       var westWidth = <xsl:value-of select="$vWestWidth"/>;
       var eastWidth = <xsl:value-of select="$vEastWidth"/>;
       var southHeight = <xsl:value-of select="$vSouthHeight"/>;
       var verticalAdjustment = <xsl:value-of select="$vVerticalAdjustment"/>;
       var horizontalAdjustment = <xsl:value-of select="$vHorizontalAdjustment"/>;
       
    $(document).ready(function(){
	    Ext.onReady( function() {
	           Ext.QuickTips.init(); 
	            if ( controller == null ) {
	              controller = new Controller();
	              controller.init();
	            }                   
	        }	           
        );
       })
	        
	    function initOverlayWindows() {
	        // overlayed windows
	        <xsl:for-each select="//deegree:Module">
	            <xsl:if test="./@hidden != 'true' and ./@overlay = 'true' ">
	               <xsl:variable name="LEFT">
	                   <xsl:choose>
		                   <xsl:when test="boolean(./@left)">
		                       <xsl:value-of select="./@left"/>
		                   </xsl:when>
		                   <xsl:otherwise>100</xsl:otherwise>
	                   </xsl:choose>
	               </xsl:variable>
	               <xsl:variable name="TOP">
                       <xsl:choose>
                           <xsl:when test="boolean(./@top)">
                               <xsl:value-of select="./@top"/>
                           </xsl:when>
                           <xsl:otherwise>100</xsl:otherwise>
                       </xsl:choose>
                   </xsl:variable>
                   <xsl:variable name="WIDTH">
                       <xsl:choose>
                           <xsl:when test="boolean(./@width)">
                               <xsl:value-of select="./@width"/>
                           </xsl:when>
                           <xsl:otherwise>300</xsl:otherwise>
                       </xsl:choose>
                   </xsl:variable>
                   <xsl:variable name="HEIGHT">
                       <xsl:choose>
                           <xsl:when test="boolean(./@height)">
                               <xsl:value-of select="./@height"/>
                           </xsl:when>
                           <xsl:otherwise>300</xsl:otherwise>
                       </xsl:choose>
                   </xsl:variable>
	                var win = new Ext.Window({
	                    id:'<xsl:value-of select="deegree:Name"/>',
	                    layout:'fit',
	                    width:<xsl:value-of select="$WIDTH"/>,
	                    height: <xsl:value-of select="$HEIGHT"/>,
	                    closable: false,
	                    animCollapse: false,
	                    header: false,
	                    resizable: false,
	                    plain: true,
	                    x: <xsl:value-of select="$LEFT"/>,
	                    y: <xsl:value-of select="$TOP"/>,
	                    items: [ {              
	                            html: '<xsl:call-template name="WINDOW"/>',
	                            border: false
	                            }]
	                });
	                win.show(this);
	            </xsl:if>
	        </xsl:for-each>
	    }
	
	
	    /**
	    * init extJS
	    */
	    function  init_iGeo () {
	    
		   var windowWidth = 0;
	       var windowHeigth = 0;
	       if ( navigator.appName.indexOf( "Microsoft" )!= -1 ) {              
	           windowWidth = document.body.offsetWidth;
	           windowHeigth = document.body.offsetHeight;              
	       } else {
	           windowWidth = window.innerWidth;
	           windowHeigth = window.innerHeight; 
	       }
	       
           try {
                initOverlayWindows();
            } catch(e) {
                alert( "initOverlayWindows: " + e);
            }
         
            <xsl:if test="boolean(//deegree:Module[./deegree:Name = 'MenuBarTop'])">
    	        try {
                    initMenubar(<xsl:value-of select="boolean(//deegree:Module[./deegree:Name = 'AdminConsole'])"/>);
                } catch(e) {
                    alert( "initMenubar: " + e);
                }
            </xsl:if>
	        
	        // panel for map view; will alway be located within center panel
	        var panel = {
                        border: false,                        
                        html: '<iframe frameborder="0" height="1550" id="IDOLMap" marginheight="0" marginwidth="0" name="IDOLMap" scrolling="no" src="./modules/olmap/olmap.jsp" width="1700"></iframe>'                        
                    };
            // panel for data table view; will always be located within south panel
            var panelSouth = {
                        border: false,                        
                        html: '<iframe frameborder="0" height="200" id="IDDataTable" marginheight="0" marginwidth="0" name="IDDataTable" scrolling="no" src="./modules/datatable/datatable.jsp" width="100%"></iframe>'
                    };
                            
	            	     
	        var vp ;
	        try {
	            vp = new Ext.Viewport({
                    layout: 'border',                    
                    items: [{
                                region: 'west',
                                layout: {
						                    type: 'vbox',
						                    padding: 5
						                },
						        width: 42,
                                title: 'control',
                                items: [ 
                                      {height:15, border:false},
                                      new Ext.Button( {                          
                                           icon: './images/page_white_gear.png',
                                           scale: 'medium',
                                           handler: function(toggled){   
	                                            westwin.show(this);    
                                           }
                                       } ), 
                                       {height:5, border:false},
                                       new Ext.Button( {                          
                                           icon: './images/text_list_bullets.png',
                                           scale: 'medium',
                                           handler: function(toggled){             
                                                eastwin.show(this);    
                                           }
                                       } ), 
                                       {height:5, border:false},
                                       new Ext.Button( {                          
                                           icon: './images/cog.png',
                                           scale: 'medium',
                                           handler: function(toggled){             
                                                southwin.show(this);    
                                           }
                                       } ) , 
                                       {height:5, border:false},
                                       new Ext.Button( {                          
                                           icon: './images/pencil.png',
                                           scale: 'medium',
                                           handler: function(toggled){             
                                                northwin.show(this);    
                                           }
                                       } )  
                                       ]                              
                           }, {
                                region: 'center',
                                title: 'map',
                                listeners: {
                                    resize: {
                                        fn: function(el, width, height) {   
                                                if ( controller != null ) {
                                                    var t = new Ext.util.DelayedTask( controller.setMapSize,  controller, [width-2, height-32] );
                                                    t.delay( 500 );
                                                }
                                            }
                                        }
                                    },
                                items: [ panel,
	                                <xsl:for-each select="//deegree:Center/deegree:Module[./@hidden != 'true' and ./@overlay != 'true' and deegree:Name != 'OLMap' and deegree:Name != 'DataTable' ]">
			                            {
			                            html: '<xsl:call-template name="CENTER"/>',
			                            border: false
			                            }
			                            <xsl:if test="position() != count(//deegree:Center/deegree:Module[./@hidden != 'true' and ./@overlay != 'true' and deegree:Name != 'OLMap' and deegree:Name != 'DataTable' ])">
			                            ,
			                            </xsl:if>                                               
				                    </xsl:for-each>     
                                 ]                                
                           }, {
                                region: 'south',
                                height: 200,
                                split: true,  
                                width: windowWidth,
                                items: [ panelSouth ]                                
                           }]
                      
                });           
                <xsl:call-template name="MAPBORDER"/>       
	         
	            vp.render();
	        } catch(e) {
	            alert( "create/render viewport: " + JSON.stringify( e ) );
	        }	       
	        controller.mapModel.setChanged( true );
	        controller.repaint();
	        
	    } ;// end init function     
    
    </xsl:template>
</xsl:stylesheet>            