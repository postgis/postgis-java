module net.postgis.jdbc.jts {
  requires java.datatransfer;
  requires java.desktop;
  requires java.logging;
  requires java.sql;

  requires org.locationtech.jts;
  requires org.postgresql.jdbc;

  requires spatial4j;

  requires net.postgis.geometry;
  requires net.postgis.jdbc;
}