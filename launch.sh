#!/bin/sh
export CLASSPATH=".:dist/BitBot.jar:dist/simple-xml-2.7.1.jar:dist/simple-5.1.5.jar:dist/json-simple-1.1.1.jar:dist/commons-codec-1.6.jar"
java -server -XX:ThreadStackSize=256k -XX:+UseParallelGC -XX:+TieredCompilation -XX:+HeapDumpOnOutOfMemoryError \
bitbot.Main z-startparam-z