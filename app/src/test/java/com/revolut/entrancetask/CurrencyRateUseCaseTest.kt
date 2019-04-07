package com.revolut.entrancetask

import com.revolut.entrancetask.domain.*
import com.revolut.entrancetask.infrastructure.AppSchedulers
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Test

import org.junit.Assert.*
import java.math.BigDecimal
import java.util.*


/**
 * Positive use case test
 *
 * - check data receiving
 * - check income amount calculation
 */
class CurrencyRateUseCaseTest {

    companion object {
        val RUB = Currency.getInstance("RUB")!!
        val USD = Currency.getInstance("USD")!!
        val EUR = Currency.getInstance("EUR")!!
    }

    private val repo: CurrencyRatesRepository = object : CurrencyRatesRepository {
        override fun loadRates(base: Currency): Single<CurrencyRelativeRates> {

            val rates = when (base) {
                USD -> listOf(
                    CurrencyRate(RUB, BigDecimal(70)),
                    CurrencyRate(EUR, BigDecimal(0.9))
                )
                EUR -> listOf(
                    CurrencyRate(RUB, BigDecimal(80)),
                    CurrencyRate(USD, BigDecimal(1.1))
                )
                else -> emptyList()
            }

            return Single.just(CurrencyRelativeRates(
                base,
                rates
            ))
        }
    }

    @Test
    fun successLoaded_isCorrect() {

        val useCase = CurrencyRatesUseCase(
            BigDecimal(10),
            EUR,
            repo,
            CurrencyFlagUrlGenerator(""),
            AppSchedulers(Schedulers.trampoline(), Schedulers.trampoline())
        )

        val resultStates: MutableList<Any> = mutableListOf()
        useCase.listState
            .subscribe { state ->
                resultStates.add(state)
            }

        useCase.load()

        assertEquals(2, resultStates.size)
        assertTrue(resultStates[0] is CurrencyListLoadingState)
        assertTrue(resultStates[1] is CurrencyListLoadedState)

        val loadedState: CurrencyListLoadedState = resultStates[1] as CurrencyListLoadedState
        val list = loadedState.list

        assertEquals(3, list.size)

        assertCurrencyListItem(CurrencyListItem(EUR, "", BigDecimal(10)),list.first())
        assertCurrencyListItem(CurrencyListItem(RUB, "", BigDecimal(800)), list[1])
        assertCurrencyListItem(CurrencyListItem(USD, "", BigDecimal(11)), list.last())
    }

    private fun assertCurrencyListItem(itemExpected: CurrencyListItem, itemActual: CurrencyListItem) {
        assertEquals(itemExpected.currency, itemActual.currency)
        assertEqualBigDecimal(itemActual.amount!!, itemExpected.amount!!)
    }

    private fun assertEqualBigDecimal(expected: BigDecimal, actual: BigDecimal) {
        assertEquals(-1, expected.minus(actual). abs().compareTo(BigDecimal(0.01)))
    }
}
