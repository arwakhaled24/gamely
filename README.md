# Gamely 🎮

A simple Android app built with **Kotlin**, **Jetpack Compose**, **MVI**, and **Clean Architecture**.  
The app displays a list of games fetched from the **RAWG API**, supports pagination, shows details, and allows local search without making extra API calls.

---

## ✨ Features

- 🕹️ **Games List** — Browse a list of popular games.
- 🔍 **Local Search** — Filter the already-loaded games **without extra API calls**.
- 📄 **Game Details** — Tap any game for more detailed info.
- ♻️ **Pagination with Paging 3**  
  - Implemented based on an article reference *[(here)](https://medium.com/@me.zahidul/mastering-android-pagination-with-paging-3-jetpack-compose-9c8bad8ee98f)*  
---

## 🛠️ Tech Stack

| Category | Technologies Used |
|---------|--------------------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Architecture** | MVI + Clean Architecture |
| **Networking** | Ktor Client |
| **Dependency Injection** | Koin |
| **Pagination** | Paging 3 |
| **Testing** | JUnit, MockK |
| **API** | RAWG Video Games Database |

---
## 🏗️ Architecture Choice

**MVI (Model-View-Intent)**

**Why MVI?**
- **Unidirectional Data Flow**: Makes state management predictable and easier to debug
- **Single Source of Truth**: All UI state is managed in one place (ViewState)
- **Better Testability**: Clear separation between business logic and UI makes unit testing straightforward
- **Scalability**: Works well with Jetpack Compose's declarative nature

---
## 🔄 Assumptions & Shortcuts
Due to time constraints, the following decisions were made:
### Local databas Not Implemented
---

## 📡 API

This app uses the **RAWG.io API** to fetch game data.  
👉 https://rawg.io/apidocs

---

## ⚙️ API Key Notes (APK Build)

The project uses **BuildConfig** to inject configuration values such as:

- `BASE_URL`
- `API_KEY`
- `GAMES_ENDPOINT`
- `GAME_DETAILS_ENDPOINT`

### ✅ If you want to run the project locally
You must add these values inside `defaultConfig` in your **app module** `build.gradle.kts`:

## 📁 Project Structure
```
app/
├── application/         
│
├── data/                 
│   ├── dto/             
│   ├── paging/          
│   ├── remote/          
│   └── repositories/    
│
├── di/                  
│
├── domain/               
│   ├── model/          
│   ├── repositories/   
│   └── usecase/        
│
├── presentation/         
│   ├── composable/      
│   ├── navigation/      
│   ├── screens/         
│   └── viewmodel/       
│       ├── gamedetails/ 
│       └── games/       
│
├── ui.theme/          
│   ├── Color.kt         
│   ├── Theme.kt        
│   └── Type.kt         
│
└── MainActivity.kt      
```
## 📽️ Simple Demo
https://github.com/user-attachments/assets/f48887d7-6355-4e6d-8458-6c2da1ccad42




