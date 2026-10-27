package com.jump.sdk.amplifyframework

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.modular.ModularBigInteger

internal actual fun srpModPow(base: BigInteger, exponent: BigInteger, modulus: BigInteger): BigInteger =
    ModularBigInteger.creatorForModulo(modulus).fromBigInteger(base).pow(exponent).toBigInteger()
