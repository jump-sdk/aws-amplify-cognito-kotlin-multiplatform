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
import com.ionspin.kotlin.bignum.integer.util.toTwosComplementByteArray
import com.ionspin.kotlin.bignum.modular.ModularBigInteger
import io.ktor.utils.io.core.toByteArray
import org.kotlincrypto.SecureRandom
import org.kotlincrypto.hash.sha2.SHA256
import org.kotlincrypto.macs.hmac.sha2.HmacSHA256
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val EPHEMERAL_KEY_LENGTH = 1024
private const val HEX = 16
private const val DERIVED_KEY_INFO = "Caldera Derived Key"
private const val DERIVED_KEY_SIZE = 16

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

@OptIn(ExperimentalEncodingApi::class)
@Suppress("TooManyFunctions")
class SRPHelper(private val password: String) {
    @Suppress("VariableNaming")
    private val N = BigInteger.parseString(HEX_N, 16)

    private val creator = ModularBigInteger.creatorForModulo(N)
    private val g = creator.fromInt(2)

    private val random = SecureRandom()

    private val k: BigInteger
    private var privateA: BigInteger
    private var publicA: ModularBigInteger
    var timestamp: String = nowAsFormattedString()
        internal set

    private val digest = SHA256()

    init {
        // Generate client private 'a' and public 'A' values
        do {
            privateA = BigInteger.fromByteArray(random.nextBytesOf(EPHEMERAL_KEY_LENGTH), Sign.POSITIVE).mod(N)
            // A = (g ^ a) % N
            publicA = g.pow(privateA)
        } while (publicA.residue == BigInteger.ZERO)

        // compute k = H(g|N)
        digest.reset()
        digest.update(N.toTwosComplementByteArray())
        k = BigInteger.fromByteArray(digest.digest(g.toByteArray()), Sign.POSITIVE)
    }

    private var userId: String? = null
    private var userPoolName: String? = null

    fun setUserPoolParams(userIdForSrp: String, userPoolName: String) {
        this.userId = userIdForSrp
        this.userPoolName = userPoolName
        if (userPoolName.contains("_")) {
            this.userPoolName = userPoolName.split(Regex("_"), 2)[1]
        }
    }

    // @TestOnly
    internal fun modN(value: BigInteger): BigInteger = value.mod(N)

    // @TestOnly
    internal fun setAValues(privateA: BigInteger, publicA: BigInteger) {
        this.privateA = privateA
        this.publicA = creator.fromBigInteger(publicA)
    }

    fun getPublicA(): String = publicA.toString(HEX)

    // u = H(A, B)
    internal fun computeU(srpB: BigInteger): BigInteger {
        digest.reset()
        digest.update(publicA.toBigInteger().toTwosComplementByteArray())
        return BigInteger.fromByteArray(digest.digest(srpB.toTwosComplementByteArray()), Sign.POSITIVE)
    }

    // x = H(salt | H(poolName | userId | ":" | password))
    @Throws(CognitoException::class)
    internal fun computeX(salt: BigInteger): BigInteger {
        digest.reset()
        digest.update(userPoolName?.toByteArray() ?: throw CognitoException.UserPoolNameNotSet)
        digest.update(userId?.toByteArray() ?: throw CognitoException.UserIdNotSet)
        digest.update(":".toByteArray())
        val userIdPasswordHash = digest.digest(password.toByteArray())

        digest.reset()
        digest.update(salt.toTwosComplementByteArray())
        return BigInteger.fromByteArray(digest.digest(userIdPasswordHash), Sign.POSITIVE)
    }

    // verifier = (g ^ x) % N
    @Throws(CognitoException::class)
    internal fun computePasswordVerifier(salt: BigInteger): ModularBigInteger {
        val xValue = computeX(salt)
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
    internal fun generateM1Signature(key: ByteArray, secretBlock: String): ByteArray {
        val mac = HmacSHA256(key)
        mac.update(userPoolName?.toByteArray() ?: throw CognitoException.UserPoolNameNotSet)
        mac.update(userId?.toByteArray() ?: throw CognitoException.UserIdNotSet)
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
     * @return A string representing the PASSWORD_CLAIM_SIGNATURE for authentication.
     */
    @Throws(CognitoException::class)
    fun getSignature(salt: String, srpB: String, secretBlock: String): String {
        val bigIntSRPB = BigInteger.parseString(srpB, HEX)
        val bigIntSalt = BigInteger.parseString(salt, HEX)

        // Check B's validity
        if (bigIntSRPB.mod(N) == BigInteger.ZERO) {
            throw CognitoException.BadSrpB
        }

        val uValue = computeU(bigIntSRPB)
        if (uValue.mod(N) == BigInteger.ZERO) {
            throw CognitoException.HashOfAAndSrpBCannotBeZero
        }

        val xValue = computeX(bigIntSalt)
        val sValue = computeS(uValue, xValue, bigIntSRPB)
        val key = computePasswordAuthenticationKey(sValue, uValue)
        val m1Signature = generateM1Signature(key, secretBlock)
        return Base64.encode(m1Signature)
    }
}

