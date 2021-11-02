package com.hazelcast.maven.attribution;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

class AttributionContext {
    final BlockingQueue<SrcFile> srcQueue = new LinkedBlockingQueue<>(1024);
    final AtomicBoolean producersRunning = new AtomicBoolean(true);
    final ConcurrentMap<String, Set<String>> foundAttribution = new ConcurrentHashMap<>();
}
