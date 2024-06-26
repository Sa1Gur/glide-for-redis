/** Copyright GLIDE-for-Redis Project Contributors - SPDX Identifier: Apache-2.0 */
package glide.ffi;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import glide.ffi.resolvers.RedisValueResolver;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class FfiTest {

    static {
        System.loadLibrary("glide_rs");
    }

    public static native long createLeakedNil();

    public static native long createLeakedSimpleString(String value);

    public static native long createLeakedOkay();

    public static native long createLeakedInt(long value);

    public static native long createLeakedBulkString(byte[] value);

    public static native long createLeakedLongArray(long[] value);

    public static native long createLeakedMap(long[] keys, long[] values);

    public static native long createLeakedDouble(double value);

    public static native long createLeakedBoolean(boolean value);

    public static native long createLeakedVerbatimString(String value);

    public static native long createLeakedLongSet(long[] value);

    @Test
    public void redisValueToJavaValue_Nil() {
        long ptr = FfiTest.createLeakedNil();
        Object nilValue = RedisValueResolver.valueFromPointer(ptr);
        assertNull(nilValue);
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "cat", "dog"})
    public void redisValueToJavaValue_SimpleString(String input) {
        long ptr = FfiTest.createLeakedSimpleString(input);
        Object simpleStringValue = RedisValueResolver.valueFromPointer(ptr);
        assertEquals(input, simpleStringValue);
    }

    @Test
    public void redisValueToJavaValue_Okay() {
        long ptr = FfiTest.createLeakedOkay();
        Object okayValue = RedisValueResolver.valueFromPointer(ptr);
        assertEquals("OK", okayValue);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, 100L, 774L, Integer.MAX_VALUE + 1L, Integer.MIN_VALUE - 1L})
    public void redisValueToJavaValue_Int(Long input) {
        long ptr = FfiTest.createLeakedInt(input);
        Object longValue = RedisValueResolver.valueFromPointer(ptr);
        assertTrue(longValue instanceof Long);
        assertEquals(input, longValue);
    }

    @Test
    public void redisValueToJavaValue_BulkString() {
        String input = "😀\n💎\n🗿";
        byte[] bulkString = input.getBytes();
        long ptr = FfiTest.createLeakedBulkString(bulkString);
        Object bulkStringValue = RedisValueResolver.valueFromPointer(ptr);
        assertEquals(input, bulkStringValue);
    }

    @Test
    public void redisValueToJavaValue_Array() {
        long[] array = {1L, 2L, 3L};
        long ptr = FfiTest.createLeakedLongArray(array);
        Object longArrayValue = RedisValueResolver.valueFromPointer(ptr);
        assertTrue(longArrayValue instanceof Object[]);
        Object[] result = (Object[]) longArrayValue;
        assertArrayEquals(new Object[] {1L, 2L, 3L}, result);
    }

    @Test
    public void redisValueToJavaValue_Map() {
        long[] keys = {12L, 14L, 23L};
        long[] values = {1L, 2L, 3L};
        long ptr = FfiTest.createLeakedMap(keys, values);
        Object mapValue = RedisValueResolver.valueFromPointer(ptr);
        assertTrue(mapValue instanceof HashMap<?, ?>);
        HashMap<?, ?> result = (HashMap<?, ?>) mapValue;
        assertAll(
                () -> assertEquals(1L, result.get(12L)),
                () -> assertEquals(2L, result.get(14L)),
                () -> assertEquals(3L, result.get(23L)));
    }

    @ParameterizedTest
    @ValueSource(doubles = {1.0d, 25.2d, 103.5d})
    public void redisValueToJavaValue_Double(Double input) {
        long ptr = FfiTest.createLeakedDouble(input);
        Object doubleValue = RedisValueResolver.valueFromPointer(ptr);
        assertEquals(input, doubleValue);
    }

    @Test
    public void redisValueToJavaValue_Boolean() {
        long ptr = FfiTest.createLeakedBoolean(true);
        Object booleanValue = RedisValueResolver.valueFromPointer(ptr);
        assertTrue((Boolean) booleanValue);
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "cat", "dog"})
    public void redisValueToJavaValue_VerbatimString(String input) {
        long ptr = FfiTest.createLeakedVerbatimString(input);
        Object verbatimStringValue = RedisValueResolver.valueFromPointer(ptr);
        assertEquals(input, verbatimStringValue);
    }

    @Test
    public void redisValueToJavaValue_Set() {
        long[] array = {1L, 2L, 2L};
        long ptr = FfiTest.createLeakedLongSet(array);
        Object longSetValue = RedisValueResolver.valueFromPointer(ptr);
        assertTrue(longSetValue instanceof HashSet<?>);
        HashSet<?> result = (HashSet<?>) longSetValue;
        assertAll(
                () -> assertTrue(result.contains(1L)),
                () -> assertTrue(result.contains(2L)),
                () -> assertEquals(result.size(), 2));
    }
}
