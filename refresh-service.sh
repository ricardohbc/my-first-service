#!/bin/bash

artifactName = hbc-microservice-template-0.1.zip

if ! [ -a target/universal/${artifactName} ]  ; then
	echo "Building the service distribution ZIP from sources..."
	sbt dist
fi

cp target/universal/${artifactName} docker/

echo 'Refreshing service in the Vagrant box ...'
vagrant ssh << EOF
cd /vagrant
sudo docker-compose stop currentservice
yes | sudo docker-compose rm
sudo docker-compose build currentservice
service docker-compose start currentservice
exit
EOF
echo 'Done'

rm docker/${artifactName}
