@echo off
@title Channel Server Console
set CLASSPATH=.;dist/BitBot.jar;../dist/ta-lib.jar;../dist/mina-core-2.0.9.jar;../dist/slf4j-api-1.7.7.jar;../dist/slf4j-jdk14-1.7.7.jar;../dist/slf4j-api-1.7.7.jar;../dist/simple-xml-2.7.1.jar;../dist/simple-5.1.5.jar;../dist/commons-lang3-3.4.jar;../dist/json-simple-1.1.1.jar;../dist/commons-codec-1.6.jar;../dist/sqljdbc41.jar"
java -server -Djsse.enableCBCProtection=false -XX:ThreadStackSize=256k -XX:+UseParallelGC -XX:+TieredCompilation -XX:+HeapDumpOnOutOfMemoryError -Xms10m -Xmx14000m -Djava.security.policy=RMI_IPCheck.policy -Djava.library.path=sqljdbc_4.0/enu/auth/x64 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.ssl=true -Dcom.sun.management.jmxremote.authenticate=true bitbot.Main start
pause