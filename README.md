# Custom role mapping in PicketLink

This project/branch shows 2 ways of role mapping on IDP side - a custom LoginModule and a custom RoleGenerator.

Based on the PicketLink forum post https://community.jboss.org/message/775375#775375.

## Custom LoginModule

Implemented in the class `org.jboss.test.RoleMappingLoginModule`, which should be chained in your login-module stack in the IDP security-domain.

	<security-domain name="idp" cache-type="default">
		<authentication>
			<login-module code="YourPreferredLoginModule" flag="required" />
			<!-- 
				<login-module code="UsersRoles" flag="required">
					<module-option name="usersProperties" value="users.properties" />
					<module-option name="rolesProperties" value="roles.properties" />
				</login-module>
			-->
			<login-module code="org.jboss.test.RoleMappingLoginModule"
				flag="required" />
		</authentication>
	</security-domain>

## Custom RoleGenerator

Implemented in the class `org.jboss.test.CustomRoleGenerator`, which can be enabled in `picketlink.xml` in your IDP application.

	<PicketLinkIDP xmlns="urn:picketlink:identity-federation:config:2.1" RoleGenerator="org.jboss.test.CustomRoleGenerator">
