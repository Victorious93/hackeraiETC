package ai.hackerai.companion

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HackerAIApp : Application() {
    lateinit var dependencyGuard: DependencyGuard
        private set

    override fun onCreate() {
        super.onCreate()
        dependencyGuard = DependencyGuard(packageManager)
    }
}
