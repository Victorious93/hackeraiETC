package ai.hackerai.companion

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DependencyGuard @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val DCA_PACKAGE = "ai.droidcommand.app"
    }

    fun isDcaInstalled(): Boolean =
        runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(DCA_PACKAGE, 0)
            true
        }.getOrDefault(false)
}
