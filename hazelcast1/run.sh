#!/bin/sh

ip addr add 127.0.0.1/32 dev lo
ip link set dev lo up

socat VSOCK-LISTEN:5701,reuseaddr,fork TCP-CONNECT:127.0.0.1:5701 &
socat TCP4-LISTEN:5702,reuseaddr,fork VSOCK-CONNECT:3:5702 &
socat TCP4-LISTEN:5703,reuseaddr,fork VSOCK-CONNECT:3:5703 &

hz start
