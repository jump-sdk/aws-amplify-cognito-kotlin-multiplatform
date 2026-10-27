# Cognito SRP Debug benchmark

Measured on 2026-10-26, Apple M4 Pro, Xcode 27.1 SDK, iPhone 17 Pro simulator
running iOS 26.5. All comparisons use Kotlin/Native **2.2.10**, the unoptimized
**Debug** test binary, the same SRP helper, and three fresh exchanges per variant.
Only the native C component uses `-O2`. No network calls are included.

| Backend | Public A (mean) | Challenge signature (mean) | Total crypto (mean) |
| --- | ---: | ---: | ---: |
| bignum-0.3.8 | 16.816 s | 18.023 s | 34.839 s |
| bignum-0.3.10 | 16.885 s | 18.578 s | 35.463 s |
| native | 0.319 s | 0.322 s | 0.641 s |

The bignum upgrade does not improve this workload. Native modular exponentiation
cuts total crypto time by about **54×**, to approximately **0.64 seconds**.
Raw per-iteration measurements are in [results](results/). These are simulator
microbenchmarks, not physical-device or backend sign-in measurements.

The benchmark deliberately preserves production private-exponent generation
(random 1024-byte input reduced modulo N); it does not substitute the shorter
fixed exponent used by the correctness tests. Every iteration constructs a new
helper, then measures `getPublicA` and `getSignature` separately. Setup and key
reuse cannot hide the public-key generation cost.

After switching to the checksum-pinned build dependency, the same simulator
measured 0.653 s Debug and 0.038 s Release per exchange, with all 15 tests passing
in each build. See [the validation rerun](results/ios-source-dependency-validation.txt).

## Repeat

From the fork root, choose an available simulator UDID:

```sh
./gradlew -p benchmarks iosSimulatorArm64Test -PsimulatorId=SIMULATOR_UDID -PsrpBackend=kotlin -PbignumVersion=0.3.8
./gradlew -p benchmarks iosSimulatorArm64Test -PsimulatorId=SIMULATOR_UDID -PsrpBackend=kotlin -PbignumVersion=0.3.10
./gradlew -p benchmarks iosSimulatorArm64Test -PsimulatorId=SIMULATOR_UDID -PsrpBackend=native -PbignumVersion=0.3.8
```

Use `--rerun-tasks` to repeat an unchanged variant. All variants also run the
shared correctness suite unless filtered with `--tests '*SrpBenchmark*'`.
Run sequentially without competing builds or benchmarks. The initial baseline
and upgrade runs filtered to the benchmark; native validation ran the full suite.
The normal fork build uses its existing Kotlin 1.9.20 compiler and was checked
separately for simulator tests, Android tests, and device/simulator framework links.

## Implementation and local JumpKit use

Shared SRP calls `srpModPow`; Android uses the platform `java.math.BigInteger`.
iOS calls an embedded, namespaced LibTomMath 1.3.0 archive. The C build is cached
per architecture and does not turn on optimization for the Kotlin SDK. The
archive is bundled with the cinterop KLIB for downstream linking. Gradle resolves
the official source ZIP with a pinned SHA-256; the first native build needs network
access and later builds can use the cached archive offline. See the
[native dependency notes](../amplifyframework/src/nativeInterop/vendor/libtommath/README.md).

The dependency stays on bignum 0.3.8 because upgrading it did not help. Shared
hashing, HKDF, wire formatting, and random exponent generation remain unchanged.
Tests cover known public-A, shared-secret and signature vectors, invalid server
public values, unsigned output boundaries, zero/exponent edge cases, and 120
comparisons with the Kotlin implementation, plus three independent Python-generated
production-size 3072-bit exponent vectors.

JumpKit can use this checkout through a Gradle composite build. Set this in
JumpKit's ignored `local.properties`:

```properties
cognitoSourceDir=../aws-amplify-cognito-kotlin-multiplatform
```

Or pass `-PcognitoSourceDir=/absolute/path/to/the/fork` to Gradle. Remove the
property to return to the published dependency. No package release or global
Gradle setting is needed for local development. A team rollout still requires
publishing the fork through its existing release workflow and bumping JumpKit's
catalog version.

## Validation in this checkout

- Fork Kotlin 1.9.20: all 14 shared tests pass on the iOS simulator and Android.
- Device and simulator Debug frameworks link successfully.
- Full JumpKit `:sdk:linkPodDebugFrameworkIosSimulatorArm64` succeeds with the local
  fork, and the resulting framework contains `jump_srp_mod_pow`.
