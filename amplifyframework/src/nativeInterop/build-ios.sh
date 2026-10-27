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
# Compile unmodified upstream units in a single translation unit.
translation_unit="$output_dir/libtommath.c"
: > "$translation_unit"
for source_file in "$upstream_dir"/*.c; do
  printf '#include "%s"\n' "$source_file" >> "$translation_unit"
done
for unit in "$translation_unit" "$source_dir/c/jump_srp.c" "$source_dir/c/jump_srp_memory.c"; do
  object_name="$(basename "$unit" .c)"
  xcrun --sdk "$sdk" clang -c "$unit" -o "$output_dir/$object_name.o" \
    -target "$triple" -isysroot "$sdk_path" -std=c99 -O2 -fvisibility=hidden \
    -I "$source_dir/c" -I "$upstream_dir" -I "$upstream_dir/.." \
    -include "$source_dir/c/jump_srp_config.h"
done
xcrun libtool -static -o "$output_dir/libjump_srp.a" "$output_dir/libtommath.o" "$output_dir/jump_srp.o" "$output_dir/jump_srp_memory.o"
