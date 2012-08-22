# Start the hsql database manager with:
java -cp ../lib/hsqldb.jar org.hsqldb.util.DatabaseManager

# insert the content of scripts/sql/quadtree_hsqldb.sql in the opening window. 

##########################
# Fill db with shape files
##########################
# Please note for hsql sql files, TABLE and INDEX are to be written in UPPERCASE!!!
# In your terminal please change to folder WEB-INF/scripts/[batch|shell]/ and run either
Shape2GenericSQLdatabase_hlsqldb.bat
# or 
Shape2GenericSQLdatabase_hlsqldb.sh