package org.infinispan.wfink.playground.cachestore.sql;

import java.util.Map;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.protostream.GeneratedSchema;

/**
 * A simple client using the recommended Protostream Marshalling and Encoding for Infinispan 12+, as this will be the best option to use the full power of Infinispan features.
 *
 * The ProtoStreamMarshaller will support primitive/scalar types without explicit definition. Because of this the simple String key can be used.
 *
 * @author <a href="mailto:WolfDieter.Fink@gmail.com">Wolf-Dieter Fink</a>
 */
public class PersonClient {
  private RemoteCacheManager remoteCacheManager;
  private RemoteCache<String, Person> personCache;

  public PersonClient(String host, String port, String cacheName) {
    ConfigurationBuilder remoteBuilder = new ConfigurationBuilder();
    remoteBuilder.addServer().host(host).port(Integer.parseInt(port)); // .marshaller(new ProtoStreamMarshaller()); // The Protobuf based marshaller is no longer required for query capabilities as it is the default for ISPN 11 and RHDG 8

    // why the initializer is needed, I would expect the annotation works automatically
    // add the Initializer as String will work
    // remoteBuilder.addContextInitializer("org.infinispan.wfink.playground.encoding.domain.LibraryInitalizerImpl");
    // add the Initializer directly as class instance to get compiler errors if missed
    PersonProtoInitalizer initializer = new org.infinispan.wfink.playground.cachestore.sql.PersonProtoInitalizerImpl();
    remoteBuilder.addContextInitializer(initializer);

    remoteCacheManager = new RemoteCacheManager(remoteBuilder.build()); // registerSchema need a cacheManager
    registerSchemas(initializer); // not needed as long as the Person.proto is added manually to the server after start

    personCache = remoteCacheManager.getCache(cacheName);

    if (personCache == null) {
      throw new RuntimeException("Cache '" + cacheName + "' not found. Please make sure the server is properly configured");
    }

  }

  /**
   * Register the Protobuf schemas for the server. Note if the server does not have the schema some operations (like queries) are failing. Also other type of clients can not read the content!
   *
   * @param initializer The AutoProtoInitializer to register the schema
   */
  private void registerSchemas(GeneratedSchema initializer) {
    // Cache to register the schemas with the server too
    final RemoteCache<String, String> protoMetadataCache = remoteCacheManager.getCache("___protobuf_metadata"); // ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME is only available if query dependency is set
    // add the schema to the server side
    protoMetadataCache.put(initializer.getProtoFileName(), initializer.getProtoFile());

    // check for definition error for the registered protobuf schemas
    String errors = protoMetadataCache.get(".errors"); // ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX is only available if query dependency is set
    if (errors != null) {
      throw new IllegalStateException("Some Protobuf schema files contain errors: " + errors + "\nSchema :\n" + initializer.getProtoFileName());
    }
  }

  private void insert() {
    System.out.println("Inserting Messages into cache...");
    personCache.put("1", new Person("Fink", "Wolf"));
    personCache.put("2", new Person("Tarrant", "Tristan"));
    personCache.put("3", new Person("Zapata", "Pedro"));
    personCache.put("4", new Person("Burns", "Will"));
    personCache.put("5", new Person("Ruivo", "Pedro"));
    personCache.put("6", new Person("Arresti", "Katia"));
    personCache.put("4", new Person("Fernandez", "Gustavo"));

    System.out.println("  -> " + personCache.size() + " inserted");
  }

  private void getAll() {
    System.out.println("read all ...");
    for (Map.Entry<String, Person> m : personCache.entrySet()) {
      System.out.println(">> " + m);

    }
    System.out.println("Persons in cache are " + personCache.size());
  }

  private void stop() {
    remoteCacheManager.stop();
  }

  public static void main(String[] args) {
    String host = "localhost";
    String port = "11222";
    String cacheName = "PersonCache";

    if (args.length > 0) {
      port = args[0];
    }
    if (args.length > 1) {
      port = args[1];
    }
    PersonClient client = new PersonClient(host, port, cacheName);

    client.getAll(); // get all from cache
    client.insert(); // add a couple of

    client.stop();
    System.out.println("\nDone !");
  }
}