/**
 * 
 */
package com.rdara.gists.patterns;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ramesh Dara
 */
public class MetricPattern {

    /**
     * Metric pattern that demonstrates collecting possible "m * n" metrics, when we need to count "m" items with each item can have "n" possible ways.
     * 
     * Ex. There may be serveral servers that might be RUNNING, STOPPED. These servers may be colocated in different colos like USA, EUROPE and ASIA. 
     * In that case we have 6 different metrics like USA_RUNNING, USA_STOPPED, EURPOE_RUNNING,....
     * 
     * Metric pattern demonstrates grouping of items  with their possible state counters, so that each possible combination can be counted. 
     * 
     * Disclaimer: I kept all classes together here so that I can share as gist that one obtain to run and experiment. Its very strongly advised to have each class
     * in its own file.
     * 
     */

    public static void main(String[] args) {
        Map<Priority, Counter<Result>> mapPriorityCounters = new ConcurrentHashMap<Priority, Counter<Result>>();

        for (Priority Priority : Priority.values()) {
            mapPriorityCounters.put(Priority, new CounterImpl<Result>());
        }

        mapPriorityCounters.get(Priority.P2).increment(Result.PASS);
        mapPriorityCounters.get(Priority.P2).increment(Result.PASS);
        mapPriorityCounters.get(Priority.P3).increment(Result.SKIP, 12);
        mapPriorityCounters.get(Priority.P4).increment(Result.FAIL, 2);
        mapPriorityCounters.get(Priority.P4).increment(Result.FAIL, 2);

        for (int i = 0; i < 5; i++) {
            mapPriorityCounters.get(Priority.P1).increment(Result.PASS);
            mapPriorityCounters.get(Priority.P1).increment(Result.EXECUTION_TIME, 3);
            long totalExecutionTime = mapPriorityCounters.get(Priority.P1).get(Result.EXECUTION_TIME);
            long count = mapPriorityCounters.get(Priority.P1).get(Result.PASS);
            mapPriorityCounters.get(Priority.P1).put(Result.AVERAGEG_EXECUTION_TIME,
                    new Long(totalExecutionTime / count));
        }

        for (Map.Entry<Priority, Counter<Result>> PriorityEntry : mapPriorityCounters.entrySet()) {
            for (Map.Entry<Result, Long> counterEntry : PriorityEntry.getValue().getMetricsMap().entrySet()) {
                System.out
                        .println(PriorityEntry.getKey() + "_" + counterEntry.getKey() + ":" + counterEntry.getValue());
            }
        }
    }

    interface Counter<T> {

        long increment(T key, long count);

        long increment(T key);

        long get(T key);

        long put(T key, Long value);

        Map<T, Long> getMetricsMap();
    }

    public static class CounterImpl<T> implements Counter<T> {

        private Map<T, Long> mapMetrics = new TreeMap<T, Long>();

        private static final Long ZERO = new Long(0);

        public long increment(T key, long count) {
            if (!getMetricsMap().containsKey(key)) {
                getMetricsMap().put(key, ZERO);
            }
            getMetricsMap().put(key, getMetricsMap().get(key) + count);
            return getMetricsMap().get(key);
        }

        public long increment(T key) {
            return increment(key, 1);
        }

        public long get(T key) {
            long retValue = 0;
            if (getMetricsMap().containsKey(key)) {
                retValue = getMetricsMap().get(key);
            }
            return retValue;
        }

        public long put(T key, Long value) {
            getMetricsMap().put(key, value);
            return value;
        }

        public Map<T, Long> getMetricsMap() {
            return mapMetrics;
        }
    }

    public enum Result {
        PASS,
        FAIL,
        SKIP,
        WARN,
        EXCEPTION,
        EXECUTION_TIME,
        AVERAGEG_EXECUTION_TIME
    }

    public enum Priority {
        PO,
        P1,
        P2,
        P3,
        P4,
        NONE
    }

}
