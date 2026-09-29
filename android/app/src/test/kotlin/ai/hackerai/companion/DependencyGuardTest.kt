package ai.hackerai.companion

import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DependencyGuardTest {
    private val pm = mockk<PackageManager>()
    private val guard = DependencyGuard(pm)

    @Test
    fun `isDcaInstalled returns true when DCA package is present`() {
        @Suppress("DEPRECATION")
        every { pm.getPackageInfo(DependencyGuard.DCA_PACKAGE, 0) } returns mockk()
        assertTrue(guard.isDcaInstalled())
    }

    @Test
    fun `isDcaInstalled returns false when DCA package is absent`() {
        @Suppress("DEPRECATION")
        every {
            pm.getPackageInfo(DependencyGuard.DCA_PACKAGE, 0)
        } throws PackageManager.NameNotFoundException()
        assertFalse(guard.isDcaInstalled())
    }

    @Test
    fun `DCA_PACKAGE constant is the correct package name`() {
        assert(DependencyGuard.DCA_PACKAGE == "ai.droidcommand.app")
    }
}
