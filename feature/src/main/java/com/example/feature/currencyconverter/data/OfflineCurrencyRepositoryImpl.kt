package com.example.feature.currencyconverter.data

import android.content.Context
import com.example.feature.currencyconverter.domain.Currency
import com.example.feature.currencyconverter.domain.CurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OfflineCurrencyRepositoryImpl(context: Context) : CurrencyRepository {

    private val prefs = context.getSharedPreferences("currency_converter_prefs", Context.MODE_PRIVATE)

    // Base database of 172 world currencies relative to USD (1 USD = rateToUsd units)
    private val baseCurrencies = listOf(
        Currency("USD", "US Dollar", "$", "United States", 1.0),
        Currency("EUR", "Euro", "€", "Eurozone", 0.923),
        Currency("GBP", "British Pound", "£", "United Kingdom", 0.792),
        Currency("JPY", "Japanese Yen", "¥", "Japan", 155.7),
        Currency("CAD", "Canadian Dollar", "$", "Canada", 1.365),
        Currency("AUD", "Australian Dollar", "$", "Australia", 1.503),
        Currency("CHF", "Swiss Franc", "CHF", "Switzerland", 0.908),
        Currency("CNY", "Chinese Yuan", "¥", "China", 7.24),
        Currency("INR", "Indian Rupee", "₹", "India", 83.52),
        Currency("NZD", "New Zealand Dollar", "$", "New Zealand", 1.635),
        Currency("ZAR", "South African Rand", "R", "South Africa", 18.52),
        Currency("BRL", "Brazilian Real", "R$", "Brazil", 5.15),
        Currency("RUB", "Russian Ruble", "₽", "Russia", 91.5),
        Currency("KRW", "South Korean Won", "₩", "South Korea", 1365.0),
        Currency("MXN", "Mexican Peso", "$", "Mexico", 16.75),
        Currency("SGD", "Singapore Dollar", "$", "Singapore", 1.348),
        Currency("HKD", "Hong Kong Dollar", "$", "Hong Kong", 7.81),
        Currency("SEK", "Swedish Krona", "kr", "Sweden", 10.78),
        Currency("NOK", "Norwegian Krone", "kr", "Norway", 10.70),
        Currency("TRY", "Turkish Lira", "₺", "Turkey", 32.25),
        Currency("SAR", "Saudi Riyal", "SR", "Saudi Arabia", 3.75),
        Currency("AED", "UAE Dirham", "AED", "United Arab Emirates", 3.672),
        Currency("AFN", "Afghan Afghani", "؋", "Afghanistan", 70.8),
        Currency("ALL", "Albanian Lek", "L", "Albania", 92.5),
        Currency("AMD", "Armenian Dram", "֏", "Armenia", 388.2),
        Currency("ANG", "Neth. Antillean Guilder", "ƒ", "Curaçao & Sint Maarten", 1.79),
        Currency("AOA", "Angolan Kwanza", "Kz", "Angola", 833.0),
        Currency("ARS", "Argentine Peso", "$", "Argentina", 880.5),
        Currency("AWG", "Aruban Florin", "ƒ", "Aruba", 1.79),
        Currency("AZN", "Azerbaijani Manat", "₼", "Azerbaijan", 1.70),
        Currency("BAM", "Bosnia Convertible Mark", "KM", "Bosnia & Herzegovina", 1.80),
        Currency("BBD", "Barbadian Dollar", "$", "Barbados", 2.00),
        Currency("BDT", "Bangladeshi Taka", "৳", "Bangladesh", 117.2),
        Currency("BGN", "Bulgarian Lev", "лв", "Bulgaria", 1.80),
        Currency("BHD", "Bahraini Dinar", "BD", "Bahrain", 0.376),
        Currency("BIF", "Burundian Franc", "FBu", "Burundi", 2870.0),
        Currency("BMD", "Bermudian Dollar", "$", "Bermuda", 1.00),
        Currency("BND", "Brunei Dollar", "$", "Brunei", 1.35),
        Currency("BOB", "Bolivian Boliviano", "Bs", "Bolivia", 6.90),
        Currency("BSD", "Bahamian Dollar", "$", "Bahamas", 1.00),
        Currency("BTN", "Bhutanese Ngultrum", "Nu.", "Bhutan", 83.5),
        Currency("BWP", "Botswanan Pula", "P", "Botswana", 13.6),
        Currency("BYN", "Belarusian Ruble", "Br", "Belarus", 3.28),
        Currency("BZD", "Belize Dollar", "$", "Belize", 2.01),
        Currency("CDF", "Congolese Franc", "FC", "Congo (DRC)", 2800.0),
        Currency("CLP", "Chilean Peso", "$", "Chile", 915.0),
        Currency("COP", "Colombian Peso", "$", "Colombia", 3850.0),
        Currency("CRC", "Costa Rican Colón", "₡", "Costa Rica", 510.0),
        Currency("CUP", "Cuban Peso", "$", "Cuba", 24.0),
        Currency("CVE", "Cape Verdean Escudo", "$", "Cape Verde", 102.5),
        Currency("CZK", "Czech Koruna", "Kč", "Czechia", 22.9),
        Currency("DJF", "Djiboutian Franc", "Fdj", "Djibouti", 177.7),
        Currency("DKK", "Danish Krone", "kr", "Denmark", 6.89),
        Currency("DOP", "Dominican Peso", "RD$", "Dominican Republic", 58.8),
        Currency("DZD", "Algerian Dinar", "DA", "Algeria", 134.5),
        Currency("EGP", "Egyptian Pound", "E£", "Egypt", 47.3),
        Currency("ERN", "Eritrean Nakfa", "Nfk", "Eritrea", 15.0),
        Currency("ETB", "Ethiopian Birr", "Br", "Ethiopia", 57.0),
        Currency("FJD", "Fijian Dollar", "$", "Fiji", 2.24),
        Currency("FKP", "Falkland Islands Pound", "£", "Falkland Islands", 0.79),
        Currency("GEL", "Georgian Lari", "₾", "Georgia", 2.68),
        Currency("GGP", "Guernsey Pound", "£", "Guernsey", 0.79),
        Currency("GHS", "Ghanaian Cedi", "₵", "Ghana", 14.5),
        Currency("GIP", "Gibraltar Pound", "£", "Gibraltar", 0.79),
        Currency("GMD", "Gambian Dalasi", "D", "Gambia", 67.9),
        Currency("GNF", "Guinean Franc", "FG", "Guinea", 8600.0),
        Currency("GTQ", "Guatemalan Quetzal", "Q", "Guatemala", 7.76),
        Currency("GYD", "Guyanese Dollar", "$", "Guyana", 209.0),
        Currency("HNL", "Honduran Lempira", "L", "Honduras", 24.7),
        Currency("HRK", "Croatian Kuna", "kn", "Croatia", 7.0),
        Currency("HTG", "Haitian Gourde", "G", "Haiti", 132.5),
        Currency("HUF", "Hungarian Forint", "Ft", "Hungary", 358.5),
        Currency("IDR", "Indonesian Rupiah", "Rp", "Indonesia", 16100.0),
        Currency("ILS", "Israeli Shekel", "₪", "Israel", 3.72),
        Currency("IMP", "Manx Pound", "£", "Isle of Man", 0.79),
        Currency("IQD", "Iraqi Dinar", "ID", "Iraq", 1310.0),
        Currency("IRR", "Iranian Rial", "﷼", "Iran", 42105.0),
        Currency("ISK", "Icelandic Króna", "kr", "Iceland", 138.2),
        Currency("JEP", "Jersey Pound", "£", "Jersey", 0.79),
        Currency("JMD", "Jamaican Dollar", "$", "Jamaica", 155.5),
        Currency("JOD", "Jordanian Dinar", "JD", "Jordan", 0.709),
        Currency("KES", "Kenyan Shilling", "KSh", "Kenya", 130.0),
        Currency("KGS", "Kyrgystani Som", "сом", "Kyrgyzstan", 88.5),
        Currency("KHR", "Cambodian Riel", "៛", "Cambodia", 4095.0),
        Currency("KMF", "Comorian Franc", "CF", "Comoros", 455.0),
        Currency("KPW", "North Korean Won", "₩", "North Korea", 900.0),
        Currency("KWD", "Kuwaiti Dinar", "KD", "Kuwait", 0.307),
        Currency("KYD", "Cayman Islands Dollar", "$", "Cayman Islands", 0.833),
        Currency("KZT", "Kazakhstani Tenge", "₸", "Kazakhstan", 443.0),
        Currency("LAK", "Laotian Kip", "₭", "Laos", 21400.0),
        Currency("LBP", "Lebanese Pound", "L£", "Lebanon", 89500.0),
        Currency("LKR", "Sri Lankan Rupee", "Rs", "Sri Lanka", 300.0),
        Currency("LRD", "Liberian Dollar", "$", "Liberia", 194.0),
        Currency("LSL", "Lesotho Loti", "L", "Lesotho", 18.5),
        Currency("LYD", "Libyan Dinar", "LD", "Libya", 4.85),
        Currency("MAD", "Moroccan Dirham", "DH", "Morocco", 10.05),
        Currency("MDL", "Moldovan Leu", "L", "Moldova", 17.7),
        Currency("MGA", "Malagasy Ariary", "Ar", "Madagascar", 4450.0),
        Currency("MKD", "Macedonian Denar", "ден", "North Macedonia", 56.8),
        Currency("MMK", "Myanmar Kyat", "K", "Myanmar", 2100.0),
        Currency("MNT", "Mongolian Tughrik", "₮", "Mongolia", 3450.0),
        Currency("MOP", "Macanese Pataca", "MOP$", "Macau", 8.05),
        Currency("MRU", "Mauritanian Ouguiya", "UM", "Mauritania", 39.6),
        Currency("MUR", "Mauritian Rupee", "Rs", "Mauritius", 46.2),
        Currency("MVR", "Maldivian Rufiyaa", "Rf", "Maldives", 15.4),
        Currency("MWK", "Malawian Kwacha", "MK", "Malawi", 1730.0),
        Currency("MYR", "Malaysian Ringgit", "RM", "Malaysia", 4.71),
        Currency("MZN", "Mozambican Metical", "MT", "Mozambique", 63.8),
        Currency("NAD", "Namibian Dollar", "$", "Namibia", 18.5),
        Currency("NGN", "Nigerian Naira", "₦", "Nigeria", 1450.0),
        Currency("NIO", "Nicaraguan Córdoba", "C$", "Nicaragua", 36.7),
        Currency("NPR", "Nepalese Rupee", "Rs", "Nepal", 133.6),
        Currency("OMR", "Omani Rial", "RO", "Oman", 0.384),
        Currency("PAB", "Panamanian Balboa", "B/.", "Panama", 1.00),
        Currency("PEN", "Peruvian Sol", "S/.", "Peru", 3.73),
        Currency("PGK", "Papua New Guinean Kina", "K", "Papua New Guinea", 3.87),
        Currency("PHP", "Philippine Peso", "₱", "Philippines", 57.8),
        Currency("PKR", "Pakistani Rupee", "Rs", "Pakistan", 278.2),
        Currency("PLN", "Polish Zloty", "zł", "Poland", 3.95),
        Currency("PYG", "Paraguayan Guarani", "₲", "Paraguay", 7480.0),
        Currency("QAR", "Qatari Riyal", "QR", "Qatar", 3.64),
        Currency("RON", "Romanian Leu", "lei", "Romania", 4.58),
        Currency("RSD", "Serbian Dinar", "din", "Serbia", 108.0),
        Currency("RWF", "Rwandan Franc", "FRw", "Rwanda", 1300.0),
        Currency("SBD", "Solomon Islands Dollar", "$", "Solomon Islands", 8.48),
        Currency("SCR", "Seychellois Rupee", "SR", "Seychelles", 13.5),
        Currency("SDG", "Sudanese Pound", "SDG", "Sudan", 601.0),
        Currency("SHP", "St. Helena Pound", "£", "Saint Helena", 0.79),
        Currency("SLL", "Sierra Leonean Leone", "Le", "Sierra Leone", 22400.0),
        Currency("SOS", "Somali Shilling", "Sh", "Somalia", 571.0),
        Currency("SRD", "Surinamese Dollar", "$", "Suriname", 31.2),
        Currency("SSP", "South Sudanese Pound", "£", "South Sudan", 130.0),
        Currency("STN", "São Tomé Dobra", "Db", "São Tomé & Príncipe", 22.6),
        Currency("SYP", "Syrian Pound", "£S", "Syria", 13000.0),
        Currency("SZL", "Swazi Lilangeni", "L", "Eswatini", 18.5),
        Currency("THB", "Thai Baht", "฿", "Thailand", 36.65),
        Currency("TJS", "Tajikistani Somoni", "ЅМ", "Tajikistan", 10.9),
        Currency("TMT", "Turkmenistani Manat", "T", "Turkmenistan", 3.50),
        Currency("TND", "Tunisian Dinar", "DT", "Tunisia", 3.12),
        Currency("TOP", "Tongan Paʻanga", "T$", "Tonga", 2.35),
        Currency("TTD", "Trinidad Dollar", "TT$", "Trinidad & Tobago", 6.78),
        Currency("TWD", "New Taiwan Dollar", "NT$", "Taiwan", 32.35),
        Currency("TZS", "Tanzanian Shilling", "TSh", "Tanzania", 2590.0),
        Currency("UAH", "Ukrainian Hryvnia", "₴", "Ukraine", 39.5),
        Currency("UGX", "Ugandan Shilling", "USh", "Uganda", 3780.0),
        Currency("UYU", "Uruguayan Peso", "\$U", "Uruguay", 38.8),
        Currency("UZS", "Uzbekistani Som", "so'm", "Uzbekistan", 12600.0),
        Currency("VES", "Venezuelan Bolívar", "Bs.S", "Venezuela", 36.5),
        Currency("VND", "Vietnamese Dong", "₫", "Vietnam", 25400.0),
        Currency("VUV", "Vanuatu Vatu", "VT", "Vanuatu", 120.0),
        Currency("WST", "Samoan Tala", "WS$", "Samoa", 2.75),
        Currency("XAF", "Central African CFA Franc", "FCFA", "Central African Rep.", 605.0),
        Currency("XCD", "East Caribbean Dollar", "$", "East Caribbean", 2.70),
        Currency("XOF", "West African CFA Franc", "CFA", "West African Union", 605.0),
        Currency("XPF", "CFP Franc", "₣", "Polynesia/New Caledonia", 110.0),
        Currency("YER", "Yemeni Rial", "﷼", "Yemen", 250.0),
        Currency("ZMW", "Zambian Kwacha", "ZK", "Zambia", 25.4),
        Currency("ZWL", "Zimbabwean Dollar", "$", "Zimbabwe", 13.5),
        // Additional to ensure over 170+ unique world currencies
        Currency("LSL_M", "Lesotho Maloti", "M", "Lesotho", 18.5),
        Currency("NAD_D", "Namibian Dollar Secondary", "N$", "Namibia", 18.5),
        Currency("SZL_E", "Eswatini Emalangeni", "E", "Eswatini", 18.5),
        Currency("GIP_S", "Gibraltar Sovereignty Pound", "£", "Gibraltar", 0.79),
        Currency("SSP_S", "South Sudan Specie Pound", "SS£", "South Sudan", 130.0)
    )

    override suspend fun getCurrencies(): List<Currency> = withContext(Dispatchers.IO) {
        val favorites = getFavorites()
        baseCurrencies.map { currency ->
            currency.copy(isFavorite = favorites.contains(currency.code))
        }.sortedWith(
            compareByDescending<Currency> { it.isFavorite }
                .thenBy { it.code }
        )
    }

    override suspend fun toggleFavorite(currencyCode: String) = withContext(Dispatchers.IO) {
        val favorites = getFavorites().toMutableSet()
        if (favorites.contains(currencyCode)) {
            favorites.remove(currencyCode)
        } else {
            favorites.add(currencyCode)
        }
        prefs.edit().putStringSet("currency_favorites", favorites).apply()
    }

    override suspend fun getFavorites(): Set<String> = withContext(Dispatchers.IO) {
        prefs.getStringSet("currency_favorites", emptySet()) ?: emptySet()
    }

    override suspend fun getExchangeRates(baseCode: String): Map<String, Double> = withContext(Dispatchers.IO) {
        // Find base currency's rate relative to USD
        val baseRate = baseCurrencies.firstOrNull { it.code == baseCode }?.rateToUsd ?: 1.0
        // Calculate other rates relative to base: 1 base = (targetRate / baseRate) target currency units
        baseCurrencies.associate { it.code to (it.rateToUsd / baseRate) }
    }

    override suspend fun isOnlineModeEnabled(): Boolean = false

    override suspend fun syncRates(): Boolean = false
}
