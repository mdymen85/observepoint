package com.challenge.observepoint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataStructureService {

    private static ConcurrentHashMap<String, Integer> ipsMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Integer> top100Map = new ConcurrentHashMap<>();
    private static LinkedHashMap<String, Integer> top100SortedMap = new LinkedHashMap<>();

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
        top100SortedMap.clear();
        top100Map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> top100SortedMap.put(x.getKey(), x.getValue()));
        return top100SortedMap;
    }

    public void clear(){
        ipsMap.clear();
        top100Map.clear();
        top100SortedMap.clear();
    }

    /**
     * Will ignore ip in a wrong format.
     *
     * If an IP exists, will merge that IP in the map
     * or if dont exists will add it.
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
     *
     * @param ip
     * @param updatedValue
     */
    private void processTop100(final String ip, final int updatedValue) {
        var thread = new Thread(() -> process(ip, updatedValue));
        thread.start();
    }

    /**
     *
     * @param ip
     * @param updatedValue
     */
    private void process(String ip, int updatedValue) {
        if (top100Map.size() >= 100) {
            updateCompleteTop100(ip, updatedValue);
        } else {
            top100Map.put(ip, updatedValue);
        }
    }

    private void updateCompleteTop100(final String ip, final int updatedValue) {
        if (ipExists(ip, updatedValue)) return;
        for (Map.Entry<String, Integer> entry : top100Map.entrySet()) {
            if (entry.getValue() < updatedValue) {
                top100Map.replace(ip, updatedValue);
            }
        }
    }

    private boolean ipExists(String ip, int updatedValue) {
        if (top100Map.containsKey(ip)) {
            top100Map.put(ip, updatedValue);
            return true;
        }
        return false;
    }
}
