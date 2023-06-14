#!/bin/bash

wget https://download.java.net/java/early_access/jdk21/26/GPL/openjdk-21-ea+26_linux-x64_bin.tar.gz
tar -zxvf openjdk-21-ea+26_linux-x64_bin.tar.gz
export LOOM_JDK=jdk-21
eval "$LOOM_JDK/bin/java -version"
echo "LOOM_JDK=$LOOM_JDK" >> $GITHUB_ENV
