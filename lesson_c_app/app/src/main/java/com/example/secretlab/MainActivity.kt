package com.example.secretlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.secretlab.data.AuthGateway
import com.example.secretlab.data.InsecureSessionStore
import com.example.secretlab.data.LocalAccountVault
import com.example.secretlab.data.NoteBox
import com.example.secretlab.data.SecureSessionStore
import com.example.secretlab.debug.SecurityLogger
import com.example.secretlab.ui.theme.SecretLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authGateway = AuthGateway()
        val stash = InsecureSessionStore(this)
        val accountVault = LocalAccountVault(this)
        val vault = SecureSessionStore(this)
        val noteBox = NoteBox()
        val logger = SecurityLogger()

        setContent {
            SecretLabTheme {
                SecretLabApp(
                    authGateway = authGateway,
                    stash = stash,
                    accountVault = accountVault,
                    vault = vault,
                    noteBox = noteBox,
                    logger = logger,
                )
            }
        }
    }
}

private enum class Pane {
    Login,
    CreateAccount,
    Home,
}

@Composable
private fun SecretLabApp(
    authGateway: AuthGateway,
    stash: InsecureSessionStore,
    accountVault: LocalAccountVault,
    vault: SecureSessionStore,
    noteBox: NoteBox,
    logger: SecurityLogger,
) {
    val initialTicket = stash.readTravelCard() ?: vault.readTravelCard()
    var pane by remember { mutableStateOf(if (initialTicket != null) Pane.Home else Pane.Login) }
    var banner by remember {
        mutableStateOf(
            if (initialTicket != null) {
                "Previous session restored from local device state."
            } else {
                "Sign in to open your notes."
            }
        )
    }
    var currentTicket by remember { mutableStateOf<String?>(initialTicket) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "Secret Lab", style = MaterialTheme.typography.headlineMedium)
            Text(text = banner)

            when (pane) {
                Pane.Login -> LoginCard(
                    localAccountMail = accountVault.readAccountMail(),
                    onSignIn = { mail, secret ->
                        val result = authGateway.signIn(mail, secret)
                        if (result != null) {
                            //stash.cacheOpenSecret(secret) // TODO(C05-1): remove local password persistence after login.
                            vault.saveTravelCard(result.travelCard) // TODO(C05-2): move the session artifact to SecureSessionStore.
                            stash.clearAll()
                            logger.reportGateOpen(secret, result.travelCard) // TODO(C05-4): stop printing full secrets after login.
                            currentTicket = result.travelCard
                            banner = "Signed in as ${result.displayName}"
                            pane = Pane.Home
                        } else {
                            banner = "Login failed"
                        }
                    },
                    onOpenCreateAccount = {
                        banner = "Open the local account flow for task C05."
                        pane = Pane.CreateAccount
                    },
                )

                Pane.CreateAccount -> CreateAccountCard(
                    initialMail = accountVault.readAccountMail() ?: "",
                    initialPassword = "",
                    onSave = { mail, secret ->
                        accountVault.saveLocalAccount(mail, secret)
                        banner = "Local account saved."
                        pane = Pane.Login
                    },
                    onBack = { pane = Pane.Login },
                )

                Pane.Home -> HomeCard(
                    noteTitle = noteBox.firstNoteTitle,
                    onSignOut = {
                        stash.clearAll()
                        vault.clearTravelCard()
                        currentTicket = null
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
    localAccountMail: String?,
    onSignIn: (String, String) -> Unit,
    onOpenCreateAccount: () -> Unit,
) {
    var mail by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Sign in", style = MaterialTheme.typography.titleLarge)
            if (localAccountMail != null) {
                Text("Local account present: $localAccountMail")
            }
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
            Button(onClick = { onSignIn(mail, secret) }) {
                Text("Sign in")
            }
            Button(onClick = onOpenCreateAccount) {
                Text("Create local account")
            }
        }
    }
}

@Composable
private fun CreateAccountCard(
    initialMail: String,
    initialPassword: String,
    onSave: (String, String) -> Unit,
    onBack: () -> Unit,
) {
    var mail by remember { mutableStateOf(initialMail) }
    var secret by remember { mutableStateOf(initialPassword) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Create local account", style = MaterialTheme.typography.titleLarge)
            Text("Use this screen for task C05.")
            OutlinedTextField(
                value = mail,
                onValueChange = { mail = it },
                label = { Text("Account email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            OutlinedTextField(
                value = secret,
                onValueChange = { secret = it },
                label = { Text("Account password") },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onSave(mail, secret) }) {
                    Text("Save local account")
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
    onSignOut: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Vault", style = MaterialTheme.typography.titleLarge)
            Text("Top note: $noteTitle")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onSignOut) {
                    Text("Sign out")
                }
            }
        }
    }
}
