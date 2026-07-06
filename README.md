# 🌍 Android AI TravelMate

一款基于 Android 开发的 AI 智能旅行助手，集成 AI 对话、旅游攻略、天气查询、景点推荐、旅行历史管理等功能，为用户提供智能化、个性化的旅行服务。

## 📱 项目简介

Android AI TravelMate 是一款面向旅游场景的智能助手应用，结合大语言模型（LLM）与多种开放 API，实现旅行规划、智能问答、天气查询、景点推荐等功能。

项目采用 MVVM 架构开发，界面使用 Material Design，支持用户登录、历史记录同步及 AI 智能交互。

---

## ✨ 功能展示

- 🤖 AI 智能旅行助手（DeepSeek / SiliconFlow）
- 🌤️ 实时天气查询（和风天气 API）
- 🗺️ 景点搜索与推荐
- 📍 城市旅游攻略生成
- 💬 AI 对话聊天
- ❤️ 收藏旅行计划
- 🕒 历史聊天记录
- 👤 用户登录注册
- 📷 用户头像设置
- 🌙 Material Design UI

---

## 📸 项目截图

> 可将 App 截图放在 screenshots 文件夹中。

| 首页 | AI聊天 | 天气 |
|------|---------|------|
| ![](screenshots/home.png) | ![](screenshots/chat.png) | ![](screenshots/weather.png) |

---

## 🛠️ 技术栈

### Android

- Java
- Android SDK
- MVVM Architecture
- RecyclerView
- ViewBinding
- Material Design

### 网络请求

- Retrofit
- OkHttp
- Gson

### AI

- DeepSeek API
- SiliconFlow API

### 第三方服务

- 和风天气 API
- 图片 API
- Markdown 渲染

---

## 📂 项目结构

```
app
├── activity
├── adapter
├── api
├── bean
├── database
├── fragment
├── utils
├── viewmodel
└── res
```

---

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/Still-abc/Android-AI-TravelMate.git
```

### 2. 使用 Android Studio 打开

```
Android Studio Hedgehog+
```

### 3. 配置 API Key

在项目中配置：ApiConfig.kt
```
AI模型 API Key URL

高德地图 API Key URL

和风天气 API Key URL
图片搜索 API Key URL
```

即可运行。

---

## 📦 开发环境

| 项目 | 版本 |
|------|------|
| Android Studio | Hedgehog / Koala |
| Gradle | 8.x |
| JDK | 17 |
| Android SDK | API 34+ |

---

## 📈 项目特点

- 基于 AI 大模型实现旅行问答
- 多 API 聚合，提高信息获取效率
- Material Design 界面设计
- MVVM 架构，模块划分清晰
- 网络请求统一封装
- 易于扩展更多旅游服务

---

## 🔮 后续计划

- [ ] 地图导航
- [ ] 行程规划
- [ ] 酒店推荐
- [ ] 机票查询
- [ ] 多语言支持
- [ ] 旅行社区
- [ ] AI 行程生成

---

## 👨‍💻 作者

**Chengyi Zeng**

GitHub：https://github.com/Still-abc

---

## ⭐ Star

如果这个项目对你有所帮助，欢迎点一个 ⭐ Star！
