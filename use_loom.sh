#!/bin/bash

wget https://download.java.net/java/early_access/loom/9/openjdk-16-loom+9-316_linux-x64_bin.tar.gz
tar -zxvf openjdk-16-loom+9-316_linux-x64_bin.tar.gz
export LOOM_JDK=jdk-16
eval "$LOOM_JDK/bin/java -version"
echo "LOOM_JDK=$LOOM_JDK" >> $GITHUB_ENV
