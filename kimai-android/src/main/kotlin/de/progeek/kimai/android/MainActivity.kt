package de.progeek.kimai.android

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import de.progeek.kimai.shared.core.di.initKoin
import de.progeek.kimai.shared.core.repositories.timesheet.TimesheetRepository
import de.progeek.kimai.shared.ui.ContentView
import de.progeek.kimai.shared.ui.root.RootComponent
import de.progeek.kimai.shared.utils.isNull
import de.progeek.kimai.shared.utils.notNull
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.stopKoin

class MainActivity : ComponentActivity() {
    private val timesheetRepository by inject<TimesheetRepository>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initKoin() {
            androidContext(applicationContext)
        }

        val root = RootComponent(
            componentContext = defaultComponentContext(),
            storeFactory = DefaultStoreFactory()
        )

        setContent {
            ContentView(component = root)
        }


        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setupNotificationService()
        } else {
            val activityResultLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    setupNotificationService()
                }
            }

            activityResultLauncher.launch(
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    private fun setupNotificationService() {
        val intent = Intent(this, NotificationService::class.java)

        lifecycleScope.launch {
            timesheetRepository.getActiveTimesheetStream().collect {
                it.isNull {
                    stopService(intent)
                }.notNull {
                    startService(intent)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopKoin()
    }
}
