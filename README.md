# Finexis - Premium Wealth & Expense Manager

Finexis is a modern, high-fidelity, secure wealth management and expense tracking application built for Android. The project leverages **Jetpack Compose** for a fully reactive, premium user interface, **Firebase** for real-time authentication and database synchronization, and local background services like **WorkManager** to keep the user engaged.

Designed with rich aesthetics, smooth animations, dynamic calculations, and native device integrations, Finexis delivers a state-of-the-art personal finance experience.

---

## 🚀 Key Features & Highlights

### 1. Dynamic Onboarding & Visual Aesthetics
* **Premium Splash Screen**: Elegant scaling wallet card animation on a deep emerald green background.
* **Interactive Onboarding Pager**: A three-slide pager featuring parallax scrolling, scale transitions, page-specific background color glows (Blue ➔ Purple ➔ Green), and floating hover illustrations (micro-animations).
* **Dynamic Theme Toggle**: Seamlessly transitions between Light and Dark mode globally, adjusting background surfaces, outlines, text elements, and system bars.

### 2. Secure Auth & Touch ID App Lock
* **Firebase Authentication**: Full integration with Firebase Auth (including display name profile updates and forgot password email triggers) along with a clean local Mock Mode fallback.
* **Touch ID (Biometrics)**: native integration of Android's `androidx.biometric:biometric` API. Users can securely enable fingerprint verification in Settings to cache credentials locally.
* **WhatsApp-like App Lock**: A full-screen security lock overlay that intercepts the app startup. Utilizing lifecycle observers, it prompts the user for Touch ID verification every time the app returns to the foreground.

### 3. Global Slide-Down Banners (FinexisToast)
* **Custom Sliding Toast system**: Banners slide down smoothly from the top of the screen (integrating status bar paddings) to overlay navigation transitions, automatically auto-dismissing after 3 seconds.
* Used for real-time auth alerts, validation feedback, settings updates, and file-sharing status alerts.

### 4. Advanced Budgeting & Auto-Partitioning
* **Monthly & Category Limits**: Set overall monthly limits and individual budgets for categories: *Food*, *Transport*, *Shopping*, and *Other*.
* **Smart Auto-Partitioning Algorithm**:
  - If you configure a monthly budget limit but leave all category budgets as zero (`0.0`), the system automatically divides the monthly budget equally among the 4 categories.
  - If you define specific category limits, the remaining unallocated budget is automatically distributed equally among any categories that were left unset (`0.0`).

### 5. Multi-Currency with Live Exchange Rates
* **Dynamic Currency Conversion**: Real-time USD rates connection with FawazAhmed's open-access CDN currency API.
* **Offline Caching**: Retrieved rates are cached locally in SharedPreferences for offline support.
* **Support for Major Currencies**: Real-time conversions across PKR (base database currency), USD, EUR, GBP, INR, SAR, and AED.
* **Dual-Scale Inputs**: User inputs and configurations (limits, transactions) are entered in the preferred currency and stored in PKR to maintain consistency.

### 6. Interactive Search, Filters & Analytics
* **Interactive Trend Chart**: Custom monthly trends bar chart with interactive floating tooltips displaying total expenses for selected months.
* **Category Breakdown**: Expandable lists displaying transaction lists with spent amounts and percentages.
* **History Screen**: Unified search, Type filters (All, Expense, Income), Date filters (Today, This Week, This Month, All Time), and Category selectors. Includes horizontal scroll bounds to prevent parent page swipes while navigating filters.

### 7. PDF Report Export
* **Native Document Drawing**: Uses Android's `PdfDocument` to construct high-resolution statements on standard A4 canvas grids.
* **Auto-Pagination**: Tracks row height bounds and automatically splits transactions across pages, drawing header banners, page numbers, and structured table headers.
* **Safe Content Sharing**: Generates temporary permissions (`content://` URI) using Android's `FileProvider` to share files through the system Share sheet.

### 8. Local Notifications & Daily Reminders
* **JSON-Serializable Notification History**: Bell icon on Home screen displaying unread notification count. Cleared/marked as read via bottom sheet.
* **Periodic Daily Reminders**: A WorkManager request scheduled to prompt users to log their expenses at 8:00 PM daily in their preferred currency format.

---

## 🛠 Tech Stack & Dependencies

* **Language**: Kotlin
* **UI Framework**: Jetpack Compose (Material 3)
* **Asynchronous / Reactive**: Kotlin Coroutines & Kotlin StateFlow
* **Database**: Firebase Firestore & SharedPreferences (Local Caching)
* **Authentication**: Firebase Auth
* **Security & Prompts**: Android Biometrics API (`androidx.biometric:biometric`)
* **Background Tasks**: Android WorkManager (`androidx.work:work-runtime-ktx`)
* **Dependency Injection & Architecture**: MVVM (Model-View-ViewModel)

---

## 📱 Screen Workflows & Detailed Functionality

Each screen in the Finexis application is carefully structured to handle a specific part of the user workflow:

* **Splash Screen ([SplashScreen.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/screens/SplashScreen.kt))**: 
  - *Purpose*: The starting point of the application.
  - *Action*: Displays a premium scaling-up wallet logo animation. It automatically verifies if the user has completed the onboarding flow and checks their Firebase login state to route them to the Onboarding Screen, Login Screen, or Main Dashboard screen.
* **Onboarding Screen ([OnboardingScreen.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/screens/OnboardingScreen.kt))**: 
  - *Purpose*: Introduces the user to the app's value proposition.
  - *Action*: Houses three interactive slides that describe expense tracking, security (Touch ID), and budgeting. Users can swipe through slides featuring color-shifting background glow blobs, parallax-shifted texts, and floating hover elements. Includes "Skip" and "Get Started" triggers that save onboarding completion flags locally.
