package com.jump.sdk.amplifyframework

sealed class CognitoException(override val message: String, open val errorCode: String? = null) : Exception(message) {
    data object BadSrpB : CognitoException("Bad server public value 'B'")
    data object HashOfAAndSrpBCannotBeZero : CognitoException("Hash of A and B cannot be zero")

    data class NotAuthorizedException(val originalMessage: String) :
        CognitoException("Invalid credentials", "NotAuthorizedException")
    data class UserNotConfirmedException(val originalMessage: String) :
        CognitoException("User not confirmed", "UserNotConfirmedException")
    data class TooManyRequestsException(val originalMessage: String, val retryAfterSeconds: Long? = null) :
        CognitoException("Rate limited", "TooManyRequestsException")
    data class CodeMismatchException(val originalMessage: String) :
        CognitoException("Invalid verification code", "CodeMismatchException")
    data class ExpiredCodeException(val originalMessage: String) :
        CognitoException("Verification code expired", "ExpiredCodeException")
    data class InvalidParameterException(val originalMessage: String, val parameterName: String? = null) :
        CognitoException("Invalid parameter", "InvalidParameterException")
    data class UserNotFoundException(val originalMessage: String) :
        CognitoException("User not found", "UserNotFoundException")
    data class NetworkException(val originalMessage: String) :
        CognitoException("Network error", "NetworkException")
    data class UnknownCognitoException(val originalMessage: String, override val errorCode: String?) :
        CognitoException("Unknown Cognito error", errorCode)
}
