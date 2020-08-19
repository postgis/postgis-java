module net.postgis.geometry {
  requires java.sql;

  requires org.slf4j;

  exports net.postgis.jdbc.geometry;
  exports net.postgis.jdbc.geometry.binary;
  exports net.postgis.jdbc.geometry.util;
}