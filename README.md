# TV Channels - Android TV App

Приложение для просмотра телевизионных каналов на Android TV с поддержкой загрузки каналов из M3U плейлистов.

## Возможности

- 📺 **Просмотр TV каналов** - Воспроизведение потокового видео
- 🔄 **Автоматическая загрузка M3U** - Загрузка каналов из онлайн плейлистов
- 💾 **Кэширование** - Сохранение каналов локально для офлайн просмотра
- 🔍 **Поиск каналов** - Поиск по названию и описанию
- ⭐ **Избранное** - Сохранение любимых каналов
- 📱 **Android TV оптимизация** - Полная поддержка пульта ДУ
- 🎮 **Leanback UI** - Нативный интерфейс Android TV

## Загрузка каналов

Приложение автоматически загружает каналы из M3U плейлиста:
- **Источник**: https://gitlab.com/iptv135435/iptvshared/raw/main/IPTV_SHARED.m3u
- **Автообновление**: Каждые 24 часа
- **Кэширование**: Локальное хранение для быстрого доступа
- **Fallback**: Локальные тестовые каналы при недоступности плейлиста

### Поддерживаемые форматы M3U

```m3u
#EXTM3U
#EXTINF:-1 tvg-id="channel_id" tvg-logo="logo_url" group-title="category",Channel Name
http://stream.url/channel.m3u8
```

### Категории каналов

- **Общие** - Основные телеканалы
- **Развлекательные** - Развлекательные программы
- **Спорт** - Спортивные каналы
- **Детские** - Детские каналы
- **Культура** - Культурные программы
- **Новости** - Новостные каналы
- **Радио** - Радиостанции
- **Другие** - Прочие каналы

## Установка

1. Клонируйте репозиторий:
```bash
git clone <repository-url>
cd TV
```

2. Откройте проект в Android Studio

3. Подключите Android TV устройство или запустите эмулятор

4. Соберите и установите приложение:
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Использование

### Основной экран
- Используйте пульт ДУ для навигации
- Выберите категорию каналов
- Нажмите OK для воспроизведения канала

### Поиск
- Нажмите кнопку поиска на пульте
- Введите название канала
- Выберите результат из списка

### Избранное
- Долгое нажатие на канал для добавления/удаления из избранного
- Избранные каналы сохраняются локально

### Настройки
- Обновление плейлиста
- Очистка кэша
- Информация о приложении

## Технические детали

### Архитектура
- **MVVM** - Model-View-ViewModel архитектура
- **Repository Pattern** - Централизованное управление данными
- **Retrofit** - HTTP клиент для API
- **Gson** - JSON парсинг
- **SharedPreferences** - Локальное хранение настроек

### Основные компоненты

#### M3UParser
Парсер M3U плейлистов с поддержкой:
- Парсинг EXTINF метаданных
- Извлечение URL стримов
- Фильтрация информационных каналов
- Категоризация по group-title

#### ChannelRepository
Центральный репозиторий для управления каналами:
- Загрузка из M3U плейлиста
- Кэширование в SharedPreferences
- Поиск каналов
- Управление избранным

#### PlaylistUpdater
Утилита для обновления плейлиста:
- Автоматическое обновление каждые 24 часа
- Отслеживание времени последнего обновления
- Управление кэшем

### Структура проекта

```
app/src/main/java/com/example/tvchannels/
├── api/                    # API клиент и сервисы
│   ├── ApiClient.kt
│   └── ChannelApiService.kt
├── data/                   # Модели данных и репозиторий
│   ├── Channel.kt
│   ├── ChannelRepository.kt
│   ├── M3UParser.kt
│   └── TestStreams.kt
├── ui/                     # UI компоненты
│   ├── ChannelAdapter.kt
│   ├── ChannelPresenter.kt
│   └── SearchFragment.kt
├── utils/                  # Утилиты
│   └── PlaylistUpdater.kt
├── service/                # TV Input Service
│   └── TVInputService.kt
├── MainActivity.kt         # Главная активность
└── PlayerActivity.kt       # Активность плеера
```

### Зависимости

```gradle
dependencies {
    implementation 'androidx.leanback:leanback:1.0.0'
    implementation 'androidx.fragment:fragment-ktx:1.5.5'
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.google.code.gson:gson:2.9.0'
    implementation 'com.github.bumptech.glide:glide:4.14.2'
}
```

## Тестовые стримы

Для демонстрации используются публичные тестовые стримы:
- Big Buck Bunny (H.264)
- Sintel (H.264)
- Tears of Steel (H.264)
- И другие открытые медиафайлы

## Разрешения

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

## Совместимость

- **Минимальная версия**: Android 8.0 (API 26)
- **Целевая версия**: Android 13 (API 33)
- **Платформа**: Android TV
- **Поддержка**: Leanback, пульт ДУ, HLS стримы

## Лицензия

Этот проект предназначен для образовательных целей. Используйте только легальные источники контента.

## Поддержка

При возникновении проблем:
1. Проверьте интернет соединение
2. Убедитесь, что M3U плейлист доступен
3. Попробуйте очистить кэш приложения
4. Перезапустите приложение

## Обновления

Приложение автоматически проверяет обновления плейлиста каждые 24 часа. Для принудительного обновления используйте опцию "Обновить плейлист" в настройках. 