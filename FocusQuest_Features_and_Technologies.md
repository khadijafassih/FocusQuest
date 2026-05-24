# FocusQuest — Features & Technologies Document

**Project:** FocusQuest Android App  
**Platform:** Android (Min SDK 24 / Target SDK 35)  
**Language:** Kotlin 2.0.21  
**Architecture:** MVVM (Model-View-ViewModel)  
**Date:** May 2026

---

## Table of Contents

1. [App Overview](#app-overview)
2. [Features](#features)
3. [Technologies & Libraries](#technologies--libraries)
4. [Technology-to-Feature Mapping](#technology-to-feature-mapping)
5. [Database Schema](#database-schema)
6. [External APIs](#external-apis)
7. [Android Components](#android-components)
8. [App Statistics](#app-statistics)

---

## App Overview

FocusQuest is a productivity Android application that combines Pomodoro-style focus timer sessions with task management, gamification (XP, levels, achievements), AI-powered task planning, and daily habit tracking. The app targets students and professionals who want to improve focus and manage tasks effectively.

---

## Features

### Feature 1 — Authentication & Onboarding

| Sub-Feature | Description |
|---|---|
| Splash Screen | Entry point with routing logic — directs to Onboarding (first launch), Sign In (returning user), or Home (already logged in) |
| Sign Up | New user registration with username, email, password, and confirmation validation |
| Sign In | Credential verification against local Room database |
| Onboarding | 3-page swipeable carousel introducing the app; includes daily goal picker (2, 4, or 6 sessions per day) |

---

### Feature 2 — Home Dashboard

| Sub-Feature | Description |
|---|---|
| Greeting | Time-based message — Good Morning / Afternoon / Evening with current date |
| Motivational Quote | Fetched from ZenQuotes API; user can refresh to get a new quote |
| Background Image | Dynamic background loaded from Unsplash API |
| Weather | Auto-detects user city via IP geolocation, fetches temperature and condition from Open-Meteo |
| Today's Stats | Live summary: tasks completed today, focus sessions today, total focus minutes today |
| Quick Actions | Shortcut buttons to Start Focus and Add Task |

---

### Feature 3 — Task Management

| Sub-Feature | Description |
|---|---|
| Task List | Displays all tasks for the logged-in user from the local database |
| Filters | Filter tabs: ALL / PENDING / COMPLETED |
| Search | Real-time text search that filters tasks as the user types |
| Add Task | Dialog with title (required), description, category, priority, and due date picker |
| Edit Task | Same dialog pre-filled with existing task data |
| Complete / Uncomplete | Checkbox toggles task completion; triggers XP reward and achievement check |
| Delete | Swipe-to-delete gesture or long press to delete |
| XP Calculation | Dynamic XP shown in dialog based on selected category and priority combination |

**XP Rewards by Category and Priority:**

| Category | High Priority | Medium Priority | Low Priority |
|---|---|---|---|
| Study | 37 XP | 25 XP | 17 XP |
| Work | 30 XP | 20 XP | 14 XP |
| Health | 30 XP | 20 XP | 14 XP |
| Personal | 22 XP | 15 XP | 10 XP |

---

### Feature 4 — Pomodoro Focus Timer

| Sub-Feature | Description |
|---|---|
| Session Types | Pomodoro (25 min), Deep Focus (50 min), Ultra Focus (90 min) |
| Break Types | Short Break (5 min), Long Break (15 min) |
| Custom Durations | Each session type can have its duration customized and saved persistently |
| Circular Progress | Animated circular progress ring showing time remaining |
| Controls | Start, Pause, and Reset buttons |
| Background Timer | Runs as a Foreground Service — timer continues when app is minimized or screen is off |
| Task Linking | User can optionally link a task to the current session |
| Ambient Sounds | Brown Noise, Rain, Lofi, White Noise — plays during focus sessions |
| Completion Notification | Push notification + haptic vibration when timer ends |
| Break Recommendation | Suggests taking a break after completing a focus session |
| XP Rewards | Pomodoro: 15 XP, Deep Focus: 30 XP, Ultra Focus: 50 XP |
| Session Counter | Displays how many focus sessions completed today |

---

### Feature 5 — Statistics & Analytics

| Sub-Feature | Description |
|---|---|
| Weekly Bar Chart | Bar chart (Mon–Sun) showing focus hours per day for the current week |
| Total Sessions | Lifetime count of completed focus sessions |
| Total Focus Hours | Cumulative hours spent in focus sessions |
| Total Tasks Completed | Lifetime count of completed tasks |
| Current Streak | Number of consecutive days with at least one focus session |

---

### Feature 6 — Profile & Settings

| Sub-Feature | Description |
|---|---|
| User Info | Displays username, email, current level, and XP progress bar (0–100 XP per level) |
| Profile Avatar | Tap to pick an image from the device gallery; shows initials if no image is set |
| Dark Mode | Three options: System Default / Light / Dark — applied at runtime without restart |
| Daily Reminder | Toggle on/off; time picker to schedule a daily focus reminder notification |
| Reminder on Reboot | Reminders are automatically rescheduled if the device restarts |
| Timer Customization | Adjust custom durations for each session type from the profile screen |
| Reset Progression | Resets XP and level back to zero (tasks and sessions are preserved) |
| View Stats | Navigate to full statistics screen |
| View Achievements | Navigate to achievements screen |
| Logout | Clears the active session and returns to Sign In |

---

### Feature 7 — Achievements

12 unlockable achievements displayed in a 2-column grid. Locked achievements appear semi-transparent.

| Achievement | Icon | Unlock Condition |
|---|---|---|
| First Session | 🎯 | Complete 1 focus session |
| Sessions 10 | 📚 | Complete 10 focus sessions |
| Sessions 50 | 🍅 | Complete 50 focus sessions |
| Tasks 10 | ✅ | Complete 10 tasks |
| Tasks 50 | 💯 | Complete 50 tasks |
| Hours 5 | ⏰ | Accumulate 5 total focus hours |
| Hours 10 | 🕐 | Accumulate 10 total focus hours |
| Streak 3 | 🔥 | Maintain a 3-day focus streak |
| Streak 7 | ⚡ | Maintain a 7-day focus streak |
| Deep Diver | 🌊 | Complete one session of 50 minutes or more |
| Ultra Beast | 🦁 | Complete one session of 90 minutes or more |
| Three Today | 🏆 | Complete 3 focus sessions in a single day |

Achievements are checked automatically after every task completion and focus session completion.

---

### Feature 8 — AI Task Planner

| Sub-Feature | Description |
|---|---|
| Chat Interface | Conversational UI with user bubbles (right) and AI bubbles (left) |
| Two-Phase Conversation | Phase 1: AI asks 2 clarifying questions about the project. Phase 2: AI generates a structured task plan |
| Task Generation | AI produces 4–8 tasks with title, priority (HIGH / MEDIUM / LOW), and description |
| Priority Indicators | 🔴 HIGH, 🟡 MEDIUM, 🟢 LOW shown in plan preview card |
| Create Tasks Button | One tap saves all AI-generated tasks directly to the Task database |
| Dismiss Plan | Dismiss the plan card without creating tasks |
| Loading Indicator | Typing animation shown while AI is generating a response |
| Error Handling | Specific messages for invalid key, rate limit, network error, and service unavailable |
| Rate Limit Retry | Automatically retries up to 3 times with exponential backoff (2s, 4s, 8s) on rate limit errors |
| Powered By | Groq API — llama-3.1-8b-instant model |

---

## Technologies & Libraries

### Core Language & Architecture

| Technology | Version | Role |
|---|---|---|
| Kotlin | 2.0.21 | Primary programming language |
| MVVM Pattern | — | Architectural pattern: ViewModel + LiveData + Repository |
| Android Architecture Components | — | ViewModel, LiveData, Room, Lifecycle |
| Kotlin Coroutines | 1.8.1 | Asynchronous programming (DB queries, API calls, timer) |
| ViewBinding | — | Type-safe access to XML views, eliminates findViewById |

---

### Database & Local Storage

| Technology | Version | Role |
|---|---|---|
| Room | 2.6.1 | SQLite ORM — persists tasks, sessions, achievements |
| KSP (Kotlin Symbol Processing) | 2.0.21-1.0.28 | Compile-time annotation processing for Room |
| SharedPreferences | — | Stores user session, settings, timer durations, reminder time |

---

### Networking

| Technology | Version | Role |
|---|---|---|
| Retrofit | 2.11.0 | HTTP client — all REST API calls |
| OkHttp | 4.12.0 | Underlying HTTP engine, connection management |
| Gson | 2.11.0 | JSON serialization and deserialization |

---

### External APIs

| API | Authentication | Used For |
|---|---|---|
| Groq API (llama-3.1-8b-instant) | Bearer Token | AI Task Planner conversations |
| ZenQuotes API | None (free) | Motivational quotes on Home screen |
| Open-Meteo API | None (free) | Weather data (temperature, condition) |
| ipapi.co | None (free) | Auto-detect user city for weather |
| Unsplash API | Client ID | Home screen background image |

---

### UI & Design

| Technology | Version | Role |
|---|---|---|
| Material Design Components | 1.12.0 | Buttons, dialogs, chips, bottom navigation, cards |
| ConstraintLayout | 2.2.1 | Primary layout engine for complex screens |
| RecyclerView | 1.4.0 | Task list, chat messages, achievements grid |
| DiffUtil | — | Efficient RecyclerView updates (avoids full redraws) |
| ViewPager2 | 1.1.0 | Onboarding page carousel |
| SwipeRefreshLayout | 1.1.0 | Pull-to-refresh gesture |
| MPAndroidChart | v3.1.0 | Weekly statistics bar chart |
| Glide | 4.16.0 | Remote image loading and caching (Unsplash) |

---

### Background & System Services

| Technology | Role |
|---|---|
| Foreground Service | Keeps timer running when app is in the background |
| AlarmManager | Schedules daily reminder notifications at user-set time |
| BroadcastReceiver (ReminderReceiver) | Fires the daily reminder notification when alarm triggers |
| BroadcastReceiver (BootReceiver) | Re-registers the daily reminder alarm after device reboot |
| CountDownTimer | Drives the per-second timer tick inside TimerService |

---

### Notifications & Media

| Technology | Role |
|---|---|
| NotificationCompat | Builds and displays notifications (timer, reminders) |
| NotificationChannel | Separate channels for focus timer and daily reminders (Android 8.0+) |
| PendingIntent | Makes notification buttons (Pause, Reset) functional |
| MediaPlayer | Plays and loops ambient sounds during focus sessions |

---

### Build & Configuration

| Technology | Version | Role |
|---|---|---|
| Android Gradle Plugin | 8.12.3 | Build system |
| Kotlin Gradle Plugin | 2.0.21 | Kotlin compilation |
| BuildConfig | — | Injects Groq API key from local.properties at build time |
| Kotlin Parcelize | — | Auto-generates Parcelable implementations |
| ProGuard | — | Code minification available for release builds |

---

## Technology-to-Feature Mapping

| Feature | Technologies Used |
|---|---|
| Splash / Routing | Activity, SharedPreferences, Intent |
| Sign Up / Sign In | Room (UserDao), ViewModel, LiveData, ViewBinding |
| Onboarding | ViewPager2, SharedPreferences, Activity |
| Home Dashboard | ViewModel, LiveData, Retrofit, Gson, Glide, Coroutines, ZenQuotes API, Open-Meteo API, ipapi.co, Unsplash API |
| Task Management | Room, ViewModel, LiveData, RecyclerView, DiffUtil, Material Dialogs, ViewBinding, Coroutines |
| Focus Timer | Foreground Service, CountDownTimer, MediaPlayer, NotificationCompat, PendingIntent, AlarmManager, Room, ViewModel |
| Statistics | Room, ViewModel, LiveData, MPAndroidChart, Coroutines |
| Profile & Settings | SharedPreferences, AppCompatDelegate (dark mode), AlarmManager, Room, Glide, Uri Permissions |
| Achievements | Room (AchievementDao), ViewModel, RecyclerView, LiveData, Coroutines |
| AI Task Planner | Retrofit, OkHttp, Gson, Groq API, ViewModel, LiveData, RecyclerView, Coroutines, BuildConfig |
| Daily Reminders | AlarmManager, BroadcastReceiver, NotificationCompat, BootReceiver, SharedPreferences |

---

## Database Schema

### Table: `tasks`
| Column | Type | Description |
|---|---|---|
| id | INTEGER (PK) | Auto-generated task ID |
| username | TEXT | Owner of the task |
| title | TEXT | Task title |
| description | TEXT | Optional task description |
| category | TEXT | Study / Work / Health / Personal |
| priority | TEXT | HIGH / MEDIUM / LOW |
| isCompleted | INTEGER | 0 = pending, 1 = completed |
| dueDate | TEXT | Optional due date |
| createdAt | INTEGER | Unix timestamp of creation |
| completedAt | INTEGER | Unix timestamp of completion (nullable) |
| xpReward | INTEGER | XP granted on completion |

### Table: `focus_sessions`
| Column | Type | Description |
|---|---|---|
| id | INTEGER (PK) | Auto-generated session ID |
| username | TEXT | Owner of the session |
| taskId | INTEGER | Linked task ID (nullable) |
| taskTitle | TEXT | Linked task title (nullable) |
| durationMinutes | INTEGER | Duration of the session in minutes |
| sessionType | TEXT | POMODORO / DEEP_FOCUS / ULTRA_FOCUS / SHORT_BREAK / LONG_BREAK |
| completedAt | INTEGER | Unix timestamp of session end |

### Table: `achievements`
| Column | Type | Description |
|---|---|---|
| id | TEXT (PK) | Achievement identifier key |
| username | TEXT | Owner of the achievement |
| unlockedAt | INTEGER | Unix timestamp of unlock |

---

## Android Components

| Component Type | Name | Purpose |
|---|---|---|
| Activity | SplashActivity | App entry point and routing |
| Activity | OnboardingActivity | First-launch onboarding flow |
| Activity | SignInActivity | User login |
| Activity | SignUpActivity | User registration |
| Activity | MainActivity | Host for all fragments, bottom nav, drawer |
| Fragment | HomeFragment | Dashboard screen |
| Fragment | TaskFragment | Task list and management |
| Fragment | TimerFragment | Pomodoro timer UI |
| Fragment | StatsFragment | Statistics and charts |
| Fragment | ProfileFragment | Profile and settings |
| Fragment | AchievementsFragment | Achievements grid |
| Fragment | AiPlannerFragment | AI chat planner |
| Service | TimerService | Background foreground service for timer |
| BroadcastReceiver | ReminderReceiver | Fires daily reminder notification |
| BroadcastReceiver | BootReceiver | Reschedules reminders on device boot |

---

## App Statistics

| Metric | Count |
|---|---|
| Total Features | 8 major features |
| Total Screens | 11 (5 Activities + 7 Fragments) |
| External APIs | 5 |
| Room Database Tables | 3 |
| Achievements | 12 |
| Android Permissions | 7 |
| Background Services | 1 Foreground Service + 2 BroadcastReceivers |
| Ambient Sound Tracks | 4 |
| Session Types | 3 Focus + 2 Break |
| Libraries Used | 15+ |

---

*Document generated for FocusQuest — MAD Mid Project, May 2026*
