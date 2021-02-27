package org.owasp.oag.integration.testInfrastructure;

import org.mapdb.DBMaker;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.services.blacklist.LocalPersistentBlacklist;

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