- `detektAndroidRelease` passes. Android lint currently reports eight SDK/dependency
  version-freshness errors (`GradleDependency` / `NewerVersionAvailable`) on the
  unchanged compile SDK and dependency pins.
- Physical-device timing and a real Cognito backend sign-in were not performed.

## Android Debug measurements

Measured on 2026-10-26 with an isolated debuggable Android instrumentation APK
(Kotlin 2.2.10, JVM target 17, min/target SDK 26, no R8). This runs on Android ART,
not the desktop JVM. Each variant starts from a fresh APK install and measures
three new SRP helpers in one process; the first exchange is included. No network
or UI work is included. CPU frequency, background load, and JIT warmup can affect
absolute timings; per-iteration output is saved in `results/android-*.txt`.

| Device/runtime | Backend | Public A mean | Signature mean | Exchange mean |
| --- | --- | ---: | ---: | ---: |
| Pixel 5, Android 14 / API 34 | bignum 0.3.8 | 13.354 s | 13.893 s | 27.246 s |
| ARM64 emulator, Android 16 / API 36 | bignum 0.3.8 | 4.113 s | 4.202 s | 8.315 s |
| ARM64 emulator, Android 16 / API 36 | bignum 0.3.10 | 7.484 s | 10.143 s | 17.628 s |
| ARM64 emulator, Android 16 / API 36 | platform `java.math.BigInteger` | 0.146 s | 0.167 s | 0.313 s |

The physical device reproduced the reported slowdown: its three exchanges took
23.948, 34.247, and 23.544 seconds. Device package verification rejected the
upgraded bignum test APK, so there is no physical-device bignum upgrade result.
The production platform backend was subsequently installed and measured below.

The library upgrade did not remove the multi-second wait in the emulator. The
platform Java BigInteger comparison cut crypto time to about 0.31 seconds, and
all 14 shared correctness tests passed against that backend on Android ART.
Android now uses this platform backend in production source; the benchmark
consumes that same source file. The prior Kotlin backend remains available only
in the benchmark for comparison.

Build each variant from the fork root:

```sh
./gradlew -p benchmarks assembleDebugAndroidTest -PandroidBackend=kotlin -PbignumVersion=0.3.8
./gradlew -p benchmarks assembleDebugAndroidTest -PandroidBackend=kotlin -PbignumVersion=0.3.10
./gradlew -p benchmarks assembleDebugAndroidTest -PandroidBackend=java
```

Install the resulting APK from `benchmarks/build/outputs/apk/androidTest/debug/`
on the selected device. Between variants uninstall only this benchmark's package,
`com.jump.sdk.srpbenchmark.test`, to reset its app/profile state. Run the isolated
benchmark with:

```sh
adb -s DEVICE_SERIAL shell am instrument -w \
  -e class com.jump.sdk.amplifyframework.AndroidSrpBenchmark \
  com.jump.sdk.srpbenchmark.test/androidx.test.runner.AndroidJUnitRunner
```

For the Java backend correctness run, use a comma-separated class filter of
`com.jump.sdk.amplifyframework.SRPHelperTests`,
`com.jump.sdk.amplifyframework.SrpArithmeticTests`, and
`com.jump.sdk.amplifyframework.SrpProductionArithmeticTests`.

No CI job was added for these measurements.

A fresh-install repeat of bignum 0.3.8 after the other variants averaged 8.005
seconds per exchange on the emulator, consistent with its initial 8.315-second
result. This confirms that the upgraded library did not improve this workload.

## Production Android platform backend on Pixel 5

Android `srpModPow` now uses `java.math.BigInteger.modPow`. It validates the
modulus and exponent, imports unsigned magnitudes explicitly, and clears its
temporary byte buffers after conversion. SRP protocol and hashing remain shared.
The test APK builds the actual production Android source rather than a duplicate
benchmark implementation.

On the same Pixel 5 (Android 14 / API 34), fresh-helper exchange times were:

| Iteration | Public A | Signature | Total |
| --- | ---: | ---: | ---: |
| First | 0.894 s | 0.444 s | 1.338 s |
| Second | 0.333 s | 0.317 s | 0.650 s |
| Third | 0.276 s | 0.291 s | 0.567 s |
| Mean | 0.501 s | 0.351 s | **0.852 s** |

