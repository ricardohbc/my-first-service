#!/bin/sh
HOST=$(hostname --ip-address)
PORT=XXXX
consul-template -consul $CONSUL_HOST -template "/opt/micro-service-0.1/conf/microservice-application.ctmpl:/opt/micro-service-0.1/conf/microservice-application.conf" -once; curl -X PUT -d "server $HOST:$PORT max_fails=2 fail_timeout=15s;" http://$CONSUL_HOST/v1/kv/$ENV\_XXX\_upstream/$HOST
consul-template -consul $CONSUL_HOST -template "/opt/newrelic/newrelic.ctmpl:/opt/newrelic/newrelic.yml" -once
