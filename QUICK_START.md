# 🚀 Быстрый старт - Сборка APK

## Шаг 1: Создайте репозиторий на GitHub

1. Перейдите на [GitHub.com](https://github.com)
2. Нажмите **New repository**
3. Назовите репозиторий (например: `tv-channels-app`)
4. Выберите **Public**
5. Нажмите **Create repository**

## Шаг 2: Загрузите файлы

### Вариант A: Через веб-интерфейс GitHub
1. В созданном репозитории нажмите **uploading an existing file**
2. Перетащите все файлы из папки `TV` в браузер
3. Нажмите **Commit changes**

### Вариант B: Через Git (если установлен)
```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/tv-channels-app.git
git push -u origin main
```

## Шаг 3: Запустите сборку

1. В репозитории перейдите в раздел **Actions**
2. Нажмите **Build Android APK** → **Run workflow**
3. Дождитесь завершения (3-5 минут)

## Шаг 4: Скачайте APK

1. В **Actions** найдите зеленую галочку ✅
2. Нажмите на неё
3. Прокрутите вниз до **Artifacts**
4. Скачайте `tv-channels-app`

## 🎯 Готово!

Теперь у вас есть APK файл для установки на Android TV!

### Установка:
1. Включите "Неизвестные источники" в настройках Android TV
2. Скопируйте APK на устройство
3. Установите через файловый менеджер

---

**Примечание**: Если что-то не работает, проверьте раздел **Actions** - там будут показаны ошибки сборки. 