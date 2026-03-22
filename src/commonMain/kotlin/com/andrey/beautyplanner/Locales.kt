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
            "nav_menu" to "Меню",
            "nav_stats" to "Статистика",
            "nav_feedback" to "Обратная связь",
            "save" to "Сохранить",
            "cancel" to "Отмена",

            "client_name" to "Имя клиента",
            "phone" to "Телефон",
            "service" to "Процедура",
            "price" to "Стоимость",
            "free" to "Свободно",

            "all_appointments_list" to "Список всех записей:",
            "no_appointments" to "Нет записей",

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

            "close" to "Закрыть",
            "export_hint" to "Скопируйте этот JSON и сохраните его. Он нужен для переноса записей на другое устройство.",
            "import_hint" to "Вставьте JSON резервной копии ниже и нажмите «Импорт». Текущие записи будут заменены.",
            "import_btn" to "Импорт",
            "import_invalid_json" to "Не удалось импортировать: проверьте JSON (пустой или неверный формат).",

            "privacy_policy" to "Политика конфиденциальности",

            "delete_title" to "Удаление",
            "delete_btn" to "Удалить",
            "delete_confirm_prefix" to "Удалить запись",
            "delete_confirm_at" to "на",
            "continue_question" to "Продолжить?",

            "transfer_appt" to "Перенести запись",
            "transfer_title" to "Перенос записи",
            "transfer_choose_time" to "Выберите время",
            "transfer_confirm" to "Перенести",

            "transfer_conflict_title" to "Время занято",
            "transfer_conflict_text" to "Это время уже занято другой записью. Вы хотите перенести текущую запись сюда и затем переназначить вторую запись?",
            "transfer_conflict_a" to "Переносим",
            "transfer_conflict_b" to "Занято",
            "transfer_agree" to "Согласовать",

            "reschedule_title_for" to "Переназначить запись для",
            "reschedule_choose_time" to "Выберите новое время",
            "reschedule_confirm" to "Сохранить",

            "start_time" to "Начало",
            "duration_hours" to "Длительность (ч)",

            "notifications_section" to "Уведомления",
            "notifications_enabled" to "Включить уведомления",
            "notif_sound_label" to "Звук уведомления",
            "notif_sound_default" to "По умолчанию",
            "notif_sound_silent" to "Без звука",
            "reminders_when" to "Напоминать за:",

            "remind_days" to "Дни",
            "remind_hours" to "Часы",
            "remind_summary" to "Итог",
            "remind_off" to "Выключено",

            // --- Support / feedback ---
            "support_section" to "Служба поддержки",
            "support_phone_label" to "Телефон поддержки",
            "support_phone_hint" to "Введите номер (например: +39 123 456 789 или +7 999 000-00-00)",
            "support_phone_empty" to "Не указан",
            "support_feedback_text" to "Если у вас неполадки — позвоните в поддержку по номеру ниже.",
            "support_call" to "Позвонить",

            // --- Stats ---
            "stats_period_day" to "День",
            "stats_period_week" to "Неделя",
            "stats_period_month" to "Месяц",
            "stats_period_year" to "Год",
            "stats_range" to "Период",

            "stats_revenue" to "Выручка",
            "stats_count" to "Количество записей",
            "stats_hours" to "Отработано часов",
            "stats_top_services" to "Процедуры",
            "stats_procedures_done" to "Проведено",
            "stats_empty" to "Нет данных за выбранный период",
            "stats_unknown_service" to "Без названия",

            // Currency
            "currency_eur" to "€",

            "month_jan" to "Январь", "month_feb" to "Февраль", "month_mar" to "Март",
            "month_apr" to "Апрель", "month_may" to "Май", "month_jun" to "Июнь",
            "month_jul" to "Июль", "month_aug" to "Август", "month_sep" to "Сентябрь",
            "month_oct" to "Октябрь", "month_nov" to "Ноябрь", "month_dec" to "Декабрь",

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
            "nav_menu" to "Menu",
            "nav_stats" to "Statistics",
            "nav_feedback" to "Support",
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
            "delete_confirm_at" to "at",
            "continue_question" to "Continue?",

            "transfer_appt" to "Transfer appointment",
            "transfer_title" to "Transfer appointment",
            "transfer_choose_time" to "Choose time",
            "transfer_confirm" to "Transfer",

            "transfer_conflict_title" to "Time is busy",
            "transfer_conflict_text" to "This time is already booked. Do you want to move the current appointment here and then reschedule the other one?",
            "transfer_conflict_a" to "Moving",
            "transfer_conflict_b" to "Booked",
            "transfer_agree" to "Agree",

            "reschedule_title_for" to "Reschedule appointment for",
            "reschedule_choose_time" to "Choose new time",
            "reschedule_confirm" to "Save",

            "start_time" to "Start time",
            "duration_hours" to "Duration (h)",

            "notifications_section" to "Notifications",
            "notifications_enabled" to "Enable notifications",
            "notif_sound_label" to "Notification sound",
            "notif_sound_default" to "Default",
            "notif_sound_silent" to "Silent",
            "reminders_when" to "Remind before:",

            "remind_days" to "Days",
            "remind_hours" to "Hours",
            "remind_summary" to "Summary",
            "remind_off" to "Off",

            // --- Support / feedback ---
            "support_section" to "Support",
            "support_phone_label" to "Support phone",
            "support_phone_hint" to "Enter phone number (e.g. +39 123 456 789)",
            "support_phone_empty" to "Not set",
            "support_feedback_text" to "If something is not working, call support using the number below.",
            "support_call" to "Call",

            // --- Stats ---
            "stats_period_day" to "Day",
            "stats_period_week" to "Week",
            "stats_period_month" to "Month",
            "stats_period_year" to "Year",
            "stats_range" to "Range",

            "stats_revenue" to "Revenue",
            "stats_count" to "Appointments",
            "stats_hours" to "Hours worked",
            "stats_top_services" to "Services",
            "stats_procedures_done" to "Done",
            "stats_empty" to "No data for selected period",
            "stats_unknown_service" to "Unnamed",

            // Currency
            "currency_eur" to "€",

            "month_jan" to "January", "month_feb" to "February", "month_mar" to "March",
            "month_apr" to "April", "month_may" to "May", "month_jun" to "June",
            "month_jul" to "July", "month_aug" to "August", "month_sep" to "September",
            "month_oct" to "October", "month_nov" to "November", "month_dec" to "December",

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
            "nav_menu" to "Menu",
            "nav_stats" to "Statistiche",
            "nav_feedback" to "Supporto",
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
            "delete_confirm_at" to "alle",
            "continue_question" to "Continuare?",

            "transfer_appt" to "Sposta appuntamento",
            "transfer_title" to "Sposta appuntamento",
            "transfer_choose_time" to "Scegli l'orario",
            "transfer_confirm" to "Sposta",

            "transfer_conflict_title" to "Orario occupato",
            "transfer_conflict_text" to "Questo orario è già prenotato. Vuoi spostare l'appuntamento qui e poi riprogrammare l'altro?",
            "transfer_conflict_a" to "Spostiamo",
            "transfer_conflict_b" to "Occupato",
            "transfer_agree" to "Concorda",

            "reschedule_title_for" to "Riprogrammare appuntamento per",
            "reschedule_choose_time" to "Scegli nuovo orario",
            "reschedule_confirm" to "Salva",

            "start_time" to "Ora inizio",
            "duration_hours" to "Durata (h)",

            "notifications_section" to "Notifiche",
            "notifications_enabled" to "Abilita notifiche",
            "notif_sound_label" to "Suono notifica",
            "notif_sound_default" to "Predefinito",
            "notif_sound_silent" to "Silenzioso",
            "reminders_when" to "Ricorda prima:",

            "remind_days" to "Giorni",
            "remind_hours" to "Ore",
            "remind_summary" to "Riepilogo",
            "remind_off" to "Disattivato",

            // --- Support / feedback ---
            "support_section" to "Supporto",
            "support_phone_label" to "Telefono supporto",
            "support_phone_hint" to "Inserisci numero (es. +39 123 456 789)",
            "support_phone_empty" to "Non impostato",
            "support_feedback_text" to "Se qualcosa non funziona, chiama il supporto usando il numero qui sotto.",
            "support_call" to "Chiama",

            // --- Stats ---
            "stats_period_day" to "Giorno",
            "stats_period_week" to "Settimana",
            "stats_period_month" to "Mese",
            "stats_period_year" to "Anno",
            "stats_range" to "Periodo",

            "stats_revenue" to "Incasso",
            "stats_count" to "Appuntamenti",
            "stats_hours" to "Ore lavorate",
            "stats_top_services" to "Servizi",
            "stats_procedures_done" to "Eseguiti",
            "stats_empty" to "Nessun dato per il periodo selezionato",
            "stats_unknown_service" to "Senza nome",

            // Currency
            "currency_eur" to "€",

            "month_jan" to "Gennaio", "month_feb" to "Febbraio", "month_mar" to "Marzo",
            "month_apr" to "Aprile", "month_may" to "Maggio", "month_jun" to "Giugno",
            "month_jul" to "Luglio", "month_aug" to "Agosto", "month_sep" to "Settembre",
            "month_oct" to "Ottobre", "month_nov" to "Novembre", "month_dec" to "Dicembre",

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
            "nav_menu" to "Меню",
            "nav_stats" to "Статистика",
            "nav_feedback" to "Звʼязок",
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
            "import_hint" to "Вставте JSON резервної копії нижче та натисніть «Імпорт». Поточні записи буде замінено.",
            "import_btn" to "Імпорт",
            "import_invalid_json" to "Не вдалося імпортувати: перевірте JSON (порожній або невірний формат).",

            "privacy_policy" to "Політика конфіденційності",

            "delete_title" to "Видалення",
            "delete_btn" to "Видалити",
            "delete_confirm_prefix" to "Видалити запис",
            "delete_confirm_at" to "о",
            "continue_question" to "Продовжити?",

            "transfer_appt" to "Перенести запис",
            "transfer_title" to "Перенесення запису",
            "transfer_choose_time" to "Оберіть час",
            "transfer_confirm" to "Перенести",

            "transfer_conflict_title" to "Час зайнято",
            "transfer_conflict_text" to "Цей час уже зайнятий. Перенести запис сюди і потім переназначити інший?",
            "transfer_conflict_a" to "Переносимо",
            "transfer_conflict_b" to "Зайнято",
            "transfer_agree" to "Погодити",

            "reschedule_title_for" to "Переназначити запис для",
            "reschedule_choose_time" to "Оберіть новий час",
            "reschedule_confirm" to "Зберегти",

            "start_time" to "Початок",
            "duration_hours" to "Тривалість (год)",

            "notifications_section" to "Сповіщення",
            "notifications_enabled" to "Увімкнути сповіщення",
            "notif_sound_label" to "Звук сповіщення",
            "notif_sound_default" to "За замовчуванням",
            "notif_sound_silent" to "Без звуку",
            "reminders_when" to "Нагадувати за:",

            "remind_days" to "Дні",
            "remind_hours" to "Години",
            "remind_summary" to "Підсумок",
            "remind_off" to "Вимкнено",

            // --- Support / feedback ---
            "support_section" to "Підтримка",
            "support_phone_label" to "Телефон підтримки",
            "support_phone_hint" to "Введіть номер (наприклад: +39 123 456 789)",
            "support_phone_empty" to "Не вказано",
            "support_feedback_text" to "Якщо щось не працює — зателефонуйте в підтримку за номером нижче.",
            "support_call" to "Подзвонити",

            // --- Stats ---
            "stats_period_day" to "День",
            "stats_period_week" to "Тиждень",
            "stats_period_month" to "Місяць",
            "stats_period_year" to "Рік",
            "stats_range" to "Період",

            "stats_revenue" to "Виручка",
            "stats_count" to "Кількість записів",
            "stats_hours" to "Відпрацьовано годин",
            "stats_top_services" to "Процедури",
            "stats_procedures_done" to "Проведено",
            "stats_empty" to "Немає даних за вибраний період",
            "stats_unknown_service" to "Без назви",

            // Currency
            "currency_eur" to "€",

            "month_jan" to "Січень", "month_feb" to "Лютий", "month_mar" to "Березень",
            "month_apr" to "Квітень", "month_may" to "Травень", "month_jun" to "Червень",
            "month_jul" to "Липень", "month_aug" to "Серпень", "month_sep" to "Вересень",
            "month_oct" to "Жовтень", "month_nov" to "Листопад", "month_dec" to "Грудень",

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

    fun daysCount(n: Int): String {
        val langCode = AppSettings.languageCodes[AppSettings.selectedLanguage] ?: "ru"
        return when (langCode) {
            "ru" -> "$n ${ruPlural(n, "день", "дня", "дней")}"
            "uk" -> "$n ${ukPlural(n, "день", "дні", "днів")}"
            "it" -> if (n == 1) "$n giorno" else "$n giorni"
            else -> if (n == 1) "$n day" else "$n days"
        }
    }

    fun hoursCount(n: Int): String {
        val langCode = AppSettings.languageCodes[AppSettings.selectedLanguage] ?: "ru"
        return when (langCode) {
            "ru" -> "$n ${ruPlural(n, "час", "часа", "часов")}"
            "uk" -> "$n ${ukPlural(n, "година", "години", "годин")}"
            "it" -> if (n == 1) "$n ora" else "$n ore"
            else -> if (n == 1) "$n hour" else "$n hours"
        }
    }

    private fun ruPlural(n: Int, one: String, few: String, many: String): String {
        val nn = kotlin.math.abs(n) % 100
        val n1 = nn % 10
        return if (nn in 11..14) many else when (n1) {
            1 -> one
            2, 3, 4 -> few
            else -> many
        }
    }

    private fun ukPlural(n: Int, one: String, few: String, many: String): String {
        val nn = kotlin.math.abs(n) % 100
        val n1 = nn % 10
        return if (nn in 11..14) many else when (n1) {
            1 -> one
            2, 3, 4 -> few
            else -> many
        }
    }
}