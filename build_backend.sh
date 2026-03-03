#!/bin/bash
source ~/.sdkman/bin/sdkman-init.sh
source ~/.nvm/nvm.sh
export JAVA_HOME="$HOME/.sdkman/candidates/java/current"
export PATH="$JAVA_HOME/bin:$PATH"
java -version 2>&1
mvn -version 2>&1
node -v 2>&1
cd /home/praolo/Downloads/IDSD/sentinel-distributed-siem
./mvnw clean install -DskipTests 2>&1
