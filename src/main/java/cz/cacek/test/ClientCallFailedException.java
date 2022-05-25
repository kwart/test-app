package cz.cacek.test;

import java.util.List;

import com.hazelcast.client.impl.protocol.exception.ErrorHolder;

public class ClientCallFailedException extends RuntimeException {

    private final List<ErrorHolder> err;

    public ClientCallFailedException(List<ErrorHolder> err) {
        this.err = err;
    }

    public List<ErrorHolder> getErr() {
        return err;
    }
}
