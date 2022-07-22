# infinispan.playground.cachestore.sql
Infinispan examples how to use the SQL cache store as persistence
=================================================================

Author: Wolf-Dieter Fink
Technologies: Infinispan, Hot Rod, Protobuf, SQL


What is it?
-----------

Examples how to use the SQL cache store with recommended protobuf and clients.
Documentation for the SQL Cache Store can be found in [Infinispan Configuration SQL Stores](https://infinispan.org/docs/stable/titles/configuring/configuring.html#sql-cache-store_persistence)

The example will use MariaDB as database behind, each other will work as well but the correct JDBC driver is needed and the SQL commands might be slightly different.

With encoding the cache content can be converted if different clients are used, the server will convert some of it automatically if possible.
Protostream is highly optimized to provide the best performance

Hot Rod is a binary TCP client-server protocol. The Hot Rod protocol facilitates faster client and server interactions in comparison to other text based protocols and allows clients to make decisions about load balancing, failover and data location operations.

The SQL cache store is documented in the [Configuring Infinispan Caches- SQL Cache Stores](https://infinispan.org/docs/stable/titles/configuring/configuring.html#sql-cache-store_persistence)


Prepare a database
-------------------
Install your prefered Database or use a Docker image.
Create a user, the examples use user infinispan for username and password. The database name for MariaDB is infinispan as well.

Type this to create the tables

        $ mysql -u infinispan -p
           Enter password: <infnispan>
           MariaDB [(none)]> use infinispan
           MariaDB [infinispan]> create table Simple (id int PRIMARY KEY, value VARCHAR(50) );


Prepare a server instance
-------------------------
Simple start a Infinispan 13+ or RHDG 8.3+ server and follow the examples below.
To keep the client simple you might need to remove the 'security-realm' from the endpoints element, as well as the security element with authorization from the cache-container.

Add the following data-source element to the infinispan.xml

      <data-sources>
         <data-source name="maria" jndi-name="jdbc/mariaDB" statistics="true">
            <connection-factory driver="org.mariadb.jdbc.MySQLDataSource" url="jdbc:mariadb://localhost:3306/infinispan" username="infinispan" password="infinispan" />
            <connection-pool initial-size="1" max-size="10" min-size="3" background-validation="1000" idle-removal="1" blocking-timeout="1000" leak-detection="10000"/>
         </data-source>
     </data-sources>

Copy the JDBC driver, i.e. mariadb-java-client-2.4.3.jar, to ISPN_HOME/server/lib.
Start the server.

You might create all the caches directly using CLI, it is possible to add the caches to the infinispan.xml file directly.

        ./bin/cli.sh -c localhost:11222
          create cache --file=template/SimpleCache.xml SimpleCache


Run the examples
----------------

Type this command to build the projects

        mvn clean package

 Follow the individual sections for each example

 Note: for the caches the console http://localhost:11222 can be used to show cache content by navigating to the CacheContainer->{CacheName}



Simple Key-Value cache
----------------------
There is no need to have a schema as there is only one column for key and value.
Type this command to run the example

       mvn exec:java -D"exec.mainClass"="org.infinispan.wfink.playground.cachestore.sql.SimpleClient"

But there could be an issue if the 'id' column is a wrong type, i.e. if VARCHAR the test client will have issues with the key type and might throws a ClassCastException or 
show each entry twice if running several times because of this!

If you drop the table and recreate it with 

       create table Simple (id VARCHAR(50) PRIMARY KEY, value VARCHAR(50) );

The SimpleClient will have an unexpected behavior! There is [ISPN-14029](https://issues.redhat.com/browse/ISPN-14029) to track this.


Errors
------

  See the [SQL Cache Store troubleshooting](https://infinispan.org/docs/stable/titles/configuring/configuring.html#sql-cache-store-troubleshooting_persistence)
