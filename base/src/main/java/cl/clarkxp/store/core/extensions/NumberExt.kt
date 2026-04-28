package cl.clarkxp.store.core.extensions

import java.text.NumberFormat
import java.util.Locale

//Extension para formatear bien decimales de USD
fun Double?.toUSD(): String {
    if (this == null) return "$0.00"
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(this)
}