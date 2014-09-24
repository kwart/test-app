# Java Security Manager Demo App

It simply tries to read `/etc/passwd` file and print user names from it.

## How to build it

You need to have [Maven 3.x](http://maven.apache.org/) installed

	$ mvn clean package
	$ cd target

## How to use it

Normal run:

	java -jar app.jar [parameters]

Run with Java Security Manager enabled:

	java -Djava.security.manager -jar app.jar [parameters]

Run with Missing Permissions Dumper enabled:

	java -Djava.security.manager=net.sourceforge.prograde.sm.DumpMissingPermissionsJSM -jar app.jar [parameters]

### Parameters

You can combine following 2 params:

 * `threaded` - runs in a new thread
 * `privileged` - code in `app-lib.jar` runs in privileged section
