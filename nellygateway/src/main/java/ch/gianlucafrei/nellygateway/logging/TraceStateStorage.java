package ch.gianlucafrei.nellygateway.logging;

public interface TraceStateStorage {

    void setState(Object state);
    Object getState();

}
