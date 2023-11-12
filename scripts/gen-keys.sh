#!/usr/bin/env sh

openssl genpkey -algorithm RSA -out src/main/resources/jwt.key -pkeyopt rsa_keygen_bits:4096
openssl rsa -in src/main/resources/jwt.key -pubout -out src/main/resources/jwt.pem
