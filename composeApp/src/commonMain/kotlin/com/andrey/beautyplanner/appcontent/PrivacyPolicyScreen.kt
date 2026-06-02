package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.openEmail

private const val PRIVACY_EMAIL = "beautyplanner2026@gmail.com"
private const val AUTHOR_NAME = "KISELOV ANDRII"

@Composable
fun PrivacyPolicyScreen(
    languageCode: String,
    onBack: () -> Unit
) {
    val fontScale = AppSettings.getFontScale()
    val content = privacyPolicyContent(languageCode)

    val docTitleFontSize = (20 * fontScale).sp
    val docTitleLineHeight = (26 * fontScale).sp
    val sectionTitleFontSize = (16 * fontScale).sp
    val sectionTitleLineHeight = (22 * fontScale).sp
    val bodyFontSize = (14 * fontScale).sp
    val bodyLineHeight = (21 * fontScale).sp
    val metaFontSize = (12 * fontScale).sp
    val metaLineHeight = (16 * fontScale).sp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 20.dp)
        ) {
            Text(
                text = content.documentTitle,
                color = MaterialTheme.colors.onBackground,
                fontSize = docTitleFontSize,
                lineHeight = docTitleLineHeight,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = content.lastUpdated,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
                fontSize = metaFontSize,
                lineHeight = metaLineHeight
            )

            Spacer(modifier = Modifier.size(16.dp))

            RichParagraphText(
                text = content.intro,
                bodyFontSize = bodyFontSize,
                bodyLineHeight = bodyLineHeight
            )

            Spacer(modifier = Modifier.size(22.dp))

            content.sections.forEach { section ->
                Text(
                    text = section.title,
                    color = MaterialTheme.colors.onBackground,
                    fontSize = sectionTitleFontSize,
                    lineHeight = sectionTitleLineHeight,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.size(10.dp))

                section.paragraphs.forEach { paragraph ->
                    RichParagraphText(
                        text = paragraph,
                        bodyFontSize = bodyFontSize,
                        bodyLineHeight = bodyLineHeight
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                }

                section.bullets.forEach { bullet ->
                    BulletItem(
                        text = bullet,
                        bodyFontSize = bodyFontSize,
                        bodyLineHeight = bodyLineHeight
                    )
                }

                if (section.emailLines.isNotEmpty()) {
                    Spacer(modifier = Modifier.size(4.dp))
                    section.emailLines.forEach { emailLine ->
                        EmailLine(
                            prefix = emailLine,
                            email = PRIVACY_EMAIL,
                            bodyFontSize = bodyFontSize,
                            bodyLineHeight = bodyLineHeight
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                }

                Spacer(modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun RichParagraphText(
    text: String,
    bodyFontSize: androidx.compose.ui.unit.TextUnit,
    bodyLineHeight: androidx.compose.ui.unit.TextUnit
) {
    val onBackground = MaterialTheme.colors.onBackground
    val annotated = buildAnnotatedString {
        val parts = text.split(AUTHOR_NAME)
        parts.forEachIndexed { index, part ->
            append(part)
            if (index < parts.lastIndex) {
                pushStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = onBackground
                    )
                )
                append(AUTHOR_NAME)
                pop()
            }
        }
    }

    Text(
        text = annotated,
        color = onBackground,
        fontSize = bodyFontSize,
        lineHeight = bodyLineHeight
    )
}

@Composable
private fun BulletItem(
    text: String,
    bodyFontSize: androidx.compose.ui.unit.TextUnit,
    bodyLineHeight: androidx.compose.ui.unit.TextUnit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            color = MaterialTheme.colors.onBackground,
            fontSize = bodyFontSize,
            lineHeight = bodyLineHeight,
            modifier = Modifier.padding(end = 8.dp)
        )

        RichParagraphText(
            text = text,
            bodyFontSize = bodyFontSize,
            bodyLineHeight = bodyLineHeight
        )
    }
}

@Composable
private fun EmailLine(
    prefix: String,
    email: String,
    bodyFontSize: androidx.compose.ui.unit.TextUnit,
    bodyLineHeight: androidx.compose.ui.unit.TextUnit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (prefix.isNotBlank()) {
            RichParagraphText(
                text = prefix,
                bodyFontSize = bodyFontSize,
                bodyLineHeight = bodyLineHeight
            )
        }

        Text(
            text = email,
            color = MaterialTheme.colors.primary,
            fontSize = bodyFontSize,
            lineHeight = bodyLineHeight,
            fontWeight = FontWeight.Medium,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable {
                openEmail(email)
            }
        )
    }
}

private data class PrivacyPolicySection(
    val title: String,
    val paragraphs: List<String> = emptyList(),
    val bullets: List<String> = emptyList(),
    val emailLines: List<String> = emptyList()
)

private data class PrivacyPolicyContent(
    val documentTitle: String,
    val lastUpdated: String,
    val intro: String,
    val sections: List<PrivacyPolicySection>
)

private fun privacyPolicyContent(languageCode: String): PrivacyPolicyContent {
    return when (languageCode) {
        "ru" -> PrivacyPolicyContent(
            documentTitle = "Политика конфиденциальности приложения Beauty Planner",
            lastUpdated = "Последнее обновление: Май 2026 г.",
            intro = "Настоящая Политика конфиденциальности описывает, как $AUTHOR_NAME (далее «мы», «наш» или «Разработчик») обрабатывает данные при использовании мобильного приложения Beauty Planner.",
            sections = listOf(
                PrivacyPolicySection(
                    title = "1. Какие данные обрабатывает приложение",
                    paragraphs = listOf(
                        "Приложение Beauty Planner предназначено для локального ведения записей, расписания и напоминаний. Основные данные, которые вы вводите в приложение, сохраняются на вашем устройстве.",
                        "К таким данным могут относиться имя клиента, номер телефона, дата и время записи, название услуги, стоимость и другие заметки, которые вы добавляете вручную."
                    )
                ),
                PrivacyPolicySection(
                    title = "2. Где хранятся данные",
                    paragraphs = listOf(
                        "На текущем этапе основная информация, которую вы вводите в приложение, хранится локально на вашем устройстве.",
                        "Приложение также поддерживает экспорт и импорт резервных копий по вашей инициативе. Такие файлы создаются только по вашему действию и сохраняются в выбранное вами место."
                    )
                ),
                PrivacyPolicySection(
                    title = "3. Разрешения устройства",
                    paragraphs = listOf(
                        "Для работы отдельных функций приложение может запрашивать доступ к возможностям устройства."
                    ),
                    bullets = listOf(
                        "Уведомления — для напоминаний о предстоящих записях.",
                        "Контакты — только если вы сами используете автоподстановку или поиск клиента по контактам устройства.",
                        "Доступ к файлам — только в рамках системного выбора файла при импорте или экспорте резервной копии."
                    )
                ),
                PrivacyPolicySection(
                    title = "4. Покупки и подписка",
                    paragraphs = listOf(
                        "Для оформления подписки Premium приложение использует Google Play Billing на Android.",
                        "Информация об оплате обрабатывается соответствующей платформой магазина приложений. Само приложение не получает данные вашей банковской карты."
                    )
                ),
                PrivacyPolicySection(
                    title = "5. Передача данных третьим лицам",
                    paragraphs = listOf(
                        "Приложение не предназначено для передачи вашей клиентской базы или записей третьим лицам.",
                        "Данные не отправляются на отдельный внешний сервер приложения, если иное не указано явно в будущих обновлениях политики конфиденциальности."
                    )
                ),
                PrivacyPolicySection(
                    title = "6. Удаление данных",
                    paragraphs = listOf(
                        "Вы можете удалить данные приложения самостоятельно, очистив данные приложения в системе или удалив созданные резервные копии.",
                        "Также внутри приложения могут быть доступны функции удаления записей и очистки локальной базы данных."
                    )
                ),
                PrivacyPolicySection(
                    title = "7. Контакты",
                    paragraphs = listOf(
                        "Если у вас есть вопросы по данной Политике конфиденциальности, вы можете связаться с нами по адресу:"
                    ),
                    emailLines = listOf("")
                )
            )
        )

        "uk" -> PrivacyPolicyContent(
            documentTitle = "Політика конфіденційності додатка Beauty Planner",
            lastUpdated = "Останнє оновлення: Травень 2026 р.",
            intro = "Ця Політика конфіденційності описує, як $AUTHOR_NAME (далі «ми», «наш» або «Розробник») обробляє дані під час використання мобільного додатка Beauty Planner.",
            sections = listOf(
                PrivacyPolicySection(
                    title = "1. Які дані обробляє додаток",
                    paragraphs = listOf(
                        "Додаток Beauty Planner призначений для локального ведення записів, розкладу та нагадувань. Основні дані, які ви вводите, зберігаються на вашому пристрої.",
                        "До таких даних можуть належати ім’я клієнта, номер телефону, дата і час запису, назва послуги, вартість та інші нотатки, які ви додаєте вручну."
                    )
                ),
                PrivacyPolicySection(
                    title = "2. Де зберігаються дані",
                    paragraphs = listOf(
                        "На поточному етапі основна інформація, яку ви вводите в додаток, зберігається локально на вашому пристрої.",
                        "Додаток також підтримує експорт та імпорт резервних копій за вашою ініціативою. Такі файли створюються лише за вашою дією та зберігаються у вибране вами місце."
                    )
                ),
                PrivacyPolicySection(
                    title = "3. Дозволи пристрою",
                    paragraphs = listOf(
                        "Для роботи окремих функцій додаток може запитувати доступ до можливостей пристрою."
                    ),
                    bullets = listOf(
                        "Сповіщення — для нагадувань про майбутні записи.",
                        "Контакти — лише якщо ви самі використовуєте автопідстановку або пошук клієнта в контактах пристрою.",
                        "Доступ до файлів — лише в межах системного вибору файлу під час імпорту або експорту резервної копії."
                    )
                ),
                PrivacyPolicySection(
                    title = "4. Покупки та підписка",
                    paragraphs = listOf(
                        "Для оформлення підписки Premium додаток використовує Google Play Billing на Android.",
                        "Інформація про оплату обробляється відповідною платформою магазину додатків. Сам додаток не отримує дані вашої банківської картки."
                    )
                ),
                PrivacyPolicySection(
                    title = "5. Передача даних третім особам",
                    paragraphs = listOf(
                        "Додаток не призначений для передачі вашої клієнтської бази або записів третім особам.",
                        "Дані не надсилаються на окремий зовнішній сервер додатка, якщо інше не буде прямо вказано в майбутніх оновленнях політики конфіденційності."
                    )
                ),
                PrivacyPolicySection(
                    title = "6. Видалення даних",
                    paragraphs = listOf(
                        "Ви можете видалити дані додатка самостійно, очистивши дані додатка в системі або видаливши створені резервні копії.",
                        "Також усередині додатка можуть бути доступні функції видалення записів і очищення локальної бази даних."
                    )
                ),
                PrivacyPolicySection(
                    title = "7. Контакти",
                    paragraphs = listOf(
                        "Якщо у вас є запитання щодо цієї Політики конфіденційності, ви можете зв’язатися з нами за адресою:"
                    ),
                    emailLines = listOf("")
                )
            )
        )

        "it" -> PrivacyPolicyContent(
            documentTitle = "Informativa sulla Privacy di Beauty Planner",
            lastUpdated = "Ultimo aggiornamento: Maggio 2026",
            intro = "La presente Informativa sulla Privacy descrive come $AUTHOR_NAME (\"noi\", \"nostro\" o \"Sviluppatore\") tratta i dati durante l’utilizzo dell’app mobile Beauty Planner.",
            sections = listOf(
                PrivacyPolicySection(
                    title = "1. Quali dati tratta l'app",
                    paragraphs = listOf(
                        "Beauty Planner è progettata per gestire localmente appuntamenti, pianificazione e promemoria. I principali dati inseriti nell’app vengono salvati sul tuo dispositivo.",
                        "Questi dati possono includere nome del cliente, numero di telefono, data e ora dell’appuntamento, nome del servizio, prezzo e altre note inserite manualmente."
                    )
                ),
                PrivacyPolicySection(
                    title = "2. Dove vengono conservati i dati",
                    paragraphs = listOf(
                        "Nello stato attuale del progetto, le principali informazioni inserite nell’app vengono conservate localmente sul tuo dispositivo.",
                        "L’app supporta anche l’esportazione e l’importazione di backup su tua iniziativa. Tali file vengono creati solo su tua azione e salvati nella posizione da te scelta."
                    )
                ),
                PrivacyPolicySection(
                    title = "3. Permessi del dispositivo",
                    paragraphs = listOf(
                        "Per il funzionamento di alcune funzioni, l’app può richiedere accesso a determinate capacità del dispositivo."
                    ),
                    bullets = listOf(
                        "Notifiche — per ricordare i prossimi appuntamenti.",
                        "Contatti — solo se utilizzi volontariamente il completamento automatico o la ricerca cliente nei contatti del dispositivo.",
                        "Accesso ai file — solo nell’ambito della selezione di file di sistema durante importazione o esportazione del backup."
                    )
                ),
                PrivacyPolicySection(
                    title = "4. Acquisti e abbonamento",
                    paragraphs = listOf(
                        "Per l’attivazione dell’abbonamento Premium su Android, l’app utilizza Google Play Billing.",
                        "Le informazioni di pagamento vengono gestite dalla piattaforma del negozio di applicazioni. L’app non riceve i dati della tua carta bancaria."
                    )
                ),
                PrivacyPolicySection(
                    title = "5. Condivisione dei dati con terze parti",
                    paragraphs = listOf(
                        "L’app non è progettata per trasferire a terzi il tuo archivio clienti o i tuoi appuntamenti.",
                        "I dati non vengono inviati a un server esterno dedicato dell’app, salvo diversa indicazione esplicita in futuri aggiornamenti dell’informativa."
                    )
                ),
                PrivacyPolicySection(
                    title = "6. Eliminazione dei dati",
                    paragraphs = listOf(
                        "Puoi eliminare i dati dell’app in autonomia cancellando i dati dell’app dal sistema o eliminando i backup creati.",
                        "All’interno dell’app possono inoltre essere disponibili funzioni per eliminare gli appuntamenti o cancellare il database locale."
                    )
                ),
                PrivacyPolicySection(
                    title = "7. Contatti",
                    paragraphs = listOf(
                        "Per qualsiasi domanda relativa alla presente Informativa sulla Privacy, puoi contattarci all’indirizzo:"
                    ),
                    emailLines = listOf("")
                )
            )
        )

        else -> PrivacyPolicyContent(
            documentTitle = "Privacy Policy for Beauty Planner",
            lastUpdated = "Last Updated: May 2026",
            intro = "This Privacy Policy explains how $AUTHOR_NAME (\"we\", \"our\", or \"Developer\") processes data when you use the Beauty Planner mobile application.",
            sections = listOf(
                PrivacyPolicySection(
                    title = "1. What data the app processes",
                    paragraphs = listOf(
                        "Beauty Planner is designed for local appointment management, planning, and reminders. The main data you enter into the app is stored on your device.",
                        "This may include client name, phone number, appointment date and time, service name, price, and other notes that you enter manually."
                    )
                ),
                PrivacyPolicySection(
                    title = "2. Where data is stored",
                    paragraphs = listOf(
                        "At the current stage of the project, the main information entered into the app is stored locally on your device.",
                        "The app also supports backup export and import initiated by you. Such files are created only by your action and stored in the location you choose."
                    )
                ),
                PrivacyPolicySection(
                    title = "3. Device permissions",
                    paragraphs = listOf(
                        "For some features, the app may request access to certain device capabilities."
                    ),
                    bullets = listOf(
                        "Notifications — to remind you about upcoming appointments.",
                        "Contacts — only if you voluntarily use autocomplete or client lookup from your device contacts.",
                        "File access — only within the system file picker used for backup import or export."
                    )
                ),
                PrivacyPolicySection(
                    title = "4. Purchases and subscription",
                    paragraphs = listOf(
                        "For Premium subscription purchases on Android, the app uses Google Play Billing.",
                        "Payment information is processed by the relevant app store platform. The app itself does not receive your bank card details."
                    )
                ),
                PrivacyPolicySection(
                    title = "5. Sharing data with third parties",
                    paragraphs = listOf(
                        "The app is not designed to transfer your client database or appointment data to third parties.",
                        "Data is not sent to a dedicated external server of the app unless explicitly stated in future updates to this Privacy Policy."
                    )
                ),
                PrivacyPolicySection(
                    title = "6. Data deletion",
                    paragraphs = listOf(
                        "You can delete app data yourself by clearing app storage in the system or removing any backup files you created.",
                        "The app may also provide features for deleting appointments and clearing the local database."
                    )
                ),
                PrivacyPolicySection(
                    title = "7. Contact",
                    paragraphs = listOf(
                        "If you have any questions about this Privacy Policy, you can contact us at:"
                    ),
                    emailLines = listOf("")
                )
            )
        )
    }
}