---
layout: post
title: "More Fun With docker"
date: 2014-07-10 20:08
comments: true
categories: linux, containers, docker, administration, CI 
---
I've been spending a little time trying to build a docker image build engine.  I am almost done.  I'll talk a bit about what I did, and also about some enterprise-level tools available that do the same things.
<!-- more -->
## What the heck is a 'docker image build engine'?
To build a docker image, you download or use a local docker base or 'golden image'.  You apply to that image a list of changes documented in a Dockerfile, and you create a new image which you can post to a registry.

That process can be complex.  The Dockerfile may change from release to release, and may contain complex instructions based om the release.  A system which managed the migration of that dockerfile, and allowed a roll-back to a previous build would be ideal.

## Yeah, but what's available now in enterprise to do this?
Here's a list of tools google told me about:
 
  * [geard](http://openshift.github.io/geard/index.html)
  * [packer](http://www.packer.io/docs/builders/docker.html)
  * [jenkis plug-in](https://wiki.jenkins-ci.org/display/JENKINS/Docker+Plugin)

also this is a container management system we might want to look into:

  * [apache mesos](http://mesos.apache.org/)

## OK Nerd, show us what you did!
Here's what I have so far:

```bash
#!/bin/bash

# get_archived_artifacts
# Jason Michael                     6/30/2014
#
# requires: alias jsudo="/project/admin/bin/sudo"
# requires: yum -y install unzip
# requires: yum -y install git
# requires: yum --enablerepo=epel6-rhel6-x86_64 -y install docker-io
# requires: vi /etc/sysconfig/docker <- http proxy (see requires/docker)
# requires: jsudo docker pull centos
# visibility: ssh example.com ls /opt/jenkins-home/jobs/cem-master-unit-tests/builds/lastSuccessfulBuild/archive/cem-web-app/distribution/target/
# assumes: ssh key exchange for current user to grab artifacts from jenkins server
# accomplishes: grabs CEM, tomcat, jdk various sources and places in TODO: /somewhere/

set +o posix 

WORKSPACE_PATH="/tmp/CEM/workspace"
DOCKERFILE_PATH="/tmp/CEM/dockerfiles"
ARTIFACT_PATH="/tmp/CEM/artifacts"
tmp_dockerfile="dockerfile$(date +'%m-%d-%Y_%H-%M-%S')"
DOCKERFILE_GOLDEN_IMAGE="centos"
DOCKERFILE_HEADER="Dockerfile"
DOCKERFILE_MAINTAINER="Jason Michael email: jason.michael@sabre.com"
DOCKER_IMAGE_NAME="cem_$DOCKERFILE_GOLDEN_IMAGE"
REGISTRY_HOST_PORT="localhost:5000"
SUDO="/project/admin/bin/sudo"

# setup archive
# archive may need rotation since we'll grab all new builds
rm -rf $WORKSPACE_PATH
mkdir -p /tmp/CEM/
mkdir -p $WORKSPACE_PATH
mkdir -p $DOCKERFILE_PATH
mkdir -p $ARTIFACT_PATH

# test environment
# ensure docker
result=$($SUDO docker version)
 
   if [ $? != 0 ]; then
      printf "Error when executing command: 'docker version'.  Is docker-io installed?"
      exit 1
   fi

# ensure golden image
result=$($SUDO docker images | grep $DOCKERFILE_GOLDEN_IMAGE)

   if [ $? != 0 ]; then
      printf "Error when executing command: 'docker images'.  Has docker golden image been pulled?"
      exit 1
   fi

# ensure artifact path

cd $WORKSPACE_PATH
# TODO: figure out some way to specify which [CEM, tomcat, jdk] we're getting

# go get sources (in artifacts/sources.lst)
OLDIFS=$IFS
while IFS=, read host path file dest
do
  scp $host:$path/$file .
  unzip $WORKSPACE_PATH/$file
  rm -f $WORKSPACE_PATH/$file
done < <(grep -v '#' $ARTIFACT_PATH/system/sources.csv)

# go get tomcat

# go get jdk

# prep Dockerfile
cd $DOCKERFILE_PATH
echo "TMP dockerfile: $tmp_dockerfile"
touch $tmp_dockerfile
cat <<EOF > $DOCKERFILE_PATH/$tmp_dockerfile

#################################################################
# $DOCKERFILE_HEADER 
#################################################################

# set base image to $DOCKERFILE_GOLDEN_IMAGE

FROM $DOCKERFILE_GOLDEN_IMAGE

MAINTAINER $DOCKERFILE_MAINTAINER

# add sys artifacts to docker
ADD envs.lst /tmp/envs.lst

RUN source /tmp/envs.lst

# install necessary RPMs
#RUN yum makecache
#RUN yum -y install unzip
#RUN yum -y install bash

# adding CEM files to docker
EOF

cd $WORKSPACE_PATH
for file in $(ls)
do
  case "$file" in
    web-app)
      echo "moving $file to docker"
      echo "ADD $file /opt/cem/$file" >> $DOCKERFILE_PATH/$tmp_dockerfile
    ;;
            
    *)
      echo "file is $file.  Ignore"
  esac
done  

# massage config files 

# setup docker build
cp $DOCKERFILE_PATH/$tmp_dockerfile $WORKSPACE_PATH/Dockerfile
cp $ARTIFACT_PATH/system/envs.lst $WORKSPACE_PATH/

# build docker image
cd $WORKSPACE_PATH;
$SUDO docker build -t $DOCKER_IMAGE_NAME .

# test docker for app, tomcat, jdk static test
# run test for tomcat, jdk

# publish docker
# TODO: build a non-localhost docker registry to publish to
$SUDO docker tag $DOCKER_IMAGE_NAME $REGISTRY_HOST_PORT/$DOCKER_IMAGE_NAME 
$SUDO docker push $REGISTRY_HOST_PORT/$DOCKER_IMAGE_NAME || echo "Oops!  Unable to push to $REGISTRY_HOST_PORT.  Try docker push $REGISTRY_HOST_PORT/$DOCKER_IMAGE_NAME later"
```

## Future enhancements:
Obviously, a lot of decoupling is in order.  *And* there's a lot of stubbs in there, like the case statement for dealing with artifacts in the middle of the script.  There needs to be a way to tell this script to deal with these files from the _outside_. This is a big clusterfuck of config, and operations, and build.  But the goal is important: to actively build a docker container from dispirate sources with configurable steps.  That being said, a rails gui in front of this which allows the user to select the golden image, the apps to install, the sources to add (along with their url's) would be sweet.  Like this: [rove](http://rove.io/?pattern=rails), but for docker.

<!-- 
see https://github.com/Shopify/liquid/wiki/Liquid-for-Designers for stuff
# H1
## H2
[I'm an inline-style link](https://www.google.com)
![alt text](https://github.com/adam-p/markdown-here/raw/master/src/common/images/icon48.png 'Logo Title Text 1')
```javascript
var s = 'JavaScript syntax highlighting';
alert(s);
```
| Tables        | Are           | Cool  |
| ------------- |:-------------:| -----:|
| col 3 is      | right-aligned | $1600 |
-->
