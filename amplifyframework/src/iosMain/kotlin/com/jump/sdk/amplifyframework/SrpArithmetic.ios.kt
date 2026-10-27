@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.jump.sdk.amplifyframework

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

internal actual fun platformModPow(base: ByteArray, exponent: ByteArray, modulus: ByteArray): ByteArray {
    val baseBytes = if (base.isEmpty()) byteArrayOf(0) else base
    val exponentBytes = if (exponent.isEmpty()) byteArrayOf(0) else exponent
    val output = ByteArray(modulus.size)
    return try {
        memScoped {
            val written = alloc<size_tVar>()
            val status = baseBytes.usePinned { b ->
                exponentBytes.usePinned { e ->
                    modulus.usePinned { m ->
                        output.usePinned { r ->
                            jump_srp_mod_pow(
                                b.addressOf(0).reinterpret(), baseBytes.size.convert(),
                                e.addressOf(0).reinterpret(), exponentBytes.size.convert(),
                                m.addressOf(0).reinterpret(), modulus.size.convert(),
                                r.addressOf(0).reinterpret(), output.size.convert(), written.ptr,
                            )
                        }
                    }
                }
            }
            check(status == 0) { "Native SRP arithmetic failed ($status)" }
            output.copyOf(written.value.toInt())
        }
    } finally {
        output.fill(0)
    }
}
