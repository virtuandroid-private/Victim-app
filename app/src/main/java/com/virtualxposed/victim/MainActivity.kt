package com.virtualxposed.victim

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.virtualxposed.victim.ui.theme.VictimAppTheme


class MainActivity : ComponentActivity() {
    private var privateService: IPrivateService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            privateService = IPrivateService.Stub.asInterface(service)
            isBound = true

            val response = privateService?.sendMessage("Hello Service!")
            println("Response from service: $response")
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            privateService = null
            isBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, PrivateService::class.java)
        this.bindService(intent, connection, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val serviceIntent = Intent(this, PrivateService::class.java).apply {
            putExtra("StartMessage", "Started private victim service!")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        setContent {
            VictimAppTheme {
                val viewModel: MainViewModel = viewModel(factory = viewModelFactory {
                    initializer {
                        MainViewModel().also {
                            it.init()
                            val privateDir = this@MainActivity.filesDir
                            it.createPrivateFile(privateDir)
                        }
                    }
                })

                val state = viewModel.state.collectAsState()
                StartScreen(state.value)
            }
        }
    }
}

@Composable
fun StartScreen(state: MainState, modifier: Modifier = Modifier) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            Alignment.Center,
        ) {
            Column {
                Text(
                    modifier = Modifier.padding(10.dp),
                    text = "Network data: ${state.data}",
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                )
                Text(
                    text = "Version: ${state.version}",
                    Modifier.padding(10.dp)
                )
                Text(
                    text = "Fingerprint: ${state.fingerprint}",
                    Modifier.padding(10.dp)
                )
                Text(
                    text = "Private file content: ${state.fileContent}",
                    modifier = Modifier.padding(10.dp),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VictimAppTheme {
        StartScreen(MainState())
    }
}