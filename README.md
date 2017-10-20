# datatype-protobuf3


## from map to proto

```java

MapToProto maper = new MapToProto(MapToProto.Options.NOT_SKIP_NULL, MapToProto.Options.IGNORE_MISSING_FIELDS);
DynamicMessage message = maper.parse(map, TestProtoMessages.BaseMessage.class);

```

Options:

* IGNORE_NOT_IMPLEMENTED_TYPE,
* IGNORE_CAST_EXCEPTION,
* IGNORE_MISSING_FIELDS,
* NOT_SKIP_NULL,




## from proto to map

```java

ProtoToMap maper = new ProtoToMap();


```
