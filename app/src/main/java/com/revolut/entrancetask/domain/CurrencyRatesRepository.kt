package com.revolut.entrancetask.domain

import io.reactivex.Observable
import io.reactivex.Single
import java.util.*

interface CurrencyRatesRepository {
    fun loadRates(base: Currency): Single<CurrencyRelativeRates>
}