#include "jump_srp.h"
#include <assert.h>
#include <stdint.h>

static uint32_t next_value(uint32_t *state) {
    *state = *state * 1664525u + 1013904223u;
    return *state;
}

static void encode(uint32_t value, uint8_t bytes[4]) {
    for (int i = 3; i >= 0; --i) {
        bytes[i] = (uint8_t)value;
        value >>= 8;
    }
}

static uint32_t reference(uint32_t base, uint32_t exponent, uint32_t modulus) {
    uint64_t result = 1 % modulus;
    uint64_t factor = base % modulus;
    while (exponent != 0) {
        if (exponent & 1) result = result * factor % modulus;
        factor = factor * factor % modulus;
        exponent >>= 1;
    }
    return (uint32_t)result;
}

int main(void) {
    uint32_t state = 0x5a17;
    /* Deterministic differential coverage of odd/even moduli and unsigned bytes.
       Products fit in uint64_t, independent of LibTomMath's implementation. */
    for (int i = 0; i < 512; ++i) {
        uint32_t base = next_value(&state);
        uint32_t exponent = next_value(&state) & 0xffff;
        uint32_t modulus = (next_value(&state) % 65536) + 1;
        uint8_t b[4], e[4], m[4];
        uint8_t output[6] = {0xa5, 0, 0, 0, 0, 0xa5};
        size_t written = 99;
        encode(base, b);
        encode(exponent, e);
        encode(modulus, m);
        assert(jump_srp_mod_pow(b, 4, e, 4, m, 4, output + 1, 4, &written) == 0);
        assert(written <= 4 && output[0] == 0xa5 && output[5] == 0xa5);
        uint32_t actual = 0;
        for (size_t j = 0; j < written; ++j) actual = (actual << 8) | output[j + 1];
        assert(actual == reference(base, exponent, modulus));
    }

    uint8_t one[] = {1}, empty_value[] = {0}, result[] = {0xa5};
    size_t zero_written = 99;
    assert(jump_srp_mod_pow(empty_value, 1, empty_value, 1, one, 1,
                            result, 1, &zero_written) == 0);
    assert(zero_written == 0);

    uint8_t b[] = {2}, e[] = {8}, m[] = {1, 1}, zero[] = {0};
    uint8_t output[] = {0xa5, 0xa5, 0xa5};
    size_t written = 99;
    /* 2^8 mod 257 requires two output bytes: failure must not overrun capacity. */
    assert(jump_srp_mod_pow(b, 1, e, 1, m, 2, output + 1, 1, &written) != 0);
    assert(written == 0 && output[0] == 0xa5 && output[2] == 0xa5);
    assert(jump_srp_mod_pow(b, 1, e, 1, zero, 1, output, 3, &written) != 0);
    assert(jump_srp_mod_pow(NULL, 1, e, 1, m, 2, output, 3, &written) != 0);
    assert(jump_srp_mod_pow(b, 1, e, 1, m, 2, output, 3, NULL) != 0);
    return 0;
}
