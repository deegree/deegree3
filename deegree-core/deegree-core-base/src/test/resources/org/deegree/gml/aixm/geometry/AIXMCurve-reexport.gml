<aixm:Curve xmlns:aixm="http://www.aixm.aero/schema/5.1" xmlns:gml="http://www.opengis.net/gml/3.2" gml:id="C1" srsName="EPSG:4326">
  <gml:metaDataProperty>
    <gml:GenericMetaData>Example for metadata: Ce curve ne pas une GML curve, c'est une AIXM curve.</gml:GenericMetaData>
  </gml:metaDataProperty>
  <gml:description>This is just for testing the parsing of standard GML properties.</gml:description>
  <gml:descriptionReference xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="http://www.aixm.org/whatever"/>
  <gml:identifier codeSpace="urn:blabla:bla">XYZ</gml:identifier>
  <gml:name>Curve C1</gml:name>
  <gml:name>C1</gml:name>
  <gml:segments>
    <gml:Arc>
      <gml:posList>2.000000 0.000000 0.000000 2.000000 -2.000000 0.000000</gml:posList>
    </gml:Arc>
    <gml:LineStringSegment interpolation="linear">
      <gml:posList>-2.000000 0.000000 0.000000 -2.000000 2.000000 0.000000</gml:posList>
    </gml:LineStringSegment>
  </gml:segments>
  <aixm:horizontalAccuracy uom="M">1.0</aixm:horizontalAccuracy>
  <aixm:annotation xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true" nilReason="inapplicable"/>
</aixm:Curve>
