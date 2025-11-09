# 🚀 Quick Start Guide - Login & RTL Features

## 🎯 Test Credentials

### Working Logins
```
1. Email: test@example.com
   Password: password123

2. Email: john@example.com
   Password: john123

3. Email: admin@tarasul.com
   Password: admin123

4. DEMO MODE (any email):
   Email: <any-valid@email.com>
   Password: password123
```

---

## 🌍 Language Switching

### Method 1: Login Screen
1. Open app
2. Click language button (top-right corner) 🌐
3. Select language:
   - 🇺🇸 English
   - 🇸🇦 العربية (Arabic - KSA)

### Method 2: Profile Screen
1. Login → Navigate to Profile tab
2. Tap "Language" in Appearance section
3. Select preferred language
4. App restarts with new language

---

## ✅ What's Included

### Security
- ✅ Encrypted login credentials (AES-256)
- ✅ Encrypted auth tokens
- ✅ Secure language preference storage

### Languages
- ✅ English (en) - Full support
- ✅ Arabic/KSA (ar) - Full RTL support

### Features
- ✅ Mock login API (1.5s delay simulation)
- ✅ Form validation
- ✅ Error handling
- ✅ Loading states
- ✅ Persistent login
- ✅ Persistent language
- ✅ No hardcoded strings (58+ strings)

---

## 📁 Key Files

### New Files
```
data/
├── SecurePreferencesManager.kt   # Encrypted storage
├── LanguageManager.kt            # Language & RTL handling
└── api/
    └── LoginApi.kt               # Mock login API

res/
├── values/strings.xml            # English strings
└── values-ar/strings.xml         # Arabic strings
```

### Updated Files
```
LoginScreen.kt         # + Language switcher + String resources
ProfileScreen.kt       # + Language menu + String resources
LoginViewModel.kt      # + Mock API integration
MainActivity.kt        # + Language initialization
ChatListScreen.kt      # + String resources
ChatScreen.kt          # + String resources
HomeScreen.kt          # + String resources
build.gradle.kts       # + Security crypto dependency
```

---

## 🧪 Quick Test

1. **Test Login**:
   ```
   Email: test@example.com
   Password: password123
   Result: ✅ Login success
   ```

2. **Test Language Switch**:
   ```
   Click language button → Select العربية
   Result: ✅ UI changes to Arabic (RTL)
   ```

3. **Test Persistence**:
   ```
   Login → Switch to Arabic → Close app → Reopen
   Result: ✅ Still logged in + Arabic language
   ```

---

## 💡 Tips

### For Developers
- All strings in `values/strings.xml` and `values-ar/strings.xml`
- Use `stringResource(R.string.xxx)` everywhere
- Language changes recreate activity automatically
- All data stored encrypted in SharedPreferences

### For Testers
- Use demo mode (any email + "password123")
- Test RTL layout with Arabic
- Verify persistence after app restart
- Check all error messages in both languages

---

## 🔧 Build Commands

```bash
# Sync Gradle
./gradlew sync

# Build Debug
./gradlew assembleDebug

# Install on Device
./gradlew installDebug

# Run Tests
./gradlew test
```

---

## 📱 Supported Languages

| Language | Code | Direction | Status |
|----------|------|-----------|--------|
| English | en | LTR | ✅ Complete |
| Arabic (KSA) | ar | RTL | ✅ Complete |

---

## 🎨 UI Features

### Login Screen
- Language switcher button (top-right)
- Email field with validation
- Password field with show/hide toggle (👁️)
- Loading indicator during login
- Error messages in current language
- Form validation

### Profile Screen
- Language menu in Appearance section
- Visual indicator for selected language
- Dark theme toggle
- All settings with icons and descriptions

---

## 🔐 Security Notes

- **Encryption**: AES-256 GCM
- **Storage**: EncryptedSharedPreferences
- **Data**: Login state, email, token, language
- **Library**: androidx.security:security-crypto

---

## 📞 Common Issues

### Issue: Language not changing
**Solution**: Make sure activity recreates after language change

### Issue: Login not persisting
**Solution**: Check SecurePreferencesManager is injected properly

### Issue: Strings still hardcoded
**Solution**: Use `stringResource(R.string.xxx)` instead of string literals

---

## ✅ Checklist

Before testing:
- [ ] Build successful
- [ ] No linter errors
- [ ] Credentials ready (test@example.com / password123)
- [ ] Device/emulator running

During testing:
- [ ] Login works with test credentials
- [ ] Language switcher visible on Login
- [ ] Can switch to Arabic → UI changes to RTL
- [ ] Can switch back to English
- [ ] Language persists after app restart
- [ ] Login state persists after app restart
- [ ] Profile language menu works
- [ ] All text in correct language
- [ ] No hardcoded strings visible

---

## 🎉 Done!

All features implemented and ready to test!

For detailed documentation, see:
- `LOGIN_AND_RTL_IMPLEMENTATION.md` - Full implementation details
- `ARCHITECTURE.md` - Architecture guide
- `PROJECT_RESTRUCTURE.md` - Previous restructure details

---

*Quick Start Guide v1.0*
