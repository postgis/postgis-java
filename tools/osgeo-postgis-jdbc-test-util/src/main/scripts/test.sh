#!/usr/bin/env bash

# The following sets variables based in command arguments
TEST_JAR="${1}"                # This should be the postgis-jdbc jar file to be tested.
DB_HOSTNAME_AND_PORT=${2}      # This should be the hostname:port of the database
DB_USERNAME=${3}               # This should be the database user to login as.
DB_PASSWORD=${4}               # This should be the database password to be used during login.

# The following can be changed to point to the jar containing the test utility.  By default it
# points to a jar file in a local maven repository.
MVN_REPO_LOCATION=~/.m2/repository
GROUP_ID_DIR=/net/postgis
TEST_UTIL_JAR=${MVN_REPO_LOCATION}${GROUP_ID_DIR}/osgeo-postgis-jdbc-test-util/0.0.1-SNAPSHOT/osgeo-postgis-jdbc-test-util-0.0.1-SNAPSHOT.jar

# The following variables are derived from the variables above.
# They are used to invoke the JVM with the test utility.
JDBC_URL="jdbc:postgresql://${DB_HOSTNAME_AND_PORT}/postgis1"
JDBC_USERNAME=${DB_USERNAME}
JDBC_PASSWORD=${DB_PASSWORD}

# The exactly commandline used to invoke the utility is output before it is actually invoked.
echo "==> java -classpath ${TEST_JAR}:${TEST_UTIL_JAR} net.postgis.osgeo.util.Main ${JDBC_URL} ${JDBC_USERNAME} ${JDBC_PASSWORD}"

java -classpath ${TEST_JAR}:${TEST_UTIL_JAR} net.postgis.osgeo.util.Main ${JDBC_URL} ${JDBC_USERNAME} ${JDBC_PASSWORD}