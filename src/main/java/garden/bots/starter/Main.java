package garden.bots.starter;

import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.wasm.types.Value;
import com.dylibso.chicory.runtime.Module;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Memory;
import com.dylibso.chicory.runtime.HostImports;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.wasm.types.ValueType;


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/*
mvn compile exec:java -Dexec.mainClass="garden.bots.starter.Main"
mvn compile exec:java
 */
public class Main {
    public static void main(String[] args) {

        // Check environment variables
        var wasmFileLocalLocation = Optional.ofNullable(System.getenv("WASM_FILE")).orElse("./demo-plugin/demo.wasm");
        var wasmFunctionName = Optional.ofNullable(System.getenv("FUNCTION_NAME")).orElse("hello");

        // Create fd_write method to avoid this warning message:
        // "WARNING: Could not find host function for import number: 0 named wasi_snapshot_preview1.fd_write"
        
        var fd_write = new HostFunction(
                (Instance instance, Value... params) -> {
                    return null;
                },
                "wasi_snapshot_preview1",
                "fd_write",
                List.of(ValueType.I32, ValueType.I32),
                List.of());

        var imports = new HostImports(new HostFunction[] {fd_write});

        Module module = Module.builder(new File(wasmFileLocalLocation)).build().withHostImports(imports);
        Instance instance = module.instantiate();

        // automatically exported by TinyGo
        ExportFunction malloc = instance.export("malloc");
        ExportFunction free = instance.export("free");

        ExportFunction pluginFunc = instance.export(wasmFunctionName);
        Memory memory = instance.memory();

        var param = "Bob Morane";

        int len = param.getBytes().length;
        // allocate {len} bytes of memory, this returns a pointer to that memory
        int ptr = malloc.apply(Value.i32(len))[0].asInt();
        // We can now write the message to the module's memory:
        memory.writeString(ptr, param);

        // Call the wasm function
        Value result = pluginFunc.apply(Value.i32(ptr), Value.i32(len))[0];
        free.apply(Value.i32(ptr), Value.i32(len));

        // Extract position and size from the result
        int valuePosition = (int) ((result.asLong() >>> 32) & 0xFFFFFFFFL);
        int valueSize = (int) (result.asLong() & 0xFFFFFFFFL);

        byte[] bytes = memory.readBytes(valuePosition, valueSize);
        String strResult = new String(bytes, StandardCharsets.UTF_8);

        System.out.println(strResult);

    }
}