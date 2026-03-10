# NailManagement

A nail art management app designed for nail technicians to organize gel products and nail style tutorials.

美甲管理 App，專為美甲師設計，用於管理凝膠色號與美甲款式的教學步驟。

## Features

### Gel Management

- Create and edit gel info (name, price, color code)
- Upload product photos for each gel
- View which nail styles use a gel

### Nail Style Management

- Create style tutorials with multi-step instructions and per-step photos
- Upload finished result photos
- Use `@` mentions in step descriptions to tag gels and auto-link them
- Tap mentions to navigate to gel or style details

### @ Mention System

- Type `@` to trigger a gel dropdown with live search
- Storage format: `[[gel:ID]]`, displayed as bold `@GelName`
- Deleting part of a mention removes the entire mention

## 功能

### 凝膠管理

- 新增、編輯凝膠資訊（名稱、價格、色號）
- 為每款凝膠上傳產品照片
- 查看凝膠被哪些款式使用

### 美甲款式管理

- 建立款式教學，包含多步驟說明與每步驟照片
- 上傳完成作品照片
- 在步驟描述中使用 `@` 標記凝膠，自動建立連結
- 點擊標記可跳轉至對應凝膠或款式詳情

### @ 標記系統

- 輸入 `@` 觸發凝膠選單，支援即時搜尋
- 儲存格式：`[[gel:ID]]`，顯示為粗體 `@凝膠名稱`
- 刪除部分標記時自動移除整個標記

## Tech Stack

- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM (ViewModel + Repository + DAO)
- **Database**: Room (SQLite)
- **Async**: Kotlin Coroutines + Flow / StateFlow
- **Image Loading**: Coil
- **Navigation**: Compose Navigation (bottom tabs for Gel / Nail Style lists)

## Project Structure

```
app/src/main/java/com/leyna/nailmanagement/
├── data/
│   ├── database/    # Room Database
│   ├── dao/         # GelDao, NailStyleDao
│   ├── entity/      # Gel, NailStyle, NailStyleGelCrossRef
│   └── repository/  # GelRepository, NailStyleRepository, ImageRepository
├── ui/
│   ├── screens/     # Screen composables
│   ├── components/  # MentionTextField and shared components
│   ├── viewmodel/   # GelViewModel, NailStyleViewModel
│   ├── navigation/  # NavHost, Routes, BottomNavItem
│   └── theme/       # Color, Theme, Type
└── MainActivity.kt
```
測試