<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

	<xsl:output encoding="iso-8859-1"  method="html"></xsl:output>

	<xsl:template match="/">
		<html>
			<head>
				<title></title>
			</head>
			<body>
				<xsl:call-template name="ITERATE"></xsl:call-template>	
			</body>
		</html>
	</xsl:template>
	
	<xsl:template name="ITERATE">
		<xsl:for-each select="./child::*">
			<ul style="line-height:1.1em; list-style-position:inside">
				<li style="line-height:1.1em; list-style-type:none">
					<xsl:call-template name="OPENNODE"></xsl:call-template>
					<xsl:choose>
						<xsl:when test="count( child::* ) = 0 ">	
							<xsl:value-of select="."></xsl:value-of>
						</xsl:when>
						<xsl:otherwise>
								<xsl:call-template name="ITERATE"></xsl:call-template>				
						</xsl:otherwise>
					</xsl:choose>			
					<xsl:call-template name="CLOSENODE"></xsl:call-template>
				</li>
			</ul>
		</xsl:for-each>
	</xsl:template>
		
	<xsl:template name="OPENNODE">
		<code style="color:#0000FF;">&lt;</code><code style="color:#993300;"><xsl:value-of select="name(.)"/></code><xsl:call-template name="NAMESP"/><xsl:apply-templates select="./@*"/><code style="color:#0000FF;">&gt;</code>
	</xsl:template>
	
	<xsl:template name="CLOSENODE">
		<code style="color:#0000FF; line-height:1.4em">&lt;/</code><code style="color:#993300;"><xsl:value-of select="name(.)" /></code><code style="color:#0000FF;">&gt;</code>
	</xsl:template>
	
	<xsl:template match="@*"><code style="color:#FF0000;"><xsl:value-of select="concat( ' ',  name(.), '=' )"/></code>"<xsl:value-of select="."/>"</xsl:template>
	
	<xsl:template name="NAMESP"><code style="color:#FF0000;"> xmlns</code><code style="color:#0000FF;">="</code><xsl:value-of select="namespace-uri(.)"/><code style="color:#0000FF;">"</code></xsl:template>

</xsl:stylesheet>
