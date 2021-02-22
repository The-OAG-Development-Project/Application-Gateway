package org.owasp.oag.logging;

public interface TraceStateStorage {

    void setState(Object state);
    Object getState();

}
