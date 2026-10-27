#include "jump_srp.h"
#include "tommath.h"

int jump_srp_mod_pow(
    const uint8_t *base, size_t base_size,
    const uint8_t *exponent, size_t exponent_size,
    const uint8_t *modulus, size_t modulus_size,
    uint8_t *result, size_t result_capacity, size_t *result_size
) {
    mp_int b, e, m, r;
    mp_err status;
    if (base == NULL || exponent == NULL || modulus == NULL || result == NULL || result_size == NULL) {
        return MP_VAL;
    }
    *result_size = 0;
    status = mp_init_multi(&b, &e, &m, &r, NULL);
    if (status != MP_OKAY) return status;
    if ((status = mp_from_ubin(&b, base, base_size)) != MP_OKAY) goto cleanup;
    if ((status = mp_from_ubin(&e, exponent, exponent_size)) != MP_OKAY) goto cleanup;
    if ((status = mp_from_ubin(&m, modulus, modulus_size)) != MP_OKAY) goto cleanup;
    if (mp_iszero(&m)) { status = MP_VAL; goto cleanup; }
    /* LibTomMath returns one for exponent zero even when the modulus is one. */
    if (mp_cmp_d(&m, 1) != MP_EQ) {
        if ((status = mp_exptmod(&b, &e, &m, &r)) != MP_OKAY) goto cleanup;
    }
    status = mp_to_ubin(&r, result, result_capacity, result_size);
cleanup:
    /* LibTomMath zeroes heap digit buffers on free; its stack scratch arrays are not cleared. */
    mp_clear_multi(&b, &e, &m, &r, NULL);
    return status;
}
