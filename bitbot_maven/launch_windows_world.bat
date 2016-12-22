@echo off
@title World Server Console
set CLASSPATH=.;dist\*;
java -server -Djsse.enableCBCProtection=false -XX:ThreadStackSize=256k -XX:+UseParallelGC -XX:+TieredCompilation -XX:+HeapDumpOnOutOfMemoryError -Xms10m -Xmx1400m -Djava.security.policy=RMI_IPCheck.policy -Djava.library.path=sqljdbc_4.0/enu/auth/x64 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.ssl=true -Dcom.sun.management.jmxremote.authenticate=true bitbot.Main startWorld server_main.properties
pause