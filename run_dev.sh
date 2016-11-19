

java -agentlib:jdwp=transport=dt_socket,server=y,address=8001,suspend=n \
  -Djava.rmi.server.hostname=`hostname` \
              -Dcom.sun.management.jmxremote.ssl=false \
              -Dcom.sun.management.jmxremote.authenticate=false \
              -Dcom.sun.management.jmxremote \
              -Dcom.sun.management.jmxremote.port=0 \
              -Dlogback.configurationFile=configs/dev.logback.xml \
              -XX:+UseG1GC -XX:+AggressiveOpts -Xms8m -Xmx16m -Dfile.encoding="UTF-8" -server \
              -jar target/config-service-0.0.1-SNAPSHOT.jar server configs/dev.yaml > out_err.log 2>&1
