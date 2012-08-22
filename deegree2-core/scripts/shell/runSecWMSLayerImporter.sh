# This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.
# Assumtion: deegree2.jar is in same folder as other libs
#
# If deegree2.jar is build with ant, setting classpath to $lib/deegree2.jar is enough, 
# because all other libraries are referenced in the deegree2 manifest file.
# Otherwise, all libraries need to be specified separately.

# set environment variables
export lib=../../lib

echo "start wms layer import"

# use long version, if deegree2.jar was build without ant 
java -Xms300m -Xmx1400m -classpath $lib/deegree2.jar:$lib/postgresql-8.0-311.jdbc3.jar:$lib/log4j-1.2.9.jar:$lib/jaxen-1.1-beta-8.jar:$lib/commons-logging.jar:$lib/ojdbc14_10g.jar org.deegree.tools.security.WMSLayerImporter -WMSAddress http://demo.deegree.org/deegree-wms/services -Driver org.postgresql.Driver -URL jdbc:postgresql://localhost:5432/security -DBUserName postgres -DBUserPassword postgres -SecAdminPassword JOSE67

# use short version, if deegree2.jar was build using ant
#java -Xms300m -Xmx1400m -classpath $lib/deegree2.jar org.deegree.tools.security.WMSLayerImporter -WMSAddress http://demo.deegree.org/deegree-wms/services -Driver org.postgresql.Driver -URL jdbc:postgresql://localhost:5432/security -DBUserName postgres -DBUserPassword postgres -SecAdminPassword JOSE67

echo "finish wms layer import"
