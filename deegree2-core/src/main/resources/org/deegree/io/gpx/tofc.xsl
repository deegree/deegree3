<?xml version="1.0"?>
<stylesheet xmlns="http://www.w3.org/1999/XSL/Transform" xmlns:gps="http://www.topografix.com/GPX/1/1" 
xmlns:gml="http://www.opengis.net/gml" xmlns:app="http://www.deegree.org/app" version="1.0" 
xmlns:mtk="http://www.rigacci.org/gpx/MtkExtensions/v1">

  <template match="gps:gpx">
    <gml:FeatureCollection>
      <attribute name="gml:id"><value-of select="generate-id(.)"/></attribute>            
      <gml:boundedBy>
        <gml:Box>
          <attribute name='srsName'>EPSG:4326</attribute>
          <gml:coordinates>
            <value-of select="gps:metadata/gps:bounds/@minlon" />
            <text>,</text>
            <value-of select="gps:metadata/gps:bounds/@minlat" />
            <text> </text>
            <value-of select="gps:metadata/gps:bounds/@maxlon" />
            <text>,</text>
            <value-of select="gps:metadata/gps:bounds/@maxlat" />
          </gml:coordinates>
        </gml:Box>
      </gml:boundedBy>
      <apply-templates select="gps:wpt | gps:trk" />
    </gml:FeatureCollection>
  </template>

  <template match="gps:wpt">
    <gml:featureMember>
      <app:waypoint>
        <attribute name="gml:id"><value-of select="generate-id(.)"/></attribute>    
        <app:elevation>
          <value-of select="gps:ele"/>
        </app:elevation>
        <app:time>
          <value-of select="gps:time"/>
        </app:time>
        <app:name>
          <value-of select="gps:name"/>
        </app:name>
        <app:symbol>
          <value-of select="gps:sym"/>
        </app:symbol>
        <app:geometry>
          <gml:Point srsName="EPSG:4326">
            <attribute name="gml:id"><value-of select="generate-id(.)"/></attribute>            
            <gml:pos>
              <value-of select="@lon"/>
              <text> </text>
              <value-of select="@lat"/>
            </gml:pos>
          </gml:Point>
        </app:geometry>
      </app:waypoint>
    </gml:featureMember>
  </template>

  <template match="gps:trk">
    <gml:featureMember>
      <app:track>
        <attribute name="gml:id"><value-of select="generate-id(.)"/></attribute>     
        <app:name>
          <value-of select="gps:name" />
        </app:name>
        <app:startTime>
          <value-of select="gps:trkseg/gps:trkpt/gps:time" />
        </app:startTime>
        <app:endTime>
          <value-of select="gps:trkseg/gps:trkpt[last()]/gps:time" />
        </app:endTime>
        <app:geometry>
          <gml:LineString srsName="EPSG:4979">
            <attribute name="gml:id"><value-of select="generate-id(.)"/></attribute>     
            <gml:posList>
              <apply-templates select="gps:trkseg/gps:trkpt" />
            </gml:posList>
          </gml:LineString>
        </app:geometry>
        <if test="boolean(gps:trkseg/gps:trkpt/gps:extensions/mtk:wptExtension/mtk:satinview)">
          <app:satellites>
            <value-of select="sum(gps:trkseg/gps:trkpt/gps:extensions/mtk:wptExtension/mtk:satinview) div count(gps:trkseg/gps:trkpt/gps:extensions/mtk:wptExtension/mtk:satinview)" />
          </app:satellites>
        </if>
      </app:track>
    </gml:featureMember>
  </template>

  <template match="gps:trkpt">
    <value-of select="@lon" />
    <text>,</text>
    <value-of select="@lat" />
    <text>,</text>
    <value-of select="gps:ele" />
    <text> </text>
  </template>

</stylesheet>
