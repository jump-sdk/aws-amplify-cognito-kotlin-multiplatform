#ifndef JUMP_SRP_CONFIG_H
#define JUMP_SRP_CONFIG_H

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
