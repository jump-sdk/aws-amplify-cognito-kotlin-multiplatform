package com.jump.sdk.amplifyframework

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.coroutines.runBlocking
import org.junit.Test
import android.os.Bundle
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.test.assertTrue
import kotlin.time.measureTime

class AndroidSrpBenchmark {
    private val srpB = BigInteger.parseString(
        string = "e05d0716c6f6bf2b96e4b08354015be28024b4bc9a022c3c7553b86a5feffe8ee0ae87ae8ebb970b9457ed56ef0f5249fad4b9" +
                "9235912c2445a803ff9c9412cb39bd860bcca12868c8e813404435f3621a2fa033270f053e15b7d2ec0617e40cafff3fef4d" +
                "5be716821b389e5338c45ac2d65b292a4739dec8d05ae7feaca6a46d56624c753b705bbc78071ef5c47152c46d7c79b52e4f" +
                "1de3a15ed68923b0f8b08cd4d6b9d358282e219ccfff37ac143b9c7283b859187f57ca445129aeef0517c394a363ed83ca65" +
                "b11a7ede22b96b7e0ff43dd5f58248dbf16c47fad319a23c15ed27c47c765841cb6474eda7a8d44a65c7242d9ff7afc6fc37" +
                "d03b43c9c6279d88248120908c4ecdfb84a6e42344cf1a57f976ef036a1eb7a64067eff195855735d4010ca33831bded8b40" +
                "b5e60a61f19900f928353203adacab71948ef3bcd3540bf926fee5578a00f039bb5c60d10e446a55c9d9620257e05418523a" +
                "fe02e1bc3f3f00c8a43841248094c8806d38f4207679170126f449a62006b42bf0",
        base = 16
    )

    private fun report(message: String) {
        InstrumentationRegistry.getInstrumentation().sendStatus(
            2,
            Bundle().apply { putString("stream", "$message\n") },
        )
    }

    @Test
    fun fullExchange() = runBlocking {
        repeat(3) { iteration ->
            val helper = SRPHelper("us-east-2_KO6fcefgd")
            val publicTime = measureTime { assertTrue(helper.getPublicA().isNotEmpty()) }
            report("SRP_BENCH iteration=$iteration publicA_ms=${publicTime.inWholeMilliseconds}")
            val signatureTime = measureTime {
                assertTrue(helper.getSignature(
                    salt = "e7dc204cebbfda6b62b8493e932f7f4c",
                    srpB = srpB.toString(16),
                    secretBlock = "AQIDBA==",
                    userIdForSrp = "username",
                    password = "Password123",
                ).isNotEmpty())
            }
            report("SRP_BENCH iteration=$iteration signature_ms=${signatureTime.inWholeMilliseconds}")
        }
    }
}
