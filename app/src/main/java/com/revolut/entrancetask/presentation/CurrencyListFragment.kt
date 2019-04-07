package com.revolut.entrancetask.presentation

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.revolut.entrancetask.R
import com.revolut.entrancetask.domain.*
import com.revolut.entrancetask.provideCurrencyListPresenter
import io.reactivex.Observable
import kotlinx.android.synthetic.main.currency_list_fragment.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator


class CurrencyListFragment : Fragment(), CurrencyListView {
    private lateinit var presenter: CurrencyListPresenter

    private val list: MutableList<CurrencyListItem> = mutableListOf()
    private var adapter: CurrencyListAdapter = CurrencyListAdapter(list)

    override val outcomeAmount: Observable<String>
        get() = adapter.outcomeAmount

    override val currencySelection: Observable<CurrencyClickEvent>
        get() = adapter.currencySelection

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        presenter = provideCurrencyListPresenter()

        return inflater.inflate(R.layout.currency_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context)
        currencyRecycler.layoutManager = layoutManager
        currencyRecycler.adapter = adapter

        // disable animation for updating item content
        (currencyRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        presenter.bindView(this)
    }

    override fun showList(listState: CurrencyListState) {
        when (listState) {
            is CurrencyListLoadedState -> {
                progressView.visibility = View.GONE
                errorView.visibility = View.GONE
                currencyRecycler.visibility = View.VISIBLE

                list.clear()
                list.addAll(listState.list)
                adapter.notifyDataSetChanged()
            }
            is CurrencyListCurrencyMovedTopState -> {
                val position = list.indexOfFirst { it.currency == listState.currency }
                val data = list[position]

                list.removeAt(position)
                list.add(0, data)
                adapter.notifyItemMoved(position, 0)
                adapter.notifyItemRangeChanged(0, 2)

                Handler().post(waitForAnimationsToFinishRunnable)
            }
            is CurrencyListUpdateValues -> {
                list.forEachIndexed { index, item ->
                    if (index > 0) {
                        listState.newValues[item.currency]?.let {
                            item.amount = it
                        }
                    }
                }
                currencyRecycler.post {
                    adapter.notifyItemRangeChanged(1, list.size - 1)
                }
            }
            is CurrencyListHideIncomeValues -> {
                list.forEachIndexed { index, item ->
                    if (index > 0) {
                        item.amount = null
                    }
                }
                currencyRecycler.post {
                    adapter.notifyItemRangeChanged(1, list.size - 1)
                }
            }
            is CurrencyListErrorState -> {
                currencyRecycler.visibility = View.GONE
                progressView.visibility = View.GONE
                errorView.visibility = View.VISIBLE
            }
            is CurrencyListLoadingState -> {
                currencyRecycler.visibility = View.GONE
                progressView.visibility = View.VISIBLE
                errorView.visibility = View.GONE
            }
        }
    }

    private val waitForAnimationsToFinishRunnable = Runnable { waitForAnimationsToFinish() }

    private fun waitForAnimationsToFinish() {
        if (currencyRecycler.isAnimating) {
            currencyRecycler.itemAnimator!!.isRunning(animationFinishedListener)
            return
        }

        onRecyclerViewAnimationsFinished()
    }

    private val animationFinishedListener = RecyclerView.ItemAnimator.ItemAnimatorFinishedListener {
        Handler().post(waitForAnimationsToFinishRunnable)
    }

    private fun onRecyclerViewAnimationsFinished() {
        currencyRecycler.scrollToPosition(0)
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }
}