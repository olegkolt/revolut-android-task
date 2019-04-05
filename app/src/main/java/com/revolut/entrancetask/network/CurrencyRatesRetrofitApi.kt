package com.revolut.entrancetask.network

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyRatesRetrofitApi {
    @GET("latest")
    fun getCurrencyRates(@Query("base") base: String): Single<CurrencyRatesResponseDto>
}