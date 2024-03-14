#!/bin/sh
if [[ XX"$IBIZ_SERVICEHUB_ENABLEDEBUG" == XX"true" ]];then
    mkdir -p /ibiztools
    mv /clouddebugger.jar /ibiztools/
    echo "The application will start in ${IBIZ_SLEEP}s..."
    sleep ${IBIZ_SLEEP}
    java ${JAVA_OPTS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=127.0.0.1:12345 -Duser.timezone=$TZ -Djava.security.egd=file:/dev/./urandom -Xbootclasspath/a:/usr/lib/jvm/java-1.8-openjdk/jre/lib/mysql-connector-java-8.0.22.jar:/usr/lib/jvm/java-1.8-openjdk/jre/lib/ojdbc8.jar:/usr/lib/jvm/java-1.8-openjdk/jre/lib/sqljdbc4-4.0.jar:/usr/lib/jvm/java-1.8-openjdk/jre/lib/postgresql-42.0.0.jar -jar /ibizservicerunner-provider.jar
else
    echo "The application will start in ${IBIZ_SLEEP}s..."
    sleep ${IBIZ_SLEEP}
    java ${JAVA_OPTS} -Duser.timezone=$TZ -Djava.security.egd=file:/dev/./urandom -Xbootclasspath/a:/usr/lib/jvm/java-1.8-openjdk/jre/lib/mysql-connector-java-8.0.22.jar:/usr/lib/jvm/java-1.8-openjdk/jre/lib/ojdbc8.jar:/usr/lib/jvm/java-1.8-openjdk/jre/lib/sqljdbc4-4.0.jar:/usr/lib/jvm/java-1.8-openjdk/jre/lib/postgresql-42.0.0.jar -jar /ibizservicerunner-provider.jar
fi
