#!/bin/bash

export CORDA_HOST="${CORDA_HOST:-localhost}"
export CORDA_PORT_P2P="${CORDA_PORT:-10002}"
export CORDA_PORT_RPC="${CORDA_PORT:-10003}"
export CORDA_LEGAL_NAME="${CORDA_LEGAL_NAME:-Corda Test Node}"
export CORDA_ORG="${CORDA_ORG:-CordaTest}"
export CORDA_ORG_UNIT="${CORDA_ORG_UNIT:-CordaTest}"
export CORDA_COUNTRY="${CORDA_COUNTRY:-GB}"
export CORDA_CITY="${CORDA_CITY:-London}"
export CORDA_EMAIL="${CORDA_EMAIL:-admin@corda.test}"
export JAVA_OPTIONS="${JAVA_OPTIONS--Xmx512m}"
export JAVA_CAPSULE="${JAVA_CAPSULE-''}"

cd /opt/corda

cat > node.conf << EOF
basedir : "/opt/corda"
p2pAddress : "$CORDA_HOST:$CORDA_PORT_P2P"
rpcAddress : "$CORDA_HOST:$CORDA_PORT_RPC"
h2port : 11000
myLegalName : "O=${CORDA_ORG}, L=${CORDA_CITY}, C=${CORDA_COUNTRY}"
keyStorePassword : "cordacadevpass"
trustStorePassword : "trustpass"
extraAdvertisedServiceIds: [ "" ]
useHTTPS : false
devMode : true
rpcUsers=[
    {
        user=corda
        password=corda_initial_password
        permissions=[
            StartFlow.net.corda.flows.CashIssueFlow,
            StartFlow.net.corda.flows.CashExitFlow,
            StartFlow.net.corda.flows.CashPaymentFlow
        ]
    }
]
EOF

chown corda:corda node.conf
exec /sbin/setuser corda java $JAVA_OPTIONS -Dcapsule.jvm.args="$CAPSULE_ARGS" -jar /opt/corda/corda.jar >>/opt/corda/logs/output.log 2>&1
