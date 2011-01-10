<?xml version="1.0" encoding="UTF-8"?>
<MultiGeometry srsName="EPSG:4326" xmlns="http://www.opengis.net/gml"
  xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.opengis.net/gml http://schemas.opengis.net/gml/3.1.1/base/gml.xsd">
  <geometryMembers>
    <Point gml:id="P1">
      <coord>
        <X>0.0</X>
        <Y>0.0</Y>
      </coord>
    </Point>
    <Point gml:id="P2">
      <coord>
        <X>1.0</X>
        <Y>1.0</Y>
      </coord>
    </Point>
    <LineString gml:id="L1">
      <pointProperty xlink:href="#P1" />
      <pointProperty>
        <Point>
          <pos>1.0 0.0</pos>
        </Point>
      </pointProperty>
      <pointProperty xlink:href="#P2" />
      <pointProperty>
        <Point>
          <pos>0.0 1.0</pos>
        </Point>
      </pointProperty>
    </LineString>
  </geometryMembers>
</MultiGeometry>
