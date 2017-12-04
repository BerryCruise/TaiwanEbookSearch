package liou.rayyuan.ebooksearchtaiwan

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.main_result_text
import kotlinx.android.synthetic.main.activity_main.main_search_button
import kotlinx.android.synthetic.main.activity_main.main_search_debug_go
import kotlinx.android.synthetic.main.activity_main.main_search_edittext
import liou.rayyuan.ebooksearchtaiwan.booksearch.BookSearchActivity
import liou.rayyuan.ebooksearchtaiwan.model.ConnectionType.GET
import liou.rayyuan.ebooksearchtaiwan.model.EbookSearchService
import liou.rayyuan.ebooksearchtaiwan.model.NetworkConnectionListener
import liou.rayyuan.ebooksearchtaiwan.model.NetworkConnector
import liou.rayyuan.ebooksearchtaiwan.model.entity.BookStores
import liou.rayyuan.ebooksearchtaiwan.model.utils.BookStoresUtils

class MainActivity : AppCompatActivity(), NetworkConnectionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main_search_button.setOnClickListener({
            val keywords: String = main_search_edittext.text.toString()

            val ebookSearchService = EbookSearchService()
            val queryStrings: HashMap<String, String> = HashMap()
            queryStrings.put("q", keywords)

            val targetURL: String? = ebookSearchService.getBooksInfo(queryStrings)
            val connector = NetworkConnector(ebookSearchService, GET, this)
            connector.execute(targetURL)
        })
        main_search_debug_go.setOnClickListener({
            val intent = Intent(this, BookSearchActivity::class.java)
            startActivity(intent)
        })
    }

    override fun onNetworkConnectionSucceed(result: String?) {
//        main_result_text.text = result
        val bookStores: BookStores? = result?.let { BookStoresUtils.convertJsonToBookStores(it) }
        main_result_text.text = bookStores!!.booksCompany!![0].title
    }

    override fun onNetworkConnectionError(result: String?) {
    }

    override fun onNetworkTimeout() {
        Toast.makeText(this, R.string.state_timeout, Toast.LENGTH_LONG).show()
    }
}