That is approximately **32× faster** than the 27.246-second mean for the prior
Kotlin backend on this device. The device reported thermal status 0 at measurement.
These are isolated Debug crypto measurements, not real backend sign-in timings.
Raw output is in `results/android-pixel5-production-platform.txt`.

All 14 shared correctness tests passed on the Pixel 5 using this production
backend (`results/android-pixel5-production-correctness.txt`), and the fork's JVM
unit tests and release Detekt check pass locally.

The fork's Android Gradle plugin is aligned with JumpKit at 8.13.2, and its wrapper
uses Gradle 8.14.6. This avoids AGP variant incompatibility when the local fork is
used through the composite build. JumpKit `:net:compileDebugKotlinAndroid` passes
with this fork. No CI workflow was added or changed.

## Release comparison

Measured 2026-10-26. Both implementations use bignum 0.3.8 for the shared
representations, the same Kotlin 2.2.10 compiler, and the same production SRP
workload. Three fresh helpers are measured per variant; no network calls are
included. Synthetic signatures and public-A hashes are reported after timing so
the compiler cannot discard the results.

- iOS: the same iPhone 17 Pro / iOS 26.5 ARM64 simulator on the Apple M4 Pro,
  using the Kotlin/Native optimized Release test binary (`releaseTest`). The
  baseline and native binaries both passed all 14 correctness tests plus the
  benchmark. These are simulator results, not physical iPhone timings.
- Android: the same Pixel 5 / Android 14, using a separate application APK with
  `debuggable=false`, R8 minification and optimization enabled, and target SDK 36.
  A small instrumentation entry point drives the workload inside that release
  application's own process and checks the non-debuggable flag at runtime. The
  entry point is kept; SRP and arithmetic dependencies are optimized by R8.
  APKs use a local development signing key for installation; their runtime build
  configuration is Release. Each variant is installed fresh to reset its app state.

| Variant | Public A mean | Signature mean | Exchange mean |
| --- | ---: | ---: | ---: |
| iOS simulator / Kotlin | 417.0 ms | 457.7 ms | **874.7 ms** |
| iOS simulator / native | 18.3 ms | 18.7 ms | **37.0 ms** |
| Pixel 5 / Kotlin | 2173.7 ms | 2302.0 ms | **4475.7 ms** |
| Pixel 5 / platform BigInteger | 126.3 ms | 95.7 ms | **222.0 ms** |

Release improvements are about **24× on the iOS simulator** (874.7 → 37.0 ms)
and **20× on the Pixel 5** (4475.7 → 222.0 ms). Android platform-backend exchanges
were 296, 179, and 191 ms; its first call is included in the mean. The old Android
Release exchanges were 4451, 4503, and 4473 ms. The three iOS native exchanges were
37 ms each, versus 887, 870, and 867 ms for Kotlin.

Raw runs are in `results/*-release-*.txt`. Android APK and R8 mapping hashes are
in `results/release-artifacts.json`; large generated APKs/mapping files are not
committed. This compares isolated Release crypto cost, not application startup,
network latency, or a real backend sign-in.

Build each pair from the fork root:

```sh
./gradlew -p benchmarks linkReleaseTestIosSimulatorArm64 :android-release:assembleRelease -PsrpBackend=kotlin -PandroidBackend=kotlin
./gradlew -p benchmarks linkReleaseTestIosSimulatorArm64 :android-release:assembleRelease -PsrpBackend=native -PandroidBackend=java
```

For iOS, run the built `benchmarks/build/bin/iosSimulatorArm64/releaseTest/test.kexe`
with `xcrun simctl spawn --standalone SIMULATOR_UDID /absolute/path/to/test.kexe`,
or use `iosSimulatorArm64ReleaseTest -PsimulatorId=SIMULATOR_UDID` with the same
backend flags. Save each binary before changing variants.

For Android, install each `benchmarks/android-release/build/outputs/apk/release/`
APK on the selected device, then run:

```sh
adb -s DEVICE_SERIAL shell am instrument -w \
  com.jump.sdk.srpreleasebenchmark/com.jump.sdk.srpreleasebenchmark.SrpReleaseInstrumentation
```

Uninstall `com.jump.sdk.srpreleasebenchmark` between variants. Run simulator
timings without concurrent builds. No CI workflow changes are required or added.
