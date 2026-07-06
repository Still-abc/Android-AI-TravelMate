package com.example.ai.ui.profile

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.ai.component.Avatar
import com.example.ai.component.BudgetCard
import com.example.ai.component.CommonCard
import com.example.ai.component.NetworkImage
import com.example.ai.component.PrimaryButton
import com.example.ai.component.SecondaryButton
import com.example.ai.model.UserEntity
import com.example.ai.navigation.AppRoute
import com.example.ai.repository.UserProfileInput
import com.example.ai.repository.UserRepository
import com.example.ai.theme.TravelBlue
import com.example.ai.theme.TravelCoral
import com.example.ai.theme.TravelSpacing
import com.example.ai.theme.TravelTeal
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val GenderOptions = listOf("男", "女", "保密")
private val TravelPreferenceOptions = listOf("美食", "自然", "摄影", "情侣", "亲子", "历史", "购物", "露营", "海边", "滑雪")

data class ProfileFormState(
    val currentUser: UserEntity? = null,
    val phone: String = "",
    val password: String = "",
    val avatar: String = "",
    val nickname: String = "",
    val gender: String = "保密",
    val birthday: String = "",
    val signature: String = "",
    val city: String = "",
    val travelPreferences: Set<String> = emptySet(),
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileFormState())
    val uiState: StateFlow<ProfileFormState> = _uiState.asStateFlow()

    private var initializedKey: String? = null

    init {
        viewModelScope.launch {
            userRepository.observeCurrentUser().collect { user ->
                _uiState.update { it.copy(currentUser = user) }
                if (initializedKey == "edit" && user != null) fillFromUser(user)
            }
        }
    }

    fun initializeForRegistration(phone: String, password: String) {
        val normalizedPhone = phone.trim()
        if (initializedKey == "register:$normalizedPhone") return
        initializedKey = "register:$normalizedPhone"
        _uiState.update {
            ProfileFormState(phone = normalizedPhone, password = password)
        }
    }

    fun initializeForEdit() {
        if (initializedKey == "edit") return
        initializedKey = "edit"
        _uiState.value.currentUser?.let(::fillFromUser)
    }

    private fun fillFromUser(user: UserEntity) {
        _uiState.update {
            it.copy(
                currentUser = user,
                phone = user.phone,
                avatar = user.avatar,
                nickname = user.nickname,
                gender = user.gender,
                birthday = user.birthday.orEmpty(),
                signature = user.signature.orEmpty(),
                city = user.city.orEmpty(),
                travelPreferences = user.travelPreference.split(',').filter { item -> item.isNotBlank() }.toSet(),
                saved = false,
                errorMessage = null
            )
        }
    }

    fun updateAvatar(value: String) = _uiState.update { it.copy(avatar = value, saved = false) }
    fun updateNickname(value: String) = _uiState.update { it.copy(nickname = value.take(20), saved = false) }
    fun updateGender(value: String) = _uiState.update { it.copy(gender = value, saved = false) }
    fun updateBirthday(value: String) = _uiState.update { it.copy(birthday = value, saved = false) }
    fun updateSignature(value: String) = _uiState.update { it.copy(signature = value.take(100), saved = false) }
    fun updateCity(value: String) = _uiState.update { it.copy(city = value, saved = false) }

    fun togglePreference(value: String) = _uiState.update {
        val next = if (value in it.travelPreferences) it.travelPreferences - value else it.travelPreferences + value
        it.copy(travelPreferences = next, saved = false)
    }

    fun saveNewProfile() = save { input -> userRepository.saveUser(input) }

    fun saveEditedProfile() {
        val user = _uiState.value.currentUser ?: run {
            _uiState.update { it.copy(errorMessage = "请先完成注册") }
            return
        }
        save { input -> userRepository.updateProfile(user, input) }
    }

    fun logout() {
        initializedKey = null
        _uiState.value = ProfileFormState()
        userRepository.logout()
    }

    private fun save(operation: suspend (UserProfileInput) -> UserEntity) {
        val input = _uiState.value.toInputOrNull()
        if (input == null) {
            _uiState.update { it.copy(errorMessage = "昵称需为 2~20 个中文、英文或数字") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { operation(input) }
                .onSuccess { user ->
                    _uiState.update { it.copy(currentUser = user, isSaving = false, saved = true, errorMessage = null) }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isSaving = false, saved = false, errorMessage = throwable.message ?: "保存失败") }
                }
        }
    }
}

