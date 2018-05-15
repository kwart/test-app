/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.test;

import static com.hazelcast.util.Preconditions.checkNotNull;
import static java.util.Collections.unmodifiableSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds blacklist and whitelist configuration in java deserialization configuration.
 */
public class ClassFilter implements Listed {

    private final Set<String> classes = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private final Set<String> packages = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    /**
     * Returns unmodifiable set of class names.
     */
    public Set<String> getClasses() {
        return unmodifiableSet(classes);
    }

    /**
     * Returns unmodifiable set of package names.
     */
    public Set<String> getPackages() {
        return unmodifiableSet(packages);
    }

    public ClassFilter addClasses(String... names) {
        checkNotNull(names);
        for (String name : names) {
            classes.add(name);
        }
        return this;
    }

    public Listed setClasses(Collection<String> names) {
        checkNotNull(names);
        classes.clear();
        classes.addAll(names);
        return this;
    }

    public Listed addPackages(String... names) {
        checkNotNull(names);
        for (String name : names) {
            packages.add(name);
        }
        return this;
    }

    public Listed setPackages(Collection<String> names) {
        checkNotNull(names);
        packages.clear();
        packages.addAll(names);
        return this;
    }

    public boolean isEmpty() {
        return classes.isEmpty() && packages.isEmpty();
    }

    /* (non-Javadoc)
     * @see com.hazelcast.test.Listed#isListed(java.lang.String)
     */
    @Override
    public boolean isListed(String className) {
        if (classes.contains(className)) {
            return true;
        }
        if (!packages.isEmpty()) {
            int dotPosition = className.lastIndexOf(".");
            if (dotPosition > 0) {
                String packageName = className.substring(0, dotPosition);
                return packages.contains(packageName);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((classes == null) ? 0 : classes.hashCode());
        result = prime * result + ((packages == null) ? 0 : packages.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ClassFilter other = (ClassFilter) obj;
        return ((classes == null && other.classes == null) || (classes != null && classes.equals(other.classes)))
                && ((packages == null && other.packages == null) || (packages != null && packages.equals(other.packages)));
    }

    @Override
    public String toString() {
        return "ClassFilter{classes=" + classes + ", packages=" + packages + "}";
    }

}
