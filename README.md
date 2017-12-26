# Welcome to Chorus

Here's description how to run Chorus.

### Prerequisites

 * [Java SDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html), version 1.8 or higher.
 * [Apache Maven](http://maven.apache.org), version 3.3.9 or higher.
 * [Apache Tomcat](http://tomcat.apache.org/download-90.cgi), 9.x or higher.
 * [MySQL](http://www.mysql.com), version 5.7.17 or higher.
 * SMTP server credentials to let the app send emails.
 * [Amazon S3](http://aws.amazon.com/s3/) storage credentials (bucket name, key and secret) to store uploaded files.

### To run the Chorus Project:

 * Build the application using 'clean install -pl "!selenium-tests" -DskipTests' command
 * Copy webapp-1.0.war from target folder to "docker/webapp/ROOT.war"
 * Install Docker if needed and run "docker-compose up"
 * Point your browser to http://localhost:8080
 * Register admin user within Web UI
 * Go to database with your favorite SQL tool and grant this user admin rights
   - UPDATE USER SET admin=1;
