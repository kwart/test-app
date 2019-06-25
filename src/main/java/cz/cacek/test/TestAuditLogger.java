package cz.cacek.test;

import com.hazelcast.auditlog.AuditLogUtils;
import com.hazelcast.auditlog.annotations.AuditMessages;
import com.hazelcast.auditlog.annotations.Message;

@AuditMessages
public interface TestAuditLogger {

    TestAuditLogger LOGGER = AuditLogUtils.getLogger(TestAuditLogger.class);
    
    @Message(value="User %s has logged in from address %s", code=1)
    public void userLoggedIn(String name, String address);
    
    @Message(value="User %s has logged in from address %s", code=2)
    public void userLoggedIn2(String name, String address);
}
