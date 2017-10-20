package ru.veryevilzed.tools.tests;

import com.google.protobuf.DynamicMessage;
import org.junit.Before;
import org.junit.Test;
import ru.veryevilzed.tools.proto.MapToProto;
import ru.veryevilzed.tools.test.TestProtoMessages;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class MapToProtoTest {

    Map<String, Object> map;

    public Map<String,Object> inmessage(){
        Map<String,Object> res = new HashMap<>();
        res.put("common", -7);
        res.put("commonU", 14);
        res.put("commonBool", 0);
        res.put("commonU64", null);
        res.put("commonEnum", TestProtoMessages.SimpleEnum.SECOND);
        return res;
    }

    public Map<String, Map<String,Object>> inmessageMap(){
        Map<String, Map<String,Object>> res = new HashMap<>();
        res.put("first", inmessage());
        res.put("second", inmessage());
        return res;
    }

    @Before
    public void before() {
        map = new HashMap<>();
        map.put("common", -5);
        map.put("commonU", 10);
        map.put("common64", -12);
        map.put("commonU64", 12);
        map.put("commonDouble", 17);
        map.put("commonBool", true);
        map.put("inmessage", inmessage());
        map.put("commonEnum", "FIRST");
        map.put("array", Arrays.asList(1,2,3,4,5));
        map.put("array64", new Long[] {4L,5L,6L});
        map.put("arrayU64", Arrays.asList(1L,2L,3L,4L,5L));
        map.put("arrayU", new Long[] {7L,8L,9L});
        map.put("mapMessage", inmessageMap());
        map.put("commonBytes", new byte[] {5,7,8});
        Map<Integer, Integer> mulX2Map = new HashMap<>();
        IntStream.range(1,10).forEach(i -> mulX2Map.put(i, i*2));
        map.put("map32", mulX2Map);
    }


    @Test
    public void testParsing() throws IOException {

        MapToProto maper = new MapToProto();
        DynamicMessage dmessage = maper.parse(map, TestProtoMessages.BaseMessage.class);
        TestProtoMessages.BaseMessage message = TestProtoMessages.BaseMessage.parseFrom(dmessage.toByteArray());

        assertEquals(message.getCommon(), -5);
        assertEquals(message.getCommonU(), 10);
        assertEquals(message.getCommon64(), -12);
        assertEquals(message.getCommonU64(), 12);
        assertEquals(message.getCommonEnum(), TestProtoMessages.SimpleEnum.FIRST);
        assertEquals(message.getInmessage().getCommon(), -7);
        assertEquals(message.getInmessage().getCommonU(), 14);
        assertEquals(message.getInmessage().getCommonEnum(), TestProtoMessages.SimpleEnum.SECOND);
        assertEquals((int)message.getArrayList().get(1), 2);
        assertEquals((long)message.getArrayU64List().get(1), 2L);
        assertEquals((byte)message.getCommonBytes().toByteArray()[0] , 5);
        assertEquals((int)message.getMap32Map().get(4) , 8);

    }
//
//
//    @Test
//    public void testCreateBuilder() {
//        AbstractMessage.Builder builder = newBuilder(TestProtoMessages.BaseMessage.class);
//        Assert.assertNotNull(builder);
//    }
}
