package com.revolut.entrancetask.domain

import com.revolut.entrancetask.infrastructure.AppSchedulers
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Keeps state of outcomeAmount, outcomeCurrency and loadedRates
 */
class CurrencyRatesUseCase(
    private var outcomeAmount: BigDecimal,
    private var outcomeCurrency: Currency,
    private val ratesRepository: CurrencyRatesRepository,
    private val urlGenerator: CurrencyFlagUrlGenerator,
    private val schedulers: AppSchedulers
) {
    companion object {
        private fun calculateIncome(rate: BigDecimal, outcomeAmount: BigDecimal): BigDecimal {
            return rate.multiply(outcomeAmount).apply {
                setScale(2, BigDecimal.ROUND_HALF_UP)
            }
        }

        private fun generateInitialState(
            currencyRates: CurrencyRelativeRates?,
            outcomeAmount: BigDecimal,
            outcomeCurrency: Currency,
            urlGenerator: CurrencyFlagUrlGenerator
        ) : CurrencyListState {
            return when {
                currencyRates == null -> CurrencyListLoadingState
                currencyRates.rates.isEmpty() -> CurrencyListErrorState
                else -> CurrencyListLoadedState(
                    list = listOf(
                        CurrencyListItem(
                            currencyRates.base,
                            urlGenerator.generate(currencyRates.base),
                            outcomeAmount
                        )
                    ) + currencyRates.rates
                        .filter { it.currency != outcomeCurrency }
                        .map { rate ->
                            CurrencyListItem(
                                rate.currency,
                                urlGenerator.generate(rate.currency),
                                calculateIncome(rate.rate, outcomeAmount)
                            )
                        }
                )
            }
        }
    }

    private val requestSubject: Subject<Currency> = PublishSubject.create()
    private val updateSubject: Subject<Currency> = PublishSubject.create()
    private val stateSubject: Subject<CurrencyListState> = PublishSubject.create()
    private var loadedRates: CurrencyRelativeRates? = null

    init {
        requestSubject.hide()
            .switchMap {
                ratesRepository
                    .loadRates(it)
                    .subscribeOn(schedulers.io)
                    .toObservable()
            }.map { ratesResult ->
                loadedRates = ratesResult
                generateInitialState(
                    currencyRates = loadedRates,
                    outcomeAmount = outcomeAmount,
                    outcomeCurrency = outcomeCurrency,
                    urlGenerator = urlGenerator
                )
            }
            .onErrorResumeNext(Observable.just(CurrencyListErrorState))
            .observeOn(schedulers.ui)
            .subscribe(stateSubject)

        updateSubject.hide()
            .switchMap { ratesRepository
                .loadRates(it)
                .subscribeOn(schedulers.io)
                .toObservable()
                .materialize()
                .filter { notification -> notification.isOnNext }
                .dematerialize<CurrencyRelativeRates>()
            }
            .filter { it.base == outcomeCurrency } // filter responses from old already changed currency
            .map { ratesResult ->
                loadedRates = ratesResult
                CurrencyListUpdateValues(
                    ratesResult.rates.map { it.currency to calculateIncome(it.rate, outcomeAmount) }.toMap()
                )
            }
            .onErrorResumeNext(Observable.empty())
            .observeOn(schedulers.ui)
            .subscribe(stateSubject)

        Observable.interval(1, TimeUnit.SECONDS)
            .map { outcomeCurrency }
            .subscribe(updateSubject)
    }

    val listState: Observable<CurrencyListState> = stateSubject.hide()

    fun load() {
        stateSubject.onNext(CurrencyListLoadingState)
        requestSubject.onNext(outcomeCurrency)
    }

    fun updateOutcomeAmount(newOutcomeAmount: BigDecimal) {
        outcomeAmount = newOutcomeAmount

        loadedRates?.let { rates ->
            stateSubject.onNext(
                CurrencyListUpdateValues(
                    rates.rates.map { it.currency to calculateIncome(it.rate, outcomeAmount) }.toMap()
                )
            )
        }
    }

    fun updateOutcomeCurrency(newOutcomeAmount: BigDecimal, newOutcomeCurrency: Currency) {
        outcomeAmount = newOutcomeAmount
        outcomeCurrency = newOutcomeCurrency
        stateSubject.onNext(CurrencyListCurrencyMovedTopState(newOutcomeCurrency))
        stateSubject.onNext(CurrencyListHideIncomeValues)

        updateSubject.onNext(outcomeCurrency)
    }
}