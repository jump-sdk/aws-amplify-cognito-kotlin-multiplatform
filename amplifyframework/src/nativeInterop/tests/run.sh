#!/bin/bash
set -euo pipefail
source_dir="$(cd "$(dirname "$0")/.." && pwd)"
upstream_dir="$1"
output_dir="$2"
mkdir -p "$output_dir"
compiler="${CC:-clang}"
flags=(-std=c99 -O2 -g -fsanitize=address,undefined -fno-omit-frame-pointer)
"$compiler" "${flags[@]}" -Wall -Wextra -Werror "$source_dir/tests/memory_test.c" -o "$output_dir/memory_test"
"$output_dir/memory_test"
translation_unit="$output_dir/libtommath.c"
: > "$translation_unit"
for source_file in "$upstream_dir"/*.c; do
  printf '#include "%s"\n' "$source_file" >> "$translation_unit"
done
"$compiler" "${flags[@]}" -I "$source_dir/c" -I "$upstream_dir" -I "$upstream_dir/.." \
  -include "$source_dir/c/jump_srp_config.h" \
  "$translation_unit" "$source_dir/c/jump_srp.c" "$source_dir/c/jump_srp_memory.c" \
  "$source_dir/tests/arithmetic_test.c" -o "$output_dir/arithmetic_test"
"$output_dir/arithmetic_test"
