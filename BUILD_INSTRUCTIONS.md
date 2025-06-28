# Инструкции по сборке APK

## Сборка через GitHub Actions (Рекомендуется)

### 1. Создайте репозиторий на GitHub

1. Перейдите на [GitHub](https://github.com)
2. Создайте новый репозиторий (например, `tv-channels-app`)
3. Загрузите все файлы проекта в репозиторий

### 2. Запустите сборку

После загрузки файлов в репозиторий:

1. Перейдите в раздел **Actions** вашего репозитория
2. Выберите workflow **Build Android APK**
3. Нажмите **Run workflow** → **Run workflow**
4. Дождитесь завершения сборки (обычно 3-5 минут)

### 3. Скачайте APK

После успешной сборки:

1. В разделе **Actions** найдите завершенный workflow
2. Нажмите на него
3. Прокрутите вниз до раздела **Artifacts**
4. Скачайте файл `tv-channels-app`

## Альтернативные способы сборки

### Локальная сборка (требует Java)

Если у вас установлена Java:

```bash
# Установите JAVA_HOME
export JAVA_HOME=/path/to/your/java

# Соберите debug APK
./gradlew assembleDebug

# APK будет создан в:
# app/build/outputs/apk/debug/app-debug.apk
```

### Сборка через Android Studio

1. Откройте проект в Android Studio
2. Выберите **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
3. APK будет создан в папке `app/build/outputs/apk/debug/`

## Установка APK

### На Android TV устройстве:

1. Включите **Неизвестные источники** в настройках
2. Скопируйте APK на устройство (USB, сеть, ADB)
3. Установите через файловый менеджер

### Через ADB:

```bash
adb install app-debug.apk
```

## Структура проекта

```
TV/
├── .github/workflows/     # GitHub Actions
├── app/                   # Основной код приложения
├── gradle/               # Gradle wrapper
├── build.gradle          # Корневой build.gradle
├── settings.gradle       # Настройки проекта
└── README.md             # Документация
```

## Возможные проблемы

### Ошибка JAVA_HOME
```
ERROR: JAVA_HOME is not set and no 'java' command could be found in the PATH
```
**Решение**: Используйте GitHub Actions или установите Java

### Ошибка Gradle
```
Could not resolve dependencies
```
**Решение**: Проверьте интернет соединение и повторите сборку

### Ошибка подписи
```
Keystore file not found
```
**Решение**: Используйте debug сборку (`assembleDebug`)

## Контакты

При возникновении проблем создайте Issue в репозитории GitHub. 