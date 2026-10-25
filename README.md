# HabitTracker

Автономный трекер привычек для Android и iOS на Kotlin Multiplatform и Compose Multiplatform.

## Что уже работает

- общий Compose UI для обеих платформ;
- добавление и удаление привычек;
- индивидуальная цель от 1 до 7 выполнений в неделю;
- отдельный недельный прогресс для каждой привычки;
- отметка выполнения за сегодня и предыдущие шесть дней;
- календарь месяца с выполненными и пропущенными днями;
- текущая серия и недельный прогресс;
- светлая и тёмная темы;
- локальное хранение без аккаунта, сети и облачного сервиса;
- стартовые примеры при первом запуске.

## Структура

- `app` — Android-приложение и `MainActivity`;
- `shared/commonMain` — модель, repository, store и весь Compose UI;
- `shared/androidMain` — хранение в `SharedPreferences`;
- `shared/iosMain` — хранение в `NSUserDefaults` и Compose-контроллер;
- `iosApp` — минимальная SwiftUI/Xcode-оболочка.

Слой `HabitRepository` отделяет UI от способа хранения. Поэтому локальное хранилище можно позднее заменить базой данных или дополнить синхронизацией, не меняя экран и его состояние.

## Запуск Android

1. Откройте корень проекта в актуальной Android Studio.
2. Выберите конфигурацию `app`.
3. Запустите на эмуляторе или устройстве с Android 7.0 (API 24) и новее.

Проверочная сборка из терминала:

```bash
./gradlew :app:assembleDebug
```

## Запуск iOS

Для iOS нужен Mac с Xcode:

1. Откройте `iosApp/iosApp.xcodeproj`.
2. В Signing & Capabilities выберите свою Apple Developer Team.
3. Выберите iPhone Simulator на Apple Silicon или физическое устройство.
4. Запустите схему `iosApp`.

Xcode перед компиляцией Swift автоматически вызывает Gradle и собирает общий framework `Shared`.

## Дальнейшее развитие

Порядок следующих итераций описан в [docs/ROADMAP.md](docs/ROADMAP.md).
