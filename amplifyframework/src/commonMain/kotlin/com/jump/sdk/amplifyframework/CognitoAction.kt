package com.jump.sdk.amplifyframework

enum class CognitoAction(val headerValue: String) {
    CONFIRM_SIGN_UP("AWSCognitoIdentityProviderService.ConfirmSignUp"),
    SIGN_UP("AWSCognitoIdentityProviderService.SignUp"),
    INITIATE_AUTH("AWSCognitoIdentityProviderService.InitiateAuth"),
    RESPOND_TO_AUTH_CHALLENGE("AWSCognitoIdentityProviderService.RespondToAuthChallenge"),
}
