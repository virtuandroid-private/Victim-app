package com.virtualxposed.victim

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.webkit.WebSettings.LOAD_NO_CACHE
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

        startService(serviceIntent)

        setContent {
            VictimAppTheme {
                val viewModel: MainViewModel = viewModel(factory = viewModelFactory {
                    initializer {
                        MainViewModel().also {
                            it.init()
                            val privateDir = this@MainActivity.filesDir
                            it.createPrivateFile(privateDir)
                            it.createAccount(privateDir)
                        }
                    }
                })

                val state = viewModel.state.collectAsState()
                StartScreen(state.value)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(state: MainState, modifier: Modifier = Modifier) {
    var showWebView by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Victim app", fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Spoofable data",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                    InfoRow(label = "Network Data", value = state.data)
                    InfoRow(label = "Version", value = state.version)
                    InfoRow(label = "Fingerprint", value = state.fingerprint, maxLines = 3)
                    InfoRow(label = "Private File Content", value = state.fileContent)
                    InfoRow(label = "Account ID", value = state.accountId)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { showWebView = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Webview")
            }
        }
    }

    val context = LocalContext.current
    val webView = remember {
        WebView.setWebContentsDebuggingEnabled(false)
        WebView(context).apply {
            this.loadUrl("https://example.com")
            this.settings.cacheMode = LOAD_NO_CACHE
            this.settings.blockNetworkLoads = false
            this.settings.blockNetworkImage = false
        }
    }

    if (showWebView) {
        Dialog(
            onDismissRequest = { showWebView = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showWebView = false }) {
                            Text("Close")
                        }
                    }

                    AndroidView(
                        modifier = modifier.fillMaxSize(),
                        factory = {
                            webView
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String?, maxLines: Int = 2) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value ?: "",
            style = MaterialTheme.typography.bodyLarge,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}