@Composable
fun ProfileSetupScreen(navController: NavHostController, phone: String, password: String, viewModel: ProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(phone, password) { viewModel.initializeForRegistration(phone, password) }
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            navController.navigate(AppRoute.Home.route) {
                popUpTo(AppRoute.CompleteProfile.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    ProfileEditorScaffold(
        title = "完善个人资料",
        uiState = uiState,
        onBack = null,
        onSave = viewModel::saveNewProfile,
        onAvatarChange = viewModel::updateAvatar,
        onNicknameChange = viewModel::updateNickname,
        onGenderChange = viewModel::updateGender,
        onBirthdayChange = viewModel::updateBirthday,
        onSignatureChange = viewModel::updateSignature,
        onCityChange = viewModel::updateCity,
        onPreferenceToggle = viewModel::togglePreference,
        saveText = "完成注册"
    )
}

@Composable
fun ProfileEditScreen(navController: NavHostController, viewModel: ProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.initializeForEdit() }
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) navController.popBackStack()
    }
    ProfileEditorScaffold(
        title = "编辑资料",
        uiState = uiState,
        onBack = { navController.popBackStack() },
        onSave = viewModel::saveEditedProfile,
        onAvatarChange = viewModel::updateAvatar,
        onNicknameChange = viewModel::updateNickname,
        onGenderChange = viewModel::updateGender,
        onBirthdayChange = viewModel::updateBirthday,
        onSignatureChange = viewModel::updateSignature,
        onCityChange = viewModel::updateCity,
        onPreferenceToggle = viewModel::togglePreference,
        saveText = "保存"
    )
}

