package com.example.ai.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.ai.component.LogoMark
import com.example.ai.component.NetworkImage
import com.example.ai.component.PrimaryButton
import com.example.ai.component.SecondaryButton
import com.example.ai.navigation.AppRoute
import com.example.ai.repository.LoginRepository
import com.example.ai.repository.RegisterRepository
import com.example.ai.theme.TravelBlue
import com.example.ai.theme.TravelSpacing
import com.example.ai.theme.TravelTeal
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val title: String = "Login",
    val phone: String = "",
    val password: String = "",
    val rememberLogin: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class RegisterUiState(
    val verificationCode: String = "",
    val verifiedPhone: String = "",
    val verifiedPassword: String = "",
    val countdown: Int = 0,
    val message: String? = null,
    val errorMessage: String? = null,
    val registered: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: LoginRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updatePhone(value: String) = _uiState.update { it.copy(phone = value, errorMessage = null) }
    fun updatePassword(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun updateRememberLogin(value: Boolean) = _uiState.update { it.copy(rememberLogin = value) }

    fun login(onSuccess: () -> Unit) {
        val phone = _uiState.value.phone.trim()
        val password = _uiState.value.password
        val error = when {
            !phone.isMainlandPhone() -> "请输入正确的手机号"
            password.length < 6 -> "密码至少 6 位"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val user = repository.login(phone, password, _uiState.value.rememberLogin)
            _uiState.update { it.copy(isLoading = false) }
            if (user != null) {
                onSuccess()
            } else {
                _uiState.update { it.copy(errorMessage = "手机号或密码错误") }
            }
        }
    }
}

@HiltViewModel
class RegisterViewModel @Inject constructor(private val repository: RegisterRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun sendVerificationCode(phone: String) {
        val normalizedPhone = phone.trim()
        if (!normalizedPhone.isMainlandPhone()) {
            _uiState.update { it.copy(errorMessage = "请输入正确的手机号", message = null) }
            return
        }
        if (_uiState.value.countdown > 0) return
        val code = (100000..999999).random().toString()
        _uiState.update {
            it.copy(
                verificationCode = code,
                countdown = 60,
                message = "验证码已发送（开发验证码：$code）",
                errorMessage = null,
                registered = false
            )
        }
        viewModelScope.launch {
            while (_uiState.value.countdown > 0) {
                delay(1000)
                _uiState.update { state -> state.copy(countdown = (state.countdown - 1).coerceAtLeast(0)) }
            }
        }
    }

    fun register(phone: String, code: String, password: String, confirmPassword: String, agree: Boolean) {
        val normalizedPhone = phone.trim()
        val error = when {
            !normalizedPhone.isMainlandPhone() -> "请输入正确的手机号"
            _uiState.value.verificationCode.isBlank() -> "请先获取短信验证码"
            code.trim() != _uiState.value.verificationCode -> "验证码不正确"
            password.length < 6 -> "密码至少 6 位"
            password != confirmPassword -> "两次输入的密码不一致"
            !agree -> "请先同意用户协议与隐私政策"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error, message = null, registered = false) }
        } else {
            _uiState.update {
                it.copy(
                    verifiedPhone = normalizedPhone,
                    verifiedPassword = password,
                    registered = true,
                    errorMessage = null,
                    message = "验证成功，请完善个人资料"
                )
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavHostController, viewModel: LoginViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AuthContent(
        mode = AuthMode.Login,
        uiState = uiState,
        onPhoneChange = viewModel::updatePhone,
        onPasswordChange = viewModel::updatePassword,
        onRememberLoginChange = viewModel::updateRememberLogin,
        onPrimary = {
            viewModel.login {
                navController.navigate(AppRoute.Home.route) {
                    popUpTo(AppRoute.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        },
        onSwitch = { navController.navigate(AppRoute.Register.route) }
    )
}

@Composable
fun RegisterScreen(navController: NavHostController, viewModel: RegisterViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.registered, uiState.verifiedPhone, uiState.verifiedPassword) {
        if (uiState.registered && uiState.verifiedPhone.isNotBlank()) {
            navController.navigate(AppRoute.CompleteProfile.createRoute(uiState.verifiedPhone, uiState.verifiedPassword)) {
                popUpTo(AppRoute.Register.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    RegisterContent(
        uiState = uiState,
        onSendCode = viewModel::sendVerificationCode,
        onRegister = viewModel::register,
        onSwitch = { navController.navigate(AppRoute.Login.route) }
    )
}

@Composable
private fun AuthContent(
    mode: AuthMode,
    uiState: LoginUiState,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberLoginChange: (Boolean) -> Unit,
    onPrimary: () -> Unit,
    onSwitch: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(TravelSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(TravelSpacing.large)
    ) {
        AuthHero(mode)
        Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.medium)) {
            AuthTextField(
                value = uiState.phone,
                onValueChange = { onPhoneChange(it.filter { char -> char.isDigit() }.take(11)) },
                label = "手机号",
                leadingIcon = Icons.Filled.AccountCircle,
                keyboardType = KeyboardType.Phone
            )
            AuthTextField(uiState.password, { onPasswordChange(it) }, "密码", Icons.Filled.Lock, KeyboardType.Password, password = true)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = uiState.rememberLogin, onCheckedChange = onRememberLoginChange)
                Text("记住登录", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = {}) { Text("忘记密码") }
            }
            uiState.errorMessage?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) }
            PrimaryButton(
                text = if (uiState.isLoading) "登录中..." else "登录",
                icon = Icons.Filled.TravelExplore,
                onClick = onPrimary,
                enabled = !uiState.isLoading
            )
            SecondaryButton(text = "使用 Google 继续", icon = Icons.Filled.AccountCircle, onClick = {})
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("没有账号？", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onSwitch) { Text("立即注册") }
        }
    }
}

@Composable
private fun RegisterContent(
    uiState: RegisterUiState,
    onSendCode: (String) -> Unit,
    onRegister: (String, String, String, String, Boolean) -> Unit,
    onSwitch: () -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agree by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(TravelSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(TravelSpacing.large)
    ) {
        AuthHero(AuthMode.Register)
        Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.medium)) {
            AuthTextField(
                value = phone,
                onValueChange = { phone = it.filter { char -> char.isDigit() }.take(11) },
                label = "手机号",
                leadingIcon = Icons.Filled.AccountCircle,
                keyboardType = KeyboardType.Phone
            )
            Row(horizontalArrangement = Arrangement.spacedBy(TravelSpacing.small), verticalAlignment = Alignment.CenterVertically) {
                AuthTextField(
                    value = code,
                    onValueChange = { code = it.filter { char -> char.isDigit() }.take(6) },
                    label = "短信验证码",
                    leadingIcon = Icons.Filled.Lock,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    onClick = { if (uiState.countdown == 0) onSendCode(phone) },
                    color = if (uiState.countdown == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = if (uiState.countdown == 0) "发送验证码" else "${uiState.countdown}s",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                        color = if (uiState.countdown == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            AuthTextField(password, { password = it }, "密码", Icons.Filled.Lock, KeyboardType.Password, password = true)
            AuthTextField(confirmPassword, { confirmPassword = it }, "确认密码", Icons.Filled.Lock, KeyboardType.Password, password = true)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = agree, onCheckedChange = { agree = it })
                Text("我已阅读并同意用户协议与隐私政策", style = MaterialTheme.typography.bodyMedium)
            }
            uiState.message?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = TravelTeal) }
            uiState.errorMessage?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) }
            PrimaryButton(
                text = "下一步",
                icon = Icons.Filled.TravelExplore,
                onClick = { onRegister(phone, code, password, confirmPassword, agree) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("已有账号？", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onSwitch) { Text("立即登录") }
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    password: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        leadingIcon = { Icon(leadingIcon, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        shape = MaterialTheme.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
private fun AuthHero(mode: AuthMode) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(MaterialTheme.shapes.extraLarge)
        ) {
            NetworkImage(
                "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80",
                "旅行插图",
                Modifier.fillMaxSize()
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))))
            )
            Surface(
                color = Color.White.copy(alpha = 0.92f),
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(TravelSpacing.medium)
            ) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = TravelTeal,
                    modifier = Modifier.padding(TravelSpacing.small).size(22.dp)
                )
            }
            Text(
                "Plan smarter. Travel better.",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(TravelSpacing.medium),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(Modifier.height(TravelSpacing.large))
        LogoMark(Modifier.size(72.dp))
        Spacer(Modifier.height(TravelSpacing.medium))
        Text("AI TravelMate", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(TravelSpacing.extraSmall))
        Text(
            if (mode == AuthMode.Login) "欢迎回来" else "手机号注册智能旅行账号",
            style = MaterialTheme.typography.titleMedium,
            color = if (mode == AuthMode.Login) TravelBlue else TravelTeal,
            fontWeight = FontWeight.Bold
        )
    }
}

private enum class AuthMode { Login, Register }

private fun String.isMainlandPhone(): Boolean = matches(Regex("^1[3-9]\\d{9}$"))
