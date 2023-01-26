#!/usr/bin/env sh

openssl genpkey -algorithm RSA -out src/main/resources/private-key.pem -pkeyopt rsa_keygen_bits:4096
openssl rsa -in src/main/resources/private-key.pem -pubout -out src/main/resources/public-key.pem
