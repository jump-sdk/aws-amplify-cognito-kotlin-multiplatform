#!/bin/bash
set -euo pipefail
source_dir="$(cd "$(dirname "$0")" && pwd)"
target="$1"
output_dir="$2"
upstream_dir="$3"
minimum_ios="$4"
case "$target" in
  ios_arm64) sdk=iphoneos; triple=arm64-apple-ios${minimum_ios} ;;
  ios_simulator_arm64) sdk=iphonesimulator; triple=arm64-apple-ios${minimum_ios}-simulator ;;
  ios_x64) sdk=iphonesimulator; triple=x86_64-apple-ios${minimum_ios}-simulator ;;
  *) echo "Unsupported native SRP target: $target" >&2; exit 1 ;;
esac
mkdir -p "$output_dir"
sdk_path="$(xcrun --sdk "$sdk" --show-sdk-path)"
cflags="-target $triple -isysroot $sdk_path -I $source_dir/c -I $upstream_dir/.. -include $source_dir/c/jump_srp_config.h"
# The upstream makefile builds objects next to the sources, so each target gets its own copy.
build_dir="$output_dir/libtommath"
rm -rf "$build_dir"
cp -R "$upstream_dir" "$build_dir"
make -C "$build_dir" libtommath.a \
  CC="xcrun --sdk $sdk clang" AR="xcrun --sdk $sdk ar" RANLIB="xcrun --sdk $sdk ranlib" CFLAGS="$cflags"
xcrun --sdk "$sdk" clang -c "$source_dir/c/jump_srp.c" -o "$output_dir/jump_srp.o" -std=c99 -O2 -I "$upstream_dir" $cflags
xcrun libtool -static -o "$output_dir/libjump_srp.a" "$build_dir/libtommath.a" "$output_dir/jump_srp.o"
