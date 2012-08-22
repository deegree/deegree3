<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:cntxt="http://www.opengis.net/context" xmlns:sld="http://www.opengis.net/sld" xmlns:deegree="http://www.deegree.org/context" xmlns:xlink="http://www.w3.org/1999/xlink">

	<xsl:template name="MAPBORDER">
		layout: 'border',
		border: false,
		items: [				
			{
				// the only module ever displayed in north: menubartop
				region: 'north',
                margins: '5 5 5 5',
				height: <xsl:value-of select="$vNorthHeight"/>, 
				items: [ {
					contentEl: 'menubar',
                    border: false
                }]
			},
			{
				region: 'center',
                margins: '0 0 0 0',
				listeners: {
					resize: {
						fn: function(el, width, height) {					
							if ( controller != null ) {	
                                // do NOT change white spaces in the following line
								var t = new Ext.util.DelayedTask( controller.setMapSize,  controller, [width-2, height-35] );
                                t.delay( 500 );
							}
						}
					}      					
				},
				items: [
					<xsl:for-each select="//deegree:Center/deegree:Module[./@hidden != 'true' and ./@overlay != 'true' ]">
						{
                        <xsl:if test="deegree:Name = 'OLMap'">
                        style: 'height: 100%;',
                        id: 'mappanelheightfix',
                        </xsl:if>
						html: '<xsl:call-template name="CENTER"/>',
						border: false
						}
						<xsl:if test="position() != count(//deegree:Center/deegree:Module[./@hidden != 'true' and ./@overlay != 'true'])">
						,
						</xsl:if>						
					</xsl:for-each>										
				]
			},
			{
				region: 'south',
				layout: 'table',
                layoutConfig: {rows:1},
				margins: '5 5 5 5',
				height: <xsl:value-of select="$vSouthHeight"/>, 
				items: [
					<xsl:for-each select="//deegree:South/deegree:Module[./@hidden != 'true' and ./@overlay != 'true']">
						{
						html: '<xsl:call-template name="SOUTH"/>',
						border: false
						}
						<xsl:if test="position() != count(//deegree:South/deegree:Module[./@hidden != 'true' and ./@overlay != 'true'])">
						,
						</xsl:if>												
					</xsl:for-each>
				]
			},
			 {
				region: 'east',
				title: 'East Side',
				collapsible: true,
				width: <xsl:value-of select="$vEastWidth"/>,
				margins: '0 5 0 5',
				layout: {
					type: 'accordion',
					animate: true
				},
				items:  [
					<xsl:for-each select="//deegree:East/deegree:Module[./@hidden != 'true' and ./@overlay != 'true']">
						{
                            html: '<xsl:call-template name="EAST"/>',
                            title: '<xsl:value-of select="deegree:Title"/>',
                            border: false
						}
						<xsl:if test="position() != count(//deegree:East/deegree:Module[./@hidden != 'true' and ./@overlay != 'true'])">
						,
						</xsl:if>												
					</xsl:for-each>										
				]
			}, 
			{
				region: 'west',
				title: 'West',
                collapsible: true,
				width: <xsl:value-of select="$vWestWidth"/>,
				margins: '0 5 0 5',
				layout: {
					type: 'accordion',
					animate: true
				},
				items: [
					<xsl:for-each select="//deegree:West/deegree:Module[./@hidden != 'true' and ./@overlay != 'true']">
						{
							html: '<xsl:call-template name="WEST"/>',
							title: '<xsl:value-of select="deegree:Title"/>',
							border: false
						}
						<xsl:if test="position() != count(//deegree:West/deegree:Module)">
						,
						</xsl:if>												
					</xsl:for-each>										
				]				
			}
		]		
	</xsl:template>

	<xsl:template name="WINDOW">
		<iframe frameborder="0" marginheight="0" marginwidth="0" >
			<xsl:attribute name="id"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
			<xsl:attribute name="name"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
			<xsl:attribute name="src"><xsl:value-of select="./deegree:Content"/></xsl:attribute>
			<xsl:attribute name="width"><xsl:value-of select="./@width"/></xsl:attribute>
			<xsl:attribute name="height"><xsl:value-of select="./@height"/></xsl:attribute>
			<xsl:attribute name="scrolling"><xsl:value-of select="./@scrolling"/></xsl:attribute>
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
		<iframe frameborder="0" marginheight="0" marginwidth="0">
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
				<xsl:when test="../@width">
					<xsl:attribute name="width"><xsl:value-of select="../@width"/></xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="width"><xsl:value-of select="$vEastWidth"/></xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:if test="./@height">
				<xsl:attribute name="height"><xsl:value-of select="./@height"/></xsl:attribute>
			</xsl:if>
		</iframe>
	</xsl:template>	
	
	<xsl:template name="WEST">
		<iframe frameborder="0" height="600" marginheight="0" marginwidth="0" scrolling="no" width="350">
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
					<xsl:when test="../@width">
						<xsl:attribute name="width"><xsl:value-of select="../@width"/></xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="width"><xsl:value-of select="$vWestWidth"/></xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:if test="./@height">
					<xsl:attribute name="height"><xsl:value-of select="./@height"/></xsl:attribute>
				</xsl:if>
		</iframe>
	</xsl:template>
   
	<xsl:template name="SOUTH">
		<iframe frameborder="0" marginheight="0" marginwidth="0">
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
            <xsl:if test="./@width">
                <xsl:attribute name="width"><xsl:value-of select="./@width"/></xsl:attribute>
            </xsl:if>
		</iframe>
	</xsl:template>
    
</xsl:stylesheet>
