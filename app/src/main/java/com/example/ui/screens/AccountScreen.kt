package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.StrongerCastRepository
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AccountScreen() {
    val currentUser by StrongerCastRepository.currentUser.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Já tenho conta, 1 = Criar conta
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("account_screen")
    ) {
        if (currentUser != null) {
            val user = currentUser!!

            // CARD "SUA CONTA" (LOGADO)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar com Marca Dourada
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(2.dp, GoldPrimary, CircleShape)
                            .background(
                                Brush.linearGradient(listOf(GoldPrimary, GoldSecondary))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user.nome.takeIf { it.isNotBlank() } ?: user.email).take(1).uppercase(),
                            color = Color(0xFF141414),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = user.nome.ifBlank { "Membro Stronger" },
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = user.email,
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Badge de Admin
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (user.admin) GoldSecondary.copy(alpha = 0.2f) else DarkSurfaceElevated)
                            .border(1.dp, if (user.admin) GoldPrimary else DarkCardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (user.admin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (user.admin) GoldPrimary else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (user.admin) "Administrador Geral" else "Membro Comum",
                                color = if (user.admin) GoldPrimary else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = DarkCardBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(18.dp))

                    // Toggle de Permissão Admin (para facilitar testes locais e validação)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Permissão de Administrador",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Habilita ou oculta o Painel Admin no menu lateral",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = user.admin,
                            onCheckedChange = { StrongerCastRepository.toggleAdmin() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF141414),
                                checkedTrackColor = GoldPrimary
                            ),
                            modifier = Modifier.testTag("toggle_admin_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botão Sair da Conta Obrigatório
                    Button(
                        onClick = {
                            StrongerCastRepository.logout()
                            Toast.makeText(context, "Você saiu da sua conta.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sair da conta",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // TELA DE LOGIN / REGISTRO COM ABAS
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column {
                    // Abas "Já tenho conta" e "Criar conta"
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = DarkSurfaceElevated,
                        contentColor = GoldPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = GoldPrimary,
                                height = 3.dp
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                errorMessage = null
                            },
                            text = {
                                Text(
                                    text = "Já tenho conta",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) GoldPrimary else TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                errorMessage = null
                            },
                            text = {
                                Text(
                                    text = "Criar conta",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) GoldPrimary else TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }

                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = if (selectedTab == 0) "Bem-vindo de volta!" else "Faça parte da comunidade",
                            color = GoldPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Autenticação integrada com Supabase Auth",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        if (selectedTab == 1) {
                            // Nome para Cadastro
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Seu Nome") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = GoldPrimary)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedLabelColor = GoldPrimary,
                                    unfocusedLabelColor = TextMuted,
                                    focusedContainerColor = DarkSurfaceElevated,
                                    unfocusedContainerColor = DarkSurfaceElevated
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_name_input")
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // E-mail
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("E-mail") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = GoldPrimary)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = GoldPrimary,
                                unfocusedLabelColor = TextMuted,
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Senha
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Senha") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = GoldPrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = TextMuted
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = GoldPrimary,
                                unfocusedLabelColor = TextMuted,
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input")
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = StatusRed,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Botão Ação Principal
                        Button(
                            onClick = {
                                errorMessage = null
                                if (selectedTab == 0) {
                                    val res = StrongerCastRepository.login(emailInput, passwordInput)
                                    if (res.isFailure) {
                                        errorMessage = res.exceptionOrNull()?.message
                                    } else {
                                        Toast.makeText(context, "Conectado com sucesso!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val res = StrongerCastRepository.register(emailInput, passwordInput, nameInput)
                                    if (res.isFailure) {
                                        errorMessage = res.exceptionOrNull()?.message
                                    } else {
                                        Toast.makeText(context, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = Color(0xFF141414)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("auth_submit_button")
                        ) {
                            Text(
                                text = if (selectedTab == 0) "Entrar na Conta" else "Criar Minha Conta",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Atalho rápido para preenchimento de teste admin
                        OutlinedButton(
                            onClick = {
                                emailInput = "admin@strongercast.com"
                                passwordInput = "stronger2026"
                                nameInput = "Líder Admin"
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldSecondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Preencher conta demo de Admin", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
