#!/bin/bash

wget https://download.java.net/java/early_access/loom/5/openjdk-19-loom+5-429_linux-x64_bin.tar.gz
tar -zxvf openjdk-19-loom+5-429_linux-x64_bin.tar.gz
export LOOM_JDK=jdk-19
eval "$LOOM_JDK/bin/java -version"
echo "LOOM_JDK=$LOOM_JDK" >> $GITHUB_ENV
