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
    *result_size = 0;
    if ((status = mp_init_multi(&b, &e, &m, &r, NULL)) != MP_OKAY) return status;
    if ((status = mp_from_ubin(&b, base, base_size)) == MP_OKAY &&
        (status = mp_from_ubin(&e, exponent, exponent_size)) == MP_OKAY &&
        (status = mp_from_ubin(&m, modulus, modulus_size)) == MP_OKAY &&
        (status = mp_exptmod(&b, &e, &m, &r)) == MP_OKAY) {
        status = mp_to_ubin(&r, result, result_capacity, result_size);
    }
    mp_clear_multi(&b, &e, &m, &r, NULL);
    return status;
}
