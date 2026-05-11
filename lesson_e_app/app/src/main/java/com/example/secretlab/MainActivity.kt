package com.example.secretlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.example.secretlab.secure.BiometricBoundSecretStore
import com.example.secretlab.secure.BiometricPromptGate
import com.example.secretlab.secure.GateToken
import com.example.secretlab.secure.SecureKeyProvider
import com.example.secretlab.secure.SecureNoteRepository
import com.example.secretlab.secure.SecretBox
import com.example.secretlab.ui.theme.SecretLabTheme
import java.security.SecureRandom

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val random = SecureRandom()
        val clock: () -> Long = { System.currentTimeMillis() / 1000L }
        val secretBox = SecretBox(SecureKeyProvider(this, random), random)
        val store = BiometricBoundSecretStore(
            secretBox = secretBox,
            clock = clock,
            maxTokenAgeSeconds = 30,
        )
        val noteRepo = SecureNoteRepository(this)

        val iv = ByteArray(SecretBox.IV_BYTES).also(random::nextBytes)
        store.setSecret(
            plaintextSecret = "otpauth://totp/demo?secret=BASE32SEED".encodeToByteArray(),
            iv = iv,
        )

        setContent {
            SecretLabTheme {
                LessonEApp(
                    activity = this,
                    store = store,
                    noteRepo = noteRepo,
                    secretBox = secretBox,
                    clock = clock,
                    biometricGate = BiometricPromptGate(clock = clock),
                )
            }
        }
    }
}

@Composable
private fun LessonEApp(
    activity: FragmentActivity,
    store: BiometricBoundSecretStore,
    noteRepo: SecureNoteRepository,
    secretBox: SecretBox,
    clock: () -> Long,
    biometricGate: BiometricPromptGate,
) {
    var banner by remember { mutableStateOf("Goal: protect a local secret behind a local user gate (simulated).") }
    var tokenIssuedAt by remember { mutableStateOf<Long?>(null) }
    var secretPreview by remember { mutableStateOf<String?>(null) }
    var gateNowEpochSeconds by remember { mutableStateOf(clock().toString()) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "Secret Lab — Local Gate", style = MaterialTheme.typography.headlineMedium)
            Text(text = banner)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Simulated gate", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "In the lab you’ll enforce the gate policy in code and verify it via unit tests. " +
                            "This screen simulates issuing a token after a successful local user-auth gate.",
                    )
                    OutlinedTextField(
                        value = gateNowEpochSeconds,
                        onValueChange = { },
                        label = { Text("Now (epoch seconds)") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(onClick = {
                        val now = clock()
                        gateNowEpochSeconds = now.toString()
                        tokenIssuedAt = now
                        banner = "Gate satisfied at t=$now. Token issued."
                    }) {
                        Text("Issue GateToken")
                    }

                    Button(onClick = {
                        if (!biometricGate.canAuthenticate(activity)) {
                            banner = "BiometricPrompt unavailable on this device/emulator."
                            return@Button
                        }
                        biometricGate.authenticate(activity) { token ->
                            if (token == null) {
                                banner = "Biometric auth failed/canceled."
                            } else {
                                tokenIssuedAt = token.issuedAtEpochSeconds
                                gateNowEpochSeconds = tokenIssuedAt.toString()
                                banner = "Biometric gate satisfied. Token issued."
                            }
                        }
                    }) {
                        Text("Verify biometrics (real prompt)")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Reveal secret (policy protected)", style = MaterialTheme.typography.titleLarge)
                    Text("Result is intentionally shown as a short preview.")
                    Button(onClick = {
                        val token = tokenIssuedAt?.let { GateToken(issuedAtEpochSeconds = it) }
                        val revealed = store.revealSecret(token)
                        secretPreview = revealed?.decodeToString()?.take(24)
                        banner = if (revealed == null) "Access denied (gate not satisfied)." else "Access granted."
                    }) {
                        Text("Reveal")
                    }
                    Text(text = "Preview: ${secretPreview ?: "—"}")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Secure note (encrypted on close)", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "This is a practical demo: open a long multi-line note behind the gate, edit it, then close to encrypt.",
                    )
                    SecureNoteCard(
                        activity = activity,
                        gate = biometricGate,
                        clock = clock,
                        store = store,
                        noteRepo = noteRepo,
                        secretBox = secretBox,
                        onBanner = { banner = it },
                    )
                }
            }
        }
    }
}

@Composable
private fun SecureNoteCard(
    activity: FragmentActivity,
    gate: BiometricPromptGate,
    clock: () -> Long,
    store: BiometricBoundSecretStore,
    noteRepo: SecureNoteRepository,
    secretBox: SecretBox,
    onBanner: (String) -> Unit,
) {
    var opened by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current

    if (!opened) {
        Button(onClick = {
            if (!gate.canAuthenticate(activity)) {
                onBanner("BiometricPrompt unavailable on this device/emulator.")
                return@Button
            }
            gate.authenticate(activity) { token ->
                if (token == null) {
                    onBanner("Biometric auth failed/canceled.")
                    return@authenticate
                }
                val allowed = store.revealSecret(token) != null
                if (!allowed) {
                    onBanner("Gate token rejected by policy (too old/future).")
                    return@authenticate
                }
                val message = noteRepo.readEncryptedNote()
                noteText = if (message == null) {
                    ""
                } else {
                    secretBox.decryptBound(message, NOTE_CONTEXT)?.decodeToString() ?: ""
                }
                opened = true
                onBanner("Secure note opened.")
            }
        }) {
            Text("Open secure note (biometrics)")
        }
        return
    }

    OutlinedTextField(
        value = noteText,
        onValueChange = { noteText = it },
        label = { Text("Secure note (multi-line)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 6,
    )
    Button(onClick = {
        keyboard?.hide()
        val iv = secretBox.generateIv()
        val message = secretBox.encryptBound(noteText.encodeToByteArray(), iv, NOTE_CONTEXT)
        noteRepo.writeEncryptedNote(message)
        opened = false
        onBanner("Secure note encrypted and saved.")
    }) {
        Text("Close & encrypt")
    }
}

private val NOTE_CONTEXT: ByteArray = "bsm:l05e:secure_note:v1".encodeToByteArray()
