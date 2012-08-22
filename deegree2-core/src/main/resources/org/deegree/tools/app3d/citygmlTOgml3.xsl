<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gml="http://www.opengis.net/gml"
  xmlns:citygml="http://www.citygml.org/citygml/1/0/0" xmlns:java="java"
  xmlns:geometryutil="org.deegree.framework.xml.GeometryUtils" xmlns:wfs="http://www.opengis.net/wfs"
  xmlns:xAL="urn:oasis:names:tc:ciq:xsdschema:xAL:2.0" xmlns:app="http://www.deegree.org/app">

  <xsl:template match="wfs:FeatureCollection | citygml:CityModel | /">
    <wfs:FeatureCollection xmlns:citygml="http://www.citygml.org/citygml/1/0/0"
      xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gml="http://www.opengis.net/gml"
      xmlns:ogc="http://www.opengis.net/ogc" xmlns:wfs="http://www.opengis.net/wfs"
      xmlns:app="http://www.deegree.org/app">
      <xsl:apply-templates select="citygml:cityObjectMember | gml:featureMember | .//citygml:Building" />
    </wfs:FeatureCollection>
  </xsl:template>

  '
  <xsl:template match="citygml:Building">
    <xsl:call-template name="FEATURE" />
  </xsl:template>

  <xsl:template name="FEATURE">
    <!-- xsl:if test="gml:boundedBy">
      <citygml:envelope>				
      <gml:Polygon>
      <xsl:attribute name="srsName"><xsl:value-of select="./gml:boundedBy/gml:Envelope/@srsName"/></xsl:attribute>
      <gml:outerBoundaryIs>
      <gml:LinearRing>
      <gml:coordinates cs="," decimal="." ts=" ">
      <xsl:value-of select="geometryutil:getPolygonCoordinatesFromEnvelope( ./gml:boundedBy/gml:Envelope )"/>
      </gml:coordinates>
      </gml:LinearRing>
      </gml:outerBoundaryIs>
      </gml:Polygon>
      </citygml:envelope>
      </xsl:if>
      <xsl:if test="boolean( gml:name )">
      <citygml:featureName>
      <xsl:value-of select="gml:name"/>
      </citygml:featureName>
      </xsl:if>
      
      <xsl:if test="boolean( citygml:lod1TerrainIntersection )">
      <citygml:lod1tis>
      <xsl:copy-of select="citygml:lod1TerrainIntersection/gml:MultiCurve"></xsl:copy-of>
      </citygml:lod1tis>
      </xsl:if>
      <xsl:if test="boolean( citygml:lod2TerrainIntersection )">
      <citygml:lod2tis>
      <xsl:copy-of select="citygml:lod2TerrainIntersection/gml:MultiCurve"></xsl:copy-of>
      </citygml:lod2tis>
      </xsl:if>
      <xsl:if test="boolean( citygml:lod3TerrainIntersection )">
      <citygml:lod3tis>
      <xsl:copy-of select="citygml:lod3TerrainIntersection/gml:MultiCurve"></xsl:copy-of>
      </citygml:lod3tis>
      </xsl:if>
      <xsl:if test="boolean( citygml:lod4TerrainIntersection )">
      <citygml:lod4tis>
      <xsl:copy-of select="citygml:lod4TerrainIntersection/gml:MultiCurve"></xsl:copy-of>
      </citygml:lod4tis>
      </xsl:if-->

    <xsl:for-each select=".//citygml:lod1Solid | .//citygml:lod1MultiSurface | .//citygml:lod1Geometry">
      <xsl:call-template name="LINK_FEAT_GEOM">
        <xsl:with-param name="LOD" select="'1'" />
      </xsl:call-template>
    </xsl:for-each>

    <xsl:for-each
      select=".//citygml:lod2Solid | .//citygml:lod2MultiSurface | .//citygml:lod2MultiCurve | .//citygml:lod2Geometry">
      <xsl:call-template name="LINK_FEAT_GEOM">
        <xsl:with-param name="LOD" select="'2'" />
      </xsl:call-template>
    </xsl:for-each>

    <xsl:for-each
      select=".//citygml:lod3Solid | .//citygml:lod3MultiSurface | .//citygml:lod3MultiCurve | .//citygml:lod3Geometry">
      <xsl:call-template name="LINK_FEAT_GEOM">
        <xsl:with-param name="LOD" select="'3'" />
      </xsl:call-template>
    </xsl:for-each>

    <xsl:for-each
      select=".//citygml:lod4Solid | .//citygml:lod4MultiSurface | .//citygml:lod4MultiCurve | .//citygml:lod4Geometry">
      <xsl:call-template name="LINK_FEAT_GEOM">
        <xsl:with-param name="LOD" select="'4'" />
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="LINK_FEAT_GEOM">
    <xsl:param name="LOD" />
    <xsl:apply-templates select="gml:Solid" />
    <xsl:apply-templates select="gml:CompositeSolid" />
    <xsl:apply-templates select="gml:MultiSurface" />
    <xsl:apply-templates select="gml:CompositeSurface" />
    <xsl:apply-templates select="gml:MultiCurve" />
    <xsl:apply-templates select="gml:CompositeCurve" />
  </xsl:template>

  <xsl:template match="gml:Solid">
    <xsl:for-each select="gml:exterior/gml:CompositeSurface/gml:surfaceMember/child::*">
      <gml:featureMember>
        <citygml:Feature>
          <app:geometry>
            <xsl:apply-templates select="." />
          </app:geometry>
          <xsl:apply-templates select="citygml:appearance/citygml:Material" />
          <xsl:apply-templates select="citygml:appearance/citygml:SimpleTexture" />
        </citygml:Feature>
      </gml:featureMember>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="gml:CompositeSolid">
    <xsl:for-each select="gml:solidMember/child::*">
      <citygml:composite>
        <citygml:LINK_FEAT_GEOM>

          <citygml:type>0</citygml:type>
          <xsl:apply-templates select="." />
        </citygml:LINK_FEAT_GEOM>
      </citygml:composite>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="gml:MultiSurface">
    <xsl:for-each select="gml:surfaceMember/child::*">
      <xsl:choose>
        <xsl:when
          test="local-name( . ) = 'TexturedSurface' or local-name( . ) = 'Polygon' or local-name( . ) = 'Surface'">
          <gml:featureMember>
            <citygml:Feature>
              <app:geometry>
                <xsl:apply-templates select="." />
              </app:geometry>
              <xsl:apply-templates select="citygml:appearance/citygml:Material" />
              <xsl:apply-templates select="citygml:appearance/citygml:SimpleTexture" />
            </citygml:Feature>
          </gml:featureMember>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="." />
        </xsl:otherwise>
      </xsl:choose>

    </xsl:for-each>

  </xsl:template>

  <xsl:template match="gml:CompositeSurface">
    <citygml:composite>
      <citygml:LINK_FEAT_GEOM>

        <citygml:type>5</citygml:type>
        <xsl:for-each select="gml:surfaceMember/child::*">
          <citygml:polygon>
            <citygml:Polygon>

              <citygml:geom>
                <xsl:apply-templates select="." />
              </citygml:geom>
              <xsl:apply-templates select="citygml:appearance/citygml:Material" />
              <xsl:apply-templates select="citygml:appearance/citygml:SimpleTexture" />
              <xsl:if test="boolean(./@orientation)">
                <citygml:orientation>
                  <xsl:value-of select="./@orientation" />
                </citygml:orientation>
              </xsl:if>
            </citygml:Polygon>
          </citygml:polygon>
        </xsl:for-each>
      </citygml:LINK_FEAT_GEOM>
    </citygml:composite>
  </xsl:template>

  <xsl:template match="gml:MultiCurve">
    <xsl:for-each select="gml:curveMember/child::*">
      <citygml:curve>
        <citygml:Curve>

          <citygml:geom>
            <xsl:copy-of select="."></xsl:copy-of>
          </citygml:geom>
        </citygml:Curve>
      </citygml:curve>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="gml:CompositeCurve">
    <xsl:for-each select="gml:curveMember/child::*">
      <citygml:composite>
        <citygml:LINK_FEAT_GEOM>

          <xsl:apply-templates select="." />
        </citygml:LINK_FEAT_GEOM>
      </citygml:composite>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="citygml:TexturedSurface">
    <xsl:apply-templates select="gml:baseSurface/child::*" />
  </xsl:template>

  <xsl:template match="gml:Surface | gml:Polygon | gml:Curve | gml:LineString | gml:Point">
    <xsl:copy>
      <xsl:choose>
        <xsl:when test="boolean( ./@srsName ) = false">
          <xsl:attribute name="srsName"><xsl:value-of select="//@srsName" />
          </xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="srsName"><xsl:value-of select="./@srsName" />
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:for-each select="./*">
        <xsl:copy-of select="."></xsl:copy-of>
      </xsl:for-each>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="citygml:Material">
    <xsl:copy-of select="*" />
  </xsl:template>

  <xsl:template match="citygml:SimpleTexture">
    <xsl:copy-of select="*" />
  </xsl:template>


</xsl:stylesheet>
