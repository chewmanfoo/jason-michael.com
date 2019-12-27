---
layout: post
title: "virtual home"
date: 2019-08-11 21:46
comments: true
categories: 
---
I bought a Dell Precision Workstation with dual xeon's and added 64 gigs of RAM.  I intend to turn it into a VM host using
KVM (it's running CentOS 7.x).  I'm struggling with this because it seems every thing I try, I find issues I have to trouble
shoot, and spending all day troubleshooting and not building is a real pain. 
<!-- more -->

## Automation

I created a 'build a VM' script, containing the following:

```bash
# /setup/deploy_vm.bash

source /setup/lib.bash

if [ $# -eq 0 ]; then
    echo "No arguments supplied!"
    echo "REQUIRED: 1 = instid [example: centos-git], 2 = insthost [example: centos-git]"
    exit 1
fi

instid="$1"
insthost="$2"
tzone="US/Central"
domain="atomic.org"
discsize="20G"
memsize="2048"

normallog "grab centos 7 cloud image"

cd /var/lib/libvirt/boot

if [ -f "CentOS-7-x86_64-GenericCloud.qcow2" ]; then
    errorlog "qcow file exists, Skipping"
else
    detaillog "qcow file does not exist.  Download it"
    wget http://cloud.centos.org/centos/7/images/CentOS-7-x86_64-GenericCloud.qcow2
fi

normallog "setup directories"

D=/var/lib/libvirt/images
VM=$instid
mkdir -vp $D/$VM

normallog "setup metadata"

cd $D/$VM

rm -f meta-data
touch meta-data

echo "instance-id: $instid" >> meta-data
echo "local-hostname: $insthost" >> meta-data

normallog "current meta-data:"

cat meta-data

normallog "setup user-data"

if [ -f ~/.ssh/id_$insthost ]; then
    errorlog "~/.ssh/id_$insthost exists.  Skipping"
else
    ssh-keygen -t ed25519 -C "VM Login ssh key foo" -f ~/.ssh/id_$insthost -P ""
fi

tmpkey=$(cat ~/.ssh/id_$insthost.pub)

cd $D/$VM

rm -f user-data
touch user-data

cat > user-data << ENDOFFILE
#cloud-config

# Hostname management
preserve_hostname: False
hostname: $insthost
fqdn: $insthost.$domain

# Users
users:
    - default
    - name: jason
      groups: ['wheel']
      shell: /bin/bash
      sudo: ALL=(ALL) NOPASSWD:ALL
      ssh-authorized-keys:
        - $tmpkey

# Configure where output will go
output:
  all: ">> /var/log/cloud-init.log"

# configure interaction with ssh server
ssh_genkeytypes: ['ed25519', 'rsa']

# Install my public ssh key to the first user-defined user configured
# in cloud.cfg in the template (which is centos for CentOS cloud images)
ssh_authorized_keys:
  - $tmpkey

# set timezone for VM
timezone: $tzone

# Remove cloud-init
runcmd:
  - systemctl stop network && systemctl start network
  - yum -y remove cloud-init
ENDOFFILE

cat user-data

normallog "Copy cloud image"
cd $D/$VM
cp /var/lib/libvirt/boot/CentOS-7-x86_64-GenericCloud.qcow2 $VM.qcow2

normallog "Create $discsize disc image"
cd $D/$VM
export LIBGUESTFS_BACKEND=direct
qemu-img create -f qcow2 -o preallocation=metadata $VM.new.image $discsize
virt-resize --quiet --expand /dev/sda1 $VM.qcow2 $VM.new.image
cd $D/$VM
mv $VM.new.image $VM.qcow2

normallog "Creating cloud-init iso"

mkisofs -o $VM-cidata.iso -V cidata -J -r user-data meta-data

normallog "Create a pool"

virsh pool-create-as --name $VM --type dir --target $D/$VM

normallog "Install VM"

cd $D/$VM
virt-install --import --name $VM \
--memory $memsize --vcpus 1 --cpu host \
--disk $VM.qcow2,format=qcow2,bus=virtio \
--disk $VM-cidata.iso,device=cdrom \
--network bridge=virbr0,model=virtio \
--os-type=linux \
--os-variant=centos7.0 \
--graphics spice \
--noautoconsole

normallog "Cleanup"

cd $D/$VM
virsh change-media $VM hda --eject --config
rm meta-data user-data centos7-vm1-cidata.iso

normallog "Get IP Address"

virsh net-dhcp-leases default
```
## Test

I tested the script a few times:



<!-- see https://github.com/Shopify/liquid/wiki/Liquid-for-Designers for stuff 
# H1
## H2
[I'm an inline-style link](https://www.google.com)
![alt text](https://github.com/adam-p/markdown-here/raw/master/src/common/images/icon48.png 'Logo Title Text 1')
```javascript
var s = 'JavaScript syntax highlighting';
alert(s);
```
   * an unordered list item (note a newline is required before the list begins)
   1. an ordered list item
| Tables        | Are           | Cool  |
| ------------- |:-------------:| -----:|
| col 3 is      | right-aligned | $1600 |
-->
