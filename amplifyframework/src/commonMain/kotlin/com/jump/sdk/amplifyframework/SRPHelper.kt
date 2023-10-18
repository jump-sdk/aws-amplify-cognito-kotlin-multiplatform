package com.jump.sdk.amplifyframework

/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import com.ionspin.kotlin.bignum.integer.util.fromTwosComplementByteArray
import com.ionspin.kotlin.bignum.integer.util.toTwosComplementByteArray
import com.ionspin.kotlin.bignum.modular.ModularBigInteger
import io.ktor.utils.io.core.toByteArray
import org.kotlincrypto.SecureRandom
import org.kotlincrypto.hash.sha2.SHA256
import org.kotlincrypto.macs.hmac.sha2.HmacSHA256
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.delay

private const val EPHEMERAL_KEY_LENGTH = 1024
private const val HEX = 16
private const val DERIVED_KEY_INFO = "Caldera Derived Key"
private const val DERIVED_KEY_SIZE = 16
private const val SRP_A_GEN_WAIT = 50L

// Precomputed safe 3072-bit prime 'N', as decimal.
// https://datatracker.ietf.org/doc/html/rfc5054#appendix-A (Page 16)
private const val HEX_N =
    "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A" +
        "431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5" +
        "AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62" +
        "F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2" +
        "EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AAAC42DAD33170D0450" +
        "7A33A85521ABDF1CBA64ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7ABF5AE8CDB0933D71E8C94E04A25619D" +
        "CEE3D2261AD2EE6BF12FFA06D98A0864D87602733EC86A64521F2B18177B200CBBE117577A615D6C770988C0BAD946E208E2" +
        "4FA074E5AB3143DB5BFCE0FD108E4B82D120A93AD2CAFFFFFFFFFFFFFFFF"

// precomputed: k = H(g|N)
private val kByteArray = byteArrayOf(83, -126, -126, -60, 53, 71, 66, -41, -53, -67, -30, 53, -97, -49, 103, -7, -11, -77, -90, -80, -121, -111, -27, 1, 27, 67, -72, -91, -74, 109, -98, -26)

// precomputed: N = BigInteger.parseString(HEX_N, 16)
private val nByteArray = byteArrayOf(0, -1, -1, -1, -1, -1, -1, -1, -1, -55, 15, -38, -94, 33, 104, -62, 52, -60, -58, 98, -117, -128, -36, 28, -47, 41, 2, 78, 8, -118, 103, -52, 116, 2, 11, -66, -90, 59, 19, -101, 34, 81, 74, 8, 121, -114, 52, 4, -35, -17, -107, 25, -77, -51, 58, 67, 27, 48, 43, 10, 109, -14, 95, 20, 55, 79, -31, 53, 109, 109, 81, -62, 69, -28, -123, -75, 118, 98, 94, 126, -58, -12, 76, 66, -23, -90, 55, -19, 107, 11, -1, 92, -74, -12, 6, -73, -19, -18, 56, 107, -5, 90, -119, -97, -91, -82, -97, 36, 17, 124, 75, 31, -26, 73, 40, 102, 81, -20, -28, 91, 61, -62, 0, 124, -72, -95, 99, -65, 5, -104, -38, 72, 54, 28, 85, -45, -102, 105, 22, 63, -88, -3, 36, -49, 95, -125, 101, 93, 35, -36, -93, -83, -106, 28, 98, -13, 86, 32, -123, 82, -69, -98, -43, 41, 7, 112, -106, -106, 109, 103, 12, 53, 78, 74, -68, -104, 4, -15, 116, 108, 8, -54, 24, 33, 124, 50, -112, 94, 70, 46, 54, -50, 59, -29, -98, 119, 44, 24, 14, -122, 3, -101, 39, -125, -94, -20, 7, -94, -113, -75, -59, 93, -16, 111, 76, 82, -55, -34, 43, -53, -10, -107, 88, 23, 24, 57, -107, 73, 124, -22, -107, 106, -27, 21, -46, 38, 24, -104, -6, 5, 16, 21, 114, -114, 90, -118, -86, -60, 45, -83, 51, 23, 13, 4, 80, 122, 51, -88, 85, 33, -85, -33, 28, -70, 100, -20, -5, -123, 4, 88, -37, -17, 10, -118, -22, 113, 87, 93, 6, 12, 125, -77, -105, 15, -123, -90, -31, -28, -57, -85, -11, -82, -116, -37, 9, 51, -41, 30, -116, -108, -32, 74, 37, 97, -99, -50, -29, -46, 38, 26, -46, -18, 107, -15, 47, -6, 6, -39, -118, 8, 100, -40, 118, 2, 115, 62, -56, 106, 100, 82, 31, 43, 24, 23, 123, 32, 12, -69, -31, 23, 87, 122, 97, 93, 108, 119, 9, -120, -64, -70, -39, 70, -30, 8, -30, 79, -96, 116, -27, -85, 49, 67, -37, 91, -4, -32, -3, 16, -114, 75, -126, -47, 32, -87, 58, -46, -54, -1, -1, -1, -1, -1, -1, -1, -1)

