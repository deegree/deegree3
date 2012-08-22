<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xlink="http://www.w3.org/1999/xlink"
   xmlns:ogc="http://www.opengis.net/ogc"
   xmlns:ows="http://www.opengis.net/ows"
   xmlns:gml="http://www.opengis.net/gml"
   xmlns:wfs="http://www.opengis.net/wfs"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xmlns:java="java"
   xmlns:geomutils="org.deegree.framework.xml.GeometryUtils"
   version="1.0">

  <xsl:output method="xml" indent="yes" />

  <!-- maps gml3.1.1 featurecollection to gml2.1.2 featurecollection -->
  <xsl:template match="wfs:FeatureCollection">
    <FeatureCollection xmlns="http://www.opengis.net/wfs">
      <xsl:copy-of select="@lockId" />
      <xsl:copy-of select="@xsi:schemaLocation" />
      <xsl:apply-templates select="@gml:id"/>
      <xsl:if test="not(boolean(gml:boundedBy))">
        <gml:boundedBy>
          <gml:null>inapplicable</gml:null>
        </gml:boundedBy>
      </xsl:if>
      <xsl:apply-templates select="*"/>
    </FeatureCollection>
  </xsl:template>

  <!-- none mappable elements-->
  <xsl:template match="gml:metaDataProperty"/>
  <xsl:template match="gml:location"/>
  <xsl:template match="@axisLabels"/>
  <xsl:template match="@uomLabels"/>
  <xsl:template match="@srsDimension"/>

  <xsl:template match="@gml:id">
    <xsl:attribute name="fid">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template name="StandardObjectProperties">
    <!--xsl:apply-templates select="gml:metaDataProperty"/-->
    <xsl:apply-templates select="gml:description"/>
    <xsl:apply-templates select="gml:name[0]"/>
  </xsl:template>

  <xsl:template match="gml:description">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="gml:name">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="gml:pos">
    <gml:coordinates cs=" " decimal="." ts=",">
      <xsl:value-of select="." />
    </gml:coordinates>
  </xsl:template>

  <xsl:template match="gml:boundedBy">
    <xsl:copy>
      <xsl:apply-templates select="gml:Envelope"/>
      <xsl:apply-templates select="gml:Null"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gml:Null">
    <xsl:param name="null" select="."/>
    <gml:Null>
      <xsl:choose>
        <xsl:when test="$null = 'inapplicable' or $null='missing' or $null='unknown'">
          <xsl:value-of select="$null"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>unavailable</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </gml:Null>
  </xsl:template>


  <xsl:template match="gml:featureMember">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:choose>
        <xsl:when test="count(*) = 0">
          <xsl:value-of select="."/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="*"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@*">
    <xsl:copy-of select="."/>
  </xsl:template>


  <!--################################
      # * Geometry Section
      # * Because deegree, uses a predefined output
      # * (see org.deegree.model.spatialschema.GMLGeometryAdapter),
      # * following geometries are copied-of,
      # * @id and @gid are never set for geometries so no need to fear them:
      # - gml:LineString (only uses gml:coordinates)
      # - gml:Point (only uses gml:coordinates)
      # - gml:Polygon (which is a surface or multisurface) uses the deprecated
      #   gml:outerBoundaryIs (outerBoundaryIs in gml:3.1.1 is an extension of gml:exterior)
      #   and the gml:innerBoundaryIs (also an deprecrecated extension of gml:interior)
      #   both use the deprecated gml:coordinates.
      # - gml:MultiPoint only uses gml:PointMember (not gml:PointMembers), there can be
      #   null member though...which in gml2 must at leas be one.
      ###############################-->

  <xsl:template name="copy_attribs">
    <xsl:copy-of select="@xlink:*"/>
    <xsl:copy-of select="@remoteSchema"/>
    <xsl:choose>
      <xsl:when test="@gid != '' ">
        <xsl:copy-of select="@gid"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:if test="@gml:id != '' ">
          <xsl:attribute name="gid" >
            <xsl:value-of select="@gml:id"/>
          </xsl:attribute>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:copy-of select="@srsName"/>
  </xsl:template>

  <xsl:template name="create_required_srsName">
    <xsl:if test="@srsName = ''">
      <xsl:attribute name="srsName">
        <xsl:text>unknown</xsl:text>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template match="gml:Envelope">
    <gml:Box>
      <xsl:call-template name="copy_attribs"/>
      <gml:coordinates decimal="." cs="&#x20;" ts=",">
        <xsl:value-of select="gml:lowerCorner" />
        <xsl:text>,</xsl:text>
        <xsl:value-of select="gml:upperCorner" />
      </gml:coordinates>
    </gml:Box>
  </xsl:template>

  <xsl:template match="gml:MultiPoint">
    <xsl:copy>
      <xsl:call-template name="copy_attribs"/>
      <xsl:call-template name="create_required_srsName"/>
      <xsl:copy-of select="gml:pointMember"/>
      <xsl:apply-templates select="gml:pointMembers" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gml:pointMembers">
    <xsl:for-each select="gml:Point">
      <gml:pointMember>
        <xsl:apply-templates select="." />
      </gml:pointMember>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="gml:MultiCurve">
    <gml:MultiLineString>
      <xsl:call-template name="copy_attribs"/>
      <xsl:call-template name="create_required_srsName"/>
      <!--name, description etc. are not valid for GML2, only select the curveMembers-->
      <xsl:apply-templates select="gml:curveMember"/>
      <xsl:apply-templates select="gml:curveMembers" />
    </gml:MultiLineString>
  </xsl:template>

  <xsl:template match="gml:curveMember" >
    <gml:lineStringMember>
      <xsl:call-template name="copy_attribs"/>
      <!-- Attention, the GML2 spec defines minOccurs=1, GML3.1.1 minOccurs=0 -->
      <!--name, description etc. are not valid for GML2, only select the curveMembers-->
      <xsl:copy-of select="gml:LineString" />
    </gml:lineStringMember>
  </xsl:template>

  <xsl:template match="gml:curveMembers">
    <xsl:for-each select="gml:Curve">
      <gml:lineStringMember>
        <xsl:apply-templates select="." />
      </gml:lineStringMember>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="gml:segments">
    <xsl:for-each select="gml:LineStringSegment">
      <xsl:apply-templates select="gml:posList" />
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="gml:MultiSurface">
    <gml:MultiPolygon>
      <xsl:call-template name="copy_attribs"/>
      <xsl:call-template name="create_required_srsName"/>
      <!--name, description etc. are not valid for GML2, only select the
      curveMembers-->
      <xsl:apply-templates select="gml:surfaceMember"/>
      <xsl:apply-templates select="gml:surfaceMembers"/>
    </gml:MultiPolygon>
  </xsl:template>

  <xsl:template match="gml:surfaceMember" >
    <gml:polygonMember>
      <xsl:call-template name="copy_attribs"/>
      <!-- Attention, the GML2 spec defines minOccurs=1, GML3.1.1 minOccurs=0 -->
      <!--name, description etc. are not valid for GML2, only select the curveMembers-->
      <xsl:apply-templates select="gml:Polygon" />
    </gml:polygonMember>
  </xsl:template>

  <xsl:template match="gml:surfaceMembers">
    <xsl:for-each select="gml:Surface">
      <gml:polygonMember>
        <xsl:apply-templates select="." />
      </gml:polygonMember>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="gml:Curve">
    <gml:LineString>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates select="*" />
    </gml:LineString>
  </xsl:template>

  <xsl:template match="gml:Polygon">
    <xsl:copy>
      <xsl:call-template name="copy_attribs"/>
      <xsl:apply-templates select="*[local-name( )!= 'name' or local-name( )!= 'metaDataProperty' or local-name( )!= 'description']"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gml:Surface">
    <gml:Polygon>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates select="gml:patches/gml:PolygonPatch/*" />
    </gml:Polygon>
  </xsl:template>

  <xsl:template match="gml:exterior">
    <gml:outerBoundaryIs>
      <xsl:call-template name="copy_attribs"/>
      <xsl:apply-templates select="gml:LinearRing"/>
    </gml:outerBoundaryIs>
  </xsl:template>

  <xsl:template match="gml:posList">
    <gml:coordinates>
      <xsl:attribute name="decimal"><xsl:text>.</xsl:text></xsl:attribute>
      <xsl:attribute name="cs"><xsl:text>,</xsl:text></xsl:attribute>
      <xsl:attribute name="ts"><xsl:text> </xsl:text></xsl:attribute>
      <xsl:variable name="dimension" >
        <xsl:choose>
          <xsl:when test="boolean(@srsDimension)">
            <xsl:value-of select="@srsDimension" />
          </xsl:when>
          <xsl:otherwise>2</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:value-of select="geomutils:toGML2Coordinates(string(.), number($dimension))"/>
    </gml:coordinates>
  </xsl:template>

  <xsl:template match="gml:LinearRing">
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates select="gml:posList" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gml:interior">
    <gml:innerBoundaryIs>
      <xsl:call-template name="copy_attribs"/>
      <xsl:apply-templates select="gml:LinearRing" />
    </gml:innerBoundaryIs>
  </xsl:template>

</xsl:stylesheet>
