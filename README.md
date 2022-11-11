# Hazelcast in Nitro Enclave PoC

## vsock

[vsock(7) — Linux manual page](https://man7.org/linux/man-pages/man7/vsock.7.html)

       The VSOCK address family facilitates communication between
       virtual machines and the host they are running on.  This address
       family is used by guest agents and hypervisor services that need
       a communications channel that is independent of virtual machine
       network configuration.

32-bit CID (Context Identifier) is alternative to IP-address

## Prepare EC2 instances

```bash
aws ec2 run-instances ... --enclave-options 'Enabled=true'
```

### Install Nitro CLI and socat

https://docs.aws.amazon.com/enclaves/latest/user/nitro-enclave-cli-install.html

```
sudo amazon-linux-extras enable aws-nitro-enclaves-cli
sudo yum clean metadata
sudo yum update -y
# sudo yum install -y aws-nitro-enclaves-cli
sudo yum install -y aws-nitro-enclaves-cli-devel
sudo usermod -aG ne $USER
sudo usermod -aG docker $USER
cat <<'EOT' | sudo tee /etc/nitro_enclaves/allocator.yaml
---
memory_mib: 2048
cpu_count: 2
# cpu_pool: 2,3,6-9
EOT
sudo systemctl restart nitro-enclaves-allocator.service
sudo systemctl enable nitro-enclaves-allocator.service
sudo systemctl restart docker
sudo systemctl enable docker

# the socat version in AL2 doesn't support vsocks
sudo yum install -y gcc tcp_wrappers-devel readline-devel openssl-devel

wget http://www.dest-unreach.org/socat/download/socat-1.7.4.4.tar.gz
tar xf socat-1.7.4.4.tar.gz
cd socat-1.7.4.4

./configure
make
sudo make install
```

## Build and run Hazelcast in Nitro Enclave

Proxy vsock connections on the parent instance:

```
killall socat
socat VSOCK-LISTEN:5701,reuseaddr,fork TCP4-CONNECT:172.31.45.242:5701 >/tmp/socat-vsock-5701.log 2>&1 &
socat VSOCK-LISTEN:5702,reuseaddr,fork TCP4-CONNECT:172.31.34.106:5702 >/tmp/socat-vsock-5702.log 2>&1 &
socat TCP4-LISTEN:5701,reuseaddr,fork VSOCK-CONNECT:21:5701 >/tmp/socat-tcp4-5701.log 2>&1 &
socat TCP4-LISTEN:5702,reuseaddr,fork VSOCK-CONNECT:22:5702 >/tmp/socat-tcp4-5702.log 2>&1 &
```

Build EIF and run enclave:

```
INSTANCENR=1
nitro-cli build-enclave --docker-dir hazelcast${INSTANCENR} --docker-uri hazelcast${INSTANCENR}:latest --output-file hazelcast${INSTANCENR}.eif
nitro-cli run-enclave --cpu-count 2 --memory 2000 --eif-path hazelcast${INSTANCENR}.eif --enclave-cid 2${INSTANCENR} --debug-mode
```
