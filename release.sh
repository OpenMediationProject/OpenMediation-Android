#!/usr/bin/env bash

buildType='debug'
rm adapter_host/host-online-plugin/src/main/assets/**
cd adapter_plugin/plugin-manager
gradle clean
gradle build

cd "build/outputs/apk/$buildType"
managerApk=`ls *.apk`
managerVer=${managerApk##*/}
managerVer=${managerVer%.*}
managerVersion=${managerVer##*-}

echo "版本号是: ${managerVer}"

managerZip="${managerVer}.zip"

path=`pwd`
echo "${path}"

zip "../../../../../../adapter_host/host-online-plugin/src/main/assets/${managerZip}" "${managerApk}"

cd ../../../../../plugin-main

gradle clean
gradle packageAllPlugin
mainZip=`ls ../../build/plugin-main-${buildType}*.zip`
cp -f ${mainZip}  ../../adapter_host/host-online-plugin/src/main/assets

mainZip=${mainZip##*/}
mainVersion=${mainZip%.*}
mainVersion=${mainVersion##*-}



cd ../..

md5val=$(md5 adapter_host/host-online-plugin/src/main/assets/${managerZip} 2>&1)
managerMd5=${md5val##*= }

echo "managerMd5:${managerMd5}"

md5val=$(md5 adapter_host/host-online-plugin/src/main/assets/${mainZip} 2>&1)
mainMd5=${md5val##*= }
echo "mainMd5:${mainMd5}"

sed -i "" "s#^manager.version=.*#manager.version=\"${managerVersion}\"#g"  extend.properties
sed -i "" "s#^manager.fileName=.*#manager.fileName=\"${managerZip}\"#g"  extend.properties
sed -i "" "s#^manager.md5=.*#manager.md5=\"${managerMd5}\"#g"  extend.properties

sed -i "" "s#^main.version=.*#main.version=\"${mainVersion}\"#g"  extend.properties
sed -i "" "s#^main.fileName=.*#main.fileName=\"${mainZip}\"#g"  extend.properties
sed -i "" "s#^main.md5=.*#main.md5=\"${mainMd5}\"#g"  extend.properties




