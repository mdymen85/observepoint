package com.challenge.observepoint;

import com.google.common.base.Stopwatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataStructureService {

    private static ConcurrentHashMap<String, Integer> ipsMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Integer> top100Map = new ConcurrentHashMap<>();

    //copy of top100Map that is going to be ordered in runtime, because ConcurrentHashMap does not allow sort.
    private static LinkedHashMap<String, Integer> top100SortedMap = new LinkedHashMap<>();

    //auxiliar map that is going to be ordered in runtime, in order to update the map.
    private LinkedHashMap<String, Integer> currentTop100Map = new LinkedHashMap<>();

    private static final int INCREASE = 1;
    private static final String IPV4_PATTERN = "(\\b25[0-5]|\\b2[0-4][0-9]|\\b[01]?[0-9][0-9]?)(\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3}";

    public Map<String, Integer> getIpsMap() {
        return this.ipsMap;
    }

    public void reset() {
        ipsMap = new ConcurrentHashMap<>();
        top100Map = new ConcurrentHashMap<>();
        top100SortedMap = new LinkedHashMap<>();
    }

    public Map<String, Integer> top100() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        top100SortedMap.clear();
        orderTop100Desc();
        stopwatch.stop();
        System.out.println("Total top100: " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return top100SortedMap;
    }


    private void orderTop100Desc() {
        top100Map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> top100SortedMap.put(x.getKey(), x.getValue()));
    }

    public void clear() {
        ipsMap.clear();
        top100Map.clear();
        top100SortedMap.clear();
    }

    /**
     * Will ignore ip in a wrong format.
     * <p>
     * If an IP exists, will merge that IP in the map
     * or if dont exists will add it.
     *
     * @param ip
     */
    public void requestHandled(final String ip) {

        log.info("Process ip : {} started.", ip);

        if (validateIp(ip)) return;
        if (!ipsMap.containsKey(ip)) {
            ipsMap.put(ip, INCREASE);
            processTop100(ip, INCREASE);
            return;
        }
        ipsMap.merge(ip, INCREASE, Integer::sum);
        processTop100(ip, ipsMap.get(ip));
    }

    private boolean validateIp(final String ip) {
        if (Strings.isBlank(ip) || !ip.matches(IPV4_PATTERN)) {
            System.err.printf("IP [%s] is not valid.%n", ip);
            return true;
        }
        return false;
    }


    /**
     * Start the top 100 registration in asyncronous way.
     *
     * @param ip
     * @param updatedValue
     */
    private void processTop100(final String ip, final int updatedValue) {
        CompletableFuture.runAsync(() -> process(ip, updatedValue));
    }

    /**
     * If the map has more than 100 elements,
     *
     * @param ip
     * @param updatedValue
     */
    private void process(final String ip, final int updatedValue) {
        if (top100Map.size() >= 100) {
            updateCompleteTop100(ip, updatedValue);
        } else {
            top100Map.put(ip, updatedValue);
        }
    }

    /**
     * 1 - if an ip already exists, will update count.
     * 2 - new in concurrentTop100Map
     * 3 - do a sorted copy top100Map to currentTopMap100
     * 4 - iterate the sorted copy in order to verify if each value is
     * lower than updatedValue. If yes, will remove the value and add
     * a new ip with their count.
     *
     * @param ip
     * @param updatedValue
     */
    private void updateCompleteTop100(final String ip, final int updatedValue) {
        if (ipExists(ip, updatedValue)) return;
        currentTop100Map = new LinkedHashMap<>();
        sortCurrentTop100Map();
        for (Map.Entry<String, Integer> entry : currentTop100Map.entrySet()) {
            if (updatedValue > entry.getValue()) {
                top100Map.remove(entry.getKey());
                top100Map.put(ip, updatedValue);
                return;
            }
        }
    }

    private void sortCurrentTop100Map() {
        top100Map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> currentTop100Map.put(x.getKey(), x.getValue()));
    }

    private boolean ipExists(final String ip, final int updatedValue) {
        if (top100Map.containsKey(ip)) {
            top100Map.put(ip, updatedValue);
            return true;
        }
        return false;
    }

}