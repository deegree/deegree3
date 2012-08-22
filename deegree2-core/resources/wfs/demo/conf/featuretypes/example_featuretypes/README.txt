deegree WFS ${deegree.version} - demo 

The featurtypedefinitions you find in this directory (examplefeaturetypes) won't be 
evaluated unless you copy the *.xsd gml application schema to the 'featuretypes'
directory. In the corresponding documentation you find references on the files 
included in this directory. 

Postgis examples:
postgis featuretypes replace the already existing ones having shape files as datasource.
Scripts to fill the database with the required tables are located in 
$wfs_home$/WEB-INF/scripts

digitize:
The digitize feature type is needed to use the digitize function in deegree iGeoPortal.

Philosopher:
The philosopher example offers a complex GML application schema.

Cascade WFS:
This featuretype demonstrates, how to cascade a remote WFS featuretype. 

Bonn, ${release.date}