package org.infinispan.wfink.playground.cachestore.sql;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

/**
 * Annotated interface to let Maven with the protoschema-processor build the SerializationContextInitializer implemenation and Person.proto file.
 *
 * The interface extends GeneratedSchema to have access to the generated schema with the client, this is to show how a remote client can register the schema. <b>But this approach is strongly not recommended because it will be possible to have different client implementations which will overwrite the
 * server side schema definition, this will cause a confusing and non deterministic behavior!</b> So the interface should extend SerializationContextInitializer only which is good enough for the client.
 *
 * @author <a href="mailto:WolfDieter.Fink@gmail.com">Wolf-Dieter Fink</a>
 */
@AutoProtoSchemaBuilder(includeClasses = { Person.class }, schemaFileName = "Person.proto", schemaFilePath = "proto", schemaPackageName = "playground")
public interface PersonProtoInitalizer extends GeneratedSchema, SerializationContextInitializer {
}