* **Login & Signup Screens ([LoginScreen.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/screens/LoginScreen.kt) & [SignupScreen.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/screens/SignupScreen.kt))**: 
  - *Purpose*: Manages secure user access.
  - *Action*: Enables manual authentication via email/password and quick Touch ID Login. Features strict validation checks (empty inputs trigger top-sliding error toasts), full name updates synced directly to user authentication profiles, and password reset sheet overlays.
* **Home Screen ([HomeScreen.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/screens/HomeScreen.kt))**: 
  - *Purpose*: The main dashboard showing financial health.
  - *Action*: Renders a welcome card with a greeting that dynamically changes based on the time of day (e.g., "Good morning," "Good afternoon," "Good evening," or "Good night"). Displays a green gradient Total Balance card with an interactive currency selector pill, a monthly budget utilization progress card, quick-action transaction inputs, and a notification bell showing live unread count badges.
* **History Screen ([HistoryScreen.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/screens/HistoryScreen.kt))**: 
  - *Purpose*: A comprehensive ledger of all transactions.
  - *Action*: Displays a clean chronological list of income and expenses. Users can search for specific logs, filter by transaction type (Income vs Expense), filter by date limits (Today, This Week, This Month, All Time), or select specific categories (Food, Transport, Shopping, Other). Horizon boundaries consume scroll events to prevent page swiping conflicts.
* **Budget Screen ([BudgetScreen.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/screens/BudgetScreen.kt))**: 
  - *Purpose*: Budget configurations and allocations tracking.
  - *Action*: Shows a monthly spent balance chart alongside individual category utilization gauges. Clicking the card opens the `BudgetDialog` sheet, where saving automatically splits unallocated budgets equally among unset categories.
* **Analytics Screen ([AnalyticsScreen.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/screens/AnalyticsScreen.kt))**: 
  - *Purpose*: Rich visual reporting and spending breakdown.
  - *Action*: Displays a custom monthly trends bar chart with floating tooltips that reveal details when tapped. Below the chart, it renders percentage charts of category spending and expandable list drawers showing transaction history for specific categories. Also includes triggers to export PDF statements.
* **Profile Settings Screen ([ProfileScreen.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/screens/ProfileScreen.kt))**: 
  - *Purpose*: Account settings and data sharing actions.
  - *Action*: Displays the user card containing verification status badges (synced via a 3-second polling system). Allows updating display names, selecting the default currency, toggling Light/Dark theme mode, enabling Touch ID lock options, exporting PDF statements, and executing secure sign-outs.

---

## 📂 Project Architecture & Codebase Map

* `ui/screens/`
  * [SplashScreen.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/screens/SplashScreen.kt): Fullscreen wallet card animations and splash transitions.
  * [OnboardingScreen.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/screens/OnboardingScreen.kt): Multi-slide interactive intro pager.
  * [LoginScreen.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/screens/LoginScreen.kt) / [SignupScreen.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/screens/SignupScreen.kt): Forms, field checks, Touch ID sign-in, and global toast warning integrations.
  * [HomeScreen.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/screens/HomeScreen.kt): Balance dashboard, unread notification badges, and currency pill actions.
  * [HistoryScreen.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/screens/HistoryScreen.kt): Multi-dimensional search/filtering grid.
  * [BudgetScreen.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/screens/BudgetScreen.kt): Budget limit tracks and dialog controllers.
  * [AnalyticsScreen.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/screens/AnalyticsScreen.kt): trend chart tooltips and category breakdowns.
  * [ProfileScreen.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/screens/ProfileScreen.kt): Account cards, currency selectors, and Touch ID toggle settings.
  * [MainContainerScreen.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/screens/MainContainerScreen.kt): Horizontal pages navigation bar, budget notifications dispatcher, and ON_START app security lock overlays.
* `ui/components/`
  * [FinexisToast.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/components/FinexisToast.kt): Global drop-down notification banners.
  * [BudgetDialog.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/components/BudgetDialog.kt): Budget limit updates and auto-partitioning algorithms.
  * [ExpenseDialog.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/components/ExpenseDialog.kt): Modal sheets for transactions, block upcoming dates, and compose shapes.
  * [ForgotPasswordBottomSheet.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/components/ForgotPasswordBottomSheet.kt) / [AccountInfoBottomSheet.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/components/AccountInfoBottomSheet.kt): Verification states, editable names, and card styling.
  * [AnimationComponents.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/components/AnimationComponents.kt): Slide-up animations and staggered index loading lists.
* `ui/utils/`
  * [BiometricHelper.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/utils/BiometricHelper.kt): Context wrappers unwrappers and native prompt triggers.
  * [CurrencyHelper.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/utils/CurrencyHelper.kt): Live exchange rates conversion ratios and format symbols.
  * [PdfExportHelper.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/ui/utils/PdfExportHelper.kt): Document pagination and sharing files helper.
* `viewmodel/`
  * [PreferenceViewModel.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/viewmodel/PreferenceViewModel.kt): Globals toast flows, themes, currencies, and biometric configurations.
  * [AuthViewModel.kt](file:///c:/Users/lenovo/AndroidProjects/Finexis/app/src/main/java/com/mazhar/finexis/viewmodel/AuthViewModel.kt): Signup credentials, profiles update, and passwords reset.

---

## 🛠 Development & Compilation

To build and compile the application, run:

```bash
# Windows
.\gradlew.bat compileDebugKotlin

# Linux / macOS
./gradlew compileDebugKotlin
```

Verify that Firebase settings (`google-services.json`) are correctly bound inside the `/app` folder to enable real-time database synchronizations.
