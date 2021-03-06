package liou.rayyuan.ebooksearchtaiwan.booksearch

import liou.rayyuan.ebooksearchtaiwan.model.entity.AdapterItem

/**
 * Created by louis383 on 2017/12/4.
 */
internal sealed class ListViewState {
    class Prepare(val scrollToTop: Boolean = false) : ListViewState()
    class Ready(val scrollPosition: Int, val adapterItems: List<AdapterItem>) : ListViewState()
    object Error : ListViewState()
}