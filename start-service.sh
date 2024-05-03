#!/bin/bash
#
#@author: jmsanchezmartagon@gmail.com
#@description: startup

function main {
  export nodename=$(uuidgen)
  echo "Node name: $nodename"
  echo "$nodename" > nodename
  java -jar $1
}

main $*
