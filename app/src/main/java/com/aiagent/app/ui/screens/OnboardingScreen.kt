package com.aiagent.app.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aiagent.app.ui.theme.ShapeTokens
import com.aiagent.app.viewmodel.SettingsViewModel

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onExit: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    var apiKey by remember { mutableStateOf("") }
    var apiUrl by remember { mutableStateOf("api.deepseek.com") }
    var showKey by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf(DialogType.NONE) }
    var apiKeyPrefixVisible by remember { mutableStateOf(true) }

    val savedApiKey by viewModel.apiKey.collectAsState(initial = "")
    val savedApiUrl by viewModel.apiUrl.collectAsState(initial = "api.deepseek.com")

    LaunchedEffect(Unit) {
        if (savedApiKey.isNotBlank()) {
            apiKey = savedApiKey
        }
        apiUrl = savedApiUrl
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(ShapeTokens.CornerExtraLarge))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "欢迎使用AI Agent",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "配置你的API密钥以开始使用",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(ShapeTokens.CornerLarge),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { newValue ->
                                apiKey = newValue
                                apiKeyPrefixVisible = newValue.isEmpty()
                            },
                            label = { Text("API密钥") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(ShapeTokens.CornerMedium),
                            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                            ),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        if (apiKeyPrefixVisible) {
                            Text(
                                text = "sk-",
                                color = Color.Gray,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 52.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    OutlinedTextField(
                        value = apiUrl,
                        onValueChange = { apiUrl = it },
                        label = { Text("API地址") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ShapeTokens.CornerMedium),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        placeholder = { Text("api.deepseek.com") }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showKey = !showKey }) {
                            Text(if (showKey) "隐藏密钥" else "显示密钥")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.saveApiKey(apiKey)
                    viewModel.saveApiUrl(apiUrl)
                    viewModel.completeFirstLaunch()
                    onComplete()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(ShapeTokens.CornerFull),
            ) {
                Text(
                    text = "开始使用",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    dialogType = DialogType.NO_KEY
                    showDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        ) {
                            append("我没有该格式密钥")
                        }
                    }
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            shape = RoundedCornerShape(ShapeTokens.CornerExtraLarge),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = if (dialogType == DialogType.NO_KEY) "提示" else "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                when (dialogType) {
                    DialogType.NO_KEY -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "请选择一个选项",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    DialogType.LATER -> {
                        Column {
                            Text(
                                text = "如果你没有通用的OpenAI格式密钥，你可以选择稍后在环境变量中配置",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    DialogType.NO_API -> {
                        Column {
                            Text(
                                text = "该软件为空壳软件，如果你没有一个API，那么该软件将无法使用",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    DialogType.NONE -> {}
                }
            },
            confirmButton = {
                when (dialogType) {
                    DialogType.NO_KEY -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    dialogType = DialogType.LATER
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(ShapeTokens.CornerFull)
                            ) {
                                Text("稍后再说")
                            }
                            Button(
                                onClick = {
                                    dialogType = DialogType.NO_API
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(ShapeTokens.CornerFull),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Text("我没有密钥")
                            }
                        }
                    }
                    DialogType.LATER -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.End
                        ) {
                            Button(
                                onClick = {
                                    showDialog = false
                                    viewModel.completeFirstLaunch()
                                    onComplete()
                                },
                                shape = RoundedCornerShape(ShapeTokens.CornerFull)
                            ) {
                                Text("进入环境变量配置")
                            }
                        }
                    }
                    DialogType.NO_API -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.End
                        ) {
                            Button(
                                onClick = {
                                    showDialog = false
                                    onExit()
                                },
                                shape = RoundedCornerShape(ShapeTokens.CornerFull),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) {
                                Text("退出软件")
                            }
                        }
                    }
                    DialogType.NONE -> {}
                }
            },
            dismissButton = {
                if (dialogType != DialogType.NO_KEY) {
                    TextButton(onClick = { showDialog = false }) {
                        Text("取消")
                    }
                }
            }
        )
    }
}

private enum class DialogType {
    NONE, NO_KEY, LATER, NO_API
}
