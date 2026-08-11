# CallerID App — Truecaller-style, 100% Free Setup

## Kya bana hai
- Incoming call detect karta hai (CallScreeningService)
- Firestore database mein number search karke naam dikhata hai (overlay popup)
- User apni contacts sync kar sakta hai (crowdsource database ke liye)
- Spam report karne ka logic (FirestoreHelper.reportSpam)

## Setup Steps (sab FREE)

### 1. Android Studio
- Android Studio (free) install karo: https://developer.android.com/studio
- Is poori `CallerIDApp` folder ko "Open an existing project" se open karo

### 2. Firebase Project (FREE Spark Plan)
1. https://console.firebase.google.com par jao, "Add Project" karo (free)
2. Project ke andar "Add App" > Android choose karo
3. Package name daalo: `com.example.calleridapp`
4. `google-services.json` file download karo
5. Us file ko `CallerIDApp/app/` folder mein daal do (root of app module, build.gradle ke bagal mein)
6. Firebase console mein "Firestore Database" enable karo — "Start in test mode" (free, baad mein security rules tight karna)

### 3. Build & Run
- Android Studio mein "Sync Project with Gradle Files" karo
- Ek real phone connect karo (emulator par call-related features test nahi ho sakte achhe se) aur Run karo

### 4. App ke andar (MainActivity ke 4 buttons)
1. Permissions grant karo (phone, call log, contacts)
2. "Set as Default Caller ID App" — ye Android ka system dialog khol dega
3. "Allow Display Over Other Apps" — overlay popup ke liye zaroori
4. "Sync My Contacts" — apni contact list database mein upload karega

Jitne zyada log app install karke apni contacts sync karenge, utna database bada aur accurate hoga — bilkul Truecaller jaisa crowdsourcing model.

## Free tier limits (Firebase Spark Plan)
- 1 GB Firestore storage
- 50,000 reads/day, 20,000 writes/day
- Shuru ke hazaaron users ke liye ye kaafi hai. Jab traffic bahut badh jaye, tabhi paid "Blaze" plan ki zarurat padegi.

## Agle steps (future improvements)
- Spam auto-block (abhi sirf identify karta hai, block nahi karta)
- Phone number ko hash karke store karna (privacy ke liye better)
- Proper login/auth add karna taaki fake reports na ho
- Play Store policy: "Default Caller ID and Spam app" declare karna hoga review ke time
