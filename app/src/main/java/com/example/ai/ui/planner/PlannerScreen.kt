package com.example.ai.ui.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.ai.ai.viewmodel.AIViewModel
import com.example.ai.component.BudgetCard
import com.example.ai.component.CommonCard
import com.example.ai.component.InterestChip
import com.example.ai.component.PrimaryButton
import com.example.ai.component.SectionHeader
import com.example.ai.component.TravelBanner
import com.example.ai.component.TravelTextField
import com.example.ai.navigation.AppRoute
import com.example.ai.thirdparty.viewmodel.TravelPlanEnrichmentViewModel
import com.example.ai.theme.TravelBlue
import com.example.ai.theme.TravelCoral
import com.example.ai.theme.TravelSpacing
import com.example.ai.theme.TravelTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class DateField { Departure, Return }

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    navController: NavHostController,
    aiViewModel: AIViewModel = hiltViewModel(),
    enrichmentViewModel: TravelPlanEnrichmentViewModel = hiltViewModel()
) {
    val aiState by aiViewModel.uiState.collectAsStateWithLifecycle()
    var destination by remember { mutableStateOf("上海") }
    var budgetInput by remember { mutableStateOf("5200") }
    var trafficBudget by remember { mutableStateOf("1200") }
    var hotelBudget by remember { mutableStateOf("2400") }
    var foodBudget by remember { mutableStateOf("900") }
    var people by remember { mutableIntStateOf(2) }
    val todayMillis = remember { System.currentTimeMillis() }
    var departureMillis by remember { mutableLongStateOf(todayMillis) }
    var returnMillis by remember { mutableLongStateOf(todayMillis + 3.daysInMillis()) }
    var activeDateField by remember { mutableStateOf<DateField?>(null) }
    var requestedPlan by remember { mutableStateOf(false) }
    var navigated by remember { mutableStateOf(false) }
    val selected = remember { mutableStateListOf("美食", "自然") }
    val interests = listOf("美食", "自然", "亲子", "摄影", "博物馆", "购物", "海岛", "小众路线")
    val totalBudget = budgetInput.moneyAmount()
    val trafficAmount = trafficBudget.moneyAmount()
    val hotelAmount = hotelBudget.moneyAmount()
    val foodAmount = foodBudget.moneyAmount()
    val allocatedBudget = trafficAmount + hotelAmount + foodAmount
    val remainingBudget = (totalBudget - allocatedBudget).coerceAtLeast(0)
    val overBudget = allocatedBudget > totalBudget && totalBudget > 0
    fun applyAiBudgetSplit() {
        val total = totalBudget.takeIf { it > 0 } ?: 5200
        trafficBudget = (total * 0.25).roundToHundred().toString()
        hotelBudget = (total * 0.46).roundToHundred().toString()
        foodBudget = (total * 0.18).roundToHundred().toString()
    }

    LaunchedEffect(aiState.travelPlan, requestedPlan) {
        val plan = aiState.travelPlan
        if (requestedPlan && !navigated && plan != null) {
            navigated = true
            enrichmentViewModel.enrichPlan(plan)
            navController.navigate(AppRoute.Itinerary.route)
        }
    }

    activeDateField?.let { field ->
        TravelDatePickerDialog(
            initialMillis = if (field == DateField.Departure) departureMillis else returnMillis,
            onDismiss = { activeDateField = null },
            onConfirm = { selectedMillis ->
                if (field == DateField.Departure) {
                    departureMillis = selectedMillis
                    if (returnMillis <= selectedMillis) returnMillis = selectedMillis + 3.daysInMillis()
                } else {
                    returnMillis = selectedMillis.coerceAtLeast(departureMillis + 1.daysInMillis())
                }
                activeDateField = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(TravelSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(TravelSpacing.large)
    ) {
        Spacer(Modifier.height(TravelSpacing.small))
        Text("AI 旅行规划", style = MaterialTheme.typography.headlineMedium)
        Text("填写目的地、日期、预算和偏好，AI 会生成真实规划请求。", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        TravelBanner(
            imageUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80",
            title = "创建你的下一段旅程",
            subtitle = "路线、酒店、美食与天气提醒一次规划",
            badge = "AI Planner"
        )

        CommonCard {
            Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.medium)) {
                SectionHeader("基础信息")
                TravelTextField(destination, { destination = it }, "目的地搜索", leadingIcon = Icons.Filled.Search)
                Row(horizontalArrangement = Arrangement.spacedBy(TravelSpacing.medium)) {
                    BudgetCard(
                        title = "出发日期",
                        value = formatTravelDate(departureMillis),
                        modifier = Modifier.weight(1f).clickable { activeDateField = DateField.Departure },
                        color = TravelBlue
                    )
                    BudgetCard(
                        title = "返程日期",
                        value = formatTravelDate(returnMillis),
                        modifier = Modifier.weight(1f).clickable { activeDateField = DateField.Return },
                        color = TravelTeal
                    )
                }
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { activeDateField = DateField.Departure },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(Modifier.padding(TravelSpacing.medium), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f).padding(horizontal = TravelSpacing.medium)) {
                            Text("日期选择", style = MaterialTheme.typography.titleMedium)
                            Text("${formatTravelDate(departureMillis)} - ${formatTravelDate(returnMillis)} · 点击修改", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        CommonCard {
            Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.medium)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("预算范围", style = MaterialTheme.typography.titleLarge)
                        Text("¥${totalBudget}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    TextButton(onClick = { applyAiBudgetSplit() }) { Text("AI分配") }
                }
                MoneyInputField("总预算", budgetInput, { budgetInput = it.moneyInput() }, TravelBlue)
                Row(horizontalArrangement = Arrangement.spacedBy(TravelSpacing.small)) {
                    MoneyInputField("交通", trafficBudget, { trafficBudget = it.moneyInput() }, TravelBlue, Modifier.weight(1f))
                    MoneyInputField("住宿", hotelBudget, { hotelBudget = it.moneyInput() }, TravelTeal, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(TravelSpacing.small)) {
                    MoneyInputField("餐饮", foodBudget, { foodBudget = it.moneyInput() }, TravelCoral, Modifier.weight(1f))
                    BudgetCard("剩余", "¥$remainingBudget", Modifier.weight(1f), if (overBudget) TravelCoral else TravelTeal)
                }
                if (overBudget) {
                    Text("交通、住宿、餐饮合计已超过总预算，请调整金额。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        CommonCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("出行人数", style = MaterialTheme.typography.titleLarge)
                    Text("$people 人 · 朋友出行", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { if (people > 1) people-- }) { Icon(Icons.Filled.Remove, contentDescription = "减少") }
                Text("$people", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { people++ }) { Icon(Icons.Filled.Add, contentDescription = "增加") }
            }
        }

        CommonCard {
            Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.medium)) {
                SectionHeader("旅行兴趣")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(TravelSpacing.small), verticalArrangement = Arrangement.spacedBy(TravelSpacing.small)) {
                    interests.forEach { item ->
                        InterestChip(
                            text = item,
                            selected = selected.contains(item),
                            onClick = {
                                if (selected.contains(item)) selected.remove(item) else selected.add(item)
                            }
                        )
                    }
                }
            }
        }

        if (aiState.loading) {
            CommonCard {
                Text("AI 正在生成旅行方案，请稍候...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        aiState.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }

        PrimaryButton(
            text = if (aiState.loading) "正在生成..." else "生成旅行方案",
            onClick = {
                if (!aiState.loading) {
                    requestedPlan = true
                    navigated = false
                    aiViewModel.generateTravelPlan(
                        departure = "当前位置",
                        destination = destination.ifBlank { "上海" },
                        startDate = formatApiDate(departureMillis),
                        endDate = formatApiDate(returnMillis),
                        budget = buildBudgetPrompt(totalBudget, trafficAmount, hotelAmount, foodAmount, remainingBudget),
                        people = people,
                        interests = selected.toList()
                    )
                }
            },
            icon = Icons.Filled.TravelExplore,
            enabled = !aiState.loading
        )
        Spacer(Modifier.height(TravelSpacing.large))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TravelDatePickerDialog(
    initialMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(datePickerState.selectedDateMillis ?: initialMillis) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun formatTravelDate(millis: Long): String = SimpleDateFormat("MM月dd日", Locale.CHINA).format(Date(millis))
private fun formatApiDate(millis: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(millis))

@Composable
private fun MoneyInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        prefix = { Text("¥", color = color, fontWeight = FontWeight.Bold) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = MaterialTheme.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = color.copy(alpha = 0.08f),
            unfocusedContainerColor = color.copy(alpha = 0.08f),
            focusedBorderColor = color,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

private fun String.moneyInput(): String = filter { it.isDigit() }.trimStart('0').ifBlank { "0" }.take(7)
private fun String.moneyAmount(): Int = toIntOrNull() ?: 0
private fun Double.roundToHundred(): Int = (this / 100).toInt().coerceAtLeast(1) * 100

private fun buildBudgetPrompt(total: Int, traffic: Int, hotel: Int, food: Int, other: Int): String =
    "总预算:${total}元；交通:${traffic}元；住宿:${hotel}元；餐饮:${food}元；其他:${other}元。请严格参考用户修改后的分项预算。"
private fun Int.daysInMillis(): Long = this * 24L * 60L * 60L * 1000L
