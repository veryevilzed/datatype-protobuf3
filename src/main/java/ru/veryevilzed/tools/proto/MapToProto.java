package ru.veryevilzed.tools.proto;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MapToProto {

    public enum Options {
        IGNORE_NOT_IMPLEMENTED_TYPE,
        IGNORE_CAST_EXCEPTION,
        IGNORE_MISSING_FIELDS,
        NOT_SKIP_NULL,
    }

    private boolean ignoreNotImplementedType = false;
    private boolean ignoreCastException = false;
    private boolean ignoreMissingFields = false;
    private boolean notSkipNull = false;

    private static DynamicMessage.Builder getBuilder(Descriptors.Descriptor descriptor) {
        return DynamicMessage.newBuilder(descriptor);
    }

    @SuppressWarnings("unchecked")
    private Object getProtobufValue(Descriptors.FieldDescriptor field, Object object) {
        if (object == null)
            return field.getDefaultValue();

        switch (field.getJavaType().name()){
            case "LONG":
                if (object instanceof Integer)
                    return new Long((int)object);
                return (long)object;
            case "INT":
                return (int)object;
            case "STRING":
                return object.toString();
            case "DOUBLE":
                if (object instanceof Integer)
                    return new Double((int)object);
                if (object instanceof Long)
                    return new Double((long)object);
                return (double)object;
            case "BYTE_STRING":
                return (byte[])object;
            case "BOOLEAN":
                if (object instanceof Integer)
                    return !object.equals(0);
                if (object instanceof Long)
                    return !object.equals(0);
                return (boolean)object;
            case "ENUM":
                if (object instanceof String)
                    return field.getEnumType().findValueByName((String)object);
                if (object instanceof Integer)
                    return field.getEnumType().findValueByNumber((Integer)object);
                return field.getEnumType().findValueByName(object.toString());
            case "MESSAGE":
                return toProtobufMessage(field.getMessageType(), (Map<String, Object>)object);
            default:
                //TODO: Unsupported!
                if (!ignoreNotImplementedType)
                    throw new RuntimeException(field.getJavaType().name()+" not implemented");
                return null;
        }

    }




    @SuppressWarnings("unchecked")
    private DynamicMessage toProtobufMessage(Descriptors.Descriptor descriptor, Map<String, Object> data){
        DynamicMessage.Builder builder = getBuilder(descriptor);
        for(Map.Entry<String, Object> entry :data.entrySet()){
            if (entry.getValue() == null && !notSkipNull)
                continue;
            Descriptors.FieldDescriptor fieldDescriptor = descriptor.findFieldByName(entry.getKey());
                if (fieldDescriptor != null) {
                    try {
                        if (fieldDescriptor.isMapField()) { // map
                            builder.clearField(fieldDescriptor);
                            ((Map<Object, Object>) entry.getValue()).entrySet().forEach(mapEntry -> builder.addRepeatedField(fieldDescriptor, toProtobufMessage(fieldDescriptor.getMessageType(), Utils.getKeyValue(mapEntry.getKey(), mapEntry.getValue()))));
                        } else if (fieldDescriptor.isRepeated()) { // repeated
                            builder.clearField(fieldDescriptor);
                            if (entry.getValue().getClass().isArray())
                                Arrays.asList((Object[])entry.getValue()).forEach(arrayEntry -> builder.addRepeatedField(fieldDescriptor, getProtobufValue(fieldDescriptor, arrayEntry)));
                            else
                                ((List<Object>) entry.getValue()).forEach(arrayEntry -> builder.addRepeatedField(fieldDescriptor, getProtobufValue(fieldDescriptor, arrayEntry)));
                        } else
                            builder.setField(fieldDescriptor, getProtobufValue(fieldDescriptor, entry.getValue()));
                    }catch (ClassCastException e){
                        if (!ignoreCastException)
                            throw new RuntimeException(String.format("Cast exception. Field %s, cast from %s to %s", entry.getKey(), entry.getValue().getClass().getName(), fieldDescriptor.getJavaType()), e);
                    }
                } else if (!ignoreMissingFields) {
                    descriptor.getFields().forEach(f -> System.out.println(f.getName()+" "+f.getJavaType()));
                    throw new RuntimeException(String.format("Fields %s not found", entry.getKey()));
                }
        }
        return builder.build();
    }

    public <T extends AbstractMessage> DynamicMessage parse(Map<String, Object> data, Class<T> clazz) {
        return toProtobufMessage(Utils.getDescriptor(clazz), data);
    }

    public <T extends AbstractMessage> MapToProto(Options... options) {
        for(Options opt : options) {
            switch (opt){
                case IGNORE_CAST_EXCEPTION:
                    this.ignoreCastException = true;
                    break;
                case IGNORE_NOT_IMPLEMENTED_TYPE:
                    this.ignoreNotImplementedType = true;
                    break;
                case IGNORE_MISSING_FIELDS:
                    this.ignoreMissingFields = true;
                    break;
                case NOT_SKIP_NULL:
                    this.notSkipNull = true;
                    break;
            }
        }
    }
}
