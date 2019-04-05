package com.revolut.entrancetask.presentation

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.revolut.entrancetask.R
import com.revolut.entrancetask.domain.*
import com.revolut.entrancetask.provideCurrencyListPresenter
import io.reactivex.Observable
import kotlinx.android.synthetic.main.currency_list_fragment.*
import java.math.BigDecimal
import java.util.*
import androidx.recyclerview.widget.RecyclerView



class CurrencyListFragment : Fragment(), CurrencyListView {
    private lateinit var presenter: CurrencyListPresenter

    private val list: MutableList<CurrencyListItem> = mutableListOf()
    private var adapter: CurrencyListAdapter = CurrencyListAdapter(list)

    override val outcomeAmount: Observable<String>
        get() = adapter.outcomeAmount

    override val currencySelection: Observable<CurrencyClickEvent>
        get() = adapter.currencySelection.doOnNext { event ->
            val position = list.indexOfFirst { it.currency == event.currency }

            val data = list[position]

            list.removeAt(position)
            list.add(0, data)

            Handler().post(waitForAnimationsToFinishRunnable)
            adapter.notifyItemMoved(position, 0)
            adapter.notifyItemRangeChanged(1, list.size - 1)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        presenter = provideCurrencyListPresenter()

        return inflater.inflate(R.layout.currency_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context)
        currencyRecycler.layoutManager = layoutManager
        currencyRecycler.adapter = adapter

        presenter.bindView(this)
    }

    override fun showList(listState: CurrencyListState) {
        when (listState) {
            is CurrencyListLoadedState -> {
                currencyRecycler.visibility = View.VISIBLE

                val diffCallback = CurrencyListDiffUtilCallBack(list, listState.list)
                val diffResult = DiffUtil.calculateDiff(diffCallback)
                list.clear()
                list.addAll(listState.list)

                currencyRecycler.post {
                    diffResult.dispatchUpdatesTo(adapter)
                    //Handler().post(waitForAnimationsToFinishRunnable)
//                    currencyRecycler.post {
//                        if (listState.focusOutcome) {
//                            currencyRecycler.scrollToPosition(0)
//                        }
//                    }
                }
            }
            is CurrencyListErrorState -> {
                currencyRecycler.visibility = View.GONE
            }
            is CurrencyListLoadingState -> {
                currencyRecycler.visibility = View.GONE
            }
        }
    }

    fun changeAdapterData() {
        // ...
        // Changes are made to the data held by the adapter
        currencyRecycler.getAdapter()!!.notifyDataSetChanged()

        // The recycler view have not started animating yet, so post a message to the
        // message queue that will be run after the recycler view have started animating.
        Handler().post(waitForAnimationsToFinishRunnable)
    }

    private val waitForAnimationsToFinishRunnable = Runnable { waitForAnimationsToFinish() }

    // When the data in the recycler view is changed all views are animated. If the
    // recycler view is animating, this method sets up a listener that is called when the
    // current animation finishes. The listener will call this method again once the
    // animation is done.
    private fun waitForAnimationsToFinish() {
        if (currencyRecycler.isAnimating()) {
            // The recycler view is still animating, try again when the animation has finished.
            currencyRecycler.getItemAnimator()!!.isRunning(animationFinishedListener)
            return
        }

        // The recycler view have animated all it's views
        onRecyclerViewAnimationsFinished()
    }

    // Listener that is called whenever the recycler view have finished animating one view.
    private val animationFinishedListener = RecyclerView.ItemAnimator.ItemAnimatorFinishedListener {
        // The current animation have finished and there is currently no animation running,
        // but there might still be more items that will be animated after this method returns.
        // Post a message to the message queue for checking if there are any more
        // animations running.
        Handler().post(waitForAnimationsToFinishRunnable)
    }

    // The recycler view is done animating, it's now time to doStuff().
    private fun onRecyclerViewAnimationsFinished() {
        currencyRecycler.scrollToPosition(0)

        currencyRecycler.post {


        }
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