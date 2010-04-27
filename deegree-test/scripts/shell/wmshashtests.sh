#!/bin/bash

. ~/.bashrc

service update hashtest

cd $HOME/workspace/d3_test/
ant

runtoold3 org.deegree.test.services.hash.HashValidator http://localhost:8080/hashtest/services src/org/deegree/test/services/wms/hash/

killTom
