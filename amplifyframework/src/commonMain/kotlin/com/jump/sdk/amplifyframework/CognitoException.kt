package com.jump.sdk.amplifyframework

sealed class CognitoException(
    override val message: String,
) : Exception(message) {
    object BadSrpB : CognitoException("Bad server public value 'B'")
    object HashOfAAndSrpBCannotBeZero : CognitoException("Hash of A and B cannot be zero")
}
