package com.hefny.hady.blogpost.api.main

import androidx.lifecycle.LiveData
import com.hefny.hady.blogpost.api.GenericResponse
import com.hefny.hady.blogpost.models.AccountProperties
import com.hefny.hady.blogpost.util.GenericApiResponse
import retrofit2.http.*

interface OpenApiMainService {
    @GET("account/properties")
    fun getAccountProperties(
        @Header("Authorization") authorization: String
    ): LiveData<GenericApiResponse<AccountProperties>>

    @PUT("account/properties/update")
    @FormUrlEncoded
    fun updateAccountProperties(
        @Header("Authorization") authorization: String,
        @Field("email") email: String,
        @Field("username") username: String
    ): LiveData<GenericApiResponse<GenericResponse>>
}