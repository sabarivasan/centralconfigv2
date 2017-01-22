package com.centralconfig.persist;

import junit.framework.TestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Test for TimedConsulKVStore
 * Created by sabarivasan on 1/20/17.
 */
public class TimedConsulKVStoreTest {
    private static final String TEST_KEY_PREFIX = "test";
    private static final String TEST_VALUE_PREFIX = "value";
    private static final Random rand = new Random();

    private static TimedKVStore kvStore;


    @BeforeClass
    public static void init() {
        kvStore = new TimedConsulKVStore("http://dev-wiz-01:8500");
    }

    @Test
    public void test1Key() throws IOException, InterruptedException {
        String key = randomKey();
        String value = randomValue();
        long tenSecondsAgo = System.currentTimeMillis() - 10000;
        try {
            // Put value 10 seconds ago and test timeline until now
            kvStore.putAt(key, value, tenSecondsAgo);
            TestCase.assertEquals(value, kvStore.getValueAt(key, System.currentTimeMillis()).get());
            TestCase.assertEquals(value, kvStore.getValueAt(key, tenSecondsAgo).get());
            TestCase.assertFalse(kvStore.getValueAt(key, tenSecondsAgo - 1).isPresent());

            // Put new value now and test timeline
            String newValue = "new" + value;
            long newValTs = System.currentTimeMillis();
            kvStore.putNow(key, newValue);
            Thread.sleep(10);
            TestCase.assertEquals(newValue, kvStore.getValueAt(key, System.currentTimeMillis()).get());
            TestCase.assertEquals(value, kvStore.getValueAt(key, newValTs - 1).get());
            TestCase.assertEquals(value, kvStore.getValueAt(key, tenSecondsAgo).get());
            TestCase.assertFalse(kvStore.getValueAt(key, tenSecondsAgo - 1).isPresent());
        } finally {
            kvStore.deleteAllValuesFor(key);
        }
    }

    @Test
    public void testMultipleKeys() throws IOException, InterruptedException {
        String[] keys = new String[] {randomKey(), randomKey(), randomKey(), randomKey(), randomKey()};
        String[] values = new String[] {randomValue(), randomValue(), randomValue(), randomValue(), randomValue()};
        final int numTimelineValues = 3;// + rand.nextInt(5);
        long tenSecondsAgo = System.currentTimeMillis() - 10000;
        long now = System.currentTimeMillis();
        try {
            for (int keyNum = 0; keyNum < keys.length; keyNum++) {
                String key = keys[keyNum];
                String valuePrefix = values[keyNum];

                // Put values at random points between 10 seconds ago and now
                // The value to put at time ts is the random value generated for each key above appended by the timestamp ts itself
                List<Long> timeline = new ArrayList<>(values.length);
                for (int n = 0; n < numTimelineValues; n++) {
                    long t = tenSecondsAgo + ((long) (rand.nextFloat() * ((float) (now - tenSecondsAgo))));
                    kvStore.putAt(key, valuePrefix + t, t);
                    timeline.add(t);
                }
                Collections.sort(timeline);

                // For each key, there was no value just before ten seconds ago
                TestCase.assertFalse(kvStore.getValueAt(key, tenSecondsAgo - 1).isPresent());

                // For each timestamp in the timeline, test the value 1ms before, at the timestamp and 1ms after the timestamp
                for (int n = 0; n < numTimelineValues; n++) {
                    long ts = timeline.get(n);

                    // Test value 1ms before ts
                    if (n == 0) {
                        // Test no value just before first timestamp
                        TestCase.assertFalse(kvStore.getValueAt(key, ts - 1).isPresent());
                    } else {
                        TestCase.assertEquals(valuePrefix + timeline.get(n - 1), kvStore.getValueAt(key, ts - 1).get());
                    }

                    // Test value at ts
                    TestCase.assertEquals(valuePrefix + ts, kvStore.getValueAt(key, ts).get());

                    // Test value 1ms after ts
                    TestCase.assertEquals(valuePrefix + ts, kvStore.getValueAt(key, ts + 1).get());
                }
            }
        } finally {
            for (String key : keys) {
                kvStore.deleteAllValuesFor(key);
            }
        }

    }



    private static String randomKey() {
        return TEST_KEY_PREFIX + TimedConsulKVStore.KEY_TIMESTAMP_SEPARATOR + TEST_KEY_PREFIX + rand.nextInt(100000);
    }

    private static String randomValue() {
        return TEST_VALUE_PREFIX + rand.nextInt(100000);
    }

}

