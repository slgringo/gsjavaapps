Required: PostgreSQL 8+, maven, JDK 8, Apache Tomcat 8 installed
Create table part_entity in database postgres with columns part_name TEXT, part_number TEXT, vendor TEXT,
 qty INTEGER, shipped DATE, receive DATE
DB username: postgres, password postgres.
Place database on local server.
Run maven commands from project root:
  mvn clean
  mvn install
Place parts.war file from {project root}/target to Tomcat webapps folder and restart Tomcat
Web app is available at http://127.0.0.1:8080/


Time spent: 8h