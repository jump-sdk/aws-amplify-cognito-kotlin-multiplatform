package com.jump.sdk.amplifyframework

object CognitoErrorParser {
    fun parseErrorResponse(responseBody: String, httpStatusCode: Int): CognitoException {
        return try {
            // Simple string parsing approach - looking for key patterns in the response
            val errorType = extractErrorType(responseBody)
            val message = extractMessage(responseBody) ?: "Unknown error"

            when {
                errorType?.contains("NotAuthorizedException") == true ->
                    CognitoException.NotAuthorizedException(message)

                errorType?.contains("UserNotConfirmedException") == true ->
                    CognitoException.UserNotConfirmedException(message)

                errorType?.contains("TooManyRequestsException") == true ->
                    CognitoException.TooManyRequestsException(message)

                errorType?.contains("CodeMismatchException") == true ->
                    CognitoException.CodeMismatchException(message)

                errorType?.contains("ExpiredCodeException") == true ->
                    CognitoException.ExpiredCodeException(message)

                errorType?.contains("InvalidParameterException") == true ->
                    CognitoException.InvalidParameterException(message)

                errorType?.contains("UserNotFoundException") == true ->
                    CognitoException.UserNotFoundException(message)

                httpStatusCode in 500..599 ->
                    CognitoException.NetworkException("Server error: $message")

                httpStatusCode == 0 || httpStatusCode in 1000..1999 ->
                    CognitoException.NetworkException("Network connectivity error: $message")

                else ->
                    CognitoException.UnknownCognitoException(message, errorType)
            }
        } catch (e: Exception) {
            when (httpStatusCode) {
                in 500..599 ->
                    CognitoException.NetworkException("Server error: HTTP $httpStatusCode")

                0, in 1000..1999 ->
                    CognitoException.NetworkException("Network connectivity error")

                else ->
                    CognitoException.UnknownCognitoException(
                        "Failed to parse error response: ${e.message}",
                        "ParseError"
                    )
            }
        }
    }

    private fun extractErrorType(responseBody: String): String? {
        val typePattern = "\"__type\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        return typePattern.find(responseBody)?.groupValues?.get(1)
    }

    private fun extractMessage(responseBody: String): String? {
        val messagePattern = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        return messagePattern.find(responseBody)?.groupValues?.get(1)
    }

    fun parseRetryAfterHeader(retryAfterHeader: String?): Long? = retryAfterHeader?.toLongOrNull()

}
