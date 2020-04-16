package com.hefny.hady.blogpost.ui.main.blog

import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.hefny.hady.blogpost.R
import com.hefny.hady.blogpost.models.BlogPost
import com.hefny.hady.blogpost.persistence.BlogQueryUtils
import com.hefny.hady.blogpost.ui.DataState
import com.hefny.hady.blogpost.ui.main.blog.state.BlogViewState
import com.hefny.hady.blogpost.ui.main.blog.viewmodel.*
import com.hefny.hady.blogpost.util.ErrorHandling
import com.hefny.hady.blogpost.util.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_blog.*

class BlogFragment : BaseBlogFragment(), BlogListAdapter.Interaction,
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var recyclerAdapter: BlogListAdapter
    private lateinit var searchView: SearchView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)
        swipe_refresh.setOnRefreshListener(this)
        initRecyclerView()
        subscribeObservers()
        if (savedInstanceState == null) {
            viewModel.loadFirstPage()
        }
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if (dataState != null) {
                // call before onDataStateChange to consume error if there is one
                handlePagination(dataState)
                stateChangeListener.onDataStateChange(dataState)
            }
            viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
                Log.d(TAG, "BlogFragment, viewState: $viewState")
                if (viewState != null) {
                    recyclerAdapter.submitList(
                        blogList = viewState.blogFields.blogList,
                        isQueryExhausted = viewState.blogFields.isQueryExhausted
                    )
                }
            })
        })
    }

    private fun handlePagination(dataState: DataState<BlogViewState>) {
        // Handle incoming data from DataState
        dataState.data?.let {
            it.data?.let {
                it.getContentIfNotHandled()?.let {
                    viewModel.handleIncomingBlogListData(it)
                }
            }
        }

        // Check for pagination end (no more results)
        // must do this b/c server will return an ApiErrorResponse if page is not valid,
        // -> meaning there is no more data.
        dataState.error?.let { event ->
            event.peekContent().response.message?.let {
                if (ErrorHandling.isPaginationDone(it)) {

                    // handle the error message event so it doesn't display in UI
                    event.getContentIfNotHandled()

                    // set query exhausted to update RecyclerView with
                    // "No more results..." list item
                    viewModel.setQueryExhausted(true)
                }
            }
        }
    }

    private fun initRecyclerView() {
        blog_post_recyclerview.apply {
            layoutManager = LinearLayoutManager(this@BlogFragment.context)
            val topSpacingDecorator = TopSpacingItemDecoration(30)
            removeItemDecoration(topSpacingDecorator) // does nothing if not applied already
            addItemDecoration(topSpacingDecorator)

            recyclerAdapter = BlogListAdapter(requestManager, this@BlogFragment)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastPosition == recyclerAdapter.itemCount.minus(1)) {
                        Log.d(TAG, "BlogFragment: attempting to load next page...")
                        viewModel.loadNextPage()
                    }
                }
            })
            adapter = recyclerAdapter
        }
    }

    private fun onBlogSearchOrFilter() {
        viewModel.loadFirstPage().let {
            resetUi()
        }
    }

    private fun resetUi() {
        blog_post_recyclerview.smoothScrollToPosition(0)
        keyboardManagement.hideSoftKeyboard()
    }

    private fun initSearchView(menu: Menu) {
        activity?.apply {
            val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
            searchView = menu.findItem(R.id.action_search).actionView as SearchView
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView.maxWidth = Integer.MAX_VALUE
            searchView.setIconifiedByDefault(true)
            searchView.isSubmitButtonEnabled = true

            // Enter on computer keyboard or Arrow on virtual keyboard
            val searchPlate = searchView.findViewById(R.id.search_src_text) as EditText
            searchPlate.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                    || actionId == EditorInfo.IME_ACTION_SEARCH
                ) {
                    val searchQuery = v.text.toString()
                    Log.e(TAG, "SearchView: (Keyboard or Arrow), executing search... $searchQuery")
                    viewModel.setQuery(searchQuery).let {
                        onBlogSearchOrFilter()
                    }
                }
                true
            }
            // Search button clicked (in toolbar)
            val searchButton = searchView.findViewById(R.id.search_go_btn) as View
            searchButton.setOnClickListener {
                val searchQuery = searchPlate.text.toString()
                Log.e(TAG, "SearchView: (Button), executing search... $searchQuery")
                viewModel.setQuery(searchQuery).let {
                    onBlogSearchOrFilter()
                }
            }
        }
    }

    override fun onItemSelected(position: Int, item: BlogPost) {
        viewModel.setBlogPost(item)
        findNavController().navigate(R.id.action_blogFragment_to_viewBlogFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // clear references (can leak memory)
        blog_post_recyclerview.adapter = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)
        initSearchView(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter_settings -> {
                showFilterOptions()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRefresh() {
        onBlogSearchOrFilter()
        swipe_refresh.isRefreshing = false
    }

    private fun showFilterOptions() {
        // 0) show dialog
        activity?.let {
            val dialog = MaterialDialog(it)
                .noAutoDismiss()
                .customView(R.layout.layout_blog_filter)
            val view = dialog.getCustomView()
            // 1) highlight the previous filter and order options
            val filter = viewModel.getFilter()
            if (filter == BlogQueryUtils.BLOG_FILTER_DATE_UPDATED) {
                view.findViewById<RadioGroup>(R.id.filter_group).check(R.id.filter_date)
            } else {
                view.findViewById<RadioGroup>(R.id.filter_group).check(R.id.filter_author)
            }
            val order = viewModel.getOrder()
            if (order == BlogQueryUtils.BLOG_ORDER_ASC) {
                view.findViewById<RadioGroup>(R.id.order_group).check(R.id.filter_asc)
            } else {
                view.findViewById<RadioGroup>(R.id.order_group).check(R.id.filter_desc)
            }
            // 2) listen for newly applied filters
            view.findViewById<TextView>(R.id.positive_button).setOnClickListener {
                Log.d(TAG, "Filter dialog: applying filters")
                val selectedFilter =
                    view.findViewById<RadioButton>(
                        view
                            .findViewById<RadioGroup>(R.id.filter_group).checkedRadioButtonId
                    )
                val selectedOrder =
                    view.findViewById<RadioButton>(
                        view.findViewById<RadioGroup>(R.id.order_group).checkedRadioButtonId
                    )
                var filter = BlogQueryUtils.BLOG_FILTER_DATE_UPDATED
                if (selectedFilter.text.toString() == getString(R.string.filter_author)) {
                    filter = BlogQueryUtils.BLOG_FILTER_USERNAME
                }
                var order = BlogQueryUtils.BLOG_ORDER_ASC
                if (selectedOrder.text.toString() == getString(R.string.filter_desc)) {
                    order = BlogQueryUtils.BLOG_ORDER_DESC
                }
                // 3) set the filter and order in the viewmodel
                // 4) save to shared preferences
                viewModel.saveFilterOptions(filter, order).let {
                    viewModel.setBLogFilter(filter)
                    viewModel.setBLogOrder(order)
                    onBlogSearchOrFilter()
                }
                dialog.dismiss()
            }
            view.findViewById<TextView>(R.id.negative_button).setOnClickListener {
                Log.d(TAG, "Filter dialog: cancelling filters")
                dialog.dismiss()
            }
            dialog.show()
        }
    }
}