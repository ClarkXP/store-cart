package cl.clarkxp.store.presentation.mvi

// 1. STATE: Define CÓMO SE VE la pantalla en un momento dado.
// Debe ser inmutable.
interface UiState

// 2. INTENT: Define QUÉ QUIERE HACER el usuario.
// Clics, entradas de texto, cargas iniciales.
interface UiIntent

// 3. EFFECT: Define QUÉ DEBE PASAR UNA SOLA VEZ (Side Effects).
// Navegación, Toasts, Snackbars.
interface UiEffect