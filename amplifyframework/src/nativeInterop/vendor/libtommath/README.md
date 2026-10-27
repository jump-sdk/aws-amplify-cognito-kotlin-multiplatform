# LibTomMath for iOS SRP arithmetic

Upstream: https://github.com/libtom/libtommath, tag `v1.3.0`, commit
`95d80fd8229d05dd6cb4ec88bc8d4f5377ff00ef`. License: [Unlicense](LICENSE).

`libtommath.c` concatenates the unmodified upstream root C files in filename
order. The headers are unmodified. No download occurs during a build.
Regenerate from a clean checkout of that commit:

```sh
python3 tools/vendor-libtommath.py /path/to/libtommath
```

The generated `../../c/jump_srp_config.h` enables only modular exponentiation,
byte conversion, initialization, cleanup, and their upstream dependencies. It
prefixes identifiers to avoid collisions with other embedded LibTomMath copies.
Fixed upstream multiplication cutoffs avoid global mutable tuning parameters.

`../../build-ios.sh` compiles this component with `-O2` for each iOS architecture,
including when Kotlin is built in Debug. The archive is embedded in the cinterop
KLIB, so consumers need no separate C dependency or compiler configuration.

This is the upstream variable-window modular exponentiation implementation; it
is not a new constant-time SRP implementation. The prior Kotlin implementation
also branches on exponent bits. SRP randomness, input validation, hashing, HKDF,
and signature formatting remain in the existing shared code.
