# LibTomMath for iOS SRP arithmetic

The native build resolves the official [LibTomMath 1.3.0 release](https://github.com/libtom/libtommath/releases/tag/v1.3.0)
as a Gradle build dependency. License: [Unlicense](LICENSE).

- Artifact: `https://github.com/libtom/libtommath/releases/download/v1.3.0/ltm-1.3.0.zip`
- SHA-256: `1edd7a4382a9bfea79ff6cbe5b898380dd1f64d13f9b3e18aaa8987c83da29fd`
- Upstream commit: `95d80fd8229d05dd6cb4ec88bc8d4f5377ff00ef`

`gradle/ios-srp-native.gradle` verifies the checksum before extracting the
unmodified C sources and headers into the module's build directory. It generates
identifier prefixes there to avoid collisions with other embedded LibTomMath
copies. Neither upstream sources nor generated prefixes are checked in.
The first native library build requires network access; Gradle caches the ZIP
and subsequent builds support `--offline`. Android/JVM compilation does not need it.

`../../c/jump_srp_config.h` enables modular exponentiation, byte conversion,
initialization, cleanup, and their upstream dependencies. Fixed multiplication
cutoffs avoid global mutable tuning parameters.

`../../build-ios.sh` compiles this component with `-O2` for each iOS architecture,
including when Kotlin is built in Debug. The archive is embedded in the cinterop
KLIB. Published SDK consumers need no source download, C dependency, or compiler
configuration; source resolution happens only when building this library.

This is the upstream variable-window modular exponentiation implementation; it
is not a new constant-time SRP implementation. The prior Kotlin implementation
also branches on exponent bits. SRP randomness, input validation, hashing, HKDF,
and signature formatting remain in the existing shared code.

The allocator hooks use volatile writes before freeing heap buffers, including
buffers replaced during reallocation. They do not reach LibTomMath's fixed-size
stack scratch arrays (`W[MP_WARRAY]` in the fast multiply, square and Montgomery
paths), and the Kotlin `BigInteger` values for SRP secrets stay in managed memory.
The C archive uses Kotlin 1.9.20's default deployment targets (iOS 12, or iOS 14
for the ARM64 simulator).

`./gradlew :amplifyframework:testNativeSrp` runs optimized host C tests under
AddressSanitizer and UndefinedBehaviorSanitizer. These also run with the
`iosSimulatorArm64Test` and `iosX64Test` tasks. They check clearing before free,
reallocation growth/shrink/failure, and the real C bridge + LibTomMath against an
independent integer reference, including output-capacity and invalid-input cases.
Host tests require Clang and fetch the same checksum-pinned source archive on
Linux or macOS. Android compilation and unit tests do not need native sources.
