#!/bin/bash

wget https://download.java.net/java/early_access/loom/2/openjdk-18-loom+2-74_linux-x64_bin.tar.gz
tar -zxvf openjdk-18-loom+2-74_linux-x64_bin.tar.gz
export LOOM_JDK=jdk-18
eval "$LOOM_JDK/bin/java -version"
echo "LOOM_JDK=$LOOM_JDK" >> $GITHUB_ENV
