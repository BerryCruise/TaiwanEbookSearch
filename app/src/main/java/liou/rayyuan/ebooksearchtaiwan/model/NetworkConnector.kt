package liou.rayyuan.ebooksearchtaiwan.model

import android.os.AsyncTask

/**
 * Created by louis383 on 2017/11/19.
 */

class NetworkConnector(private val ebookSearchService: EbookSearchService, private val type: ConnectionType, private val listener: NetworkConnectionListener) : AsyncTask<String, Int, Pair<String?, Int>>() {

    override fun doInBackground(vararg strings: String): Pair<String?, Int>? {
        return ebookSearchService.connect(strings[0], type)
    }

    override fun onPostExecute(result: Pair<String?, Int>?) {
        super.onPostExecute(result)
        val responseCode: Int? = result?.second
        when (responseCode) {
            EbookSearchService.timeout -> listener.onNetworkTimeout()
            EbookSearchService.succeed -> listener.onNetworkConnectionSucceed(result.first)
            in EbookSearchService.error .. Int.MAX_VALUE -> listener.onNetworkConnectionError(result?.first)
        }
    }
}
