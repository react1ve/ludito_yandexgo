# Интеграция YandexMaps - Android приложение

## Описание проекта

Это Android приложение с интеграцией YandexMaps SDK, позволяющее искать локации, добавлять адреса в
избранное и работать с картой. Приложение разработано на языке Kotlin с использованием Jetpack
Compose для пользовательского интерфейса.

## Функциональность

- **Интеграция YandexMaps:** Полная интеграция с API YandexMaps с использованием предоставленного
  тестового ключа
- **Поиск:** Возможность поиска локаций на карте
- **Перемещаемый пин:** Реализация перемещаемого маркера в центре экрана, аналогично Яндекс GO
- **Управление адресами:** Добавление и удаление адресов из "Адресной книги" с сохранением в
  локальной базе данных
- **Мультимодульная архитектура:** Разделение ответственности с помощью модульного подхода

## Стек технологий

- **Язык программирования:** Kotlin
- **UI фреймворк:** XML, Custom View
- **Архитектура:** MVVM с принципами Clean Architecture
- **Внедрение зависимостей:** Koin
- **Локальное хранилище:** Room Database
- **Карты:** YandexMaps SDK
- **Асинхронные операции:** Kotlin Coroutines & Flow
- **Система сборки:** Gradle (Kotlin DSL)

## Настройка и запуск проекта

## Интеграция YandexMaps

В приложении используется YandexMaps SDK со следующим тестовым ключом:
Добавите в ваш `local.properties` следующие ключи:

```
YANDEX_MAPS_API_KEY=ВАШ_КЛЮЧ_ОТ_YANDEX_MAP_КИТА
YANDEX_GEOCODER_API_KEY=ВАШ_КЛЮЧ_ОТ_YANDEX_GEOCODER_КИТА
```

## Основные компоненты

### Экран карты

- Отображает YandexMaps с полем поиска вверху
- Показывает перемещаемый пин в центре экрана
- Предоставляет кнопки для добавления текущего местоположения в избранное

### Экран адресной книги

- Отображает список всех сохраненных адресов
- Позволяет удалять сохраненные адреса
- Предоставляет навигацию обратно на карту

### Функциональность поиска

- Поиск локаций в реальном времени с использованием API YandexMaps
- Отображение результатов поиска в виде списка
- Возможность выбора результата и навигации к этому месту на карте


## Реализация дизайна

Пользовательский интерфейс следует предоставленным макетам Figma с особым вниманием к:

- Точному представлению экрана карты
- Реализации функциональности поиска
- Макету и функциональности адресной книги
- Механике размещения и перемещения пина

![Скриншот задания](images/task.png)

https://www.figma.com/design/DjBQ7Xubvmy6rVdoFbB01V/Тестовое-Ludito?node-id=1-6&t=gNJuD9BnVYysYPXU-0