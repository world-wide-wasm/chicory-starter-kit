#!/bin/bash
tinygo build -scheduler=none --no-debug \
  -o demo.wasm \
  -target=wasi -panic=trap -scheduler=none main.go

ls -lh *.wasm
