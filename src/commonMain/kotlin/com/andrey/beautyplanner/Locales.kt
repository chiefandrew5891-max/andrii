package com.andrey.beautyplanner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object Locales {

    var currentLanguage by mutableStateOf(
        AppSettings.languageCodes[AppSettings.selectedLanguage] ?: "ru"
    )

    private val strings = mapOf(
        "ru" to mapOf(
            "app_name" to "Beauty Planner",
            "nav_main" to "Главная",
            "nav_settings" to "Настройки",
            "nav_day" to "Детали дня",
            "save" to "Сохранить",
            "cancel" to "Отмена",

            "client_name" to "Имя клиента",
            "phone" to "Телефон",
            "service" to "Процедура",
            "price" to "Стоимость",
            "free" to "Свободно",

            "all_appointments_list" to "Список всех записей:",
            "no_appointments" to "Нет записей",

            // Upcoming list
            "upcoming_appointments_list" to "Предстоящие записи:",
            "no_upcoming_appointments" to "Нет предстоящих записей",

            "language_label" to "Язык",
            "theme_label" to "Тема",
            "font_size_label" to "Размер шрифта",
            "theme_light" to "Светлая",
            "theme_dark" to "Тёмная",
            "font_small" to "Мелкий",
            "font_medium" to "Средний",
            "font_large" to "Крупный",

            "backup_section" to "Резервное копирование",
            "export_db" to "Экспорт",
            "import_db" to "Импорт",

            // Backup dialogs
            "close" to "Закрыть",
            "export_hint" to "Скопируйте этот JSON и сохраните его. Он нужен для переноса записей на другое устройство.",
            "import_hint" to "Вставьте JSON резервной копии ниже и нажмите «Импорт». Текущие записи будут заменены.",
            "import_btn" to "Импорт",
            "import_invalid_json" to "Не удалось импортировать: проверьте JSON (пустой или неверный формат).",

            "privacy_policy" to "Политика конфиденциальности",

            "delete_title" to "Удаление",
            "delete_btn" to "Удалить",
            "delete_confirm_prefix" to "Удалить запись",
            "transfer_appt" to "Перенести запись",

            "start_time" to "Начало",
            "duration_hours" to "Длительность (ч)",

            // Months (nominative) for calendar title
            "month_jan" to "Январь", "month_feb" to "Февраль", "month_mar" to "Март",
            "month_apr" to "Апрель", "month_may" to "Май", "month_jun" to "Июнь",
            "month_jul" to "Июль", "month_aug" to "Август", "month_sep" to "Сентябрь",
            "month_oct" to "Октябрь", "month_nov" to "Ноябрь", "month_dec" to "Декабрь",

            // Months (genitive) for collapsed header: "21 марта"
            "month_jan_gen" to "января",
            "month_feb_gen" to "февраля",
            "month_mar_gen" to "марта",
            "month_apr_gen" to "апреля",
            "month_may_gen" to "мая",
            "month_jun_gen" to "июня",
            "month_jul_gen" to "июля",
            "month_aug_gen" to "августа",
            "month_sep_gen" to "сентября",
            "month_oct_gen" to "октября",
            "month_nov_gen" to "ноября",
            "month_dec_gen" to "декабря",

            "mon" to "Пн", "tue" to "Вт", "wed" to "Ср", "thu" to "Чт", "fri" to "Пт", "sat" to "Сб", "sun" to "Вс"
        ),

        "en" to mapOf(
            "app_name" to "Beauty Planner",
            "nav_main" to "Main",
            "nav_settings" to "Settings",
            "nav_day" to "Day Details",
            "save" to "Save",
            "cancel" to "Cancel",

            "client_name" to "Client Name",
            "phone" to "Phone",
            "service" to "Service",
            "price" to "Price",
            "free" to "Free",

            "all_appointments_list" to "All appointments list:",
            "no_appointments" to "No appointments",

            "upcoming_appointments_list" to "Upcoming appointments:",
            "no_upcoming_appointments" to "No upcoming appointments",

            "language_label" to "Language",
            "theme_label" to "Theme",
            "font_size_label" to "Font Size",
            "theme_light" to "Light",
            "theme_dark" to "Dark",
            "font_small" to "Small",
            "font_medium" to "Medium",
            "font_large" to "Large",

            "backup_section" to "Backup",
            "export_db" to "Export",
            "import_db" to "Import",

            "close" to "Close",
            "export_hint" to "Copy this JSON and save it. You can use it to transfer appointments to another device.",
            "import_hint" to "Paste the backup JSON below and press “Import”. Current appointments will be replaced.",
            "import_btn" to "Import",
            "import_invalid_json" to "Import failed: please check the JSON (empty or invalid format).",

            "privacy_policy" to "Privacy Policy",

            "delete_title" to "Delete",
            "delete_btn" to "Delete",
            "delete_confirm_prefix" to "Delete appointment for",
            "transfer_appt" to "Transfer appointment",

            "start_time" to "Start time",
            "duration_hours" to "Duration (h)",

            "month_jan" to "January", "month_feb" to "February", "month_mar" to "March",
            "month_apr" to "April", "month_may" to "May", "month_jun" to "June",
            "month_jul" to "July", "month_aug" to "August", "month_sep" to "September",
            "month_oct" to "October", "month_nov" to "November", "month_dec" to "December",

            // Genitive формы для EN не нужны — делаем равными обычным месяцам,
            // чтобы в свернутой шапке не показывало "month_mar_gen".
            "month_jan_gen" to "January",
            "month_feb_gen" to "February",
            "month_mar_gen" to "March",
            "month_apr_gen" to "April",
            "month_may_gen" to "May",
            "month_jun_gen" to "June",
            "month_jul_gen" to "July",
            "month_aug_gen" to "August",
            "month_sep_gen" to "September",
            "month_oct_gen" to "October",
            "month_nov_gen" to "November",
            "month_dec_gen" to "December",

            "mon" to "Mon", "tue" to "Tue", "wed" to "Wed", "thu" to "Thu", "fri" to "Fri", "sat" to "Sat", "sun" to "Sun"
        ),

        "it" to mapOf(
            "app_name" to "Beauty Planner",
            "nav_main" to "Home",
            "nav_settings" to "Impostazioni",
            "nav_day" to "Dettagli",
            "save" to "Salva",
            "cancel" to "Annulla",

            "client_name" to "Nome cliente",
            "phone" to "Telefono",
            "service" to "Procedura",
            "price" to "Prezzo",
            "free" to "Libero",

            "all_appointments_list" to "Elenco appuntamenti:",
            "no_appointments" to "Nessun appuntamento",

            "upcoming_appointments_list" to "Prossimi appuntamenti:",
            "no_upcoming_appointments" to "Nessun appuntamento futuro",

            "language_label" to "Lingua",
            "theme_label" to "Tema",
            "font_size_label" to "Dimensione carattere",
            "theme_light" to "Chiaro",
            "theme_dark" to "Scuro",
            "font_small" to "Piccolo",
            "font_medium" to "Medio",
            "font_large" to "Grande",

            "backup_section" to "Backup",
            "export_db" to "Esporta",
            "import_db" to "Importa",

            "close" to "Chiudi",
            "export_hint" to "Copia questo JSON e salvalo. Puoi usarlo per trasferire gli appuntamenti su un altro dispositivo.",
            "import_hint" to "Incolla qui sotto il JSON del backup e premi “Importa”. Gli appuntamenti attuali verranno sostituiti.",
            "import_btn" to "Importa",
            "import_invalid_json" to "Importazione non riuscita: controlla il JSON (vuoto o formato non valido).",

            "privacy_policy" to "Privacy Policy",

            "delete_title" to "Elimina",
            "delete_btn" to "Elimina",
            "delete_confirm_prefix" to "Eliminare appuntamento per",
            "transfer_appt" to "Sposta appuntamento",

            "start_time" to "Ora inizio",
            "duration_hours" to "Durata (h)",

            // В IT месяцы обычно пишутся со строчной буквы, но раз у тебя календарь с Заглавной — оставляем как есть.
            "month_jan" to "Gennaio", "month_feb" to "Febbraio", "month_mar" to "Marzo",
            "month_apr" to "Aprile", "month_may" to "Maggio", "month_jun" to "Giugno",
            "month_jul" to "Luglio", "month_aug" to "Agosto", "month_sep" to "Settembre",
            "month_oct" to "Ottobre", "month_nov" to "Novembre", "month_dec" to "Dicembre",

            // Genitive формы для IT тоже отдельно не нужны — делаем равными обычным месяцам.
            "month_jan_gen" to "Gennaio",
            "month_feb_gen" to "Febbraio",
            "month_mar_gen" to "Marzo",
            "month_apr_gen" to "Aprile",
            "month_may_gen" to "Maggio",
            "month_jun_gen" to "Giugno",
            "month_jul_gen" to "Luglio",
            "month_aug_gen" to "Agosto",
            "month_sep_gen" to "Settembre",
            "month_oct_gen" to "Ottobre",
            "month_nov_gen" to "Novembre",
            "month_dec_gen" to "Dicembre",

            "mon" to "Lun", "tue" to "Mar", "wed" to "Mer", "thu" to "Gio", "fri" to "Ven", "sat" to "Sab", "sun" to "Dom"
        ),

        "uk" to mapOf(
            "app_name" to "Beauty Planner",
            "nav_main" to "Головна",
            "nav_settings" to "Налаштування",
            "nav_day" to "Деталі дня",
            "save" to "Зберегти",
            "cancel" to "Скасувати",

            "client_name" to "Ім'я клієнта",
            "phone" to "Телефон",
            "service" to "Процедура",
            "price" to "Вартість",
            "free" to "Вільно",

            "all_appointments_list" to "Список всіх записів:",
            "no_appointments" to "Немає записів",

            "upcoming_appointments_list" to "Майбутні записи:",
            "no_upcoming_appointments" to "Немає майбутніх записів",

            "language_label" to "Мова",
            "theme_label" to "Тема",
            "font_size_label" to "Розмір шрифту",
            "theme_light" to "Світла",
            "theme_dark" to "Темна",
            "font_small" to "Дрібний",
            "font_medium" to "Середній",
            "font_large" to "Великий",

            "backup_section" to "Резервне копіювання",
            "export_db" to "Експорт",
            "import_db" to "Імпорт",

            "close" to "Закрити",
            "export_hint" to "Скопіюйте цей JSON і збережіть його. Він потрібен для перенесення записів на інший пристрій.",
            "import_hint" to "Вставьте JSON резервної копії ��ижче та натисніть «Імпорт». Поточні записи буде замінено.",
            "import_btn" to "Імпорт",
            "import_invalid_json" to "Не вдалося імпортувати: перевірте JSON (порожній або невірний формат).",

            "privacy_policy" to "Політика конфиденційності",

            "delete_title" to "Видалення",
            "delete_btn" to "Видалити",
            "delete_confirm_prefix" to "Видалити запис",
            "transfer_appt" to "Перенести запис",

            "start_time" to "Початок",
            "duration_hours" to "Тривалість (год)",

            "month_jan" to "Січень", "month_feb" to "Лютий", "month_mar" to "Березень",
            "month_apr" to "Квітень", "month_may" to "Травень", "month_jun" to "Червень",
            "month_jul" to "Липень", "month_aug" to "Серпень", "month_sep" to "Вересень",
            "month_oct" to "Жовтень", "month_nov" to "Листопад", "month_dec" to "Грудень",

            // Для UK (украинского) тоже добавляем *_gen.
            // Правильные формы родительного:
            "month_jan_gen" to "січня",
            "month_feb_gen" to "лютого",
            "month_mar_gen" to "березня",
            "month_apr_gen" to "квітня",
            "month_may_gen" to "травня",
            "month_jun_gen" to "червня",
            "month_jul_gen" to "липня",
            "month_aug_gen" to "серпня",
            "month_sep_gen" to "вересня",
            "month_oct_gen" to "жовтня",
            "month_nov_gen" to "листопада",
            "month_dec_gen" to "грудня",

            "mon" to "Пн", "tue" to "Вт", "wed" to "Ср", "thu" to "Чт", "fri" to "Пт", "sat" to "Сб", "sun" to "Нд"
        )
    )

    fun t(key: String): String {
        val langCode = AppSettings.languageCodes[AppSettings.selectedLanguage] ?: "ru"
        return strings[langCode]?.get(key) ?: strings["en"]?.get(key) ?: key
    }
}