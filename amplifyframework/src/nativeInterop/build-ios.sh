#!/bin/bash
set -euo pipefail
source_dir="$(cd "$(dirname "$0")" && pwd)"
target="$1"
output_dir="$2"
case "$target" in
  ios_arm64) sdk=iphoneos; triple=arm64-apple-ios13.0 ;;
  ios_simulator_arm64) sdk=iphonesimulator; triple=arm64-apple-ios13.0-simulator ;;
  ios_x64) sdk=iphonesimulator; triple=x86_64-apple-ios13.0-simulator ;;
  *) echo "Unsupported native SRP target: $target" >&2; exit 1 ;;
esac
mkdir -p "$output_dir"
sdk_path="$(xcrun --sdk "$sdk" --show-sdk-path)"
for unit in vendor/libtommath/libtommath c/jump_srp; do
  object_name="${unit##*/}"
  xcrun --sdk "$sdk" clang -c "$source_dir/$unit.c" -o "$output_dir/$object_name.o" \
    -target "$triple" -isysroot "$sdk_path" -std=c99 -O2 -fvisibility=hidden \
    -I "$source_dir/c" -I "$source_dir/vendor/libtommath" \
    -include "$source_dir/c/jump_srp_config.h"
done
xcrun libtool -static -o "$output_dir/libjump_srp.a" "$output_dir/libtommath.o" "$output_dir/jump_srp.o"
