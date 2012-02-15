<?xml version="1.0" encoding="UTF-8"?>
<Curve gml:id="C1" xmlns="http://www.aixm.aero/schema/5.1" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:xlink="http://www.w3.org/1999/xlink"
  srsName="EPSG:4326" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.aixm.aero/schema/5.1 ../../../../gml/schema/aixm/AIXM_Features.xsd">
  <gml:metaDataProperty>
    <gml:GenericMetaData>Example for metadata: Ce curve ne pas une GML curve, c'est une AIXM curve.
    </gml:GenericMetaData>
  </gml:metaDataProperty>
  <gml:description>This is just for testing the parsing of standard GML properties.</gml:description>
  <gml:descriptionReference xlink:href="http://www.aixm.org/whatever" />
  <gml:identifier codeSpace="urn:blabla:bla">XYZ</gml:identifier>
  <gml:name>Curve C1</gml:name>
  <gml:name>C1</gml:name>
  <gml:segments>
    <gml:Arc interpolation="circularArc3Points">
      <gml:posList srsName="EPSG:4326">2 0 0 2 -2 0</gml:posList>
    </gml:Arc>
    <gml:LineStringSegment interpolation="linear">
      <gml:posList srsName="EPSG:4326">-2 0 0 -2 2 0</gml:posList>
    </gml:LineStringSegment>
  </gml:segments>
  <horizontalAccuracy uom="M">1.0</horizontalAccuracy>
  <annotation xsi:nil="true" nilReason="inapplicable" />
</Curve>
