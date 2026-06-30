package com.andrey.beautyplanner

object CurrencyCatalog {

    data class CurrencyInfo(
        val code: String,
        val symbol: String,
        val countries: String = ""
    )

    val all: List<CurrencyInfo> = listOf(
        CurrencyInfo("AUD", "$", "Australia"),
        CurrencyInfo("AED", "د.إ", "UAE"),
        CurrencyInfo("BDT", "৳", "Bangladesh"),
        CurrencyInfo("BOB", "Bs", "Bolivia"),
        CurrencyInfo("BRL", "R$", "Brazil"),
        CurrencyInfo("CAD", "C$", "Canada"),
        CurrencyInfo("CHF", "CHF", "Liechtenstein, Switzerland"),
        CurrencyInfo("CLP", "$", "Chile"),
        CurrencyInfo("COP", "COL$", "Colombia"),
        CurrencyInfo("CRC", "₡", "Costa Rica"),
        CurrencyInfo("CZK", "Kč", "Czech Republic"),
        CurrencyInfo("DKK", "kr", "Denmark"),
        CurrencyInfo("DZD", "DA", "Algeria"),
        CurrencyInfo("EGP", "E£", "Egypt"),
        CurrencyInfo("EUR", "€", "Eurozone"),
        CurrencyInfo("GBP", "£", "United Kingdom, Gibraltar"),
        CurrencyInfo("GEL", "₾", "Georgia"),
        CurrencyInfo("GHS", "₵", "Ghana"),
        CurrencyInfo("HKD", "HK$", "Hong Kong"),
        CurrencyInfo("HUF", "Ft", "Hungary"),
        CurrencyInfo("IDR", "Rp", "Indonesia"),
        CurrencyInfo("ILS", "₪", "Israel"),
        CurrencyInfo("INR", "₹", "India"),
        CurrencyInfo("IQD", "د.ع", "Iraq"),
        CurrencyInfo("JOD", "JD", "Jordan"),
        CurrencyInfo("JPY", "¥", "Japan"),
        CurrencyInfo("KES", "KSh", "Kenya"),
        CurrencyInfo("KRW", "₩", "South Korea"),
        CurrencyInfo("KZT", "₸", "Kazakhstan"),
        CurrencyInfo("LKR", "Rs", "Sri Lanka"),
        CurrencyInfo("MAD", "DH", "Morocco"),
        CurrencyInfo("MMK", "K", "Myanmar"),
        CurrencyInfo("MNT", "₮", "Mongolia"),
        CurrencyInfo("MOP", "MOP$", "Macau"),
        CurrencyInfo("MXN", "$", "Mexico"),
        CurrencyInfo("MYR", "RM", "Malaysia"),
        CurrencyInfo("NGN", "₦", "Nigeria"),
        CurrencyInfo("NOK", "kr", "Norway"),
        CurrencyInfo("NZD", "$", "New Zealand"),
        CurrencyInfo("PEN", "S/.", "Peru"),
        CurrencyInfo("PHP", "₱", "Philippines"),
        CurrencyInfo("PKR", "Rs", "Pakistan"),
        CurrencyInfo("PLN", "zł", "Poland"),
        CurrencyInfo("PYG", "₲", "Paraguay"),
        CurrencyInfo("QAR", "QR", "Qatar"),
        CurrencyInfo("RON", "lei", "Romania"),
        CurrencyInfo("RSD", "дин.", "Serbia"),
        CurrencyInfo("RUB", "₽", "Russia"),
        CurrencyInfo("SAR", "SR", "Saudi Arabia"),
        CurrencyInfo("SEK", "kr", "Sweden"),
        CurrencyInfo("SGD", "$", "Singapore"),
        CurrencyInfo("THB", "฿", "Thailand"),
        CurrencyInfo("TRY", "₺", "Turkey"),
        CurrencyInfo("TWD", "NT$", "Taiwan"),
        CurrencyInfo("TZS", "TSh", "Tanzania"),
        CurrencyInfo("UAH", "₴", "Ukraine"),
        CurrencyInfo("USD", "$", "United States and others"),
        CurrencyInfo("VND", "₫", "Vietnam"),
        CurrencyInfo("XAF", "FCFA", "Cameroon and others"),
        CurrencyInfo("XOF", "CFA", "Senegal and others"),
        CurrencyInfo("ZAR", "R", "South Africa")
    )

    fun getByCode(code: String): CurrencyInfo? {
        return all.find { it.code.equals(code, ignoreCase = true) }
    }

    fun getSymbol(code: String): String {
        return getByCode(code)?.symbol ?: code.uppercase()
    }
}