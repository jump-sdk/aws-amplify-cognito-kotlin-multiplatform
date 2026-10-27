#!/usr/bin/env python3
"""Regenerate the pinned LibTomMath amalgamation from an existing upstream checkout."""
import re
import shutil
import subprocess
import sys
from pathlib import Path

COMMIT = "95d80fd8229d05dd6cb4ec88bc8d4f5377ff00ef"
upstream = Path(sys.argv[1]).resolve()
assert subprocess.check_output(["git", "-C", str(upstream), "rev-parse", "HEAD"], text=True).strip() == COMMIT
assert not subprocess.check_output(["git", "-C", str(upstream), "status", "--porcelain"], text=True).strip()
native = Path(__file__).resolve().parents[1] / "amplifyframework/src/nativeInterop"
vendor = native / "vendor/libtommath"
vendor.mkdir(parents=True, exist_ok=True)
for header in upstream.glob("tommath*.h"):
    shutil.copyfile(header, vendor / header.name)
shutil.copyfile(upstream / "LICENSE", vendor / "LICENSE")
parts = [f"/* LibTomMath v1.3.0, commit {COMMIT}.\n * Unmodified source files concatenated in filename order; see README.md. */\n"]
for source in sorted(upstream.glob("*.c")):
    parts.extend([f"\n/* BEGIN {source.name} */\n", source.read_text()])
(vendor / "libtommath.c").write_text("".join(parts))
identifiers, macros = set(), set()
for source in list(vendor.glob("*.h")) + [vendor / "libtommath.c"]:
    text = source.read_text()
    identifiers.update(re.findall(r"\b(?:mp_|s_mp_)\w+\b", text))
    macros.update(re.findall(r"#\s*define\s+((?:mp_|s_mp_)\w+)", text))
config = """#ifndef JUMP_SRP_CONFIG_H
#define JUMP_SRP_CONFIG_H

#define LTM_NOTHING
#define MP_FIXED_CUTOFFS
#define MP_NO_FILE
#define BN_MP_INIT_MULTI_C
#define BN_MP_CLEAR_MULTI_C
#define BN_MP_FROM_UBIN_C
#define BN_MP_TO_UBIN_C
#define BN_MP_EXPTMOD_C

/* Keep this embedded copy independent of other SDKs that use LibTomMath. */
"""
config += "".join(f"#define {symbol} jump_srp_{symbol}\n" for symbol in sorted(identifiers - macros))
(native / "c/jump_srp_config.h").write_text(config + "\n#endif\n")
