# corda-docker
Docker configuration and scripts to create a Corda image. This docker image is based on [Phusion's base-image](https://github.com/phusion/baseimage-docker) - Docker optimised Ubuntu 16.04 LTS.


## Usage

* Check Dockerfile (e.g. to adjust version or Expose ports)
* `docker build -t corda:1.0 . ` - to create base Corda image (called _corda:1.0_)
* `docker create --env CORDA_CITY=Wroclaw --env CORDA_COUNTRY=PL --env CORDA_LEGAL_NAME="Very important node" --name corda1.0 -t corda:1.0` - to create configured container based on above (_corda:1.0_) image and called _corda1.0_
* `docker start corda1.0` - to start the _corda1.0_ container
* `docker exec -t -i corda1.0 bash` - to log in to the container


## Node configuration
Corda image can be configured with the following environment variables as seen in the Usage example above. Table below lists all available variables as well as default values.


### Environment Variables

Docker environment variable | Corda configuration | default value
--- | --- | ---
CORDA_HOST | hostname for Artemis |  localhost
CORDA_PORT_P2P | P2P port |10002
CORDA_PORT_RPC | RPC port |10003
CORDA_LEGAL_NAME | common name for myLegalName| Corda Test Node
CORDA_ORG | organisation  for myLegalName | CordaTest
CORDA_ORG_UNIT | organizational unit for myLegalName | CordaTest
CORDA_COUNTRY | country for myLegalName | GB
CORDA_CITY | City for myLegalName and nearestCity | London
CORDA_EMAIL | emailAddress | admin@corda.test
JAVA_OPTIONS | option for JVM | -Xmx512m
JAVA_CAPSULE | option passed to capsule | '' (empty string)

### Java Options

With docker environment you can not only control Corda node set up but also pass Java specific variables. There are Docker variables controlling Java behaviour. The first one - **JAVA_OPTIONS** passes options for JVM. The default option is to start Corda with 512 MB heap memory (`-Xmx512m`). If you need to pass variable to inside Corda capsule, use **JAVA_CAPSULE**.
