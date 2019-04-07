package com.revolut.entrancetask

import com.revolut.entrancetask.domain.CurrencyFlagUrlGenerator
import com.revolut.entrancetask.domain.CurrencyRatesUseCase
import com.revolut.entrancetask.infrastructure.AppSchedulers
import com.revolut.entrancetask.network.CurrencyRatesNetworkRepository
import com.revolut.entrancetask.network.CurrencyRatesRetrofitApi
import com.revolut.entrancetask.presentation.CurrencyListPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal
import java.util.*

private val DEFAULT_CURRENCY = Currency.getInstance("EUR")!!
private val DEFAULT_OUTCOME_AMOUNT = BigDecimal(100)
private const val CURRENCY_ACCESS_POINT = "https://revolut.duckdns.org/"
private const val CURRENCY_FLAG_URL_PATTERN = "https://raw.githubusercontent.com/transferwise/currency-flags/master/src/flags/%s.png"

fun provideCurrencyListPresenter() : CurrencyListPresenter {
    return CurrencyListPresenter(
        CurrencyRatesUseCase(
            DEFAULT_OUTCOME_AMOUNT,
            DEFAULT_CURRENCY,
            CurrencyRatesNetworkRepository(provideCurrencyRatesApi()),
            CurrencyFlagUrlGenerator(CURRENCY_FLAG_URL_PATTERN),
            AppSchedulers(
                io = Schedulers.io(),
                ui = AndroidSchedulers.mainThread()
            )
        )
    )
}

fun provideCurrencyRatesApi() : CurrencyRatesRetrofitApi {
    val client = OkHttpClient().newBuilder()
        //.addInterceptor(httpLoggingInterceptor)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(CURRENCY_ACCESS_POINT)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()

    return retrofit.create<CurrencyRatesRetrofitApi>(CurrencyRatesRetrofitApi::class.java)
}