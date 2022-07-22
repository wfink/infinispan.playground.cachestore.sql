package org.infinispan.wfink.playground.cachestore.sql;

import java.util.Map;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

/**
 * A simple client using the recommended Protosream Marshalling and Encoding for Infinispan 13+, as Protobuf will be the best option to use the full power of Infinispan features. The ProtoStreamMarshaller will support primitive/scalar types without explicit definition. Because of this the simple
 * Integer and String can be used.
 *
 * @author <a href="mailto:WolfDieter.Fink@gmail.com">Wolf-Dieter Fink</a>
 */
public class SimpleClient {
  private RemoteCacheManager remoteCacheManager;
  private RemoteCache<Integer, String> simpleCache;

  public SimpleClient(String host, String port, String cacheName) {
    ConfigurationBuilder remoteBuilder = new ConfigurationBuilder();
    remoteBuilder.addServer().host(host).port(Integer.parseInt(port));

    remoteCacheManager = new RemoteCacheManager(remoteBuilder.build());

    simpleCache = remoteCacheManager.getCache(cacheName);

    if (simpleCache == null) {
      throw new RuntimeException("Cache '" + cacheName + "' not found. Please make sure the server is properly configured");
    }

  }

  /**
   * Insert 5 entries with Integer key 1...5
   */
  private void insert() {
    System.out.println("Inserting Messages into cache...");
    simpleCache.put(Integer.valueOf(1), "value1");
    simpleCache.put(Integer.valueOf(2), "value2");
    simpleCache.put(Integer.valueOf(3), "value3");
    simpleCache.put(Integer.valueOf(4), "value4");
    simpleCache.put(Integer.valueOf(5), "value5");

    System.out.println("  -> " + simpleCache.size() + " inserted");
  }

  /**
   * Try to get entry #1 #3 by Integer key
   */
  private void getByKey() {
    System.out.println("Entry #1 : " + simpleCache.get(Integer.valueOf(1)));
    System.out.println("Entry #3 : " + simpleCache.get(Integer.valueOf(3)));
  }

  /**
   * Try to change entry #1 #3 by Integer key
   */
  private void changeByKey() {
    System.out.println("  ->  change key #1 #3");
    simpleCache.put(Integer.valueOf(1), "value1.1");
    simpleCache.put(Integer.valueOf(3), "value3.1");

  }

  /**
   * Use the EntrySet to list all cache entries.
   */
  private void getAll() {
    System.out.println("read all ...");
    for (Map.Entry<Integer, String> m : simpleCache.entrySet()) {
      System.out.println(">> " + m);

    }
    System.out.println("Entries in cache are " + simpleCache.size());
  }

  private void stop() {
    remoteCacheManager.stop();
  }

  public static void main(String[] args) {
    String host = "localhost";
    String port = "11222";
    String cacheName = "SimpleCache";

    if (args.length > 0) {
      port = args[0];
    }
    if (args.length > 1) {
      port = args[1];
    }
    SimpleClient client = new SimpleClient(host, port, cacheName);

    client.getAll();
    client.insert();
    client.getAll();
    client.getByKey();
    client.changeByKey();
    client.getAll();
    client.getByKey();

    client.stop();
    System.out.println("\nDone !");
  }
}