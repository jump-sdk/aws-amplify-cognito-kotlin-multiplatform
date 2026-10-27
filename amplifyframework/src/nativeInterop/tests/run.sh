#!/bin/bash
set -euo pipefail
source_dir="$(cd "$(dirname "$0")/.." && pwd)"
upstream_dir="$1"
output_dir="$2"
mkdir -p "$output_dir"
compiler="${CC:-clang}"
flags="-g -fsanitize=address,undefined -fno-omit-frame-pointer"
cflags="$flags -I $source_dir/c -I $upstream_dir/.. -include $source_dir/c/jump_srp_config.h"
build_dir="$output_dir/libtommath"
rm -rf "$build_dir"
cp -R "$upstream_dir" "$build_dir"
make -C "$build_dir" libtommath.a CC="$compiler" CFLAGS="$cflags"
"$compiler" -std=c99 -O2 -I "$upstream_dir" $cflags \
  "$source_dir/c/jump_srp.c" "$source_dir/tests/arithmetic_test.c" "$build_dir/libtommath.a" \
  -o "$output_dir/arithmetic_test"
"$output_dir/arithmetic_test"
