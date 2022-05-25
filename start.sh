#!/bin/bash

cat <<'EOT'

██████╗ ███████╗███████╗████████╗    ██╗███╗   ██╗    ██████╗ ███████╗ █████╗ ███████╗███████╗
██╔══██╗██╔════╝██╔════╝╚══██╔══╝    ██║████╗  ██║    ██╔══██╗██╔════╝██╔══██╗██╔════╝██╔════╝
██████╔╝█████╗  ███████╗   ██║       ██║██╔██╗ ██║    ██████╔╝█████╗  ███████║███████╗█████╗  
██╔══██╗██╔══╝  ╚════██║   ██║       ██║██║╚██╗██║    ██╔═══╝ ██╔══╝  ██╔══██║╚════██║██╔══╝  
██║  ██║███████╗███████║   ██║       ██║██║ ╚████║    ██║     ███████╗██║  ██║███████║███████╗
╚═╝  ╚═╝╚══════╝╚══════╝   ╚═╝       ╚═╝╚═╝  ╚═══╝    ╚═╝     ╚══════╝╚═╝  ╚═╝╚══════╝╚══════╝

EOT

if [ "$JAVA_HOME" ]; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA="$(command -v java 2>/dev/null)"
fi

if [ -z "$JAVA" ]; then
    echo "Cannot find a way to start the JVM: neither JAVA_HOME is set nor the java command is on the PATH"
    exit 1
fi


JAVA_VERSION=$(${JAVA} -version 2>&1 | sed -En 's/.* version "([0-9]+).*$/\1/p')
if [ "$JAVA_VERSION" -ge "9" ]; then
    JDK_OPTS="\
        --add-modules java.se \
        --add-exports java.base/jdk.internal.ref=ALL-UNNAMED \
        --add-opens java.base/java.lang=ALL-UNNAMED \
        --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
        --add-opens java.management/sun.management=ALL-UNNAMED \
        --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED \
    "
fi

"$JAVA" -Djava.util.logging.config.file=logging.properties $JDK_OPTS -jar target/test-app.jar

