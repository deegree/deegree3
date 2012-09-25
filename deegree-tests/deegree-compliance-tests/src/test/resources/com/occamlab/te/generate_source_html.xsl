<!-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

  The contents of this file are subject to the Mozilla Public License
  Version 1.1 (the "License"); you may not use this file except in
  compliance with the License. You may obtain a copy of the License at
  http://www.mozilla.org/MPL/ 

  Software distributed under the License is distributed on an "AS IS" basis,
  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  the specific language governing rights and limitations under the License. 

  The Original Code is TEAM Engine.

  The Initial Developer of the Original Code is Northrop Grumman Corporation
  jointly with The National Technology Alliance.  Portions created by
  Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
  Grumman Corporation. All Rights Reserved.

  Contributor(s): No additional contributors to date

 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:transform
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:txsl="http://www.w3.org/1999/XSL/Transform/target"
 xmlns:ctl="http://www.occamlab.com/ctl"
 xmlns:te="java:com.occamlab.te.TECore"
 xmlns:saxon="http://saxon.sf.net/"
 version="2.0">
	<xsl:strip-space elements="*"/>
	<xsl:output method="html" indent="yes"/>
	<xsl:output name="xml" omit-xml-declaration="yes" cdata-section-elements="ctl:comment ctl:body" indent="yes"/>
	
	<xsl:param name="filename"/>

 	<xsl:template match="@*">
		<xsl:copy-of select="."/>
	</xsl:template>

 	<xsl:template match="text()">
	</xsl:template>

 	<xsl:template match="*">
 		<xsl:copy>
 			<xsl:apply-templates select="@*"/>
 			<xsl:apply-templates/>
 		</xsl:copy>
	</xsl:template>

 	<xsl:template match="ctl:package">
 		<xsl:variable name="element">
 			<xsl:copy>TextContent</xsl:copy>
 		</xsl:variable>
 		<xsl:variable name="serialization" select="saxon:serialize($element/*, 'xml')"/>
 		<pre>
 			<xsl:value-of select="substring-before($serialization, 'TextContent')"/>
 		</pre>
		<xsl:apply-templates/>
 		<pre>
 			<xsl:value-of select="substring-after($serialization, 'TextContent')"/>
 		</pre>
	</xsl:template>

 	<xsl:template match="ctl:suite|ctl:profile|ctl:test|ctl:function|ctl:parser">
 		<a name="{@name}"/>
 		<pre>
	 		<xsl:variable name="this">
 				<xsl:copy-of select="."/>
 			</xsl:variable>
	 		<xsl:variable name="element">
	 			<xsl:for-each select="..">
	 				<xsl:copy>
		 				<xsl:copy-of select="$this/*"/>
	 				</xsl:copy>
	 			</xsl:for-each>
 			</xsl:variable>
 			<xsl:variable name="serialization" select="saxon:serialize($element/*, 'xml')"/>
 			<xsl:variable name="prefix" select="concat(substring-before($serialization, '&gt;'), '&gt;')"/>
 			<xsl:value-of select="substring($serialization, string-length($prefix)+1, string-length($serialization) - string-length($prefix) - string-length(name($element/*)) - 3)"/>
	 	</pre>
	</xsl:template>

	<xsl:template match="/">
		<html>
			<head>
				<title>Source for <xsl:value-of select="$filename"/></title>
			</head>
			<body>
				<xsl:apply-templates/>
			</body>
		</html>
	</xsl:template>
</xsl:transform>

