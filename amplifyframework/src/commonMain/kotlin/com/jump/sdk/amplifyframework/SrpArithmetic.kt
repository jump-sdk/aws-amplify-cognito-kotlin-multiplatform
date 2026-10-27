package com.jump.sdk.amplifyframework

import com.ionspin.kotlin.bignum.integer.BigInteger

/** Modular exponentiation for SRP with a positive modulus and nonnegative exponent. */
internal expect fun srpModPow(base: BigInteger, exponent: BigInteger, modulus: BigInteger): BigInteger