private enum class SrpGenerationState { NOT_STARTED, STARTED, COMPLETED }

@OptIn(ExperimentalEncodingApi::class)
@Suppress("TooManyFunctions")
class SRPHelper(userPool: String) {
    @Suppress("VariableNaming")
    private val N = BigInteger.fromTwosComplementByteArray(nByteArray)
    private val creator = ModularBigInteger.creatorForModulo(N)
    private val g = creator.fromInt(2)
    private val random = SecureRandom()
    private val k: BigInteger = BigInteger.fromTwosComplementByteArray(kByteArray)
    private val digest = SHA256()
    private val userPoolName: String

    private var srpState: SrpGenerationState = SrpGenerationState.NOT_STARTED

    @Suppress("LateinitUsage")
    private lateinit var privateA: BigInteger

    @Suppress("LateinitUsage")
    private lateinit var publicA: ModularBigInteger

    var timestamp: String = nowAsFormattedString()
        internal set

    init {
        if (userPool.contains("_")) {
            this.userPoolName = userPool.split(Regex("_"), 2)[1]
        } else {
            this.userPoolName = userPool
        }
    }

    // Generate client private 'a' and public 'A' values
    suspend fun getPublicA(): String =
        when (srpState) {
            SrpGenerationState.NOT_STARTED -> {
                do {
                    privateA = BigInteger
                        .fromByteArray(
                            source = random.nextBytesOf(EPHEMERAL_KEY_LENGTH),
                            sign = Sign.POSITIVE,
                        )
                        .mod(N)
                    // A = (g ^ a) % N
                    publicA = g.pow(privateA)
                } while (publicA.residue == BigInteger.ZERO)
                srpState = SrpGenerationState.COMPLETED
                publicA.toString(HEX)
            }

            SrpGenerationState.STARTED -> {
                do { delay(SRP_A_GEN_WAIT) } while (srpState != SrpGenerationState.COMPLETED)
                publicA.toString(HEX)
            }
            SrpGenerationState.COMPLETED -> publicA.toString(HEX)
        }

    // @TestOnly
    internal fun modN(value: BigInteger): BigInteger = value.mod(N)

    // @TestOnly
    internal fun setAValues(privateA: BigInteger, publicA: BigInteger) {
        this.privateA = privateA
        this.publicA = creator.fromBigInteger(publicA)
    }

    // u = H(A, B)
    internal fun computeU(srpB: BigInteger): BigInteger {
        digest.reset()
        digest.update(publicA.toBigInteger().toTwosComplementByteArray())
        return BigInteger.fromByteArray(digest.digest(srpB.toTwosComplementByteArray()), Sign.POSITIVE)
    }

    // x = H(salt | H(poolName | userId | ":" | password))
    @Throws(CognitoException::class)
    internal fun computeX(salt: BigInteger, userIdForSrp: String, password: String): BigInteger {
        digest.reset()
        digest.update(userPoolName.toByteArray())
        digest.update(userIdForSrp.toByteArray())
        digest.update(":".toByteArray())
        val userIdPasswordHash = digest.digest(password.toByteArray())

        digest.reset()
        digest.update(salt.toTwosComplementByteArray())
        return BigInteger.fromByteArray(digest.digest(userIdPasswordHash), Sign.POSITIVE)
    }

