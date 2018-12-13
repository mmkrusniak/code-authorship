#!/bin/bash
mvn exec:java -Dexec.mainClass="com.github.compling.App"
cd ..
mvn exec:java -Dexec.mainClass="com.github.compling.App"