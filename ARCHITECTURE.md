# Tarasul Android - Architecture Documentation

## Project Structure

This project follows Clean Architecture principles with MVVM pattern, providing a scalable and maintainable codebase.

### Layer Architecture

```
app/
├── feature/          # Feature modules (UI + ViewModels)
│   ├── auth/         # Authentication features
│   ├── chat/         # Chat features (ChatListScreen, ChatScreen, ViewModels)
│   ├── home/         # Home screen with bottom navigation
│   └── login/        # Login screen
├── data/             # Data layer (Repositories)
├── di/               # Dependency Injection modules
├── viewmodels/       # Shared ViewModels
└── ui/               # Base UI components

core/
├── designsystem/     # Design system (Theme, Colors, Components)
└── realtime/         # Real-time communication (WebSocket, etc.)
```

## Key Components

### 1. Navigation Flow

**Login → Home → ChatList / Profile**
**ChatList → ChatScreen (per contact)**

- `NavGraph.kt`: Central navigation graph using Jetpack Compose Navigation
- Navigation parameters are type-safe and validated
- Back stack is managed properly to prevent memory leaks

### 2. Features

#### Login Screen
- Modern UI with password visibility toggle
- Form validation
- Loading states
- Keyboard actions for better UX

#### Home Screen
- Bottom navigation with two tabs: Chats and Profile
- Modular design - each tab is a separate composable
- No nested screens - follows single responsibility principle

#### Chat List Screen
- Displays contacts from repository
- ViewModel-driven with proper state management
- Loading states and error handling
- Dummy data for demonstration

#### Chat Screen
- Individual chat interface per contact
- Real-time message sending (local state)
- Modern message bubbles
- Online/offline indicators

#### Profile Screen
- Modern, clean UI
- Dark theme toggle (app-wide)
- Settings organized by categories
- Scalable for adding more settings

### 3. Data Layer

#### Repositories
- `SettingsRepository`: Manages app settings (theme, etc.)
- `ContactRepository`: Single source of truth for contacts
- `RealtimeRepository`: Handles real-time connections

**Benefits:**
- Separation of concerns
- Easy to mock for testing
- Can switch data sources (local/remote) without changing UI
- Supports caching strategies

### 4. ViewModels

All screens use ViewModels for:
- State management
- Business logic
- Lifecycle awareness
- Data persistence across configuration changes

**Examples:**
- `MainViewModel`: App-wide state (theme)
- `LoginViewModel`: Authentication state
- `ChatListViewModel`: Contacts list state
- `ChatViewModel`: Individual chat state

### 5. Design System

Located in `core/designsystem`, provides:
- **Theme**: Light and dark themes with Material 3
- **Colors**: Comprehensive color palette
- **Typography**: Consistent text styles
- **Components**: Reusable UI components (AppButton, AppTextField, etc.)

**Benefits:**
- Consistency across the app
- Easy to update design
- Supports theming
- Reduces code duplication

## Scalability Features

### 1. Modular Architecture
- Features are isolated and can be moved to separate modules
- Core modules can be shared across features
- Easy to add new features without affecting existing ones

### 2. Repository Pattern
- Data sources are abstracted
- Easy to add remote APIs, databases, or caching
- Supports offline-first architecture

### 3. State Management
- Unidirectional data flow
- Single source of truth
- Predictable state updates
- Easy to debug

### 4. Dependency Injection (Hilt)
- Automatic dependency management
- Scoped lifecycles
- Easy to test
- Reduces boilerplate

### 5. Navigation
- Type-safe navigation with Compose Navigation
- Deep linking support ready
- Proper back stack management

## Performance Optimizations

### 1. Lazy Loading
- Lists use `LazyColumn` for efficient rendering
- Only visible items are composed

### 2. State Hoisting
- State is hoisted to the appropriate level
- Minimizes recompositions

### 3. Remember and Derivation
- Expensive calculations are remembered
- Derived state is computed efficiently

### 4. ViewModel Scoping
- ViewModels are scoped to their lifecycle
- Data survives configuration changes
- Prevents memory leaks

### 5. Theme System
- Material 3 theming
- Dynamic colors support (Android 12+)
- Efficient color scheme switching

## Best Practices Implemented

1. **Single Responsibility**: Each class has one clear purpose
2. **Separation of Concerns**: UI, business logic, and data are separated
3. **DRY (Don't Repeat Yourself)**: Reusable components and utilities
4. **SOLID Principles**: Code follows SOLID design principles
5. **Testability**: Architecture supports unit and integration testing
6. **Type Safety**: Leverages Kotlin's type system for safety
7. **Error Handling**: Proper error states and user feedback
8. **Accessibility**: Follows accessibility guidelines

## Future Enhancements

### Easy to Add:
1. **Remote API Integration**: Update repositories to fetch from API
2. **Local Database**: Add Room for offline support
3. **Push Notifications**: Integrate Firebase Cloud Messaging
4. **Image Upload**: Add media handling in chat
5. **User Authentication**: Real authentication with backend
6. **Search Functionality**: Add search in contacts and messages
7. **Group Chats**: Extend chat model for group conversations
8. **Message Reactions**: Add emoji reactions to messages
9. **Voice/Video Calls**: Integrate WebRTC
10. **Analytics**: Add Firebase Analytics or custom analytics

## Testing Strategy

### Unit Tests
- ViewModels: Test business logic
- Repositories: Test data operations
- Utilities: Test helper functions

### Integration Tests
- Navigation: Test screen transitions
- UI: Test user interactions

### UI Tests
- Compose testing: Test composables
- End-to-end: Test complete user flows

## Development Guidelines

1. **Feature Development**:
   - Create feature package
   - Add screen composables
   - Create ViewModel if needed
   - Add to navigation graph
   - Update repositories if needed

2. **Code Style**:
   - Follow Kotlin coding conventions
   - Use meaningful names
   - Keep functions small and focused
   - Document complex logic

3. **Git Workflow**:
   - Feature branches for new features
   - Descriptive commit messages
   - Code review before merge

## Dependencies

Key dependencies and their purposes:
- **Jetpack Compose**: Modern UI toolkit
- **Hilt**: Dependency injection
- **Navigation Compose**: Type-safe navigation
- **DataStore**: Settings persistence
- **Coroutines**: Asynchronous programming
- **StateFlow**: Reactive state management
- **Material 3**: Design system

## Conclusion

This architecture provides a solid foundation for a scalable, maintainable, and performant Android application. The modular design allows for easy feature additions, the repository pattern enables flexible data management, and the MVVM architecture ensures clear separation of concerns.
