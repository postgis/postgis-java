module net.postgis.jdbc {
  requires java.logging;
  requires java.sql;

  requires org.postgresql.jdbc;

  requires net.postgis.geometry;

  exports net.postgis.jdbc;
}