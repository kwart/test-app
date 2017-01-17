# Microbenchmark for JBPAPP-11257

Verification microbenchmark for performance issue in `SimpleRoleGroup` class from `jbosssx.jar`.

## Run the benchmark

```bash
git clone -b JBPAPP-11257-micro-benchmark https://github.com/kwart/test-app.git
cd test-app

mvn install:install-file -Dfile=/path/to/patched/jbosssx.jar -DgroupId=org.jboss.security -DartifactId=jbosssx -Dversion=2.0.5.SP3-1-JBPAPP-11257 -Dpackaging=jar

echo "Running unpatched"

mvn clean install
java -jar target/test-app.jar

echo "Running patched"

mvn clean install -Dpatched
java -jar target/test-app.jar

```