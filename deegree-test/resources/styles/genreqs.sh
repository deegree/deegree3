#!/bin/bash
# generate the requests

loc=../../src/main/resources/org/deegree/test/services/wms/similaritytests.txt

for file in points/*sld
do
  urlencoder $file /tmp/out
  req="REQUEST=GetMap&SERVICE=WMS&VERSION=1.1.1&WIDTH=460&HEIGHT=348&LAYERS=&TRANSPARENT=TRUE&FORMAT=image%2Fpng&BBOX=450600.9334882666,4374562.432070117,451756.85256045486,4375436.909976903&SRS=EPSG:26912&STYLES=&sld_body=$(cat /tmp/out)"
  if(grep "$req" $loc > /dev/null) then
    echo Request for $file already contained.
  else
    echo Adding request for $file.
    echo "$req" >> $loc
  fi
done

for file in lines/*sld
do
  urlencoder $file /tmp/out
  req="REQUEST=GetMap&SERVICE=WMS&VERSION=1.1.1&WIDTH=466&HEIGHT=348&LAYERS=&TRANSPARENT=TRUE&FORMAT=image%2Fpng&BBOX=440583.93230647047,4648214.232622321,443449.60943283123,4650354.266184496&SRS=EPSG:26912&STYLES=&sld_body=$(cat /tmp/out)"
  if(grep "$req" $loc > /dev/null) then
    echo Request for $file already contained.
  else
    echo Adding request for $file.
    echo "$req" >> $loc
  fi
done

for file in polygons/*sld
do
  urlencoder $file /tmp/out
  req="REQUEST=GetMap&SERVICE=WMS&VERSION=1.1.1&WIDTH=466&HEIGHT=348&LAYERS=&TRANSPARENT=TRUE&FORMAT=image%2Fpng&BBOX=424211.2614308995,4582608.284684761,452798.55237625004,4603956.733716912&SRS=EPSG:26912&STYLES=&sld_body=$(cat /tmp/out)"
  if(grep "$req" $loc > /dev/null) then
    echo Request for $file already contained.
  else
    echo Adding request for $file.
    echo "$req" >> $loc
  fi
done
