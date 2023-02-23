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

        $ mysql -u infinispan --password=infinispan -D infinispan
           MariaDB [infinispan]> create table Simple (id int PRIMARY KEY, value VARCHAR(50) );
           MariaDB [infinispan]> create table Person (id VARCHAR(20) PRIMARY KEY, name VARCHAR(50), firstname VARCHAR(50) );


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


Entry with simpe PKey and Protobuf schema
-----------------------------------------
If there are more attributes a schema is needed to load the data from the DB table into cached objects.
The schema should use the protostream-processor and @AutoProtoSchemaBuilder annotation to generate the necessary schema definition and concrete SerializationContext implementation.
After mvn has build the project the schema is available and can be used to publish it to the server.
Otherwise a server start will fail if the cache with store is configured with the Person schema and the schema is not avaialable.
Use the following command, or any prefered REST client to propagate the schema to the server. This should be done before the cache is created.

       curl -X PUT --data-binary @target/classes/proto/Person.proto http://127.0.0.1:11222/rest/v2/schemas/Person.proto

The generated file Person.proto is added to the server.
Now the cache can be created with the following CLI command

       $SERVER_HOMEbin/cli.sh -c localhost:11222
        > create cache --file=template/PersonCache.xml PersonCache

The command should return without any error message.
Type this command to run the example

       mvn exec:java -D"exec.mainClass"="org.infinispan.wfink.playground.cachestore.sql.PersonClient"
     

Note
- To prevent from server restart the schema must be added to the server before the cache is created. Otherwise the cache is not started and the client will fail with ISPN008047 error
- The protobuf schema and cached class might contain additional attributes
- The DB table can not have additional attributes which are not mapped by teh schema. In that case ISPN008046 error is thrown during cache initialization and will show the additional columns.
- configure expiration is not supported by the SQL store and might lead to unexpected behavior, see [ISPN-14037](https://issues.redhat.com/browse/ISPN-14037)

Enhancements
------------
It is possible to mark the store as read-only, in this case any change of the cached data is not updating the DB table. But consider it is not possible to prevent clients from 
add or change entries. The downside is that such changes are lost if the server is restarted. There is a [Feature request ISPN-14531](https://issues.redhat.com/browse/ISPN-14531) to prevent from this.

If the client code for adding content is removed after the first invocation and/or the database table is populated with some enties and the store is marked as read-only the client will reload the cache from the database.

Errors
------

  See the [SQL Cache Store troubleshooting](https://infinispan.org/docs/stable/titles/configuring/configuring.html#sql-cache-store-troubleshooting_persistence)
