#!/bin/bash

echo 'Refreshing services'
vagrant ssh << EOF
cd /vagrant
sudo docker-compose stop hbc-microservice-template
yes | sudo docker-compose rm
sudo docker-compose build hbc-microservice-template
service docker-compose start hbc-microservice-template
EOF
exit
echo 'Done'