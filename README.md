# Attribute parsing from DN in LDAP

What's wrong with this role-name parsing from DN?

```java
private static String parseRole(String dn, String groupNameAttribute) {
	StringTokenizer st = new StringTokenizer(dn, ",");
	while (st != null && st.hasMoreTokens()) {
		String keyVal = st.nextToken();
		if (keyVal.indexOf(groupNameAttribute) > -1) {
			StringTokenizer kst = new StringTokenizer(keyVal, "=");
			kst.nextToken();
			String simpleName = kst.nextToken();
			return simpleName;
		}
	}
	return null;
}
```

## Why not to use "key=value".indexOf(attribute)?

Because user gets false positives.

## Why not to use StringTokenizer to parse role names from DN?

Because it ignores escaping in LDAP names.

## Possible solution

Use `javax.naming.ldap.LdapName` which implements [RFC-2253](https://tools.ietf.org/html/rfc2253).

```java
private static String parseRoleNew(String dn, String groupNameAttribute) {
	try {
		LdapName ldapName = new LdapName(dn);
		for (int i = ldapName.size() - 1; i >= 0; i--) {
			String rdnString = ldapName.get(i);
			Rdn rdn = new Rdn(rdnString);
			Attribute attr = rdn.toAttributes().get(groupNameAttribute);
			if (attr != null) {
				Object value = attr.get();
				if (value != null) {
					return (value instanceof byte[]) ? new String((byte[]) value) : value.toString();
				}
			}
		}
	} catch (NamingException e) {
		e.printStackTrace();
	}
	return null;
}
```

## Example

Run [App.java](src/main/java/org/jboss/test/App.java) class. Here is the output:

```
Parsing role from dn cn=double,ou=admin for attribute ou
Original impl: double
     New impl: admin

Parsing role from dn CN=L. Eagle,O=Sue\, Grabbit and Runn,C=GB for attribute O
Original impl: Sue\
     New impl: Sue, Grabbit and Runn

Parsing role from dn OU=Sales+CN=J. Smith,O=Widget Inc.,C=US for attribute OU
Original impl: Sales+CN
     New impl: Sales

Parsing role from dn CN=Before\0DAfter,O=Test,C=GB for attribute CN
Original impl: Before\0DAfter
     New impl: Before
After

Parsing role from dn OU=#4869,O=Test,C=GB for attribute OU
Original impl: #4869
     New impl: Hi
```