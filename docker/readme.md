## How to start building a Docker image from a Docker file and run it in a container

Before using our own Dockerfile let's run the following in order to be able to validate that Java 7 will be actually installed by our Dockerfile:

* docker run -t -i ubuntu:14.04 /bin/bash
* execute `java -version` You should het a `command not found` error (exactly what we expect since that is just a fresh Ubuntu box with no applications installed).
* type `exit` or `Ctrl-D`

Just for curiosity now run:
* docker ps -a (shows all the containers both running or previously runned)
* docker images (shows all images created in your local laptop)


Now let's build your own image from our Docker file:

* cd to the folder *hbc-microservice-template/docker* (where the Dockerfile is located)
* `docker build -t <your-username-in-dockerhub>/<reponame>:<tag> .` Notes: 

	* You don't have to have created a DockerHub account already. Username and repo name will become relevant when in the future we will share this image on DockerHub;
	* the dot character at the end is important: it tells to docker where it should expect to find the Dockerfile;
	* Docker will read the Dockerfile and will build an image according to the instruction contained in it;
	* This operation might take a while;
	* Example: docker build -t fabiocognigni/hbc-microservice-template:v1.1 .

* execute `docker images` to double check the new image has been created.

* execute `docker run -t -i <your-username-in-dockerhub>/<reponame>:<tag> /bin/bash` .
This command: 

	* creates a running container out of that image; 
	* execute the command `bin/bash`
	* provide a standard input console to intercat with it.

* execute: `java -version` . Success!!
