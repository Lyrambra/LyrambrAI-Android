# AI Agent - Android 通用AI智能体应用

一个基于 Material Design 3 的空壳 Android AI 智能体应用，支持 OpenAI 协议的 API 接入。

## 功能特性

### 核心功能
- 🔑 **API密钥配置** - 首次启动引导配置，支持自定义API地址
- 💬 **AI对话** - 流式对话界面，支持Markdown渲染
- ⏰ **定时任务** - 定时触发AI任务，系统横幅通知提醒
- ⚙️ **个性化设置** - 自定义系统提示词、显示选项

### 显示效果
- 📝 **Markdown支持** - 标题、列表、代码块、表格等格式
- 💡 **思考过程** - 支持显示模型思考过程，可开关
- 🎨 **Material Design 3** - 遵循最新设计规范

### 设计规范
- 标准化梯度圆角：小圆角 → 中圆角 → 大圆角 → 超大圆角 → 胶囊形 → 正圆形
- 连续平滑轮廓，柔和化设计
- 形状形变动画（Shape Morphing）
- 线性动画与手势支持

## 项目结构

```
app/src/main/java/com/aiagent/app/
├── MainActivity.kt              # 主Activity
├── data/
│   ├── AppPreferences.kt        # DataStore偏好设置
│   ├── ChatMessage.kt           # 聊天消息模型
│   └── ScheduledTask.kt         # 定时任务模型
├── scheduler/
│   ├── TaskScheduler.kt         # 任务调度器
│   ├── TaskAlarmReceiver.kt     # 闹钟广播接收器
│   └── TaskExecutionService.kt  # 任务执行服务
├── ui/
│   ├── animation/
│   │   └── AnimationUtils.kt    # 动画工具类
│   ├── components/
│   │   ├── MarkdownText.kt      # Markdown渲染组件
│   │   └── ExpandableCard.kt    # 可展开卡片组件
│   ├── navigation/
│   │   └── AppNavigation.kt     # 导航系统
│   ├── screens/
│   │   ├── OnboardingScreen.kt  # 首次启动引导页
│   │   ├── ChatScreen.kt        # 聊天主界面
│   │   ├── TasksScreen.kt       # 定时任务列表
│   │   ├── AddTaskScreen.kt     # 添加/编辑任务
│   │   └── SettingsScreen.kt    # 设置页面
│   └── theme/
│       ├── Theme.kt             # 主题定义
│       ├── Color.kt             # 颜色定义
│       ├── Shape.kt             # 形状/圆角定义
│       └── Type.kt              # 字体定义
└── viewmodel/
    ├── ChatViewModel.kt         # 聊天ViewModel
    ├── TasksViewModel.kt        # 任务ViewModel
    └── SettingsViewModel.kt     # 设置ViewModel
```

## 圆角规范

| 级别 | 尺寸 | 用途 |
|------|------|------|
| Extra Small | 4dp | 小组件、标签 |
| Small | 8dp | 卡片元素、小按钮 |
| Medium | 12dp | 输入框、小型卡片 |
| Large | 16dp | 主要卡片、弹窗 |
| Extra Large | 28dp | 大型卡片、头部元素 |
| Full | 9999dp | 胶囊按钮、圆形元素 |

## 构建要求

- Android Studio Hedgehog 或更高版本
- minSdk: 26 (Android 8.0)
- targetSdk: 34 (Android 14)
- Kotlin 1.9.20+
- Jetpack Compose

## 快速开始

1. 使用 Android Studio 打开项目
2. 等待 Gradle 同步完成
3. 连接 Android 设备或启动模拟器
4. 点击运行按钮

## API配置

应用支持任何兼容 OpenAI 协议的 API 服务：
- API密钥：以 `sk-` 开头的密钥
- API地址：如 `api.deepseek.com`、`api.openai.com` 等

## 注意事项

- 本应用为空壳软件，不内置任何AI模型
- 需要用户自行提供有效的 API 密钥才能使用
- 所有数据存储在本地设备上
- 本项目完全由AI编写，最初为自用目的，后由于一些个人原因公开，本人不对任何由该项目及其神秘AI代码造成的任何损失负责
