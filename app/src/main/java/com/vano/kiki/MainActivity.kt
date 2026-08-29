package com.vano.kiki

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vano.kiki.input.GamepadDetector
import com.vano.kiki.mapping.AppInfoUi
import com.vano.kiki.mapping.AppPickerDialog
import com.vano.kiki.mapping.MappedAppStore
import com.vano.kiki.overlay.OverlayService

private val PinkBg = Color(0xFFFFE1F5)
private val PinkAccent = Color(0xFFFF7FD1)
private val PinkAccentLight = Color(0xFFFFC2EC)
private val PinkCard = Color(0xFFFFF0FA)

private val KikiColors = lightColorScheme(
    primary = PinkAccent,
    onPrimary = Color.White,
    background = PinkBg,
    surface = Color.White,
    outline = PinkAccent
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = KikiColors) {
                KikiHomeScreen()
            }
        }
    }
}

@Composable
fun KikiHomeScreen() {
    val context = LocalContext.current
    val detector = remember { GamepadDetector(context) }
    val gamepad by detector.state.collectAsState()

    val mappedApps = remember {
        mutableStateListOf<AppInfoUi>().apply { addAll(MappedAppStore.load(context)) }
    }
    var showAppPicker by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        detector.start()
        onDispose { detector.stop() }
    }

    if (showAppPicker) {
        AppPickerDialog(
            onDismiss = { showAppPicker = false },
            onAppSelected = { app ->
                if (mappedApps.none { it.packageName == app.packageName }) {
                    mappedApps.add(app)
                    MappedAppStore.save(context, mappedApps)
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(PinkBg).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().background(
                brush = Brush.verticalGradient(listOf(PinkAccentLight, PinkAccent)),
                shape = RoundedCornerShape(28.dp)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "\uD83C\uDFAE", fontSize = 56.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Card(shape = RoundedCornerShape(50), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Text(
                        text = if (gamepad.connected) "Terhubung" else "Tidak terhubung",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (gamepad.connected) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = gamepad.deviceName ?: "", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = PinkCard),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("\u2705 kiki telah diaktifkan", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (gamepad.connected) "\u2705 Perangkat terhubung" else "\u2B1C Perangkat terhubung",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Card(
                modifier = Modifier.weight(0.6f),
                colors = CardDefaults.cardColors(containerColor = PinkCard),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Logcat", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("aplikasi pemetaan", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "\uFF0B",
                        color = PinkAccent,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showAppPicker = true }
                    )
                    Text("\u2699", color = PinkAccent, fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (mappedApps.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada aplikasi. Tap + buat nambah.", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            } else {
                mappedApps.forEach { app ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(app.label, fontWeight = FontWeight.Medium)
                            OutlinedButton(onClick = {
                                if (!Settings.canDrawOverlays(context)) {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                    )
                                } else {
                                    context.startForegroundService(Intent(context, OverlayService::class.java))
                                    context.packageManager.getLaunchIntentForPackage(app.packageName)?.let {
                                        context.startActivity(it)
                                    }
                                }
                            }) {
                                Text("mulai")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(onClick = { /* TODO: keluar aman */ }, modifier = Modifier.fillMaxWidth()) {
            Text("keluar aman dari kiki")
        }
    }
}
