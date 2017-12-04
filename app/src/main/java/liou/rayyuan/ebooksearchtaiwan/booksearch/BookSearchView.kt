package liou.rayyuan.ebooksearchtaiwan.booksearch

import android.net.Uri
import liou.rayyuan.ebooksearchtaiwan.view.ViewState

/**
 * Created by louis383 on 2017/12/2.
 */

interface BookSearchView {
    fun setupInterface()
    fun bookCompanyIsEmpty()
    fun readmooIsEmpty()
    fun koboIsEmpty()
    fun taazeIsEmpty()
    fun bookWalkerIsEmpty()
    fun setMainResultView(viewState: ViewState)
    fun scrollToTop()
    fun openBookLink(uri: Uri)
    fun isInternetConnectionAvailable(): Boolean
    fun showInternetRequestDialog()
    fun showInternetConnectionTimeout()
    fun hideVirtualKeyboard()
    fun getApplicationString(stringId: Int): String
}
