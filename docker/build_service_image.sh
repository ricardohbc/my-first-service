#!/usr/bin/env bash

echo "Building Distribution ZIP for service..."
if ! [ -a ../target/universal/service-0.1.zip ]  ; then
	echo "Distribution ZIP not found, building from source..."
	cd ../ && sbt "project service" dist
	cd docker
fi

echo "Copying service ZIP to current directory..."
cp ../target/universal/service-0.1.zip .

tag=hbcdigital/service:service-0.1

echo "Building service Docker Container.."
sudo docker build -t ${tag} .

echo "Removing ZIP..."
rm service-0.1.zip

echo "Completed building image: $tag"

