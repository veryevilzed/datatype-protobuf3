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
        IGNORE_NOT_IMPLEMENTED_TYPE
    }

    private boolean notSkipNull = false;
    private boolean listAsArrya = false;
    private boolean enumAsString = false;
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
                return null; //TODO
            case "MESSAGE":
                return null; //TODO
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
                return parse((AbstractMessage)value);
            default:
                if (!ignoreNotImplementedType)
                    throw new RuntimeException(df.getJavaType().name()+" not implemented");
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> parse(AbstractMessage proto) {
        Descriptors.Descriptor descriptor = Utils.getDescriptor(proto.getClass());
        Map<String, Object> res = new HashMap<>();
        for(Descriptors.FieldDescriptor df : descriptor.getFields()){
            Object value = proto.getAllFields().get(df);
            if (value == null && !notSkipNull)
                continue;

            System.out.printf("%s %s = %s (%s)%n", df.getName(), df.getJavaType(), value, value != null ? value.getClass().getName() : "" );
            if (df.isMapField()) {
                //TODO: да щя сделаю!
            }else if (df.isRepeated()) {
                if (value!=null) {
                    List r = (List)((List) value).stream().map(i -> fromProtoValue(df, i)).collect(Collectors.toList());
                    if (listAsArrya)
                        res.put(df.getName(), createArrayFromList(df, r));
                    else
                        res.put(df.getName(), r);
                }
            }else {
                res.put(df.getName(), fromProtoValue(df,value));
            }
        }
        return res;
    }

    public ProtoToMap(Options... options) {
        for(ProtoToMap.Options opt : options) {
            switch (opt){
                case NOT_SKIP_NULL:
                    this.notSkipNull = true;
                    break;
                case LIST_AS_ARRAY:
                    this.listAsArrya = true;
                    break;
                case ENUM_AS_STRING:
                    this.enumAsString = true;
                    break;
            }
        }
    }

}