    // verifier = (g ^ x) % N
    @Throws(CognitoException::class)
    internal fun computePasswordVerifier(
        salt: BigInteger,
        userIdForSrp: String,
        password: String,
    ): ModularBigInteger {
        val xValue = computeX(salt = salt, userIdForSrp = userIdForSrp, password = password)
        return g.pow(xValue)
    }

    // s = ((B - k * (g ^ x) % N) ^ (a + u * x) % N) % N
    internal fun computeS(uValue: BigInteger, xValue: BigInteger, srpB: BigInteger): BigInteger {
        return creator
            .fromBigInteger(
                srpB.subtract(
                    k.multiply(
                        g.pow(xValue).toBigInteger(),
                    ),
                ),
            )
            .pow(
                privateA.add(uValue.multiply(xValue)),
            )
            .toBigInteger()
    }

    // p = MAC("Caldera Derived Key" | 1, MAC(s, u))[0:16]
    internal fun computePasswordAuthenticationKey(ikm: BigInteger, salt: BigInteger): ByteArray {
        val prk = HmacSHA256(salt.toTwosComplementByteArray()).doFinal(ikm.toTwosComplementByteArray())
        val mac = HmacSHA256(prk)
        mac.update(DERIVED_KEY_INFO.toByteArray())
        val hkdf = mac.doFinal(Char(1).toString().toByteArray())
        return hkdf.copyOf(DERIVED_KEY_SIZE)
    }

    // M1 = MAC(poolId | userId | secret | timestamp, key)
    @Throws(CognitoException::class)
    internal fun generateM1Signature(
        key: ByteArray,
        secretBlock: String,
        userIdForSrp: String,
    ): ByteArray {
        val mac = HmacSHA256(key)
        mac.update(userPoolName.toByteArray())
        mac.update(userIdForSrp.toByteArray())
        mac.update(Base64.decode(secretBlock))
        return mac.doFinal(timestamp.toByteArray())
    }

    /**
     * Generates a PASSWORD_CLAIM_SIGNATURE for Amplify Cognito authentication.
     *
     * This function calculates the PASSWORD_CLAIM_SIGNATURE, which is used in the authentication
     * process with Amazon Cognito Identity Provider. It combines the provided salt, SRP_B value,
     * and secret block to create a secure signature for authentication.
     *
     * The parameters are returned from calling AWSCognitoIdentityProviderService.InitiateAuth
     * Note that you MUST call setUserPoolParams() before calling this function or it will throw
     * a [CognitoException].
     *
     * @param salt The salt value used in the authentication process.
     * @param srpB The SRP_B value provided by the Cognito service.
     * @param secretBlock The secret block - should be passed into PASSWORD_CLAIM_SECRET_BLOCK
     * for the subsequent call to AWSCognitoIdentityProviderService.RespondToAuthChallenge
     * @param userIdForSrp The user ID used in the authentication process.
     * @param password The password used in the authentication process.
     * @return A string representing the PASSWORD_CLAIM_SIGNATURE for authentication.
     */
    @Throws(CognitoException::class, CancellationException::class)
    suspend fun getSignature(
        salt: String,
        srpB: String,
        secretBlock: String,
        userIdForSrp: String,
        password: String,
    ): String {
        val bigIntSrpB = BigInteger.parseString(srpB, HEX)
        val bigIntSalt = BigInteger.parseString(salt, HEX)

        // Check B's validity
        if (bigIntSrpB.mod(N) == BigInteger.ZERO) {
            throw CognitoException.BadSrpB
        }

        val uValue = computeU(bigIntSrpB)
        if (uValue.mod(N) == BigInteger.ZERO) {
            throw CognitoException.HashOfAAndSrpBCannotBeZero
        }

        val xValue = computeX(salt = bigIntSalt, userIdForSrp = userIdForSrp, password = password)
        val sValue = computeS(uValue, xValue, bigIntSrpB)
        val key = computePasswordAuthenticationKey(sValue, uValue)
        val m1Signature = generateM1Signature(
            key = key,
            secretBlock = secretBlock,
            userIdForSrp = userIdForSrp,
        )
        return Base64.encode(m1Signature)
    }
}
