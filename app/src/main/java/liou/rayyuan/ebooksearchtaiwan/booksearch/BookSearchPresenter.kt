package liou.rayyuan.ebooksearchtaiwan.booksearch

import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import liou.rayyuan.ebooksearchtaiwan.Presenter
import liou.rayyuan.ebooksearchtaiwan.model.*
import liou.rayyuan.ebooksearchtaiwan.model.entity.Book
import liou.rayyuan.ebooksearchtaiwan.model.entity.BookStores
import liou.rayyuan.ebooksearchtaiwan.utils.DefaultStoreNames
import liou.rayyuan.ebooksearchtaiwan.view.BookResultAdapter
import liou.rayyuan.ebooksearchtaiwan.view.BookResultClickHandler
import liou.rayyuan.ebooksearchtaiwan.view.BookResultView
import liou.rayyuan.ebooksearchtaiwan.view.FullBookStoreResultAdapter
import liou.rayyuan.ebooksearchtaiwan.view.ViewState.*
import liou.rayyuan.ebooksearchtaiwan.viewmodel.BookListViewModel
import okhttp3.ResponseBody

/**
 * Created by louis383 on 2017/12/2.
 */
class BookSearchPresenter(private val apiManager: APIManager, private val eventTracker: EventTracker) : Presenter<BookSearchView>,
        LifecycleObserver, OnNetworkConnectionListener, BookResultClickHandler {

    private var view: BookSearchView? = null
    private lateinit var bookListViewModel: BookListViewModel

    private lateinit var fullBookStoreResultsAdapter: FullBookStoreResultAdapter
    private val maxListNumber: Int = 10
    private var eggCount: Int = 0
    private var bookStores: BookStores? = null
    private val defaultResultSort =
            setOf(DefaultStoreNames.READMOO,
                    DefaultStoreNames.KOBO,
                    DefaultStoreNames.BOOK_WALKER,
                    DefaultStoreNames.BOOK_COMPANY,
                    DefaultStoreNames.TAAZE,
                    DefaultStoreNames.PLAY_STORE,
                    DefaultStoreNames.PUBU,
                    DefaultStoreNames.HYREAD)

    override fun attachView(view: BookSearchView) {
        this.view = view
        view.setupUI()

        bookListViewModel = view.getViewModelProvider().get(BookListViewModel::class.java)
        bookListViewModel.apiManager = apiManager
        bookListViewModel.networkConnectionListener = this
        if (bookListViewModel.isRequestingData()) {
            view.setMainResultView(PREPARE)
        }

        val bookLiveData = bookListViewModel.getBookList(force = false)
        bookLiveData?.let { liveData ->
            liveData.listener = this
            // Restore ViewModels
            liveData.observe(view.getLifeCycleOwner(), Observer {
                prepareBookSearchResult(it)
            })
        }

        view.getLifeCycleOwner().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun detachView() {
        this.view = null
    }

    fun hintPressed() {
        view?.focusBookSearchEditText()

        eggCount++
        if (eggCount == 10) {
            view?.showEasterEgg01()
            eventTracker.logEvent(EventTracker.SHOW_EASTER_EGG_01)
        }
        eventTracker.logEvent(EventTracker.CLICK_INFO_BUTTON)
    }

    fun backToTop(canResultListScrollVertically: Boolean) {
        if (canResultListScrollVertically) {
            view?.backToListTop()
            eventTracker.logEvent(EventTracker.CLICK_BACK_TO_TOP_BUTTON)
        } else {
            view?.focusBookSearchEditText()
            eventTracker.logEvent(EventTracker.CLICK_TO_SEARCH_BUTTON)
        }
    }

    fun searchBook(keyword: String) {
        if (!this::bookListViewModel.isInitialized) {
            return
        }

        view?.let {
            if (keyword.trim().isNotBlank()) {
                it.hideVirtualKeyboard()

                if (it.isInternetConnectionAvailable()) {

                    if (bookListViewModel.isRequestingData()) {
                        bookListViewModel.forceStop()
                    }

                    bookListViewModel.getBookList(keyword, true)
                            ?.observe(it.getLifeCycleOwner(), Observer { bookStores ->
                                prepareBookSearchResult(bookStores)
                            })

                    it.setMainResultView(PREPARE)
                    resetCurrentResults()
                } else {
                    it.showInternetRequestDialog()
                }

            } else {
                it.showKeywordIsEmpty()
            }
        }
    }

    private fun resetCurrentResults() {
        fullBookStoreResultsAdapter.clean()
    }

    fun setResultRecyclerView(recyclerView: RecyclerView) {
        fullBookStoreResultsAdapter = FullBookStoreResultAdapter(this)
        recyclerView.adapter = fullBookStoreResultsAdapter
    }

    private fun prepareBookSearchResult(bookStores: BookStores?) {
        this.bookStores = bookStores

        view?.scrollToTop()
        val bestResultsAdapter = BookResultAdapter(false, -1, DefaultStoreNames.BEST_RESULT, eventTracker)

        bookStores?.generateBookStoresResultMap(defaultResultSort)?.let { resultMap ->
            for (defaultStoreName in resultMap.keys) {
                resultMap[defaultStoreName]?.run {
                    addResult(bestResultsAdapter, defaultStoreName, this)
                }
            }
        }

        if (bestResultsAdapter.itemCount > 1) {
            bestResultsAdapter.sortByMoney()
        }

        val bestResultView = BookResultView(DefaultStoreNames.BEST_RESULT, bestResultsAdapter)
        fullBookStoreResultsAdapter.addResultToBeginning(bestResultView)
        view?.setMainResultView(READY)
    }

    private fun addResult(bestAdapter: BookResultAdapter, defaultStoreName: DefaultStoreNames, books: List<Book>) {
        if (books.isNotEmpty()) {
            bestAdapter.addBook(books.first(), defaultStoreName)
        }
        fullBookStoreResultsAdapter.addResult(generateBookResultView(defaultStoreName, maxListNumber, books))
    }

    private fun generateBookResultView(defaultStoreName: DefaultStoreNames, maxListNumber: Int, books: List<Book>): BookResultView {
        val bookResultAdapter = BookResultAdapter(true, maxListNumber, defaultStoreName, eventTracker)
        bookResultAdapter.setBooks(books)
        return BookResultView(defaultStoreName, bookResultAdapter)
    }

    fun logThemeChangedEvent(isDarkThemeEnabled: Boolean) {
        val themeResult = if (isDarkThemeEnabled) {
            "dark"
        } else {
            "light"
        }

        eventTracker.logEvent(EventTracker.USER_THEME_CHOSEN, Bundle().apply {
            putString("theme", themeResult)
        })
    }

    fun logISBNScanningSucceed() {
        eventTracker.logEvent(EventTracker.SCANNED_ISBN)
    }

    //region OnNetworkConnectionListener
    override fun onNetworkTimeout() {
        view?.showInternetConnectionTimeout()
    }

    override fun onNetworkErrorOccurred(errorBody: ResponseBody?) {
        view?.setMainResultView(ERROR)
        view?.showNetworkErrorMessage()
    }

    override fun onExceptionOccurred(message: String) {
        view?.setMainResultView(ERROR)
        if (message == NetworkLiveData.GENERIC_NETWORK_ISSUE) {
            view?.showNetworkErrorMessage()
        } else {
            view?.showToast(message)
        }
    }
    //endregion

    //region BookResultClickHandler
    override fun onBookCardClicked(book: Book) {
        view?.openBookLink(Uri.parse(book.link))
    }
    //endregion
}