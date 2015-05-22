## How to run the service

### Assumptions

* (Boot2docker and) Docker installed: [https://docs.docker.com/installation/mac/](https://docs.docker.com/installation/mac/)
* Play (Activator) installed: [https://www.playframework.com/documentation/2.2.x/Installing](https://www.playframework.com/documentation/2.2.x/Installing)
* git clone https://github.com/saksdirect/HBCSimulators.git

### Set up (one-time ONLY IF YOU WANT TO USE PORT FORWARDING)

* cd docker
* ./set-port-forward-oms.sh (be sure docker VM is poweroff by running `boot2docker status` and then `boot2docker stop`)
* `boot2docker up` 
* export the docker related environment variables (`DOCKER_HOST`, `DOCKER_CERT_PATH`, `DOCKER_TLS_VERIFY`) as instructed by the output of the command above (or set them permanently in your `~/.profile`)

### Boot Up 'boot2docker'
```sh
boot2docker up
```

Export any applicable environment variables, for example:
```sh
export DOCKER_TLS_VERIFY=1  
export DOCKER_HOST=tcp://192.168.59.103:2376  
export DOCKER_CERT_PATH=/Users/mikeroth/.boot2docker/certs/boot2docker-vm  
```

### Create/Run Service Image
The single command line argument can be ONE of the following services:
* toggle

To build the service image:  
```sh
./build_service_image.sh {service}
```

To start the service container:  
```sh
./start-service.sh {service}
```

To stop the service container:  
```sh
./stop-service.sh {service}
```

### Tag and Push Service Images to Internal Docker Registry

```sh
./tag_and_push.sh {service}
```

### Build JVM Base Image (Centos 6.6)

**NOTE**: The 'build_service_image.sh' will build the JVM Base image if it does not already exist, but if you would like to build it standalone you may do so via the following steps.  

To build the JVM Base image:  
```sh
./build-jvm-base-image-centos.sh
```

To start the JVM Base container:  
```sh
./start-jvm-base-centos.sh
```

To stop the JVM Base container:  
```sh
./stop-jvm-base-centos.sh
```

To tag and push JVM Base image to Internal Docker Registry:
```sh
./tag_and_push.sh
```
