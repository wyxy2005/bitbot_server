@echo off
@title Channel Server Console
set CLASSPATH=.;dist\*;
java -server -Djsse.enableCBCProtection=false -XX:ThreadStackSize=256k -XX:+UseParallelGC -XX:+TieredCompilation -XX:+HeapDumpOnOutOfMemoryError -Xms100m -Xmx3000m  -Djava.security.policy=RMI_IPCheck.policy -Djava.library.path=sqljdbc_4.0/enu/auth/x64 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.ssl=true -Dcom.sun.management.jmxremote.authenticate=true bitbot.Main start server_main.properties
pause