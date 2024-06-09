// Package main
package main

import "unsafe"

// Read memory
func readBufferFromMemory(bufferPosition *uint32, length uint32) []byte {
	subjectBuffer := make([]byte, length)
	pointer := uintptr(unsafe.Pointer(bufferPosition))
	for i := 0; i < int(length); i++ {
		s := *(*int32)(unsafe.Pointer(pointer + uintptr(i)))
		subjectBuffer[i] = byte(s)
	}
	return subjectBuffer
}

// Copy data to memory 
func copyBufferToMemory(buffer []byte) uint64 {
	bufferPtr := &buffer[0]
	unsafePtr := uintptr(unsafe.Pointer(bufferPtr))

	pos := uint32(unsafePtr)
	size := uint32(len(buffer))

	return (uint64(pos) << uint64(32)) | uint64(size)
}

// hello function
//go:wasm-module main
//export hello
func hello(valuePosition *uint32, length uint32) uint64 {
  
  // read the memory to get the argument(s)
  valueBytes := readBufferFromMemory(valuePosition, length)

  message := "👋 Hello " + string(valueBytes) + " 😃"

  // copy the value to memory
  // get the position and the size of the buffer (in memory)
  posSize := copyBufferToMemory([]byte(message))

  // return the position and size
  return posSize

}

func main() {}
