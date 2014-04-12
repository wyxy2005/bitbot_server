@echo off
@title Server Console
set CLASSPATH=.;dist/BitBot.jar;../dist/simple-xml-2.7.1.jar;../dist/simple-5.1.5.jar;../dist/json-simple-1.1.1.jar;../dist/commons-codec-1.6.jar;../dist/sqljdbc4.jar"
java -server -Djsse.enableCBCProtection=false -XX:ThreadStackSize=256k -XX:+UseParallelGC -XX:+TieredCompilation -XX:+HeapDumpOnOutOfMemoryError -Xms10m -Xmx14000m -Djava.library.path=sqljdbc_4.0/enu/auth/x64 bitbot.Main readMT4Data BTCUSD1440.csv
pause