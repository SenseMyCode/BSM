package com.example.secretlab

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.secretlab.data.AuthGateway
import com.example.secretlab.data.NoteBox
import com.example.secretlab.data.SecureSessionStore
import com.example.secretlab.debug.SecurityLogger
import com.example.secretlab.mfa.AttemptLimiter
import com.example.secretlab.mfa.LabMfaFixture
import com.example.secretlab.mfa.MfaLoginGateway
import com.example.secretlab.mfa.TotpEngine
import com.example.secretlab.ui.theme.SecretLabTheme
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vault = SecureSessionStore(this)
        vault.ensureProvisioned()

        val logger = SecurityLogger()
        logger.reportProvisioning(vault.readProvisioningUri().orEmpty())

        val authGateway = AuthGateway()
        val noteBox = NoteBox()
        val totpSecret = vault.readTotpSecret() ?: LabMfaFixture.TotpSecretBase32
        val provisioningUri = vault.readProvisioningUri() ?: LabMfaFixture.provisioningUri()
        val mfaGateway = MfaLoginGateway(
            authGateway = authGateway,
            totpEngine = TotpEngine(window = 1),
            limiter = AttemptLimiter(maxFailures = 3, cooldownSeconds = 30),
            secretBase32 = totpSecret,
        )

        setContent {
            SecretLabTheme {
                SecretLabApp(
                    vault = vault,
                    noteBox = noteBox,
                    logger = logger,
                    mfaGateway = mfaGateway,
                    provisioningUri = provisioningUri,
                )
            }
        }
    }
}

private enum class Pane {
    Login,
    Mfa,
    Fallback,
    Home,
}

@Composable
private fun SecretLabApp(
    vault: SecureSessionStore,
    noteBox: NoteBox,
    logger: SecurityLogger,
    mfaGateway: MfaLoginGateway,
    provisioningUri: String,
) {
    val restoredTicket = vault.readTravelCard()
    var pane by remember { mutableStateOf(if (restoredTicket != null) Pane.Home else Pane.Login) }
    var banner by remember {
        mutableStateOf(
            if (restoredTicket != null) {
                "Previous session restored from encrypted local state."
            } else {
                "Sign in with password and then complete TOTP."
            }
        )
    }
    var currentTicket by remember { mutableStateOf(restoredTicket) }
    var currentChallengeId by remember { mutableStateOf("") }
    var currentDisplayName by remember { mutableStateOf("") }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "Secret Lab MFA", style = MaterialTheme.typography.headlineMedium)
            Text(text = banner)

            when (pane) {
                Pane.Login -> LoginCard(
                    onPasswordStep = { mail, secret ->
                        val result = mfaGateway.beginPasswordSignIn(mail, secret)
                        if (result != null) {
                            currentChallengeId = result.challengeId
                            currentDisplayName = result.displayName
                            banner = "Password factor accepted. Continue with TOTP."
                            pane = Pane.Mfa
                        } else {
                            banner = "Password step failed"
                        }
                    }
                )

                Pane.Mfa -> MfaCard(
                    challengeId = currentChallengeId,
                    provisioningUri = provisioningUri,
                    onVerifyTotp = { code ->
                        val result = mfaGateway.verifyTotp(currentChallengeId, code)
                        if (result.granted) {
                            currentTicket = result.travelCard
                            currentTicket?.let(vault::saveTravelCard)
                            logger.reportMfaSuccess(currentChallengeId, result.travelCard.orEmpty())
                            banner = "MFA completed for $currentDisplayName"
                            pane = Pane.Home
                        } else {
                            banner = "MFA failed: ${result.reason}"
                        }
                    },
                    onOpenFallback = {
                        banner = "Fallback is available for account recovery. Assess whether it is weaker than the main path."
                        pane = Pane.Fallback
                    },
                    onBack = {
                        pane = Pane.Login
                        banner = "Restarted login flow."
                    },
                )

                Pane.Fallback -> FallbackCard(
                    onRecover = { mail, code ->
                        val result = mfaGateway.fallbackRecover(mail, code)
                        if (result.granted) {
                            currentTicket = result.travelCard
                            currentTicket?.let(vault::saveTravelCard)
                            logger.reportFallback(mail, code)
                            banner = "Fallback granted access without TOTP."
                            pane = Pane.Home
                        } else {
                            banner = "Fallback denied"
                        }
                    },
                    onBack = {
                        pane = Pane.Mfa
                        banner = "Return to the TOTP step."
                    },
                )

                Pane.Home -> HomeCard(
                    noteTitle = noteBox.firstNoteTitle,
                    ticketPreview = currentTicket?.take(10).orEmpty(),
                    onSignOut = {
                        vault.clearTravelCard()
                        currentTicket = null
                        currentChallengeId = ""
                        currentDisplayName = ""
                        banner = "Session cleared"
                        pane = Pane.Login
                    }
                )
            }
        }
    }
}