@Composable
fun ProfileScreen(navController: NavHostController, viewModel: ProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user = uiState.currentUser

    val travelDays = user?.let { u ->
        val daysSinceCreation = ((System.currentTimeMillis() - u.createTime) / (1000 * 60 * 60 * 24)).toInt()
        maxOf(daysSinceCreation, 1)
    } ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(TravelSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(TravelSpacing.large)
    ) {
        Spacer(Modifier.height(TravelSpacing.small))
        CommonCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(user?.avatar.orEmpty(), Modifier.size(72.dp), initials = "")
                Column(Modifier.weight(1f).padding(horizontal = TravelSpacing.medium)) {
                    Text(user?.nickname ?: "暂无个人资料", style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(user?.phone ?: "注册后可查看完整资料", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { navController.navigate(AppRoute.EditProfile.route) }, enabled = user != null) {
                    Icon(Icons.Filled.Edit, contentDescription = "编辑资料")
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(TravelSpacing.small), modifier = Modifier.fillMaxWidth()) {
            BudgetCard("旅行天数", "${travelDays}天", Modifier.weight(1f), TravelBlue)
            BudgetCard("收藏", "0个", Modifier.weight(1f), TravelTeal)
            BudgetCard("规划", "0条", Modifier.weight(1f), TravelCoral)
        }
        CommonCard {
            Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.small)) {
                ProfileInfoRow(Icons.Filled.Wc, "性别", user?.gender ?: "-")
                ProfileInfoRow(Icons.Filled.LocationCity, "城市", user?.city ?: "-")
                ProfileInfoRow(Icons.Filled.Favorite, "旅行偏好", user?.travelPreference?.replace(",", "、")?.ifBlank { "-" } ?: "-")
            }
        }
        CommonCard {
            Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.small)) {
                ProfileRow(Icons.Filled.Favorite, "我的收藏", "城市、景点、酒店", TravelCoral) { navController.navigate(AppRoute.Favorite.route) }
                ProfileRow(Icons.Filled.ListAlt, "我的规划", "AI 生成的旅行路线", TravelBlue) { navController.navigate(AppRoute.History.route) }
                ProfileRow(Icons.Filled.ReceiptLong, "我的订单", "机票酒店占位入口", TravelTeal) {}
                ProfileRow(Icons.Filled.Settings, "设置", "偏好、通知、语言", TravelBlue) { navController.navigate(AppRoute.Settings.route) }
                ProfileRow(Icons.Filled.Info, "关于", "AI TravelMate 1.0", TravelTeal) {}
            }
        }
        PrimaryButton("退出登录", onClick = {
            viewModel.logout()
            navController.navigate(AppRoute.Login.route) { popUpTo(AppRoute.Home.route) { inclusive = true } }
        }, icon = Icons.Filled.Logout)
        Spacer(Modifier.height(TravelSpacing.large))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ProfileEditorScaffold(
    title: String,
    uiState: ProfileFormState,
    onBack: (() -> Unit)?,
    onSave: () -> Unit,
    onAvatarChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onBirthdayChange: (String) -> Unit,
    onSignatureChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onPreferenceToggle: (String) -> Unit,
    saveText: String
) {
    var showAvatarSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingCameraUri?.let { onAvatarChange(it.toString()) }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = context.createProfileImageUri()
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedUri ->
            context.copyProfileImageToFile(selectedUri)?.let { onAvatarChange(it.toString()) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(TravelSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(TravelSpacing.medium)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
            }
            Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .clickable { showAvatarSheet = true },
                contentAlignment = Alignment.Center
            ) {
                if (uiState.avatar.isNotBlank()) {
                    NetworkImage(uiState.avatar, "头像", Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "头像占位", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(96.dp))
                }
            }
        }

        OutlinedTextField(
            value = uiState.nickname,
            onValueChange = onNicknameChange,
            label = { Text("昵称") },
            isError = uiState.nickname.isNotBlank() && !uiState.nickname.isValidNickname(),
            supportingText = { Text("2~20 个中文、英文或数字") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        CommonCard {
            Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.small)) {
                Text("性别", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(TravelSpacing.small), modifier = Modifier.fillMaxWidth()) {
                    GenderOptions.forEach { gender ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = uiState.gender == gender, onClick = { onGenderChange(gender) })
                            Text(gender)
                        }
                    }
                }
            }
        }

        Surface(onClick = { showDatePicker = true }, color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large) {
            Row(Modifier.fillMaxWidth().padding(TravelSpacing.medium), verticalAlignment = Alignment.CenterVertically) {
                Text("出生日期", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Text(uiState.birthday.ifBlank { "可选" }, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        OutlinedTextField(
            value = uiState.signature,
            onValueChange = onSignatureChange,
            label = { Text("个性签名") },
            supportingText = { Text("${uiState.signature.length}/100") },
            minLines = 3,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.city,
            onValueChange = onCityChange,
            label = { Text("所在城市") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )

        CommonCard {
            Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.small)) {
                Text("旅行偏好", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(TravelSpacing.small), verticalArrangement = Arrangement.spacedBy(TravelSpacing.small)) {
                    TravelPreferenceOptions.forEach { preference ->
                        FilterChip(
                            selected = preference in uiState.travelPreferences,
                            onClick = { onPreferenceToggle(preference) },
                            label = { Text(preference) }
                        )
                    }
                }
            }
        }

        uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        PrimaryButton(saveText, onClick = onSave, modifier = Modifier.fillMaxWidth(), icon = Icons.Filled.Edit)
        Spacer(Modifier.height(TravelSpacing.large))
    }

    if (showAvatarSheet) {
        ModalBottomSheet(onDismissRequest = { showAvatarSheet = false }) {
            AvatarActionRow(Icons.Filled.CameraAlt, "拍照") {
                showAvatarSheet = false
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            AvatarActionRow(Icons.Filled.PhotoLibrary, "从相册选择") {
                showAvatarSheet = false
                galleryLauncher.launch("image/*")
            }
            AvatarActionRow(Icons.AutoMirrored.Filled.ArrowBack, "取消") { showAvatarSheet = false }
            Spacer(Modifier.height(TravelSpacing.medium))
        }
    }

    if (showDatePicker) {
        val initialMillis = uiState.birthday.parseProfileDateMillis() ?: System.currentTimeMillis()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onBirthdayChange(formatProfileDate(datePickerState.selectedDateMillis ?: initialMillis))
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun AvatarActionRow(icon: ImageVector, text: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(horizontal = TravelSpacing.medium, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(text, modifier = Modifier.padding(start = TravelSpacing.medium), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, title: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = TravelSpacing.extraSmall), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(title, modifier = Modifier.padding(horizontal = TravelSpacing.medium), style = MaterialTheme.typography.titleMedium)
        Text(value, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent, shape = MaterialTheme.shapes.large) {
        Row(Modifier.fillMaxWidth().padding(TravelSpacing.small), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(color.copy(alpha = 0.12f), MaterialTheme.shapes.medium), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Column(Modifier.weight(1f).padding(horizontal = TravelSpacing.medium)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun ProfileFormState.toInputOrNull(): UserProfileInput? {
    if (!nickname.trim().isValidNickname()) return null
    if (phone.isBlank()) return null
    return UserProfileInput(
        phone = phone,
        password = password,
        nickname = nickname.trim(),
        avatar = avatar,
        gender = gender,
        birthday = birthday.ifBlank { null },
        signature = signature,
        city = city,
        travelPreferences = travelPreferences.toList()
    )
}

private fun String.isValidNickname(): Boolean = trim().matches(Regex("^[\\u4e00-\\u9fa5A-Za-z0-9]{2,20}$"))

private fun Context.createProfileImageUri(): Uri {
    val directory = File(filesDir, "profile_images").apply { mkdirs() }
    val file = File(directory, "avatar_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
}

private fun Context.copyProfileImageToFile(uri: Uri): Uri? = runCatching {
    val directory = File(filesDir, "profile_images").apply { mkdirs() }
    val file = File(directory, "avatar_${System.currentTimeMillis()}.jpg")
    contentResolver.openInputStream(uri)?.use { input ->
        file.outputStream().use { output -> input.copyTo(output) }
    } ?: return@runCatching null
    FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
}.getOrNull()

private fun formatProfileDate(millis: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(millis))

private fun String.parseProfileDateMillis(): Long? = runCatching {
    SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(this)?.time
}.getOrNull()
