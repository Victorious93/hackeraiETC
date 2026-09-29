package ai.hackerai.companion

import android.content.pm.PackageManager

class DependencyGuard(private val packageManager: PackageManager) {
    companion object {
        const val DCA_PACKAGE = "ai.droidcommand.app"
    }

    fun isDcaInstalled(): Boolean =
        runCatching {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(DCA_PACKAGE, 0)
            true
        }.getOrDefault(false)
}