@Composable
private fun LoginCard(
    onPasswordStep: (String, String) -> Unit,
) {
    var mail by remember { mutableStateOf(LabMfaFixture.DemoEmail) }
    var secret by remember { mutableStateOf(LabMfaFixture.DemoPassword) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Password step", style = MaterialTheme.typography.titleLarge)
            Text("Demo account is prefilled so you can focus on the MFA flow.")
            OutlinedTextField(
                value = mail,
                onValueChange = { mail = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            OutlinedTextField(
                value = secret,
                onValueChange = { secret = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = { onPasswordStep(mail, secret) }) {
                Text("Continue to MFA")
            }
        }
    }
}

@Composable
private fun MfaCard(
    challengeId: String,
    provisioningUri: String,
    onVerifyTotp: (String) -> Unit,
    onOpenFallback: () -> Unit,
    onBack: () -> Unit,
) {
    var code by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("TOTP step", style = MaterialTheme.typography.titleLarge)
            Text("Challenge: ${challengeId.take(12)}")
            Text("Scan the QR below in an authenticator app, then type the current 6-digit code.")
            ProvisioningQr(provisioningUri = provisioningUri)
            Text("URI preview: ${provisioningUri.take(42)}...")
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("TOTP code") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onVerifyTotp(code) }) {
                    Text("Verify TOTP")
                }
                Button(onClick = onOpenFallback) {
                    Text("Use fallback")
                }
                Button(onClick = onBack) {
                    Text("Back")
                }
            }
        }
    }
}

@Composable
private fun ProvisioningQr(
    provisioningUri: String,
) {
    val qrBitmap = remember(provisioningUri) { buildQrBitmap(provisioningUri, sizePx = 640) }
    if (qrBitmap != null) {
        Image(
            bitmap = qrBitmap.asImageBitmap(),
            contentDescription = "Provisioning QR code",
            modifier = Modifier.size(220.dp),
        )
    } else {
        Text("QR generation failed. Use LabMfaFixture.provisioningUri() manually.", color = Color.Red)
    }
}

private fun buildQrBitmap(
    content: String,
    sizePx: Int,
): Bitmap? {
    return runCatching {
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
        Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888).apply {
            for (x in 0 until sizePx) {
                for (y in 0 until sizePx) {
                    setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
        }
    }.getOrNull()
}

@Composable
private fun FallbackCard(
    onRecover: (String, String) -> Unit,
    onBack: () -> Unit,
) {
    var mail by remember { mutableStateOf(LabMfaFixture.DemoEmail) }
    var code by remember { mutableStateOf(LabMfaFixture.FallbackEmailCode) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Fallback recovery", style = MaterialTheme.typography.titleLarge)
            Text("This path is intentionally weaker than the main MFA flow.")
            OutlinedTextField(
                value = mail,
                onValueChange = { mail = it },
                label = { Text("Recovery email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Email recovery code") },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onRecover(mail, code) }) {
                    Text("Recover access")
                }
                Button(onClick = onBack) {
                    Text("Back")
                }
            }
        }
    }
}

@Composable
private fun HomeCard(
    noteTitle: String,
    ticketPreview: String,
    onSignOut: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Vault", style = MaterialTheme.typography.titleLarge)
            Text("Top note: $noteTitle")
            Text("Travel card: $ticketPreview")
            Button(onClick = onSignOut) {
                Text("Sign out")
            }
        }
    }
}
