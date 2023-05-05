#!/usr/bin/env bash

branchName=$1

#Checkout specific branch pf  camunda bpmn definition
git clone https://github.com/hmcts/wa-task-configuration-template.git
cd wa-task-configuration-template

echo "Switch to ${branchName} branch on wa-task-configuration-template"
git checkout ${branchName}
cd ..

#Copy camunda folder which contains dmn files
cp -r ./wa-task-configuration-template/src/main/resources .
rm -rf ./wa-task-configuration-template

./bin/import-dmn-diagram.sh . wa wa
