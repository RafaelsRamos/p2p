package com.scookie.p2p

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.scookie.p2p.ui.theme.P2PTheme

class MainActivity : ComponentActivity() {

    private val locationPermissions = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
    private lateinit var connector: Connector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            P2PTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CheckPermission(
                        onPermissionDenied = { Toast.makeText(applicationContext, "We need the permission", Toast.LENGTH_SHORT).show() },
                        onPermissionGranted = { connector = Connector(act = this@MainActivity) }
                    )

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            text = "Discover",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clickable { connector.startDiscovery() }
                                .padding(16.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Cyan),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Advertise",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clickable { connector.startAdvertising() }
                                .padding(16.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Cyan),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Send",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { connector.sendPayload("Hello") }
                                .height(100.dp)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Cyan),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun CheckPermission(
        onPermissionDenied: () -> Unit,
        onPermissionGranted: () -> Unit

    ) {
        val locationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { permissions ->
                val permissionsGranted = permissions.values.reduce { acc, isPermissionGranted ->
                    acc && isPermissionGranted
                }

                if (!permissionsGranted) {
                    onPermissionDenied.invoke()
                } else {
                    onPermissionGranted.invoke()
                }
            })
        LaunchedEffect(true) {
            locationPermissionLauncher.launch(locationPermissions)
        }
    }

    override fun onStop() {
        connector.stop()
        super.onStop()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    P2PTheme {
        Greeting("Android")
    }
}