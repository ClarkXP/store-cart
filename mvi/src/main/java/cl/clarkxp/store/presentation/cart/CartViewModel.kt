package cl.clarkxp.store.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.clarkxp.store.domain.usecase.CartState as DomainCartState
import cl.clarkxp.store.domain.usecase.ClearCartUseCase
import cl.clarkxp.store.domain.usecase.DecreaseQuantityUseCase
import cl.clarkxp.store.domain.usecase.GetCartUseCase
import cl.clarkxp.store.domain.usecase.IncreaseQuantityUseCase
import cl.clarkxp.store.presentation.cart.mvi.CartEffect
import cl.clarkxp.store.presentation.cart.mvi.CartIntent
import cl.clarkxp.store.presentation.cart.mvi.CartState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    getCartUseCase: GetCartUseCase,
    private val increaseQuantityUseCase: IncreaseQuantityUseCase,
    private val decreaseQuantityUseCase: DecreaseQuantityUseCase,
    private val clearCartUseCase: ClearCartUseCase
) : ViewModel() {

    // --- CANALES ---
    private val intentChannel = Channel<CartIntent>(Channel.UNLIMITED)

    private val _effect = Channel<CartEffect>()
    val effect = _effect.receiveAsFlow()

    // Estado interno para controlar el diálogo
    private val _showDialog = MutableStateFlow(false)

    // ESTADO UI COMBINADO
    val state: StateFlow<CartState> = combine(
        getCartUseCase(),
        _showDialog
    ) { domainState: DomainCartState, showDialog: Boolean ->
        CartState(
            isLoading = false,
            items = domainState.items,
            totalAmount = domainState.totalAmount,
            showClearDialog = showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CartState()
    )

    init {
        processIntents()
    }

    // --- PROCESADOR DE INTENCIONES (Input Único Secuencial) ---
    private fun processIntents() {
        viewModelScope.launch {
            intentChannel.consumeAsFlow().collect { intent ->
                when (intent) {
                    is CartIntent.IncreaseQuantity -> {
                        // Operación atómica: UPDATE SET quantity = quantity + 1
                        increaseQuantityUseCase(intent.item.id)
                    }
                    is CartIntent.DecreaseQuantity -> {
                        // Operación atómica: decrementa o elimina si llega a 0
                        decreaseQuantityUseCase(intent.item.id)
                    }
                    is CartIntent.OnClearCartClick -> {
                        if (state.value.items.isNotEmpty()) {
                            _showDialog.value = true
                        } else {
                            sendEffect(CartEffect.ShowSnackbar("El carro ya está vacío"))
                        }
                    }
                    is CartIntent.OnConfirmClearCart -> {
                        clearCartUseCase()
                        _showDialog.value = false
                        sendEffect(CartEffect.ShowSnackbar("Carro vaciado"))
                    }
                    is CartIntent.OnDismissClearDialog -> {
                        _showDialog.value = false
                    }
                    is CartIntent.OnBackClick -> sendEffect(CartEffect.NavigateBack)
                    is CartIntent.OnCheckoutClick -> sendEffect(CartEffect.ShowSnackbar("Compra simulada exitosa"))
                }
            }
        }
    }

    fun onIntent(intent: CartIntent) {
        val result = intentChannel.trySend(intent)
        if (result.isFailure) {
            // Manejo opcional si el canal se cierra/falla
        }
    }

    private fun sendEffect(effect: CartEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}