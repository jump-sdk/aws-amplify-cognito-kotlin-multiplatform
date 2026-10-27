#ifndef JUMP_SRP_H
#define JUMP_SRP_H
#include <stddef.h>
#include <stdint.h>

/* Unsigned, big-endian inputs and output with a modulus above one. Returns zero on success. */
int jump_srp_mod_pow(
    const uint8_t *base, size_t base_size,
    const uint8_t *exponent, size_t exponent_size,
    const uint8_t *modulus, size_t modulus_size,
    uint8_t *result, size_t result_capacity, size_t *result_size
);
#endif
