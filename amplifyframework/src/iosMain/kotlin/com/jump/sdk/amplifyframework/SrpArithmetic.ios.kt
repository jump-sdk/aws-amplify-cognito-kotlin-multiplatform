@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.jump.sdk.amplifyframework

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import com.jump.sdk.amplifyframework.nativecrypto.jump_srp_mod_pow
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.posix.size_tVar

internal actual fun srpModPow(base: BigInteger, exponent: BigInteger, modulus: BigInteger): BigInteger {
    require(modulus > BigInteger.ZERO) { "Modulus must be positive" }
    require(exponent >= BigInteger.ZERO) { "Exponent must be nonnegative" }
    val baseBytes = base.mod(modulus).toByteArray().let { if (it.isEmpty()) byteArrayOf(0) else it }
    val exponentBytes = exponent.toByteArray().let { if (it.isEmpty()) byteArrayOf(0) else it }
    val modulusBytes = modulus.toByteArray()
    val output = ByteArray(modulusBytes.size)
    return try {
        memScoped {
            val written = alloc<size_tVar>()
            val status = baseBytes.usePinned { b ->
                exponentBytes.usePinned { e ->
                    modulusBytes.usePinned { m ->
                        output.usePinned { r ->
                            jump_srp_mod_pow(
                                b.addressOf(0).reinterpret(), baseBytes.size.convert(),
                                e.addressOf(0).reinterpret(), exponentBytes.size.convert(),
                                m.addressOf(0).reinterpret(), modulusBytes.size.convert(),
                                r.addressOf(0).reinterpret(), output.size.convert(), written.ptr,
                            )
                        }
                    }
                }
            }
            check(status == 0) { "Native SRP arithmetic failed ($status)" }
            if (written.value == 0UL) BigInteger.ZERO
            else {
                val result = output.copyOf(written.value.toInt())
                try {
                    BigInteger.fromByteArray(result, Sign.POSITIVE)
                } finally {
                    result.fill(0)
                }
            }
        }
    } finally {
        baseBytes.fill(0)
        exponentBytes.fill(0)
        output.fill(0)
    }
}
