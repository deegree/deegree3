<?xml version="1.0" encoding="UTF-8"?>
<ctl:package
 xmlns="http://www.w3.org/2001/XMLSchema"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:ctl="http://www.occamlab.com/ctl"
 xmlns:parsers="http://www.occamlab.com/te/parsers"
 xmlns:myparsers="http://teamengine.sourceforge.net/parsers"
 xmlns:saxon="http://saxon.sf.net/"
 xmlns:wfs="http://www.opengis.net/wfs"
 xmlns:ows="http://www.opengis.net/ows"
 xmlns:gml="http://www.opengis.net/gml" 
 xmlns:ogc="http://www.opengis.net/ogc"
 xmlns:sf="http://cite.opengeospatial.org/gmlsf" 
 xmlns:xi="http://www.w3.org/2001/XInclude">

	<!-- Sample usage:
    (1)
    <ctl:call-test name="ctl:assert-xpath">
		<ctl:with-param name="expr">/wfs:WFS_Capabilities</ctl:with-param>
		<ctl:with-param name="doc" select="$doc"/>
    </ctl:call-test>
    (2)
    <xsl:variable name="expression">/wfs:WFS_Capabilities</xsl:variable>
	<ctl:call-test name="ctl:assert-xpath">
	    <ctl:with-param name="expr" select="$expression"/>
	    <ctl:with-param name="doc" select="$doc"/>
	</ctl:call-test>
    -->
	<ctl:test name="ctl:assert-xpath">
		<ctl:param name="expr">An XPath expression</ctl:param>
		<ctl:param name="doc">An XML document</ctl:param>
		<ctl:assertion>
        Evaluates the given XPath expression against the input document and 
        returns a boolean result according to the XPath specification (see 
        http://www.w3.org/TR/xpath#section-Boolean-Functions).
        </ctl:assertion>
		<ctl:code>
			<xsl:for-each select="$doc">
				<xsl:choose>
					<xsl:when test="saxon:evaluate($expr)"/>
					<xsl:otherwise>
						<ctl:message>The expression '<xsl:value-of select="$expr"/>' is false.</ctl:message>
						<ctl:fail/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</ctl:code>
	</ctl:test>
	
    <ctl:function name="wfs:extract-gml-id">
      <!-- TODO return a sequence of id values -->
      <ctl:param name="response">A wfs:FeatureCollection document</ctl:param>
      <ctl:return>The gml:id value for the first feature instance.</ctl:return>
      <ctl:description>Extracts the gml:id value for the first feature in the collection.</ctl:description>
      <ctl:code>
        <xsl:choose>
          <xsl:when test="boolean($response//gml:featureMember)">
            <xsl:value-of select="$response//gml:featureMember[1]/*[1]/@gml:id"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$response//gml:featureMembers/*[1]/@gml:id"/>
          </xsl:otherwise>
        </xsl:choose>
      </ctl:code>
   </ctl:function>
   
   <ctl:function name="wfs:disjoint-envelopes">
      <ctl:param name="env">The gml:Envelope specifying an area of interest</ctl:param>
      <ctl:param name="bbox">The gml:boundedBy property describing the extent of some feature.</ctl:param>
      <ctl:return>
      Returns '1' if the envelopes are disjoint or '0' if they are not (i.e. they 
      intersect). Returns '-1' if the CRS references do not match.
      </ctl:return>
      <ctl:description>Determines if two envelopes are disjoint. If not, they intersect.</ctl:description>
      <ctl:code>
         <xsl:variable name="minX1" select="xsd:decimal(substring-before($env//gml:lowerCorner, ' '))" />
         <xsl:variable name="maxX1" select="xsd:decimal(substring-before($env//gml:upperCorner, ' '))" />
         <xsl:variable name="minY1" select="xsd:decimal(substring-after($env//gml:lowerCorner, ' '))" />
         <xsl:variable name="maxY1" select="xsd:decimal(substring-after($env//gml:upperCorner, ' '))" />
         <xsl:variable name="minX2" select="xsd:decimal(substring-before($bbox//gml:lowerCorner, ' '))" />
         <xsl:variable name="maxX2" select="xsd:decimal(substring-before($bbox//gml:upperCorner, ' '))" />
         <xsl:variable name="minY2" select="xsd:decimal(substring-after($bbox//gml:lowerCorner, ' '))" />
         <xsl:variable name="maxY2" select="xsd:decimal(substring-after($bbox//gml:upperCorner, ' '))" />
         <xsl:choose>
<!-- TODO: need a more sophisticated CRS matching scheme.  For now, disable CRS check.  CEM 10-1-09
            <xsl:when test="$env//@srsName != $bbox//@srsName">-1</xsl:when>
-->
            <xsl:when test="($minX2 gt $maxX1) or ($minY2 gt $maxY1) or ($maxX2 lt $minX1) or ($maxY2 lt $minY1)">1</xsl:when>
            <xsl:otherwise>0</xsl:otherwise>
         </xsl:choose>
      </ctl:code>
   </ctl:function>
   
   <ctl:function name="wfs:encode">
      <ctl:param name="s">String to encode</ctl:param>
      <ctl:java class="java.net.URLEncoder" method="encode"/>
   </ctl:function>

   <ctl:function name="wfs:sleep">
      <ctl:param name="milliseconds"/>
      <ctl:java class="java.lang.Thread" method="sleep"/>
   </ctl:function>
   
</ctl:package>
