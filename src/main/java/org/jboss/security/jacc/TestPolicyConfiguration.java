package org.jboss.security.jacc;

import java.security.Permission;
import java.security.PermissionCollection;

import javax.security.jacc.EJBRoleRefPermission;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;

import org.jboss.security.PicketBoxLogger;
import org.jboss.security.PicketBoxMessages;
import org.jboss.security.util.state.IllegalTransitionException;
import org.jboss.security.util.state.State;
import org.jboss.security.util.state.StateMachine;

public class TestPolicyConfiguration implements PolicyConfiguration {

  /** The JACC context id associated with the policy */
  private String contextID;
  /** The Policy impl which handles the JACC permissions */
  private DelegatingPolicy policy;
  /** A state machine whihc enforces the state behavior of this config */
  private StateMachine configStateMachine;

  protected TestPolicyConfiguration(String contextID, DelegatingPolicy policy, StateMachine configStateMachine)
      throws PolicyContextException {
    this.contextID = contextID;
    this.policy = policy;
    this.configStateMachine = configStateMachine;

    if (contextID == null)
      throw PicketBoxMessages.MESSAGES.invalidNullArgument("contextID");
    if (policy == null)
      throw PicketBoxMessages.MESSAGES.invalidNullArgument("policy");
    if (configStateMachine == null)
      throw PicketBoxMessages.MESSAGES.invalidNullArgument("configStateMachine");

    validateState("getPolicyConfiguration");
    PicketBoxLogger.LOGGER.debugJBossPolicyConfigurationConstruction(contextID);
  }

  void initPolicyConfiguration(boolean remove) throws PolicyContextException {
    validateState("getPolicyConfiguration");
    policy.initPolicyConfiguration(contextID, remove);
  }

  public void addToExcludedPolicy(Permission permission) throws PolicyContextException {
    PicketBoxLogger.LOGGER.traceAddPermissionToExcludedPolicy(permission);
    validateState("addToExcludedPolicy");
    policy.addToExcludedPolicy(contextID, permission);
  }

  public void addToExcludedPolicy(PermissionCollection permissions) throws PolicyContextException {
    PicketBoxLogger.LOGGER.traceAddPermissionsToExcludedPolicy(permissions);
    validateState("addToExcludedPolicy");
    policy.addToExcludedPolicy(contextID, permissions);
  }

  public void addToRole(String roleName, Permission permission) throws PolicyContextException {
    PicketBoxLogger.LOGGER.traceAddPermissionToRole(permission);
    validateState("addToRole");
    if ("Admin".equals(roleName) && permission instanceof EJBRoleRefPermission) {
      System.err.println("Skipping Admin's EJBRoleRefPermission " + permission);
    } else {
      policy.addToRole(contextID, roleName, permission);
      System.err.println("Permission for role " + roleName + " added: " + permission);
    }
  }

  public void addToRole(String roleName, PermissionCollection permissions) throws PolicyContextException {
    PicketBoxLogger.LOGGER.traceAddPermissionsToRole(permissions);
    validateState("addToRole");
    if ("Admin".equals(roleName)) {
      System.err.println("Skipping Admin's permissions " + permissions);
    } else {
      policy.addToRole(contextID, roleName, permissions);
      System.err.println("Permission for role " + roleName + " added: " + permissions);
    }
  }

  public void addToUncheckedPolicy(Permission permission) throws PolicyContextException {
    PicketBoxLogger.LOGGER.traceAddPermissionToUncheckedPolicy(permission);
    validateState("addToUncheckedPolicy");
    policy.addToUncheckedPolicy(contextID, permission);
  }

  public void addToUncheckedPolicy(PermissionCollection permissions) throws PolicyContextException {
    PicketBoxLogger.LOGGER.traceAddPermissionsToUncheckedPolicy(permissions);
    validateState("addToUncheckedPolicy");
    policy.addToUncheckedPolicy(contextID, permissions);
  }

  public void commit() throws PolicyContextException {
    PicketBoxLogger.LOGGER.tracePolicyConfigurationCommit(contextID);
    validateState("commit");
    policy.commit(contextID);
  }

  public void delete() throws PolicyContextException {
    PicketBoxLogger.LOGGER.tracePolicyConfigurationDelete(contextID);
    validateState("delete");
    policy.delete(contextID);
  }

  public String getContextID() throws PolicyContextException {
    validateState("getContextID");
    return contextID;
  }

  public boolean inService() throws PolicyContextException {
    validateState("inService");
    State state = configStateMachine.getCurrentState();
    boolean inService = state.getName().equals("inService");
    return inService;
  }

  public void linkConfiguration(PolicyConfiguration link) throws PolicyContextException {
    PicketBoxLogger.LOGGER.traceLinkConfiguration(link.getContextID());
    validateState("linkConfiguration");
    policy.linkConfiguration(contextID, link);
  }

  public void removeExcludedPolicy() throws PolicyContextException {
    PicketBoxLogger.LOGGER.traceRemoveExcludedPolicy(contextID);
    validateState("removeExcludedPolicy");
    policy.removeExcludedPolicy(contextID);
  }

  public void removeRole(String roleName) throws PolicyContextException {
    PicketBoxLogger.LOGGER.traceRemoveRole(roleName, contextID);
    validateState("removeRole");
    policy.removeRole(contextID, roleName);
  }

  public void removeUncheckedPolicy() throws PolicyContextException {
    PicketBoxLogger.LOGGER.traceRemoveUncheckedPolicy(contextID);
    validateState("removeUncheckedPolicy");
    policy.removeUncheckedPolicy(contextID);
  }

  protected void validateState(String action) throws PolicyContextException {
    try {
      configStateMachine.nextState(action);
    } catch (IllegalTransitionException e) {
      throw new PolicyContextException(PicketBoxMessages.MESSAGES.operationNotAllowedMessage(), e);
    }
  }
}
