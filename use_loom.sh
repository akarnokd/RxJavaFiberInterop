#!/bin/bash

wget https://download.java.net/java/early_access/loom/7/openjdk-17-loom+7-342_linux-x64_bin.tar.gz
tar -zxvf openjdk-17-loom+7-342_linux-x64_bin.tar.gz
export LOOM_JDK=jdk-17
eval "$LOOM_JDK/bin/java -version"
echo "LOOM_JDK=$LOOM_JDK" >> $GITHUB_ENV
