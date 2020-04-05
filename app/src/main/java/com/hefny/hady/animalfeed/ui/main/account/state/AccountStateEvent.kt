package com.hefny.hady.animalfeed.ui.main.account.state

sealed class AccountStateEvent {
    class GetAccountPropertiesEvent() : AccountStateEvent()
    data class UpdateAccountPropertiesEvent(
        val userName: String,
        val password: String
    ) : AccountStateEvent()

    data class ChangePasswordEvent(
        val currentPassword: String,
        val newPassword: String,
        val confirmNewPassword: String
    ) : AccountStateEvent()

    class None() : AccountStateEvent()
}