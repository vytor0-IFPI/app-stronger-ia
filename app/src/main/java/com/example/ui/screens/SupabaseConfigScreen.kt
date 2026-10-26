package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.StrongerCastRepository
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun SupabaseConfigScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isConnected by StrongerCastRepository.isConnectedToSupabase.collectAsState()

    var urlInput by remember { mutableStateOf(StrongerCastRepository.supabaseUrl) }
    var keyInput by remember { mutableStateOf(StrongerCastRepository.supabaseAnonKey) }
    var isConnecting by remember { mutableStateOf(false) }
    var statusFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var isErrorFeedback by remember { mutableStateOf(false) }

    val fullSqlScript = """
-- ==============================================================================
-- STRONGER CAST - Supabase Database Schema & Row Level Security (RLS)
-- 100% Free Tier Compatible | PostgreSQL / Supabase
-- ==============================================================================

-- 1. Profiles Table
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT,
    nome TEXT,
    admin BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Perfis visíveis para o próprio usuário e admins" ON public.profiles FOR SELECT
TO authenticated, anon USING (auth.uid() = id OR (SELECT admin FROM public.profiles WHERE id = auth.uid()) = TRUE);

-- 2. Trigger para vincular usuário do Auth ao perfil
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.profiles (id, email, nome, admin)
  VALUES (NEW.id, NEW.email, COALESCE(NEW.raw_user_meta_data->>'nome', split_part(NEW.email, '@', 1)),
  CASE WHEN NEW.email ILIKE '%admin%' THEN TRUE ELSE FALSE END);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 3. Tabela de Episódios
CREATE TABLE IF NOT EXISTS public.episodes (
    id BIGSERIAL PRIMARY KEY,
    titulo TEXT NOT NULL,
    convidado TEXT,
    data TEXT NOT NULL,
    youtube TEXT,
    spotify TEXT,
    mp3 TEXT,
    capa TEXT,
    publicado BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);
ALTER TABLE public.episodes ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Episódios publicados visíveis publicamente" ON public.episodes FOR SELECT TO anon, authenticated USING (publicado = TRUE);
CREATE POLICY "Admins podem criar episódios" ON public.episodes FOR ALL TO authenticated
USING (EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND admin = TRUE));

-- 4. Tabela de Devocionais
CREATE TABLE IF NOT EXISTS public.devotionals (
    id BIGSERIAL PRIMARY KEY,
    titulo TEXT NOT NULL,
    versiculo TEXT NOT NULL,
    texto TEXT NOT NULL,
    data TEXT NOT NULL,
    publicado BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);
ALTER TABLE public.devotionals ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Devocionais publicados visíveis publicamente" ON public.devotionals FOR SELECT TO anon, authenticated USING (publicado = TRUE);

-- 5. Tabela de Canais Recomendados
CREATE TABLE IF NOT EXISTS public.recommended_channels (
    id BIGSERIAL PRIMARY KEY,
    titulo TEXT NOT NULL,
    canal TEXT NOT NULL,
    link TEXT NOT NULL,
    tipo TEXT NOT NULL CHECK (tipo IN ('video', 'canal')),
    publicado BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);
ALTER TABLE public.recommended_channels ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Canais publicados visíveis publicamente" ON public.recommended_channels FOR SELECT TO anon, authenticated USING (publicado = TRUE);

-- 6. Storage Bucket de Mídias (MP3 e Capas até 50MB)
INSERT INTO storage.buckets (id, name, public) VALUES ('media', 'media', true) ON CONFLICT DO NOTHING;
    """.trimIndent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("supabase_config_screen")
    ) {
        // Banner Informativo de Status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isConnected) StatusGreen else DarkCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) StatusGreen.copy(alpha = 0.2f) else DarkSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = if (isConnected) StatusGreen else GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Banco de Dados Supabase",
                                color = GoldPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isConnected) "Conectado e Sincronizado" else "Aguardando URL do projeto",
                                color = if (isConnected) StatusGreen else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = DarkCardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "A sua chave de API fornecida já está pré-configurada abaixo. Insira a URL do seu projeto Supabase e clique em 'Conectar & Sincronizar' para vincular seus episódios, devocionais e canais em tempo real.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Parâmetros de Conexão
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Credenciais do Projeto Supabase",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo da URL do Projeto
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = {
                        urlInput = it
                        statusFeedbackMessage = null
                    },
                    label = { Text("Supabase Project URL") },
                    placeholder = { Text("https://seu-projeto.supabase.co") },
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = GoldPrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("supabase_url_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = GoldSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Onde achar: Painel Supabase -> Project Settings -> API -> Project URL",
                        color = TextMuted,
                        fontSize = 10.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Campo da Chave de API
                OutlinedTextField(
                    value = keyInput,
                    onValueChange = {
                        keyInput = it
                        statusFeedbackMessage = null
                    },
                    label = { Text("Chave de API (Publishable / Anon)") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = GoldPrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("supabase_key_input")
                )

                if (statusFeedbackMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = statusFeedbackMessage ?: "",
                        color = if (isErrorFeedback) StatusRed else StatusGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botão de Conectar & Sincronizar
                Button(
                    onClick = {
                        isConnecting = true
                        statusFeedbackMessage = null
                        coroutineScope.launch {
                            val result = StrongerCastRepository.testAndSyncSupabase(urlInput, keyInput)
                            isConnecting = false
                            if (result.isSuccess) {
                                isErrorFeedback = false
                                statusFeedbackMessage = result.getOrNull()
                                Toast.makeText(context, "Conectado ao Supabase!", Toast.LENGTH_SHORT).show()
                            } else {
                                isErrorFeedback = true
                                statusFeedbackMessage = result.exceptionOrNull()?.message
                            }
                        }
                    },
                    enabled = !isConnecting,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF141414)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("save_supabase_connection_button")
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(
                            color = Color(0xFF141414),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verificando conexão...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Conectar & Sincronizar Banco", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Script SQL Completo do Supabase
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Script SQL do Supabase",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Execute no SQL Editor do seu projeto",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Supabase SQL", fullSqlScript)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Script SQL copiado com sucesso!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary, contentColor = Color(0xFF141414)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copiar SQL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bloco de código
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0D0D0D))
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = fullSqlScript,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
