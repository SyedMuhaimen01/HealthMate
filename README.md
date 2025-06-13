Android Dev Task:
 
 
Create a “Daily Health Tracker” Android App
Overview:
Build an Android app that allows users to track their daily health metrics such as water intake, step count, sleep hours, mood, and weight. The app should persist data, show daily summaries, and visualize trends over time.
Core Features:
User Interface:
Home screen with today’s health summary.
Bottom navigation with sections: Dashboard, History, Add Entry, and Settings.
Daily Entry:
Allow the user to input:
Water intake (ml or cups)
Sleep hours
Steps (manually or synced with Google Fit)
Mood (emoji-based scale)
Weight (kg/lbs)
History View:
List of past days with summary of entries.
Filter by date range.
Data Visualization:
Show bar or line charts using MPAndroidChart for trends (last 7 or 30 days).
Use Room DB or Firebase for persistent storage.
Reminders & Notifications:
Daily notification at set time to remind user to add health data.
Option to customize reminder time in settings.
Optional Advanced Features:
Sync with Firebase Authentication (optional user login).
Sync with Google Fit APIs (for step count).
Export data to CSV/PDF.
Dark/light theme support.
Tech Stack:
Language: Kotlin
Architecture: MVVM
UI: Jetpack Compose or XML (based on your current focus)
Storage: Room DB or Firebase Firestore
Charts: MPAndroidChart
Optional: WorkManager for daily reminders
