<?xml version="1.0" encoding="UTF-8"?>
<stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/XSL/Transform" xmlns:ll="http://www.lat-lon.de" xmlns:gml="http://www.opengis.net/gml" version="1.0">

  <!-- Two modes: sanitize to transform some XML to a more GML-like structure,
       and copy to copy the existing GML-like structure
       Example: sanitize the GetFeatureInfo response of the cascaded ZipCodes
       layer from demo.deegree.org (it does not really need sanitizing, in this
       case the script just copies the response nodes):
       <deegreewms:DataSource failOnException="0" queryable="1">
         <deegreewms:Name>ZipCodes</deegreewms:Name>
         <deegreewms:Type>REMOTEWMS</deegreewms:Type>
         <deegreewms:OWSCapabilities>
           <deegreewms:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="http://demo.deegree.org/deegree-wms/services?request=GetCapabilities&amp;version=1.1.1&amp;service=WMS" xlink:type="simple"/>
         </deegreewms:OWSCapabilities>
         <deegreewms:FilterCondition>
           <deegreewms:WMSRequest>SERVICE=WMS&amp;VERSION=1.1.1&amp;REQUEST=GetMap&amp;FORMAT=image/png&amp;TRANSPARENT=true&amp;BGCOLOR=0xFFFFFF&amp;EXCEPTIONS=application/vnd.ogc.se_inimage&amp;STYLES=&amp;LAYERS=ZipCodes</deegreewms:WMSRequest>
         </deegreewms:FilterCondition>
         <deegreewms:FeatureInfoTransformation>
           <deegreewms:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="sanitize.xsl" xlink:type="simple"/>
         </deegreewms:FeatureInfoTransformation>
       </deegreewms:DataSource>
    -->
  <template match="/">
    <ll:FeatureCollection>
      <choose>
        <when test="boolean(FeatureInfoResponse/FIELDS | featureInfo/query_layer/object)">
          <apply-templates select="." mode="sanitize" />
        </when>
        <otherwise>
          <apply-templates select="." mode="copy" />
        </otherwise>
      </choose>
    </ll:FeatureCollection>
  </template>

  <template match="FeatureInfoResponse/FIELDS" mode="sanitize">
    <gml:featureMember>
      <ll:feature>
        <for-each select="@*">
          <variable name="attName"><value-of select="translate( local-name(.), 'ßäöüÄÖÜ', 'saouAOU'  )"/></variable>
          <element name="ll:{$attName}">
            <value-of select="." />
          </element>
        </for-each>
      </ll:feature>
    </gml:featureMember>
  </template>

  <template match="featureInfo/query_layer/object" mode="sanitize">
    <gml:featureMember>
      <variable name="layerName"><value-of select="../@name" /></variable>
      <element name="ll:{$layerName}">
        <for-each select="*">
          <variable name="propName"><value-of select="translate( local-name(.), 'ßäöüÄÖÜ', 'saouAOU'  )"/></variable>
          <element name="ll:{$propName}">
            <value-of select="." />
          </element>
        </for-each>
      </element>
    </gml:featureMember>
  </template>

  <template match="*" mode="copy">
      <for-each select="*">
        <if test="local-name(.) != 'boundedBy'">
        <gml:featureMember>
			  <copy-of select="*" />
          </gml:featureMember>
        </if>
      </for-each>
  </template>  

</stylesheet>
