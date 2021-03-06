package liou.rayyuan.ebooksearchtaiwan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import liou.rayyuan.ebooksearchtaiwan.model.EventTracker
import liou.rayyuan.ebooksearchtaiwan.model.RemoteConfigManager
import liou.rayyuan.ebooksearchtaiwan.model.UserPreferenceManager
import org.koin.android.ext.android.inject

/**
 * Created by louis383 on 2018/9/29.
 */
abstract class BaseActivity : AppCompatActivity() {

    protected val userPreferenceManager: UserPreferenceManager by inject()
    protected val remoteConfigManager: RemoteConfigManager by inject()
    protected val eventTracker: EventTracker by inject()

    private val isOpenWithDarkTheme = userPreferenceManager.isDarkTheme()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (isDarkTheme()) {
            setTheme(R.style.NightTheme)
        }
        super.onCreate(savedInstanceState)
    }

    protected fun isDarkTheme(): Boolean = userPreferenceManager.isDarkTheme()

    protected fun isThemeChanged(): Boolean = isOpenWithDarkTheme != isDarkTheme()

    protected fun logThemeChangedEvent(isDarkThemeEnabled: Boolean) {
        val themeResult = if (isDarkThemeEnabled) {
            "dark"
        } else {
            "light"
        }

        eventTracker.logEvent(EventTracker.USER_THEME_CHOSEN, Bundle().apply {
            putString("theme", themeResult)
        })
    }

}