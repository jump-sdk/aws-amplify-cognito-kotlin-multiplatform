package com.jump.sdk.amplifyframework

import java.math.BigInteger

internal actual fun platformModPow(base: ByteArray, exponent: ByteArray, modulus: ByteArray): ByteArray =
    BigInteger(1, base).modPow(BigInteger(1, exponent), BigInteger(1, modulus)).toByteArray()
