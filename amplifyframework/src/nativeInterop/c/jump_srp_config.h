#ifndef JUMP_SRP_CONFIG_H
#define JUMP_SRP_CONFIG_H

#include <stdlib.h>
#include "jump_srp_memory.h"

#define MP_MALLOC malloc
#define MP_CALLOC calloc
#define MP_REALLOC jump_srp_realloc
#define MP_FREE jump_srp_free
/* The allocator hooks perform an optimizer-resistant wipe. */
#define MP_NO_ZERO_ON_FREE

#define LTM_NOTHING
#define MP_FIXED_CUTOFFS
#define MP_NO_FILE
#define BN_MP_INIT_MULTI_C
#define BN_MP_CLEAR_MULTI_C
#define BN_MP_FROM_UBIN_C
#define BN_MP_TO_UBIN_C
#define BN_MP_EXPTMOD_C

/* Namespace the verified dependency without keeping generated symbols in source control. */
#include "jump_srp_symbols.h"

#endif
