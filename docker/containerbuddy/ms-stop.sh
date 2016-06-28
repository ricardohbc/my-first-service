#!/bin/sh
HOST=$(hostname --ip-address)
PORT=XXXX
curl -X DELETE http://$CONSUL_HOST/v1/kv/$ENV\_XXX\_upstream/$HOST
