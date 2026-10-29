# LibTomMath for iOS SRP arithmetic

The iOS build resolves the official [LibTomMath 1.3.0 release](https://github.com/libtom/libtommath/releases/tag/v1.3.0)
as a Gradle build dependency. License: [Unlicense](LICENSE).

- Artifact: `https://github.com/libtom/libtommath/releases/download/v1.3.0/ltm-1.3.0.zip`
- SHA-256: `1edd7a4382a9bfea79ff6cbe5b898380dd1f64d13f9b3e18aaa8987c83da29fd`
- Upstream commit: `95d80fd8229d05dd6cb4ec88bc8d4f5377ff00ef`

`gradle/ios-srp-native.gradle` checks the SHA-256, extracts the sources into the
build directory, and generates a header that prefixes LibTomMath's symbols, so
another LibTomMath copy in the same app can't replace ours at link time.
`../../build-ios.sh` builds it with the upstream makefile for each iOS target and
adds the `jump_srp.c` bridge. `../../c/jump_srp_config.h` limits the build to what
`mp_exptmod` needs. The archive is embedded in the cinterop KLIB, so SDK consumers
need no download, C toolchain or setup. The first library build needs network
access; after that, Gradle's cache works offline.

`mp_exptmod` is not constant time, and neither was the prior Kotlin code.
LibTomMath's default zero-on-free clears heap digit buffers. Buffers that `realloc`
replaces, its stack scratch arrays and the Kotlin `BigInteger` secrets are not
cleared.
