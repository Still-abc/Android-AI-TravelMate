package com.example.ai.ai.prompt

import com.example.ai.ai.model.ChatMessage

object PromptManager {
    fun chatPrompt(): ChatMessage = ChatMessage(
        role = "system",
        content = "你是 AI TravelMate 的旅行助手。回答要简洁、实用、有温度，优先给出可执行的旅行建议。"
    )

    fun travelPlanPrompt(
        departure: String,
        destination: String,
        date: String,
        budget: String,
        people: Int,
        interests: List<String>
    ): List<ChatMessage> = listOf(
        ChatMessage(
            role = "system",
            content = "你是资深旅行规划师。必须把最终答案放在 assistant message 的 content 中，只返回一个合法 JSON object，不要 Markdown，不要代码块，不要解释文字，不要省略字段。字段必须匹配 Android 数据类：title, departure, destination, days, budget, hotels, foods, tips。days[] 每项必须包含 day, date, theme, schedules。days[].schedules[] 每项必须包含 time, title, type, location, description, cost, durationMinutes。budget 字段必须包含 total, currency, traffic, hotel, food, ticket, other。hotels[] 必须包含 name, location, pricePerNight, reason。foods[] 必须包含 name, location, averageCost, reason。tips[] 必须包含 title, content。所有数字字段必须返回数字，不要返回字符串。"
        ),
        ChatMessage(
            role = "user",
            content = "出发地:$departure\n目的地:$destination\n日期:$date\n预算:$budget\n人数:$people\n兴趣:${interests.joinToString()}\n请生成详细旅行计划 JSON，至少 2 天，每天至少 3 个 schedule。"
        )
    )

    fun weatherPrompt(
        weather: String,
        temperature: String,
        humidity: String,
        wind: String,
        airQuality: String
    ): List<ChatMessage> = listOf(
        ChatMessage(
            role = "system",
            content = "你是旅行天气建议助手。只返回合法 JSON，不要 Markdown，不要代码块。字段：outfit, umbrella, sunscreen, outdoor。"
        ),
        ChatMessage(
            role = "user",
            content = "天气:$weather\n温度:$temperature\n湿度:$humidity\n风力:$wind\n空气质量:$airQuality"
        )
    )

    fun budgetPrompt(budget: String, city: String, people: Int): List<ChatMessage> = listOf(
        ChatMessage(
            role = "system",
            content = "你是旅行预算分析师。只返回合法 JSON，不要 Markdown，不要代码块。字段：reasonable, summary, hotelAdvice, trafficAdvice, foodAdvice, savingAdvice。"
        ),
        ChatMessage(role = "user", content = "预算:$budget\n城市:$city\n人数:$people")
    )

    fun guideSummaryPrompt(guide: String): List<ChatMessage> = listOf(
        ChatMessage(
            role = "system",
            content = "你是旅行攻略总结助手。只返回合法 JSON，不要 Markdown，不要代码块。字段：scenicSpots, routes, foods, warnings。"
        ),
        ChatMessage(role = "user", content = guide)
    )
}
