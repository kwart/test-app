package org.jboss.test;

public class ObjectWithFixedHashcode {

    @Override
    public int hashCode() {
        return 0;
    }

}
