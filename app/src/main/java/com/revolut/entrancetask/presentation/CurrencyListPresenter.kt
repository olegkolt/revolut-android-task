package com.revolut.entrancetask.presentation

import android.util.Log
import com.revolut.entrancetask.domain.CurrencyRatesUseCase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class CurrencyListPresenter(
    private val useCase: CurrencyRatesUseCase
) {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var view: CurrencyListView? = null

    fun onStart() {
        view!!.let { view ->
            compositeDisposable.add(
                useCase.listState
                    .subscribe ({
                        view.showList(it)
                    }, {
                        Log.d("TTT", "error", it) //@todo
                    })
            )

            compositeDisposable.add(
                view.outcomeAmount
                    .subscribe {
                        val amount = try {
                            BigDecimal(it)
                        } catch (ex: NumberFormatException) {
                            BigDecimal.ZERO
                        }
                        useCase.updateOutcomeAmount(amount)
                    }
            )

            compositeDisposable.add(
                view.currencySelection
                    .subscribe {
                        //useCase.updateOutcomeCurrency(it.amount, it.currency)
                    }
            )

            useCase.load()
        }
    }

    fun onStop() {
        compositeDisposable.dispose()
    }

    fun bindView(view: CurrencyListView) {
        this.view = view
    }
}