<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:cntxt="http://www.opengis.net/context" xmlns:sld="http://www.opengis.net/sld" xmlns:deegree="http://www.deegree.org/context" xmlns:xlink="http://www.w3.org/1999/xlink">

	<xsl:template name="MAPBORDER">
	   <xsl:if test="boolean( //deegree:West )">	 
            <xsl:variable name="WIDTH">
                <xsl:choose>
                    <xsl:when test="boolean(//deegree:West/@width)">
                        <xsl:value-of select="//deegree:West/@width"/>
                    </xsl:when>
                    <xsl:otherwise>300</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:variable name="HEIGHT">
                <xsl:choose>
                    <xsl:when test="boolean(//deegree:West/@height)">
                        <xsl:value-of select="//deegree:West/@height"/>
                    </xsl:when>
                    <xsl:otherwise>300</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
		var westwin = new Ext.Window({
                        title: 'Functions',                                                
                        closeAction:'hide',
                        closable: true,
                        closed: true,
                        animCollapse: false,
                        plain: true,
                        x: 50,
                        y: 150,                       
		                width: <xsl:value-of select="$WIDTH"/>,
		                height: <xsl:value-of select="$HEIGHT"/>,
		                margins: '0 5 0 5',
		                layout: {
		                    type: 'accordion',
		                    animate: true
		                },
		                listeners: {
                                    resize: {
                                        fn: function(el, width, height) {   
                                                var pos = el.getPosition();
                                                storeFrame( 'west', pos[0], pos[1], width, height, el.isVisible() );
                                            }
                                        },
	                                move: {
                                        fn: function(el, x, y) {   
                                                storeFrame( 'west', x, y, el.getWidth(), el.getHeight(), el.isVisible() );
                                             }
                                         }
                                    },
                        items: [
	                         <xsl:for-each select="//deegree:West/deegree:Module">
	                             <xsl:if test="./@hidden != 'true'">
	                                 {
	                                 html: '<xsl:call-template name="WEST"/>',
	                                 title: '<xsl:value-of select="deegree:Title"/>',
	                                 border: false
	                                 }
	                                 <xsl:if test="position() != count(//deegree:West/deegree:Module[./@hidden != 'true' and ./@overlay != 'true'])">
	                                 ,
	                                 </xsl:if>                                               
	                             </xsl:if>
	                         </xsl:for-each>
	                        ]                         
                    });
          westwin.show( this );
          westwin.hide();
       </xsl:if>
        
        <xsl:if test="boolean( //deegree:East )">
            <xsl:variable name="WIDTH">
                <xsl:choose>
                    <xsl:when test="boolean(//deegree:East/@width)">
                        <xsl:value-of select="//deegree:East/@width"/>
                    </xsl:when>
                    <xsl:otherwise>300</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:variable name="HEIGHT">
                <xsl:choose>
                    <xsl:when test="boolean(//deegree:East/@height)">
                        <xsl:value-of select="//deegree:East/@height"/>
                    </xsl:when>
                    <xsl:otherwise>300</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
        var eastwin = new Ext.Window({
                        title: 'Information',                        
                        closeAction:'hide',
                        closable: true,
                        closed: true,
                        animCollapse: false,
                        plain: true,
                        x: 600,
                        y: 150,                       
                        width: <xsl:value-of select="$WIDTH"/>,
                        height: <xsl:value-of select="$HEIGHT"/>,
                        margins: '0 5 0 5',
                        layout: {
                            type: 'accordion',
                            animate: true
                        },
                        listeners: {
                                    resize: {
                                        fn: function(el, width, height) {   
                                                var pos = el.getPosition();
                                                storeFrame( 'east', pos[0], pos[1], width, height, el.isVisible() );
                                            }
                                        },
                                    move: {
                                        fn: function(el, x, y) {   
                                                storeFrame( 'east', x, y, el.getWidth(), el.getHeight(), el.isVisible() );
                                             }
                                         }
                                    },
                        items: [
                             <xsl:for-each select="//deegree:East/deegree:Module">
                                 <xsl:if test="./@hidden != 'true'">
                                     {
                                     html: '<xsl:call-template name="EAST"/>',
                                     title: '<xsl:value-of select="deegree:Title"/>',
                                     border: false
                                     }
                                     <xsl:if test="position() != count(//deegree:East/deegree:Module[./@hidden != 'true' and ./@overlay != 'true'])">
                                     ,
                                     </xsl:if>                                               
                                 </xsl:if>
                             </xsl:for-each>
                            ]                         
                    });
            eastwin.show( this );
            eastwin.hide();
        </xsl:if>
        
        <xsl:if test="boolean( //deegree:South )">
            <xsl:variable name="WIDTH">
                <xsl:choose>
                    <xsl:when test="boolean(//deegree:South/@width)">
                        <xsl:value-of select="//deegree:South/@width"/>
                    </xsl:when>
                    <xsl:otherwise>300</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:variable name="HEIGHT">
                <xsl:choose>
                    <xsl:when test="boolean(//deegree:South/@height)">
                        <xsl:value-of select="//deegree:South/@height"/>
                    </xsl:when>
                    <xsl:otherwise>300</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
        var southwin = new Ext.Window({
                        title: 'Information',                        
                        closeAction:'hide',
                        closable: true,
                        closed: true,
                        animCollapse: false,
                        plain: true,
                        x: 600,
                        y: 150,                       
                        width: <xsl:value-of select="$WIDTH"/>,
                        height: <xsl:value-of select="$HEIGHT"/>,
                        margins: '0 5 0 5',
                        layout: {
                            type: 'accordion',
                            animate: true
                        },
                        listeners: {
                                    resize: {
                                        fn: function(el, width, height) {   
                                                var pos = el.getPosition();
                                                storeFrame( 'south', pos[0], pos[1], width, height, el.isVisible() );
                                            }
                                        },
                                    move: {
                                        fn: function(el, x, y) {   
                                                storeFrame( 'south', x, y, el.getWidth(), el.getHeight(), el.isVisible() );
                                             }
                                         }
                                    },
                        items: [
                             <xsl:for-each select="//deegree:South/deegree:Module">
                                 <xsl:if test="./@hidden != 'true'">
                                     {
                                     html: '<xsl:call-template name="SOUTH"/>',
                                     title: '<xsl:value-of select="deegree:Title"/>',
                                     border: false
                                     }
                                     <xsl:if test="position() != count(//deegree:South/deegree:Module[./@hidden != 'true' and ./@overlay != 'true'])">
                                     ,
                                     </xsl:if>                                               
                                 </xsl:if>
                             </xsl:for-each>
                            ]                         
                    });
            southwin.show( this );
            southwin.hide();
        </xsl:if>
        
        <xsl:if test="boolean( //deegree:North )">
            <xsl:variable name="WIDTH">
                <xsl:choose>
                    <xsl:when test="boolean(//deegree:North/@width)">
                        <xsl:value-of select="//deegree:North/@width"/>
                    </xsl:when>
                    <xsl:otherwise>300</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:variable name="HEIGHT">
                <xsl:choose>
                    <xsl:when test="boolean(//deegree:North/@height)">
                        <xsl:value-of select="//deegree:North/@height"/>
                    </xsl:when>
                    <xsl:otherwise>300</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
        var northwin = new Ext.Window({
                        title: 'Information',                        
                        closeAction:'hide',
                        closable: true,
                        closed: true,
                        animCollapse: false,
                        plain: true,
                        x: 600,
                        y: 150,                       
                        width: <xsl:value-of select="$WIDTH"/>,
                        height: <xsl:value-of select="$HEIGHT"/>,
                        margins: '0 5 0 5',
                        layout: {
                            type: 'accordion',
                            animate: true
                        },
                        listeners: {
                                    resize: {
                                        fn: function(el, width, height) {   
                                                var pos = el.getPosition();
                                                storeFrame( 'north', pos[0], pos[1], width, height, el.isVisible() );
                                            }
                                        },
                                    move: {
                                        fn: function(el, x, y) {   
                                                storeFrame( 'north', x, y, el.getWidth(), el.getHeight(), el.isVisible() );
                                             }
                                         }
                                    },
                        items: [
                             <xsl:for-each select="//deegree:North/deegree:Module">
                                 <xsl:if test="./@hidden != 'true'">
                                     {
                                     html: '<xsl:call-template name="NORTH"/>',
                                     title: '<xsl:value-of select="deegree:Title"/>',
                                     border: false
                                     }
                                     <xsl:if test="position() != count(//deegree:North/deegree:Module[./@hidden != 'true' and ./@overlay != 'true'])">
                                     ,
                                     </xsl:if>                                               
                                 </xsl:if>
                             </xsl:for-each>
                            ]                         
                    });
            northwin.show( this );
            northwin.hide();
        </xsl:if>
        
	</xsl:template>

	<xsl:template name="WINDOW">
		<iframe frameborder="0" height="600" marginheight="0" marginwidth="0" scrolling="no" width="350">
			<xsl:attribute name="id"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
			<xsl:attribute name="name"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
			<xsl:attribute name="src"><xsl:value-of select="./deegree:Content"/></xsl:attribute>
		</iframe>
	</xsl:template>
	
	<xsl:template name="CENTER">
		<iframe frameborder="0" marginheight="0" marginwidth="0" >
			<xsl:attribute name="id"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
			<xsl:attribute name="name"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
			<xsl:attribute name="src"><xsl:value-of select="./deegree:Content"/></xsl:attribute>
			<xsl:choose>
				<xsl:when test="./@scrolling != ''">
					<xsl:attribute name="scrolling"><xsl:value-of select="./@scrolling"/></xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="scrolling">auto</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>			
				<xsl:when test="./@width">
					<xsl:attribute name="width"><xsl:value-of select="./@width"/></xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="width"><xsl:value-of select="$vMapWidth"/></xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="./@height">
					<xsl:attribute name="height"><xsl:value-of select="./@height"/></xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="height"><xsl:value-of select="$vMapHeight"/></xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
		</iframe>
	</xsl:template>
	<xsl:template name="EAST">
		<iframe frameborder="0" marginheight="0" marginwidth="0" width="100%">
		<xsl:message>
		  <xsl:copy-of select="."></xsl:copy-of>
		</xsl:message>
			<xsl:attribute name="id"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
			<xsl:attribute name="name"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
			<xsl:attribute name="src"><xsl:value-of select="./deegree:Content"/></xsl:attribute>
			<xsl:choose>
				<xsl:when test="./@scrolling != ''">
					<xsl:attribute name="scrolling"><xsl:value-of select="./@scrolling"/></xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="scrolling">auto</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>			
			<xsl:if test="./@height">
				<xsl:attribute name="height"><xsl:value-of select="./@height"/></xsl:attribute>
			</xsl:if>
		</iframe>
	</xsl:template>	
	
	<xsl:template name="WEST">
		<iframe frameborder="0" marginheight="0" marginwidth="0" width="100%">
			<xsl:attribute name="id"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
			<xsl:attribute name="name"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
			<xsl:attribute name="src"><xsl:value-of select="./deegree:Content"/></xsl:attribute>
			<xsl:choose>
				<xsl:when test="./@scrolling != ''">
					<xsl:attribute name="scrolling"><xsl:value-of select="./@scrolling"/></xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="scrolling">auto</xsl:attribute>
				</xsl:otherwise>
				</xsl:choose>				
				<xsl:if test="./@height">
					<xsl:attribute name="height"><xsl:value-of select="./@height"/></xsl:attribute>
				</xsl:if>
		</iframe>
	</xsl:template>
	
	<xsl:template name="SOUTH">
		<iframe frameborder="0" marginheight="0" marginwidth="0" width="100%">
			<xsl:attribute name="id"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
			<xsl:attribute name="name"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
			<xsl:attribute name="src"><xsl:value-of select="./deegree:Content"/></xsl:attribute>
			<xsl:choose>
				<xsl:when test="./@scrolling != ''">
					<xsl:attribute name="scrolling"><xsl:value-of select="./@scrolling"/></xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="scrolling">auto</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="./@height">
					<xsl:attribute name="height"><xsl:value-of select="./@height"/></xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="height"><xsl:value-of select="$vSouthHeight"/></xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
		</iframe>
	</xsl:template>
	
	<xsl:template name="NORTH">
        <iframe frameborder="0" marginheight="0" marginwidth="0" width="100%">
            <xsl:attribute name="id"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
            <xsl:attribute name="name"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
            <xsl:attribute name="src"><xsl:value-of select="./deegree:Content"/></xsl:attribute>
            <xsl:choose>
                <xsl:when test="./@scrolling != ''">
                    <xsl:attribute name="scrolling"><xsl:value-of select="./@scrolling"/></xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="scrolling">auto</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="./@height">
                    <xsl:attribute name="height"><xsl:value-of select="./@height"/></xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="height"><xsl:value-of select="$vSouthHeight"/></xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
        </iframe>
    </xsl:template>

</xsl:stylesheet>
