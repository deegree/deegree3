<?xml version="1.0" encoding="UTF-8"?>
<gml:FeatureCollection gml:id="f106"
  xmlns:gml="http://www.opengis.net/gml"
  xmlns:sf="http://cite.opengeospatial.org/gmlsf"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://cite.opengeospatial.org/gmlsf schemas/cite-gmlsf2.xsd">

  <gml:description>
  Test data for assessing compliance with the GMLSF profile at level SF-2.
  </gml:description>
  <gml:name>CITE/WFS-1.1</gml:name>
  <gml:boundedBy>
    <gml:Envelope srsName="urn:ogc:def:crs:EPSG::4326">
      <gml:lowerCorner>34.94 -10.52</gml:lowerCorner>
      <gml:upperCorner>71.96 32.19</gml:upperCorner>
    </gml:Envelope>
  </gml:boundedBy>

  <!-- first -->
  <gml:featureMember>
    <sf:PrimitiveGeoFeature gml:id="f001">
      <gml:description>description-f001</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f001</gml:name>
      <sf:pointProperty>
        <gml:Point gml:id="g003">
          <gml:description>description-g003</gml:description>
          <gml:pos>39.73245 2.00342</gml:pos>
        </gml:Point>
      </sf:pointProperty>
      <sf:intProperty>155</sf:intProperty>
      <sf:uriProperty>http://www.opengeospatial.org/</sf:uriProperty>
      <sf:measurand>1.2765E4</sf:measurand>
      <sf:dateProperty>2006-10-25Z</sf:dateProperty>
      <sf:decimalProperty>5.03</sf:decimalProperty>
    </sf:PrimitiveGeoFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:PrimitiveGeoFeature gml:id="f002">
      <gml:description>description-f002</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f002</gml:name>
      <sf:pointProperty>
        <gml:Point gml:id="g001">
          <gml:description>description-g001</gml:description>
          <gml:pos>59.41276 0.22601</gml:pos>
        </gml:Point>
      </sf:pointProperty>
      <sf:intProperty>154</sf:intProperty>
      <sf:uriProperty>http://www.opengeospatial.org/</sf:uriProperty>
      <sf:measurand>1.2769E4</sf:measurand>
      <sf:dateProperty>2006-10-23Z</sf:dateProperty>
      <sf:decimalProperty>4.02</sf:decimalProperty>
    </sf:PrimitiveGeoFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:PrimitiveGeoFeature gml:id="f003">
      <gml:description>description-f003</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f003</gml:name>
      <sf:curveProperty>
        <gml:LineString>
          <gml:posList>46.074 9.799 46.652 10.466 47.114 11.021</gml:posList>
        </gml:LineString>
      </sf:curveProperty>
      <sf:intProperty>180</sf:intProperty>
      <sf:measurand>672.1</sf:measurand>
      <sf:dateTimeProperty>2006-06-28T07:08:00+02:00</sf:dateTimeProperty>
      <sf:dateProperty>2006-09-01Z</sf:dateProperty>
      <sf:decimalProperty>12.92</sf:decimalProperty>
    </sf:PrimitiveGeoFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:PrimitiveGeoFeature gml:id="f008">
      <gml:description>description-f008</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f008</gml:name>
      <sf:surfaceProperty>
        <gml:Polygon gml:id="g005">
          <gml:name>MU3</gml:name>
          <gml:exterior>
            <gml:LinearRing>
              <gml:posList>45.174 30.899 45.652 30.466 45.891 30.466 45.174 30.899</gml:posList>
            </gml:LinearRing>
          </gml:exterior>
        </gml:Polygon>
      </sf:surfaceProperty>
      <sf:intProperty>300</sf:intProperty>
      <sf:measurand>7.835E2</sf:measurand>
      <sf:dateTimeProperty>2006-06-28T07:08:00+02:00</sf:dateTimeProperty>
      <sf:dateProperty>2006-12-12Z</sf:dateProperty>
      <sf:decimalProperty>18.92</sf:decimalProperty>
    </sf:PrimitiveGeoFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:AggregateGeoFeature gml:id="f005">
      <gml:description>description-f005</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f005</gml:name>
      <sf:multiPointProperty>
        <gml:MultiPoint srsName="urn:ogc:def:crs:EPSG::4326">
          <gml:pointMember>
            <gml:Point><gml:pos>70.83 29.86</gml:pos></gml:Point>
          </gml:pointMember>
          <gml:pointMember>
            <gml:Point><gml:pos>68.87 31.08</gml:pos></gml:Point>
          </gml:pointMember>
          <gml:pointMember>
            <gml:Point><gml:pos>71.96 32.19</gml:pos></gml:Point>
          </gml:pointMember>
        </gml:MultiPoint>
      </sf:multiPointProperty>
      <sf:doubleProperty>2012.78</sf:doubleProperty>
      <sf:strProperty>
      Ma quande lingues coalesce, li grammatica del resultant lingue es plu
      simplic e regulari quam ti del coalescent lingues. Li nov lingua franca
      va esser plu simplic e regulari quam li existent Europan lingues.
      </sf:strProperty>
      <sf:featureCode>BK030</sf:featureCode>
    </sf:AggregateGeoFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:AggregateGeoFeature gml:id="f009">
      <gml:description>description-f009</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f009</gml:name>
      <sf:multiCurveProperty>
        <gml:MultiCurve>
          <gml:curveMember>
            <gml:LineString>
              <gml:posList>55.174 -5.899 55.652 -5.466 55.891 -5.899 58.174 -5.899 58.652 -5.466 58.891 -5.899</gml:posList>
            </gml:LineString>
          </gml:curveMember>
          <gml:curveMember>
            <gml:LineString srsName="urn:ogc:def:crs:EPSG::4326">
              <gml:posList>53.265 -5.188 54.354 -4.775 52.702 -4.288 53.611 -4.107 55.823 -4.010</gml:posList>
            </gml:LineString>
                  </gml:curveMember>
                </gml:MultiCurve>
      </sf:multiCurveProperty>
      <sf:doubleProperty>20.01</sf:doubleProperty>
      <sf:strProperty>Ma quande lingues coalesce, li grammatica del resultant.</sf:strProperty>
      <sf:featureCode>GB007</sf:featureCode>
    </sf:AggregateGeoFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:AggregateGeoFeature gml:id="f010">
      <gml:description>description-f010</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f010</gml:name>
      <sf:multiSurfaceProperty>
        <gml:MultiSurface>
          <gml:surfaceMember>
                        <gml:Polygon>
                <gml:exterior>
                  <gml:LinearRing>
                    <gml:posList>50 20 54 19 55 20 60 30 52 28 51 27 49 29 47 27 50 20</gml:posList>
                </gml:LinearRing>
                </gml:exterior>
                <gml:interior>
                  <gml:LinearRing>
                    <gml:posList>55 25 56 25.2 56 25.1 55 25</gml:posList>
                  </gml:LinearRing>
                </gml:interior>
                        </gml:Polygon>
                  </gml:surfaceMember>
                  <gml:surfaceMember>
                          <gml:Polygon>
                <gml:exterior>
                  <gml:LinearRing>
                    <gml:posList>35.5 20.0 35.0 24.0 35.0 28.0 39.0 27.5 37.0 22.0 35.5 20.0</gml:posList>
                </gml:LinearRing>
                </gml:exterior>
                <gml:interior>
                  <gml:LinearRing>
                    <gml:posList>36.0 26.0 37.0 25.0 36.8 27.0 36.0 26.0</gml:posList>
                  </gml:LinearRing>
                </gml:interior>
                          </gml:Polygon>
                  </gml:surfaceMember>
        </gml:MultiSurface>
      </sf:multiSurfaceProperty>
      <sf:doubleProperty>24510</sf:doubleProperty>
      <sf:strProperty>
      Ma quande lingues coalesce, li grammatica del resultant lingue es plu
      simplic e regulari quam ti del coalescent lingues. Li nov lingua franca
      va esser plu simplic e regulari quam li existent Europan lingues.
      </sf:strProperty>
      <sf:featureCode>AK020</sf:featureCode>
    </sf:AggregateGeoFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:EntitéGénérique gml:id="f004">
      <gml:description>description-f004</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f004</gml:name>
      <sf:attribut.Géométrie>
        <gml:Polygon gml:id="g002">
          <gml:name>MU1</gml:name>
          <gml:exterior>
            <gml:LinearRing>
              <gml:posList>60.5 0 64 0 64 6.25 60.5 6.25 60.5 0</gml:posList>
            </gml:LinearRing>
          </gml:exterior>
          <gml:interior>
            <gml:LinearRing>
              <gml:posList>61.5 2 62.5 2 62 4 61.5 2</gml:posList>
            </gml:LinearRing>
          </gml:interior>
        </gml:Polygon>
      </sf:attribut.Géométrie>
      <sf:boolProperty>1</sf:boolProperty>
      <sf:str4Property>abc3</sf:str4Property>
      <sf:featureRef>name-f003</sf:featureRef>
    </sf:EntitéGénérique>
  </gml:featureMember>

  <gml:featureMember>
    <sf:EntitéGénérique gml:id="f007">
      <gml:description>description-f007</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f007</gml:name>
      <sf:attribut.Géométrie>
        <gml:Polygon gml:id="g004">
          <gml:name>MU2</gml:name>
          <gml:exterior>
            <gml:LinearRing>
              <gml:posList>35 15 40 16 39 20 37 22.5 36 18 35 15</gml:posList>
            </gml:LinearRing>
          </gml:exterior>
          <gml:interior>
            <gml:LinearRing>
              <gml:posList>37.1 17.5 37.2 17.6 37.3 17.7 37.4 17.8 37.5 17.9 37 17.9 37.1 17.5</gml:posList>
            </gml:LinearRing>
          </gml:interior>
        </gml:Polygon>
      </sf:attribut.Géométrie>
      <sf:boolProperty>0</sf:boolProperty>
      <sf:str4Property>def4</sf:str4Property>
    </sf:EntitéGénérique>
  </gml:featureMember>

  <gml:featureMember>
    <sf:PrimitiveGeoFeature gml:id="f015">
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f015</gml:name>
      <sf:pointProperty>
        <gml:Point>
          <gml:pos>34.94 -10.52</gml:pos>
        </gml:Point>
      </sf:pointProperty>
      <sf:intProperty>-900</sf:intProperty>
      <sf:measurand>-2.4</sf:measurand>
      <sf:decimalProperty>7.90</sf:decimalProperty>
    </sf:PrimitiveGeoFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:AggregateGeoFeature gml:id="f016">
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f016</gml:name>
      <sf:multiSurfaceProperty>
        <gml:MultiSurface>
          <gml:surfaceMember>
            <gml:Polygon srsName="urn:ogc:def:crs:EPSG::4326">
              <gml:description>Donec vulputate leo cursus magna.</gml:description>
              <gml:exterior>
                <gml:LinearRing>
                  <gml:posList>57.5 6.0 57.5 8.0 60.0 8.0 62.5 9.0 62.5 5.0 60.0 6.0 57.5 6.0</gml:posList>
                </gml:LinearRing>
              </gml:exterior>
              <gml:interior>
                <gml:LinearRing>
                  <gml:posList>58.0 6.5 59.0 6.5 59.0 7.0 58.0 6.5</gml:posList>
                </gml:LinearRing>
              </gml:interior>
            </gml:Polygon>
                  </gml:surfaceMember>
        </gml:MultiSurface>
      </sf:multiSurfaceProperty>
      <sf:doubleProperty>-182.9</sf:doubleProperty>
      <sf:strProperty>In rhoncus nisl sit amet sem.</sf:strProperty>
      <sf:featureCode>EE010</sf:featureCode>
    </sf:AggregateGeoFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:EntitéGénérique gml:id="f017">
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f017</gml:name>
      <sf:attribut.Géométrie>
        <gml:LineString>
          <gml:posList>50.174 4.899 52.652 5.466 53.891 6.899 54.382 7.780 54.982 8.879 </gml:posList>
        </gml:LineString>
      </sf:attribut.Géométrie>
      <sf:boolProperty>false</sf:boolProperty>
      <sf:str4Property>qrst</sf:str4Property>
      <sf:featureRef>name-f015</sf:featureRef>
    </sf:EntitéGénérique>
  </gml:featureMember>

  <gml:featureMember>
    <sf:PrimitiveGeoFeature gml:id="f091">
      <gml:description>description-f091</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f091</gml:name>
      <sf:pointProperty>
        <gml:Point gml:id="g091">
          <gml:pos>49.84117 -2.19514</gml:pos>
        </gml:Point>
      </sf:pointProperty>
      <sf:intProperty>-12678967543233</sf:intProperty>
      <sf:measurand>-12.78e-2</sf:measurand>
      <sf:decimalProperty>80.02</sf:decimalProperty>
      <sf:relatedFeature xlink:type="simple" xlink:href="#f003"
        xlink:title="A related PrimitiveGeoFeature instance"
        xlink:actuate="onRequest" />
    </sf:PrimitiveGeoFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:PrimitiveGeoFeature gml:id="f092">
      <gml:description>description-f092</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f092</gml:name>
      <sf:intProperty>4567</sf:intProperty>
      <sf:measurand>1445.57</sf:measurand>
      <sf:dateTimeProperty>2007-08-21T14:47:24+01:00</sf:dateTimeProperty>
      <sf:decimalProperty>3.14</sf:decimalProperty>
      <sf:relatedFeature xlink:type="simple" xlink:href="#f091" />
    </sf:PrimitiveGeoFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:PrimitiveGeoFeature gml:id="f093">
      <gml:description>description-f093</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f093</gml:name>
      <sf:curveProperty>
        <gml:Curve>
          <gml:segments>
            <gml:LineStringSegment>
              <gml:posList>36.074 8.799 36.652 9.466 37.114 10.021</gml:posList>
            </gml:LineStringSegment>
            <gml:LineStringSegment>
              <gml:posList>37.114 10.021 36.652 9.556</gml:posList>
            </gml:LineStringSegment>
          </gml:segments>
        </gml:Curve>
      </sf:curveProperty>
      <sf:intProperty>-1234</sf:intProperty>
      <sf:measurand>1594.01</sf:measurand>
      <sf:decimalProperty>9.86</sf:decimalProperty>
      <sf:relatedFeature xlink:type="simple" xlink:href="#f094" />
    </sf:PrimitiveGeoFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:PrimitiveGeoFeature gml:id="f094">
      <gml:description>description-f094</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f094</gml:name>
      <sf:intProperty>882</sf:intProperty>
      <sf:measurand>26.33</sf:measurand>
      <sf:decimalProperty>1000.00</sf:decimalProperty>
      <sf:relatedFeature xlink:type="simple" xlink:href="http://vancouver1.demo.galdosinc.com/wfs/http?request=GetFeature&amp;service=WFS&amp;version=1.1.0&amp;typename=sf:PrimitiveGeoFeature#f205" />
    </sf:PrimitiveGeoFeature>
  </gml:featureMember>

  <!-- second -->
  <gml:featureMember>
    <sf:ComplexGeoFeature gml:id="f101">
      <gml:description>
      Aliquam sed lorem. Nam non risus ut felis pellentesque consequat. Curabitur
      pede. Suspendisse potenti. Mauris lacinia. Donec risus leo, luctus at,
      aliquet vel, congue in, mi. Mauris pulvinar mollis felis. Etiam posuere,
      ante et luctus blandit, purus metus sollicitudin nisl, eget gravida lorem
      elit sed pede. Nunc tristique elementum sem. Nullam feugiat mauris quis
      tellus. Nulla cursus augue at ipsum. Mauris volutpat posuere tellus. Quisque
      accumsan tellus sit amet elit. Nam tempor libero id urna. Donec ac sapien.
      Class aptent taciti sociosqu ad litora torquent per conubia nostra, per
      inceptos hymenaeos. Ut sollicitudin dapibus magna. Phasellus erat turpis,
      adipiscing eu, lobortis ut, convallis a, nisl. Vivamus rutrum magna eget
      metus consectetuer ultrices. Sed ac nisi tincidunt risus euismod condimentum.
      </gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f101</gml:name>
      <sf:geometryProperty>
        <gml:LineString srsName="urn:ogc:def:crs:EPSG::4979">
          <gml:posList>46.074 9.799 600.2 46.652 10.466 781.4</gml:posList>
        </gml:LineString>
      </sf:geometryProperty>
      <sf:observation uom="http://www.bipm.fr/en/si/derived_units/Celsius">2.4</sf:observation>
      <sf:typeCode codeSpace="https://www.dgiwg.org/FAD">AQ064</sf:typeCode>
      <sf:auditTrail>
        <sf:Event>
          <sf:action>Insert</sf:action>
          <sf:timestamp>2006-06-29T12:37:00+02:00</sf:timestamp>
          <sf:userid>Phineas.Fogg</sf:userid>
        </sf:Event>
        <sf:Event>
          <sf:action>Update</sf:action>
          <sf:timestamp>2006-06-30T10:37:00+02:00</sf:timestamp>
          <sf:userid>Phineas.Fogg</sf:userid>
        </sf:Event>
        <sf:Event>
          <sf:action>Update</sf:action>
          <sf:timestamp>2006-06-30T16:07:00+02:00</sf:timestamp>
          <sf:userid>Axel.Lidenbrock</sf:userid>
        </sf:Event>
        <sf:Event>
          <sf:action>Update</sf:action>
          <sf:timestamp>2006-07-12T16:16:00+02:00</sf:timestamp>
          <sf:userid>Axel.Lidenbrock</sf:userid>
        </sf:Event>
        <sf:Event>
          <sf:action>Update</sf:action>
          <sf:timestamp>2006-09-06T09:47:07Z</sf:timestamp>
          <sf:userid>Eugène.Rastignac</sf:userid>
        </sf:Event>
      </sf:auditTrail>
    </sf:ComplexGeoFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:ComplexGeoFeature gml:id="f102">
      <gml:description>
      Integer euismod risus. Class aptent taciti sociosqu ad litora torquent per
      conubia nostra, per inceptos hymenaeos. Phasellus nec risus.
      </gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f102</gml:name>
      <gml:name>Vivamus blandit</gml:name>
      <sf:geometryProperty>
        <gml:Curve srsName="urn:ogc:def:crs:EPSG::4979">
          <gml:segments>
            <gml:LineStringSegment interpolation="linear">
              <gml:posList>45.174 2.899 601 45.652 2.466 504 45.891 2.899 876</gml:posList>
            </gml:LineStringSegment>
            <gml:LineStringSegment interpolation="linear">
              <gml:posList>45.891 2.899 587 48.174 2.899 702 48.652 2.466 676 48.891 2.899 699</gml:posList>
            </gml:LineStringSegment>
          </gml:segments>
        </gml:Curve>
      </sf:geometryProperty>
      <sf:observation uom="http://www.bipm.fr/en/si/si_brochure/chapter2/2-1/metre.html">14.1</sf:observation>
      <sf:typeCode codeSpace="https://www.dgiwg.org/FAD">SA060</sf:typeCode>
      <sf:auditTrail>
        <sf:Event>
          <sf:action>Insert</sf:action>
          <sf:timestamp>2006-10-21T11:28:00+01:00</sf:timestamp>
          <sf:userid>Axel.Lidenbrock</sf:userid>
        </sf:Event>
        <sf:Event>
          <sf:action>Update</sf:action>
          <sf:timestamp>2006-10-30T21:05:00+01:00</sf:timestamp>
          <sf:userid>Phineas.Fogg</sf:userid>
        </sf:Event>
      </sf:auditTrail>
    </sf:ComplexGeoFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:ComplexGeoFeature gml:id="f103">
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f103</gml:name>
      <sf:geometryProperty>
        <gml:Surface srsName="urn:ogc:def:crs:EPSG::4326">
          <gml:patches>
            <gml:PolygonPatch interpolation="planar">
              <gml:exterior>
                <gml:LinearRing>
                  <gml:posList>37.5 6.0 37.5 8.0 40.0 8.0 40.0 6.0 37.5 6.0</gml:posList>
                </gml:LinearRing>
              </gml:exterior>
              <gml:interior>
                <gml:LinearRing>
                  <gml:posList>38.0 6.5 39.0 6.5 39.0 7.0 38.0 6.5</gml:posList>
                </gml:LinearRing>
              </gml:interior>
            </gml:PolygonPatch>
            <gml:PolygonPatch interpolation="planar">
              <gml:exterior>
                <gml:LinearRing>
                  <gml:posList>40.0 6.0 40.0 8.0 42.5 9.0 42.5 5.0 40.0 6.0</gml:posList>
                </gml:LinearRing>
              </gml:exterior>
            </gml:PolygonPatch>
          </gml:patches>
        </gml:Surface>
      </sf:geometryProperty>
      <sf:observation uom="http://www.bipm.fr/en/si/derived_units/density">2.63E3</sf:observation>
      <sf:typeCode codeSpace="https://www.dgiwg.org/FAD">DB160</sf:typeCode>
      <sf:b64BinaryProperty mimeType="text/plain">
      VHdhcyBicmlsbGlnLCBhbmQgdGhlIHNsaXRoeSB0b3Zlcw==
      </sf:b64BinaryProperty>
      <sf:auditTrail>
        <sf:Event>
          <sf:action>Insert</sf:action>
          <sf:timestamp>2006-10-21T11:28:00+01:00</sf:timestamp>
          <sf:userid>Axel.Lidenbrock</sf:userid>
        </sf:Event>
      </sf:auditTrail>
    </sf:ComplexGeoFeature>
  </gml:featureMember>

  <!-- third -->
  <gml:featureMember>
    <sf:LinkedFeature gml:id="f201">
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f201</gml:name>
      <sf:reference xlink:type="simple" xlink:href="#f091"
          xlink:role="http://cite.opengeospatial.org/gmlsf#PrimitiveGeoFeature" />
    </sf:LinkedFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:LinkedFeature gml:id="f202">
      <gml:description>description-f202</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f202</gml:name>
      <sf:reference xlink:type="simple" xlink:href="#f092"
        xlink:title="A related feature" xlink:actuate="onRequest"
        xlink:role="http://cite.opengeospatial.org/gmlsf#PrimitiveGeoFeature" />
      <sf:extent>
        <gml:Point gml:id="g202">
          <gml:pos>49.86136 -2.17433</gml:pos>
        </gml:Point>
      </sf:extent>
    </sf:LinkedFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:LinkedFeature gml:id="f203">
      <gml:description>description-f203</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f203</gml:name>
      <sf:reference xlink:type="simple" xlink:href="#f204"
        xlink:role="http://cite.opengeospatial.org/gmlsf#LinkedFeature" />
    </sf:LinkedFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:LinkedFeature gml:id="f204">
      <gml:description>description-f204</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f204</gml:name>
      <sf:reference xlink:type="simple" xlink:href="#f201" />
      <sf:extent>
        <gml:MultiPoint gml:id="g204">
          <gml:pointMember xlink:type="simple" xlink:href="#g003" />
          <gml:pointMember xlink:type="simple" xlink:href="#g202"
            xlink:role="http://www.opengis.net/gml#Point" />
        </gml:MultiPoint>
      </sf:extent>
    </sf:LinkedFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:LinkedFeature gml:id="f205">
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f205</gml:name>
      <sf:reference xlink:type="simple" xlink:href="#f203" />
      <sf:extent xlink:type="simple" xlink:href="#g002"
        xlink:title="Geometry reference"
        xlink:role="http://www.opengis.net/gml#Polygon" />
    </sf:LinkedFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:LinkedFeature gml:id="f206">
      <gml:description>description-f206</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f206</gml:name>
      <sf:reference xlink:type="simple" xlink:href="#f207" />
      <sf:extent>
        <gml:Point>
          <gml:pos>49.63245 9.19251</gml:pos>
        </gml:Point>
      </sf:extent>
    </sf:LinkedFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:LinkedFeature gml:id="f207">
      <gml:description>description-f207</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f207</gml:name>
      <sf:reference xlink:type="simple" xlink:href="#f210" />
    </sf:LinkedFeature>
  </gml:featureMember>

  <gml:featureMember>
    <sf:LinkedFeature gml:id="f208">
      <gml:description>description-f208</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f208</gml:name>
      <sf:reference xlink:type="simple"
        xlink:title="External feature reference"
        xlink:href="ftp://vancouver1.demo.galdosinc.com/wfs/http?request=GetFeature&amp;service=WFS&amp;version=1.1.0&amp;typename=sf:LinkedFeature#f205" />
    </sf:LinkedFeature>
  </gml:featureMember>
  
 <!-- OPTIONAL, there is a test expecting an error on not finding the reference -->
  <!-- gml:featureMember>
    <sf:LinkedFeature gml:id="f209">
      <gml:description>description-f209</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f209</gml:name>
      <sf:reference xlink:type="simple" xlink:href="#_6c566516-a435-11dc-8314-0800200c9a66" />
    </sf:LinkedFeature>
  </gml:featureMember-->

  <gml:featureMember>
    <sf:LinkedFeature gml:id="f210">
      <gml:description>description-f210</gml:description>
      <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">name-f210</gml:name>
      <sf:reference xlink:type="simple" xlink:href="#f206" />
    </sf:LinkedFeature>
  </gml:featureMember>

</gml:FeatureCollection>
