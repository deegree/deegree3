<!-- This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license. -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:cntxt="http://www.opengis.net/context" xmlns:sld="http://www.opengis.net/sld" xmlns:deegree="http://www.deegree.org/context" xmlns:xlink="http://www.w3.org/1999/xlink">

    <xsl:template name="INITEXTJS">
       
    
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
	        });
          })
	        
	    function initOverlayWindows() {
            // overlayed windows            
            <xsl:for-each select="//deegree:Module">
                <xsl:if test="./@overlay = 'true' ">
                     var win = new Ext.Window({
                        id: 'win<xsl:value-of select="deegree:Name"/>',
                        title: '<xsl:value-of select="deegree:Name"/>',
                        layout:'fit',
                        width:<xsl:value-of select="./@width"/>+20,
                        height: <xsl:value-of select="./@height"/>+60,
                        closeAction:'hide',
                        closable: <xsl:value-of select="boolean(./@hidden = 'true')"/>,
                        collapsible: true,
                        animCollapse: false,
                        autoScroll: true,
                        plain: true,
                        x: <xsl:value-of select="./@left"/>,
                        y: <xsl:value-of select="./@top"/>,
                        items: [ {              
                                html: '<xsl:call-template name="WINDOW"/>',
                                border: false,
                                <xsl:if test="boolean(./@scrolling) and ./@scrolling != 'no'">
                                height: <xsl:value-of select="./@height"/>,
                                autoScroll: true
                                </xsl:if>
                                }]
                    });                   
                </xsl:if>
                <xsl:if test="./@hidden != 'true' and ./@overlay = 'true' ">
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
	        	      
	        var vp ;
	        try {
	            vp = new Ext.Viewport (
	                {
	                    listeners: {
	                    resize: {
	                        fn: function(el, width, height) {
	                            if ( vp != null ) {
	                                 var mp = Ext.getCmp( 'MapPanel' );
	                                 if ( mp != null ) {
	                                     mp.setHeight( height - 60 - 10 ); // mpHeight = height - region:north(height) - region:north(margin top+bottom)
	                                     mp.setWidth( width );
	                                 }	                                
		                          }
		                       }
		                    }
		                },
	                    layout: 'border',                           
	                    items:[
	                        {
	                            region: 'north',
                                margins: '5 5 5 5', // top, right, bottom, left
	                            collapsible: false,
	                            split: false,
	                            height: 60,
	                            html: '<img border="0" src="./images/logo-deegree.png"></img> iGeoPortal'
                                //html: '<iframe frameborder="0" height="60" marginheight="0" marginwidth="0" scrolling="no" width="100%" src="header.html"></iframe>'
	                        },
	                        {
	                            id: 'centerfield',
	                            region: 'center',
                                border: false,
	                            items:[ 
		                            {
	                                 id: 'MapPanel',         
	                                 height: (windowHeigth-90),
                                     width: windowWidth,                             
	                                 <xsl:call-template name="MAPBORDER"/>
	                                }
	                            ]	                            
	                        }
	                    ]
	                }
	            ); // end of viewport
	        } catch(e) {
	            alert( "create viewport: " + e);
	        }
	        
	        try {   
	            vp.render();
	        } catch(e) {
	            alert( "render viewport: " + e);
	        }	       
	        
	    } ;// end init function     
    
    </xsl:template>
</xsl:stylesheet>            