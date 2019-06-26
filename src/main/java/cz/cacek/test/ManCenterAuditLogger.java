package cz.cacek.test;

import com.hazelcast.auditlog.AuditLogUtils;
import com.hazelcast.auditlog.annotations.AuditMessages;
import com.hazelcast.auditlog.annotations.Message;

@AuditMessages(prefix="MC")
public interface ManCenterAuditLogger {

    ManCenterAuditLogger LOGGER = AuditLogUtils.getLogger(ManCenterAuditLogger.class);
    
    @Message(value="User %s has logged in from address %s", code=1)
    public void userLoggedIn(String name, String address);
    
    @Message(value="User %s has logged out", code=2)
    public void userLoggedOut(String name);
}
