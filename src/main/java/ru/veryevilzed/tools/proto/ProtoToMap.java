package ru.veryevilzed.tools.proto;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Descriptors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProtoToMap {

    public enum Options {
        NOT_SKIP_NULL,
        LIST_AS_ARRAY,
        ENUM_AS_STRING,
        IGNORE_NOT_IMPLEMENTED_TYPE,
        CREATE_EMPTY_ARRAY,
        CREATE_EMPTY_MAP,
    }

    private boolean notSkipNull = false;
    private boolean listAsArray = false;
    private boolean enumAsString = false;
    private boolean createEmptyArray = false;
    private boolean createEmptyMap = false;
    private boolean ignoreNotImplementedType = false;

    private Object createArrayFromList(Descriptors.FieldDescriptor df, List object) {
        switch (df.getJavaType().name()){
            case "LONG":
                return object.toArray(new Long[0]);
            case "INT":
                return object.toArray(new Integer[0]);
            case "STRING":
                return object.toArray(new String[0]);
            case "DOUBLE":
                return object.toArray(new Double[0]);
            case "BOOLEAN":
                return object.toArray(new Boolean[0]);
            case "ENUM":
                if (enumAsString)
                    return object.toArray(new String[0]);
                return object.toArray(new Object[0]);
            case "MESSAGE":
                return object.toArray(new Object[0]);
            default:
                if (!ignoreNotImplementedType)
                    throw new RuntimeException(df.getJavaType().name()+" not implemented");
                return null;
        }
    }


    private Object fromProtoValue(Descriptors.FieldDescriptor df, Object value){
        if (value == null)
            return null;
        switch (df.getJavaType().name()){
            case "LONG":
            case "INT":
            case "STRING":
            case "DOUBLE":
            case "BOOLEAN":
                return value;
            case "ENUM":
                if (enumAsString)
                    return value.toString();
                return value;
            case "MESSAGE":
                return parse(df.getMessageType(), (AbstractMessage)value);
            default:
                if (!ignoreNotImplementedType)
                    throw new RuntimeException(df.getJavaType().name()+" not implemented");
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parse(Descriptors.Descriptor descriptor, AbstractMessage proto) {
        System.out.println("Parse: "+ proto.getClass().getName()+"\n\n");
        Map<String, Object> res = new HashMap<>();
        for(Descriptors.FieldDescriptor df : descriptor.getFields()){
            Object value = proto.getAllFields().get(df);
            if (value == null && !notSkipNull)
                continue;

            if (df.isMapField()) {
                if (value != null) {
                    Map map = new HashMap();
                    ((List) value).forEach(obj -> {
                        Map<String, Object> so = parse(df.getMessageType(), (AbstractMessage) obj);
                        map.put(so.get("key"), so.get("value"));
                    });
                    res.put(df.getName(), map);
                }else
                    res.put(df.getName(), createEmptyMap ? new HashMap() : null);
            }else if (df.isRepeated()) {
                if (value!=null) {
                    List r = (List)((List) value).stream().map(i -> fromProtoValue(df, i)).collect(Collectors.toList());
                    if (listAsArray)
                        res.put(df.getName(), createArrayFromList(df, r));
                    else
                        res.put(df.getName(), r);
                }else
                    res.put(df.getName(), createEmptyArray ? new ArrayList() : null);
            }else {
                res.put(df.getName(), fromProtoValue(df,value));
            }
        }
        return res;
    }

    public Map<String, Object> parse(AbstractMessage proto) {
       return this.parse(Utils.getDescriptor(proto.getClass()), proto);
    }

    public ProtoToMap(Options... options) {
        for(ProtoToMap.Options opt : options) {
            switch (opt){
                case NOT_SKIP_NULL:
                    this.notSkipNull = true;
                    break;
                case LIST_AS_ARRAY:
                    this.listAsArray = true;
                    break;
                case ENUM_AS_STRING:
                    this.enumAsString = true;
                    break;
                case IGNORE_NOT_IMPLEMENTED_TYPE:
                    this.ignoreNotImplementedType = true;
                    break;
                case CREATE_EMPTY_MAP:
                    this.createEmptyMap = true;
                    break;
                case CREATE_EMPTY_ARRAY:
                    this.createEmptyArray = true;
                    break;
            }
        }
    }

}
