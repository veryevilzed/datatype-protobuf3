package ru.veryevilzed.tools.tests;

import com.sun.corba.se.spi.ior.ObjectKey;
import org.junit.Assert;
import org.junit.Test;
import ru.veryevilzed.tools.proto.ProtoToMap;
import ru.veryevilzed.tools.test.TestProtoMessages;

import java.util.Arrays;
import java.util.Map;

public class ProtoToMapTest {

    @Test
    public void protoParse() {

        TestProtoMessages.BaseMessage message = TestProtoMessages.BaseMessage.newBuilder()
                .setCommon(10)
                .setCommon64(150L)
                .addAllArray(Arrays.asList(5,7,20, 5))
                .putMap32(5,12).putMap32(6,16).putMap32(7,18)
                .build();

        ProtoToMap mapper = new ProtoToMap(ProtoToMap.Options.LIST_AS_ARRAY);
        Map<String, Object> map = mapper.parse(message);
        System.out.printf("%s%n", map.toString() );
        Assert.assertArrayEquals((Integer[])map.get("array"), new Integer[] {5,7,20, 5});
    }

}
