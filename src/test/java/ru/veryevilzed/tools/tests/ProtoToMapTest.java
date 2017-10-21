package ru.veryevilzed.tools.tests;

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
                .setCommonEnum(TestProtoMessages.SimpleEnum.SECOND)
                .setOtherMessage(TestProtoMessages.OtherMessage.newBuilder()
                        .setEnumItem(TestProtoMessages.OtherMessage.OtherEnum.ITEM)
                        )
                .build();

        ProtoToMap mapper = new ProtoToMap(ProtoToMap.Options.LIST_AS_ARRAY);
        Map<String, Object> map = mapper.parse(message);
        Assert.assertArrayEquals((Integer[])map.get("array"), new Integer[] {5,7,20, 5});
        Assert.assertEquals(map.get("commonEnum"), TestProtoMessages.SimpleEnum.SECOND.toString());
        Assert.assertEquals(((Map<String,Object>)map.get("otherMessage")).get("enumItem"), TestProtoMessages.OtherMessage.OtherEnum.ITEM.toString());
    }

    @Test
    public void enumAsString() {

        TestProtoMessages.BaseMessage message = TestProtoMessages.BaseMessage.newBuilder()
                .setInmessage(
                        TestProtoMessages.BaseMessage.newBuilder()
                        .setCommonEnum(TestProtoMessages.SimpleEnum.SECOND)
                )
                .setOtherMessage(TestProtoMessages.OtherMessage.newBuilder()
                        .setEnumItem(TestProtoMessages.OtherMessage.OtherEnum.ITEM)
                        .build())
                .setCommonEnum(TestProtoMessages.SimpleEnum.SECOND)
                .build();

        ProtoToMap mapper = new ProtoToMap();
        Map<String, Object> map = mapper.parse(message);
        Assert.assertEquals(map.get("commonEnum"), "SECOND");
        Assert.assertEquals(((Map<String,Object>)map.get("inmessage")).get("commonEnum"), "SECOND");
        Assert.assertEquals(((Map<String,Object>)map.get("otherMessage")).get("enumItem"), "ITEM");
    }
}
