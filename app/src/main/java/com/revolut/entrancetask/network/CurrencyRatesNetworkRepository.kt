package com.revolut.entrancetask.network

import com.revolut.entrancetask.domain.CurrencyRate
import com.revolut.entrancetask.domain.CurrencyRatesRepository
import com.revolut.entrancetask.domain.CurrencyRelativeRates
import io.reactivex.Single
import java.util.*

class CurrencyRatesNetworkRepository(private val api: CurrencyRatesRetrofitApi) : CurrencyRatesRepository {
    override fun loadRates(base: Currency): Single<CurrencyRelativeRates> {
        return api.getCurrencyRates(base.currencyCode)
            .map { response ->
                CurrencyRelativeRates(
                    base = Currency.getInstance(response.base),
                    rates = response.rates.map {
                        CurrencyRate(Currency.getInstance(it.key), it.value)
                    }
                ) }
    }
}