#!/bin/bash

wget https://download.java.net/java/early_access/loom/20/openjdk-20-loom+20-40_linux-x64_bin.tar.gz
tar -zxvf openjdk-20-loom+20-40_linux-x64_bin.tar.gz
export LOOM_JDK=jdk-20
eval "$LOOM_JDK/bin/java -version"
echo "LOOM_JDK=$LOOM_JDK" >> $GITHUB_ENV
