# BZ-1481103 reproducer

This reproducer shows NPE in IBM JDK when ChannelBinding without HostAddress is used. 

https://bugzilla.redhat.com/show_bug.cgi?id=1481103

## Steps to reproduce

```bash
# Get a kerberos ticket
kinit jcacek@REDHAT.COM

# Check the credential cache file path
# search for Ticket cache line in the output of klist
klist

# Run the reproducer and use the credential cache file path as krb5cc.path system property
mvn install exec:java -Dkrb5cc.path=/tmp/krb5cc_1000
```

### Output
```
$ kinit jcacek@REDHAT.COM
Password for jcacek@REDHAT.COM:
 
$ klist
Ticket cache: FILE:/tmp/krb5cc_1000
Default principal: jcacek@REDHAT.COM

Valid starting      Expires             Service principal
22.8.2017 17:10:06  23.8.2017 03:10:00  krbtgt/REDHAT.COM@REDHAT.COM

$ mvn install exec:java -Dkrb5cc.path=/tmp/krb5cc_1000
[INFO] Scanning for projects...
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] Building test-app 1.0-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ test-app ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ test-app ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ test-app ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ test-app ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ test-app ---
[INFO] 
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ test-app ---
[INFO] Building jar: /home/kwart/projects/test-app/target/test-app.jar
[INFO] META-INF/maven/org.jboss.test/test-app/pom.xml already added, skipping
[INFO] META-INF/maven/org.jboss.test/test-app/pom.properties already added, skipping
[INFO] 
[INFO] --- maven-shade-plugin:2.2:shade (default) @ test-app ---
[INFO] Replacing original artifact with shaded artifact.
[INFO] Replacing /home/kwart/projects/test-app/target/test-app.jar with /home/kwart/projects/test-app/target/test-app-1.0-SNAPSHOT-shaded.jar
[INFO] 
[INFO] --- maven-install-plugin:2.4:install (default-install) @ test-app ---
[INFO] Installing /home/kwart/projects/test-app/target/test-app.jar to /home/kwart/.m2/repository/org/jboss/test/test-app/1.0-SNAPSHOT/test-app-1.0-SNAPSHOT.jar
[INFO] Installing /home/kwart/projects/test-app/pom.xml to /home/kwart/.m2/repository/org/jboss/test/test-app/1.0-SNAPSHOT/test-app-1.0-SNAPSHOT.pom
[INFO] 
[INFO] >>> exec-maven-plugin:1.2.1:java (default-cli) > validate @ test-app >>>
[INFO] 
[INFO] <<< exec-maven-plugin:1.2.1:java (default-cli) < validate @ test-app <<<
[INFO] 
[INFO] 
[INFO] --- exec-maven-plugin:1.2.1:java (default-cli) @ test-app ---
[JGSS_DBG_CRED]  org.jboss.test.App.main() JAAS config: debug=true
[JGSS_DBG_CRED]  org.jboss.test.App.main() JAAS config: credsType=initiate only (default)
[JGSS_DBG_CRED]  org.jboss.test.App.main() config: useDefaultCcache=false
[JGSS_DBG_CRED]  org.jboss.test.App.main() config: useCcache=file:/tmp/krb5cc_1000
[JGSS_DBG_CRED]  org.jboss.test.App.main() config: useDefaultKeytab=false (default)
[JGSS_DBG_CRED]  org.jboss.test.App.main() config: useKeytab=null
[JGSS_DBG_CRED]  org.jboss.test.App.main() JAAS config: forwardable=false (default)
[JGSS_DBG_CRED]  org.jboss.test.App.main() JAAS config: renewable=false (default)
[JGSS_DBG_CRED]  org.jboss.test.App.main() JAAS config: proxiable=false (default)
[JGSS_DBG_CRED]  org.jboss.test.App.main() JAAS config: tryFirstPass=false (default)
[JGSS_DBG_CRED]  org.jboss.test.App.main() JAAS config: useFirstPass=false (default)
[JGSS_DBG_CRED]  org.jboss.test.App.main() JAAS config: moduleBanner=false (default)
[JGSS_DBG_CRED]  org.jboss.test.App.main() JAAS config: interactive login? no
[JGSS_DBG_CRED]  org.jboss.test.App.main() Retrieving Kerberos creds from cache for principal=null
[JGSS_DBG_CRED]  org.jboss.test.App.main() Non-interactive login; no callbacks necessary.
[JGSS_DBG_CRED]  org.jboss.test.App.main() Done retrieving Kerberos creds from cache
[JGSS_DBG_CRED]  org.jboss.test.App.main() Login successful
[JGSS_DBG_CRED]  org.jboss.test.App.main() kprincipal : jcacek@REDHAT.COM
[JGSS_DBG_CRED]  org.jboss.test.App.main() jcacek@REDHAT.COM added to Subject
[JGSS_DBG_CRED]  org.jboss.test.App.main() Kerberos ticket added to Subject
[JGSS_DBG_CRED]  org.jboss.test.App.main() No keys to add to Subject for jcacek@REDHAT.COM
[WARNING] 
java.lang.reflect.InvocationTargetException
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:90)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:55)
	at java.lang.reflect.Method.invoke(Method.java:508)
	at org.codehaus.mojo.exec.ExecJavaMojo$1.run(ExecJavaMojo.java:297)
	at java.lang.Thread.run(Thread.java:785)
Caused by: java.lang.NullPointerException
	at com.ibm.security.krb5.internal.HostAddress.<init>(Unknown Source)
	at com.ibm.security.jgss.mech.krb5.Z.<init>(Unknown Source)
	at com.ibm.security.jgss.mech.krb5.g.setChannelBinding(Unknown Source)
	at com.ibm.security.jgss.GSSContextImpl.setChannelBinding(Unknown Source)
	at org.jboss.test.App.lambda$0(App.java:67)
	at org.jboss.test.App$$Lambda$1.00000000180800A0.run(Unknown Source)
	at java.security.AccessController.doPrivileged(AccessController.java:686)
	at javax.security.auth.Subject.doAs(Subject.java:569)
	at org.jboss.test.App.main(App.java:58)
	... 6 more
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 3.153 s
[INFO] Finished at: 2017-08-22T17:10:51+02:00
[INFO] Final Memory: 10M/17M
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.codehaus.mojo:exec-maven-plugin:1.2.1:java (default-cli) on project test-app: An exception occured while executing the Java class. null: InvocationTargetException: NullPointerException -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException

```