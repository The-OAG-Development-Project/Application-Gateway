package ch.gianlucafrei.nellygateway.integration.testInfrastructure;

import ch.gianlucafrei.nellygateway.GlobalClockSource;
import ch.gianlucafrei.nellygateway.services.blacklist.LocalPersistentBlacklist;
import org.mapdb.DBMaker;

/**
 * This implementation of the local session blacklist uses only a in-memory database
 * to speed up tests and avoid file lock problems
 */
public class LocalInMemoryBlacklist extends LocalPersistentBlacklist {

    public LocalInMemoryBlacklist(GlobalClockSource clockSource) {
        super(clockSource, null);
    }

    @Override
    protected void initDb(String filename) {

        this.db = DBMaker.memoryDB()
                .transactionEnable()
                .closeOnJvmShutdown()
                .make();
    }
}
