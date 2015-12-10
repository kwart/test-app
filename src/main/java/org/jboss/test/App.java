package org.jboss.test;

import java.util.StringTokenizer;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

/**
 * Hello world!
 *
 * @author Josef Cacek
 */
public class App {

	public static void main(String[] args) {
		// wrong attribute name
		compareRoleParsing("cn=double,ou=admin", "ou");

		// escaping example from RFC 2253 - Section 5 -
		// https://tools.ietf.org/html/rfc2253
		compareRoleParsing("CN=L. Eagle,O=Sue\\, Grabbit and Runn,C=GB", "O");
		compareRoleParsing("OU=Sales+CN=J. Smith,O=Widget Inc.,C=US", "OU");
		compareRoleParsing("CN=Before\\0DAfter,O=Test,C=GB", "CN");
		compareRoleParsing("OU=#4869,O=Test,C=GB", "OU");
	}

	private static void compareRoleParsing(String dn, String groupNameAttribute) {
		System.out.println(String.format("Parsing role from dn %s for attribute %s", dn, groupNameAttribute));
		System.out.println("Original impl: " + parseRole(dn, groupNameAttribute));
		System.out.println("     New impl: " + parseRoleNew(dn, groupNameAttribute));
		System.out.println();
	}

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
}
