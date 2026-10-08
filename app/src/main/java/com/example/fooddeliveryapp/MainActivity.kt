package com.example.fooddeliveryapp

import android.os.Bundle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FreshGreen = Color(0xFF0B7A53)
private val LimeGreen = Color(0xFFA8D94B)
private val Background = Color(0xFFF7F8F5)
private val DarkText = Color(0xFF17211B)
private val GrayText = Color(0xFF6B746D)
private val LightGreen = Color(0xFFEAF5E2)
private val Border = Color(0xFFE1E6E1)
private val Red = Color(0xFFD64545)

private data class Product(
    val id: Int,
    val name: String,
    val category: String,
    val price: Int,
    val oldPrice: Int,
    val unit: String,
    val emoji: String,
    val rating: Double,
    val description: String
)

private data class CartItem(val product: Product, val quantity: Int)
private data class Address(
    val name: String = "",
    val phone: String = "",
    val line: String = "",
    val city: String = "",
    val pincode: String = ""
)
private data class Order(
    val id: String,
    val total: Int,
    val items: Int,
    val payment: String
)

private enum class Screen {
    SPLASH, WELCOME, LOGIN, SIGNUP,
    HOME, SEARCH, CATEGORIES, PRODUCT, FAVORITES,
    CART, ADDRESS, CHECKOUT, PAYMENT,
    SUCCESS, TRACKING, ORDERS,
    PROFILE, SETTINGS, NOTIFICATIONS, HELP
}

private val categories = listOf(
    "All", "Fruits", "Vegetables", "Dairy", "Bakery",
    "Snacks", "Beverages", "Breakfast", "Personal Care",
    "Household", "Frozen & Ready", "Meat & Seafood"
)

private val categoryEmoji = mapOf(
    "All" to "🛍️",
    "Fruits" to "🍎",
    "Vegetables" to "🥦",
    "Dairy" to "🥛",
    "Bakery" to "🍞",
    "Snacks" to "🍿",
    "Beverages" to "🧃",
    "Breakfast" to "🥣",
    "Personal Care" to "🧴",
    "Household" to "🧹",
    "Frozen & Ready" to "🍕",
    "Meat & Seafood" to "🥩"
)

private fun createProducts(): List<Product> {
    val data = listOf(
        "Fruits" to listOf(
            "Fresh Bananas" to "🍌", "Red Apples" to "🍎",
            "Green Apples" to "🍏", "Fresh Oranges" to "🍊",
            "Alphonso Mangoes" to "🥭", "Green Grapes" to "🍇",
            "Watermelon" to "🍉", "Papaya" to "🥭",
            "Pomegranate" to "🍎", "Pineapple" to "🍍"
        ),
        "Vegetables" to listOf(
            "Fresh Tomatoes" to "🍅", "Potatoes" to "🥔",
            "Red Onions" to "🧅", "Carrots" to "🥕",
            "Broccoli" to "🥦", "Cauliflower" to "🥬",
            "Spinach" to "🌿", "Green Capsicum" to "🫑",
            "Cucumber" to "🥒", "Beetroot" to "🫜"
        ),
        "Dairy" to listOf(
            "Full Cream Milk" to "🥛", "Toned Milk" to "🥛",
            "Fresh Curd" to "🍶", "Paneer" to "🧀",
            "Cheese Slices" to "🧀", "Butter" to "🧈",
            "Buttermilk" to "🥛", "Greek Yogurt" to "🥣",
            "Fresh Cream" to "🍶", "Flavoured Milk" to "🧋"
        ),
        "Bakery" to listOf(
            "White Bread" to "🍞", "Brown Bread" to "🍞",
            "Multigrain Bread" to "🥖", "Burger Buns" to "🍔",
            "Croissants" to "🥐", "Chocolate Muffins" to "🧁",
            "Fruit Cake" to "🍰", "Rusk" to "🍞",
            "Garlic Bread" to "🥖", "Pav Buns" to "🍔"
        ),
        "Snacks" to listOf(
            "Potato Chips" to "🥔", "Nacho Chips" to "🌽",
            "Salted Peanuts" to "🥜", "Trail Mix" to "🥜",
            "Popcorn" to "🍿", "Chocolate Cookies" to "🍪",
            "Cream Biscuits" to "🍪", "Masala Namkeen" to "🥨",
            "Protein Bar" to "🍫", "Granola Bar" to "🥣"
        ),
        "Beverages" to listOf(
            "Orange Juice" to "🧃", "Apple Juice" to "🍎",
            "Mango Juice" to "🥭", "Cola" to "🥤",
            "Lemon Drink" to "🍋", "Green Tea" to "🍵",
            "Black Tea" to "☕", "Instant Coffee" to "☕",
            "Mineral Water" to "💧", "Coconut Water" to "🥥"
        ),
        "Breakfast" to listOf(
            "Corn Flakes" to "🥣", "Rolled Oats" to "🌾",
            "Fruit Muesli" to "🥣", "Poha" to "🍚",
            "Upma Mix" to "🥣", "Pancake Mix" to "🥞",
            "Peanut Butter" to "🥜", "Strawberry Jam" to "🍓",
            "Natural Honey" to "🍯", "Chocolate Spread" to "🍫"
        ),
        "Personal Care" to listOf(
            "Daily Shampoo" to "🧴", "Hair Conditioner" to "🧴",
            "Body Wash" to "🧼", "Face Wash" to "🫧",
            "Herbal Toothpaste" to "🪥", "Soft Toothbrush" to "🪥",
            "Hand Wash" to "🧴", "Face Moisturizer" to "🧴",
            "Deodorant" to "🌸", "Hair Oil" to "💆"
        ),
        "Household" to listOf(
            "Dishwash Liquid" to "🧽", "Laundry Detergent" to "🧺",
            "Floor Cleaner" to "🧹", "Toilet Cleaner" to "🚽",
            "Garbage Bags" to "🗑️", "Facial Tissues" to "🧻",
            "Paper Towels" to "🧻", "Aluminium Foil" to "📦",
            "Kitchen Sponges" to "🧽", "Air Freshener" to "🌸"
        ),
        "Frozen & Ready" to listOf(
            "Frozen Green Peas" to "🫛", "French Fries" to "🍟",
            "Frozen Sweet Corn" to "🌽", "Veg Nuggets" to "🍗",
            "Frozen Paratha" to "🫓", "Vanilla Ice Cream" to "🍨",
            "Frozen Pizza" to "🍕", "Spring Rolls" to "🥟",
            "Frozen Momos" to "🥟", "Ready Pasta" to "🍝"
        ),
        "Meat & Seafood" to listOf(
            "Chicken Breast" to "🍗",
            "Chicken Curry Cut" to "🍗",
            "Chicken Wings" to "🍗",
            "Chicken Legs" to "🍗",
            "Chicken Mince" to "🍗",
            "Mutton Curry Cut" to "🥩",
            "Mutton Mince" to "🥩",
            "Mutton Chops" to "🍖",
            "Fish Fillets" to "🐟",
            "Fresh Rohu Fish" to "🐟",
            "Fresh Katla Fish" to "🐟",
            "Prawns" to "🍤",
            "Salmon Fillet" to "🐟",
            "Eggs" to "🥚",
            "Quail Eggs" to "🥚"
        )
    )

    val result = mutableListOf<Product>()
    var id = 1
    data.forEach { (category, items) ->
        items.forEachIndexed { index, pair ->
            val price = 35 + (id * 17 % 220)
            result += Product(
                id = id++,
                name = pair.first,
                category = category,
                price = price,
                oldPrice = price + 25 + (index % 3) * 10,
                unit = when (category) {
                    "Fruits" -> when (pair.first) {
                        "Watermelon", "Pineapple", "Papaya" -> "1 pc"
                        else -> "1 kg"
                    }
                    "Vegetables" -> "1 kg"
                    "Dairy" -> when (pair.first) {
                        "Full Cream Milk", "Toned Milk", "Buttermilk", "Flavoured Milk" -> "500 ml"
                        "Fresh Curd", "Greek Yogurt" -> "400 g"
                        "Paneer", "Cheese Slices" -> "200 g"
                        "Butter" -> "100 g"
                        "Fresh Cream" -> "200 ml"
                        else -> "1 pack"
                    }
                    "Bakery" -> "1 pack"
                    "Snacks" -> "1 pack"
                    "Beverages" -> when (pair.first) {
                        "Mineral Water" -> "1 L"
                        else -> "1 pack"
                    }
                    "Breakfast" -> "500 g"
                    "Personal Care", "Household" -> "1 pack"
                    "Frozen & Ready" -> "1 pack"
                    "Meat & Seafood" -> when (pair.first) {
                        "Eggs", "Quail Eggs" -> "12 pcs"
                        "Prawns" -> "500 g"
                        else -> "500 g"
                    }
                    else -> "1 pack"
                },
                emoji = pair.second,
                rating = 4.1 + ((index % 9) * 0.1),
                description = "Fresh, quality ${pair.first.lowercase()} selected for everyday shopping."
            )
        }
    }
    return result
}

private val products = createProducts()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FreshCartApp() }
    }
}

private fun friendlyAuthError(message: String?): String {
    val text = message.orEmpty().lowercase()
    return when {
        "password" in text && "6" in text -> "Password should be at least 6 characters."
        "email address is already in use" in text || "already in use" in text -> "This email is already registered. Please login instead."
        "badly formatted" in text || "invalid email" in text -> "Please enter a valid email address."
        "no user record" in text || "user not found" in text -> "No account found with this email."
        "password is invalid" in text || "wrong password" in text -> "Incorrect password. Please try again."
        "too many requests" in text -> "Too many attempts. Please wait and try again."
        "network" in text -> "Network error. Check your internet connection."
        else -> "Something went wrong. Please try again."
    }
}

@Composable
private fun FreshCartApp() {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var screen by remember { mutableStateOf(Screen.SPLASH) }
    var previousScreen by remember { mutableStateOf(Screen.HOME) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var selectedCategory by remember { mutableStateOf("All") }
    var search by remember { mutableStateOf("") }
    var favourites by remember { mutableStateOf(setOf<Int>()) }
    var cart by remember { mutableStateOf(listOf<CartItem>()) }
    var address by remember { mutableStateOf(Address()) }
    var payment by remember { mutableStateOf("Cash on Delivery") }
    var orders by remember { mutableStateOf(listOf<Order>()) }
    var coupon by remember { mutableStateOf("") }
    var couponApplied by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf("") }
    var authLoading by remember { mutableStateOf(false) }
    var dataLoaded by remember { mutableStateOf(false) }

    fun saveUserData(uid: String) {
        val cartData = cart.map {
            mapOf(
                "productId" to it.product.id,
                "quantity" to it.quantity
            )
        }

        val addressData = mapOf(
            "name" to address.name,
            "phone" to address.phone,
            "line" to address.line,
            "city" to address.city,
            "pincode" to address.pincode
        )

        val ordersData = orders.map {
            mapOf(
                "id" to it.id,
                "total" to it.total,
                "items" to it.items,
                "payment" to it.payment
            )
        }

        val userData = mapOf(
            "name" to userName,
            "email" to email,
            "favoriteIds" to favourites.toList(),
            "cart" to cartData,
            "address" to addressData,
            "orders" to ordersData
        )

        db.collection("users")
            .document(uid)
            .set(userData, com.google.firebase.firestore.SetOptions.merge())
    }

    fun loadUserData(uid: String, onLoaded: () -> Unit = {}) {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userName = document.getString("name").orEmpty()
                    email = document.getString("email")
                        ?: auth.currentUser?.email.orEmpty()

                    val savedFavorites = document.get("favoriteIds") as? List<*>
                    favourites = savedFavorites
                        ?.mapNotNull { item -> (item as? Number)?.toInt() }
                        ?.toSet()
                        ?: emptySet()

                    val savedCart = document.get("cart") as? List<*>
                    cart = savedCart?.mapNotNull { item ->
                        val map = item as? Map<*, *> ?: return@mapNotNull null
                        val productId = (map["productId"] as? Number)?.toInt() ?: return@mapNotNull null
                        val quantity = (map["quantity"] as? Number)?.toInt() ?: return@mapNotNull null
                        val product = products.firstOrNull { it.id == productId } ?: return@mapNotNull null
                        CartItem(product, quantity)
                    } ?: emptyList()

                    val savedAddress = document.get("address") as? Map<*, *>
                    if (savedAddress != null) {
                        address = Address(
                            name = savedAddress["name"] as? String ?: "",
                            phone = savedAddress["phone"] as? String ?: "",
                            line = savedAddress["line"] as? String ?: "",
                            city = savedAddress["city"] as? String ?: "",
                            pincode = savedAddress["pincode"] as? String ?: ""
                        )
                    }

                    val savedOrders = document.get("orders") as? List<*>
                    orders = savedOrders?.mapNotNull { item ->
                        val map = item as? Map<*, *> ?: return@mapNotNull null
                        val id = map["id"] as? String ?: return@mapNotNull null
                        val total = (map["total"] as? Number)?.toInt() ?: return@mapNotNull null
                        val items = (map["items"] as? Number)?.toInt() ?: return@mapNotNull null
                        val paymentMethod = map["payment"] as? String ?: "Cash on Delivery"
                        Order(id, total, items, paymentMethod)
                    } ?: emptyList()
                } else {
                    email = auth.currentUser?.email.orEmpty()
                }

                dataLoaded = true
                onLoaded()
            }
            .addOnFailureListener {
                email = auth.currentUser?.email.orEmpty()
                dataLoaded = true
                onLoaded()
            }
    }

    fun saveCurrentUserData() {
        val uid = auth.currentUser?.uid ?: return
        saveUserData(uid)
    }

    val filtered = products.filter {
        (selectedCategory == "All" || it.category == selectedCategory) &&
                (search.isBlank() || it.name.contains(search, true) || it.category.contains(search, true))
    }

    val cartCount = cart.sumOf { it.quantity }
    val subtotal = cart.sumOf { it.product.price * it.quantity }
    val delivery = if (cart.isEmpty()) 0 else if (subtotal >= 499) 0 else 40
    val discount = if (couponApplied) minOf(100, subtotal / 5) else 0
    val total = subtotal + delivery - discount

    fun navigate(to: Screen) {
        previousScreen = screen
        screen = to
    }

    fun addToCart(product: Product) {
        val found = cart.find { it.product.id == product.id }
        cart = if (found == null) {
            cart + CartItem(product, 1)
        } else {
            cart.map {
                if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it
            }
        }
        saveCurrentUserData()
    }

    fun increase(id: Int) {
        cart = cart.map {
            if (it.product.id == id) it.copy(quantity = it.quantity + 1) else it
        }
        saveCurrentUserData()
    }

    fun decrease(id: Int) {
        cart = cart.mapNotNull {
            if (it.product.id != id) it
            else if (it.quantity > 1) it.copy(quantity = it.quantity - 1) else null
        }
        saveCurrentUserData()
    }

    fun remove(id: Int) {
        cart = cart.filterNot { it.product.id == id }
        saveCurrentUserData()
    }

    fun toggleFavourite(id: Int) {
        val newFavourites = if (id in favourites) favourites - id else favourites + id
        favourites = newFavourites
        saveCurrentUserData()
    }

    BackHandler(enabled = screen !in setOf(Screen.SPLASH, Screen.WELCOME, Screen.HOME)) {
        screen = when (screen) {
            Screen.PRODUCT, Screen.CATEGORIES, Screen.SEARCH, Screen.FAVORITES -> Screen.HOME
            Screen.CART -> Screen.HOME
            Screen.ADDRESS -> Screen.CART
            Screen.CHECKOUT -> Screen.ADDRESS
            Screen.PAYMENT -> Screen.CHECKOUT
            Screen.SUCCESS, Screen.TRACKING, Screen.ORDERS -> Screen.HOME
            Screen.PROFILE, Screen.SETTINGS, Screen.NOTIFICATIONS, Screen.HELP -> Screen.HOME
            Screen.LOGIN, Screen.SIGNUP -> Screen.WELCOME
            else -> previousScreen
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = FreshGreen,
            secondary = LimeGreen,
            background = Background,
            surface = Color.White
        )
    ) {
        when (screen) {
            Screen.SPLASH -> Splash {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    if (!dataLoaded) {
                        email = currentUser.email.orEmpty()
                        loadUserData(currentUser.uid) {
                            screen = Screen.HOME
                        }
                    } else {
                        screen = Screen.HOME
                    }
                } else {
                    dataLoaded = true
                    screen = Screen.WELCOME
                }
            }

            Screen.WELCOME -> Welcome { navigate(Screen.LOGIN) }

            Screen.LOGIN -> AuthScreen(
                title = "Welcome Back 👋",
                button = "Login",
                name = "",
                email = email,
                password = password,
                showName = false,
                onName = {},
                onEmail = { email = it; authError = "" },
                onPassword = { password = it; authError = "" },
                authError = authError,
                loading = authLoading,
                showForgotPassword = true,
                onForgotPassword = {
                    if (email.isBlank()) {
                        authError = "Enter your email address first."
                    } else {
                        authLoading = true
                        auth.sendPasswordResetEmail(email.trim())
                            .addOnCompleteListener { task ->
                                authLoading = false
                                authError = if (task.isSuccessful) {
                                    "Password reset email sent. Check your inbox."
                                } else {
                                    friendlyAuthError(task.exception?.message)
                                }
                            }
                    }
                },
                onSubmit = {
                    if (email.isBlank() || password.isBlank()) {
                        authError = "Please enter email and password."
                    } else {
                        authLoading = true
                        auth.signInWithEmailAndPassword(email.trim(), password)
                            .addOnCompleteListener { task ->
                                authLoading = false
                                if (task.isSuccessful) {
                                    authError = ""
                                    val currentUser = task.result?.user
                                    email = currentUser?.email.orEmpty()
                                    dataLoaded = false
                                    if (currentUser != null) {
                                        loadUserData(currentUser.uid) {
                                            screen = Screen.HOME
                                        }
                                    } else {
                                        screen = Screen.HOME
                                    }
                                } else {
                                    authError = friendlyAuthError(task.exception?.message)
                                }
                            }
                    }
                },
                footer = "Don't have an account? Sign up",
                onFooter = { authError = ""; navigate(Screen.SIGNUP) }
            )

            Screen.SIGNUP -> AuthScreen(
                title = "Create Account 🛒",
                button = "Create Account",
                name = userName,
                email = email,
                password = password,
                showName = true,
                onName = { userName = it; authError = "" },
                onEmail = { email = it; authError = "" },
                onPassword = { password = it; authError = "" },
                authError = authError,
                loading = authLoading,
                showForgotPassword = false,
                onForgotPassword = {},
                onSubmit = {
                    if (userName.isBlank() || email.isBlank() || password.isBlank()) {
                        authError = "Please fill in all fields."
                    } else if (password.length < 6) {
                        authError = "Password should be at least 6 characters."
                    } else {
                        authLoading = true
                        auth.createUserWithEmailAndPassword(email.trim(), password)
                            .addOnCompleteListener { task ->
                                authLoading = false
                                if (task.isSuccessful) {
                                    authError = ""
                                    val currentUser = task.result?.user
                                    email = currentUser?.email.orEmpty()
                                    dataLoaded = true
                                    if (currentUser != null) {
                                        saveUserData(currentUser.uid)
                                    }
                                    screen = Screen.HOME
                                } else {
                                    authError = friendlyAuthError(task.exception?.message)
                                }
                            }
                    }
                },
                footer = "Already have an account? Login",
                onFooter = { authError = ""; navigate(Screen.LOGIN) }
            )

            Screen.HOME -> Home(
                products = filtered,
                favourites = favourites,
                cartCount = cartCount,
                selectedCategory = selectedCategory,
                search = search,
                onSearch = { search = it; navigate(Screen.SEARCH) },
                onCategory = {
                    selectedCategory = it
                    navigate(Screen.CATEGORIES)
                },
                onProduct = { selectedProduct = it; navigate(Screen.PRODUCT) },
                onFavourite = ::toggleFavourite,
                onAdd = ::addToCart,
                onCart = { navigate(Screen.CART) },
                onFavorites = { navigate(Screen.FAVORITES) },
                onProfile = { navigate(Screen.PROFILE) },
                onOrders = { navigate(Screen.ORDERS) }
            )

            Screen.SEARCH -> ProductList(
                title = "Search Products 🔎",
                products = filtered,
                favourites = favourites,
                search = search,
                onSearch = { search = it },
                onProduct = { selectedProduct = it; navigate(Screen.PRODUCT) },
                onFavourite = ::toggleFavourite,
                onAdd = ::addToCart,
                onBack = { screen = Screen.HOME },
                emptyText = "No products found"
            )

            Screen.CATEGORIES -> CategoryScreen(
                selected = selectedCategory,
                products = filtered,
                favourites = favourites,
                onFavourite = ::toggleFavourite,
                onAdd = ::addToCart,
                onSelect = { selectedCategory = it },
                onProduct = { selectedProduct = it; navigate(Screen.PRODUCT) },
                onBack = { screen = Screen.HOME }
            )

            Screen.PRODUCT -> selectedProduct?.let {
                ProductDetails(
                    product = it,
                    favourite = it.id in favourites,
                    onFavourite = { toggleFavourite(it.id) },
                    onAdd = {
                        addToCart(it)
                        navigate(Screen.CART)
                    },
                    onBack = { screen = Screen.HOME }
                )
            }

            Screen.FAVORITES -> ProductList(
                title = "My Favorites ❤️",
                products = products.filter { it.id in favourites },
                favourites = favourites,
                search = "",
                onSearch = {},
                onProduct = { selectedProduct = it; navigate(Screen.PRODUCT) },
                onFavourite = ::toggleFavourite,
                onAdd = ::addToCart,
                onBack = { screen = Screen.HOME },
                emptyText = "No favorite products yet ❤️"
            )

            Screen.CART -> CartScreen(
                cart = cart,
                subtotal = subtotal,
                delivery = delivery,
                discount = discount,
                total = total,
                coupon = coupon,
                couponApplied = couponApplied,
                onCoupon = { coupon = it },
                onApplyCoupon = { couponApplied = coupon.equals("FRESH20", true) },
                onIncrease = ::increase,
                onDecrease = ::decrease,
                onRemove = ::remove,
                onCheckout = { if (cart.isNotEmpty()) navigate(Screen.ADDRESS) },
                onBack = { screen = Screen.HOME }
            )

            Screen.ADDRESS -> AddressScreen(
                address = address,
                onSave = {
                    address = it
                    saveCurrentUserData()
                    navigate(Screen.CHECKOUT)
                },
                onBack = { screen = Screen.CART }
            )

            Screen.CHECKOUT -> CheckoutScreen(
                cart = cart,
                address = address,
                subtotal = subtotal,
                delivery = delivery,
                discount = discount,
                total = total,
                onAddress = { screen = Screen.ADDRESS },
                onPayment = { navigate(Screen.PAYMENT) },
                onBack = { screen = Screen.ADDRESS }
            )

            Screen.PAYMENT -> PaymentScreen(
                payment = payment,
                total = total,
                onPayment = { payment = it },
                onPlaceOrder = {
                    val order = Order(
                        id = "FC${System.currentTimeMillis().toString().takeLast(6)}",
                        total = total,
                        items = cartCount,
                        payment = payment
                    )
                    orders = listOf(order) + orders
                    cart = emptyList()
                    saveCurrentUserData()
                    navigate(Screen.SUCCESS)
                },
                onBack = { screen = Screen.CHECKOUT }
            )

            Screen.SUCCESS -> SuccessScreen(
                onTrack = { navigate(Screen.TRACKING) },
                onOrders = { navigate(Screen.ORDERS) },
                onHome = { screen = Screen.HOME }
            )

            Screen.TRACKING -> TrackingScreen(onBack = { screen = Screen.HOME })

            Screen.ORDERS -> OrdersScreen(
                orders = orders,
                onTrack = { navigate(Screen.TRACKING) },
                onBack = { screen = Screen.HOME }
            )

            Screen.PROFILE -> ProfileScreen(
                name = if (userName.isBlank()) "FreshCart User" else userName,
                email = email,
                cartCount = cartCount,
                favouriteCount = favourites.size,
                onOrders = { navigate(Screen.ORDERS) },
                onFavorites = { navigate(Screen.FAVORITES) },
                onSettings = { navigate(Screen.SETTINGS) },
                onNotifications = { navigate(Screen.NOTIFICATIONS) },
                onHelp = { navigate(Screen.HELP) },
                onLogout = {
                    auth.signOut()
                    favourites = emptySet()
                    cart = emptyList()
                    address = Address()
                    orders = emptyList()
                    userName = ""
                    email = ""
                    password = ""
                    authError = ""
                    dataLoaded = false
                    screen = Screen.WELCOME
                },
                onHome = { screen = Screen.HOME }
            )

            Screen.SETTINGS -> SimplePage(
                title = "Settings ⚙️",
                items = listOf(
                    "🔔 Notifications" to { navigate(Screen.NOTIFICATIONS) },
                    "📍 Delivery Address" to { navigate(Screen.ADDRESS) },
                    "🔒 Account & Security" to {},
                    "🌐 Language: English" to {},
                    "🌙 Appearance" to {}
                ),
                onBack = { screen = Screen.PROFILE }
            )

            Screen.NOTIFICATIONS -> SimplePage(
                title = "Notifications 🔔",
                items = listOf(
                    "🎉 Fresh20 discount is available" to {},
                    "📦 Your previous order was delivered" to {},
                    "🚚 Fast delivery is available in your area" to {},
                    "🥦 Fresh vegetables are on sale today" to {}
                ),
                onBack = { screen = Screen.PROFILE }
            )

            Screen.HELP -> SimplePage(
                title = "Help & Support ❓",
                items = listOf(
                    "📞 Contact Support" to {},
                    "💬 Chat with Support" to {},
                    "📦 Track an Order" to { navigate(Screen.TRACKING) },
                    "💳 Payment Help" to {},
                    "↩️ Returns & Refunds" to {}
                ),
                onBack = { screen = Screen.PROFILE }
            )
        }
    }
}


// ============================================================
// COMMON UI
// ============================================================

@Composable
private fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
            Spacer(Modifier.width(4.dp))
        }
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkText,
            modifier = Modifier.weight(1f))
        if (action != null) action()
    }
}

@Composable
private fun SearchBox(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search groceries...") },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun ProductCard(
    product: Product,
    favourite: Boolean,
    onProduct: () -> Unit,
    onFavourite: () -> Unit,
    onAdd: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onProduct() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(78.dp).clip(RoundedCornerShape(16.dp))
                    .background(LightGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(product.emoji, fontSize = 42.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                if (product.oldPrice > product.price) {
                    val percent = ((product.oldPrice - product.price) * 100 / product.oldPrice)
                    Surface(
                        color = LightGreen,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("$percent% OFF", color = FreshGreen, fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                    Spacer(Modifier.height(3.dp))
                }
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(product.category, color = GrayText, fontSize = 12.sp)
                Text(product.unit, color = GrayText, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⭐ ${"%.1f".format(product.rating)}", fontSize = 12.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("₹${product.price}", color = FreshGreen,
                        fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.width(5.dp))
                    Text("₹${product.oldPrice}", color = GrayText, fontSize = 11.sp)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onFavourite) {
                    Icon(
                        if (favourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        tint = if (favourite) Red else FreshGreen
                    )
                }
                Button(
                    onClick = onAdd,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ Add")
                }
            }
        }
    }
}

@Composable
private fun BottomBar(
    cartCount: Int,
    onHome: () -> Unit,
    onFavorites: () -> Unit,
    onCart: () -> Unit,
    onProfile: () -> Unit
) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(selected = true, onClick = onHome,
            icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
        NavigationBarItem(selected = false, onClick = onFavorites,
            icon = { Icon(Icons.Default.FavoriteBorder, null) }, label = { Text("Favorites") })
        NavigationBarItem(selected = false, onClick = onCart,
            icon = {
                BadgedBox(badge = {
                    if (cartCount > 0) Badge { Text(cartCount.toString()) }
                }) { Icon(Icons.Default.ShoppingCart, null) }
            }, label = { Text("Cart") })
        NavigationBarItem(selected = false, onClick = onProfile,
            icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
    }
}

// ============================================================
// HOME
// ============================================================

@Composable
private fun Home(
    products: List<Product>,
    favourites: Set<Int>,
    cartCount: Int,
    selectedCategory: String,
    search: String,
    onSearch: (String) -> Unit,
    onCategory: (String) -> Unit,
    onProduct: (Product) -> Unit,
    onFavourite: (Int) -> Unit,
    onAdd: (Product) -> Unit,
    onCart: () -> Unit,
    onFavorites: () -> Unit,
    onProfile: () -> Unit,
    onOrders: () -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomBar(cartCount, { }, onFavorites, onCart, onProfile)
        }
    ) { p ->
        LazyColumn(
            Modifier.fillMaxSize().background(Background).padding(p),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("FreshCart 🛒", color = FreshGreen,
                            fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Fresh groceries, delivered fast", color = GrayText)
                    }
                    IconButton(onClick = onProfile) {
                        Icon(Icons.Default.Person, null, tint = FreshGreen)
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text("Delivering to", color = GrayText, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = FreshGreen,
                            modifier = Modifier.size(18.dp))
                        Text(" Your selected address", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(12.dp))
                    SearchBox(search, onSearch)
                }
            }

            item {
                Card(
                    Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(FreshGreen)
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Fresh deals every day 🎉",
                                color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Use FRESH20 and save up to ₹100",
                                color = Color.White.copy(alpha = .9f))
                            Spacer(Modifier.height(8.dp))
                            Text("FREE DELIVERY above ₹499",
                                color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text("🛍️", fontSize = 48.sp)
                    }
                }
            }

            item {
                Text("Shop by Category", fontSize = 19.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            item {
                LazyColumn(
                    Modifier.height(190.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    items(categories.chunked(6)) { row ->
                        Row(Modifier.fillMaxWidth()) {
                            row.forEach { category ->
                                Card(
                                    Modifier.weight(1f).padding(4.dp)
                                        .clickable { onCategory(category) },
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(categoryEmoji[category] ?: "🛍️", fontSize = 28.sp)
                                        Text(category, fontSize = 10.sp, maxLines = 1)
                                    }
                                }
                            }
                            repeat(6 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Popular Products", fontSize = 19.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f))
                    TextButton(onClick = onOrders) { Text("My Orders") }
                }
            }

            items(products.take(10), key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    favourite = product.id in favourites,
                    onProduct = { onProduct(product) },
                    onFavourite = { onFavourite(product.id) },
                    onAdd = { onAdd(product) }
                )
            }
        }
    }
}

// ============================================================
// LISTS / CATEGORY
// ============================================================

@Composable
private fun ProductList(
    title: String,
    products: List<Product>,
    favourites: Set<Int>,
    search: String,
    onSearch: (String) -> Unit,
    onProduct: (Product) -> Unit,
    onFavourite: (Int) -> Unit,
    onAdd: (Product) -> Unit,
    onBack: () -> Unit,
    emptyText: String
) {
    Scaffold { p ->
        Column(Modifier.fillMaxSize().background(Background).padding(p)) {
            AppTopBar(title, onBack)
            if (search.isNotEmpty() || title.contains("Search")) {
                SearchBox(search, onSearch)
                Spacer(Modifier.height(8.dp))
            }
            if (products.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(emptyText, color = GrayText)
                }
            } else {
                LazyColumn {
                    items(products, key = { it.id }) {
                        ProductCard(
                            it, it.id in favourites,
                            { onProduct(it) },
                            { onFavourite(it.id) },
                            { onAdd(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryScreen(
    selected: String,
    products: List<Product>,
    favourites: Set<Int>,
    onFavourite: (Int) -> Unit,
    onAdd: (Product) -> Unit,
    onSelect: (String) -> Unit,
    onProduct: (Product) -> Unit,
    onBack: () -> Unit
) {
    Scaffold { p ->
        Column(Modifier.fillMaxSize().background(Background).padding(p)) {
            AppTopBar("Categories 🗂️", onBack)
            LazyColumn {
                item {
                    Text("Choose a category", fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp))
                }
                items(categories) { c ->
                    Card(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { onSelect(c) },
                        colors = CardDefaults.cardColors(
                            if (c == selected) LightGreen else Color.White
                        )
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(categoryEmoji[c] ?: "🛍️", fontSize = 28.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(c, fontWeight = if (c == selected) FontWeight.Bold else FontWeight.Normal)
                            if (c == selected) {
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Default.Check, null, tint = FreshGreen)
                            }
                        }
                    }
                }
                item {
                    Text("${products.size} products",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp))
                }
                items(products) { product ->
                    ProductCard(
                        product = product,
                        favourite = product.id in favourites,
                        onProduct = { onProduct(product) },
                        onFavourite = { onFavourite(product.id) },
                        onAdd = { onAdd(product) }
                    )
                }
            }
        }
    }
}

// ============================================================
// PRODUCT DETAILS
// ============================================================

@Composable
private fun ProductDetails(
    product: Product,
    favourite: Boolean,
    onFavourite: () -> Unit,
    onAdd: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold { p ->
        Column(Modifier.fillMaxSize().background(Background).padding(p)) {
            AppTopBar(
                "Product Details",
                onBack,
                action = {
                    IconButton(onClick = onFavourite) {
                        Icon(if (favourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            null, tint = if (favourite) Red else FreshGreen)
                    }
                }
            )
            LazyColumn(contentPadding = PaddingValues(bottom = 30.dp)) {
                item {
                    Box(
                        Modifier.fillMaxWidth().height(230.dp).padding(20.dp)
                            .clip(RoundedCornerShape(28.dp)).background(LightGreen),
                        contentAlignment = Alignment.Center
                    ) { Text(product.emoji, fontSize = 110.sp) }
                }
                item {
                    Column(Modifier.padding(20.dp)) {
                        Text(product.category, color = FreshGreen, fontWeight = FontWeight.Bold)
                        Text(product.name, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text("⭐ ${"%.1f".format(product.rating)}  •  ${product.unit}",
                            color = GrayText)
                        Spacer(Modifier.height(12.dp))
                        if (product.oldPrice > product.price) {
                            val percent = ((product.oldPrice - product.price) * 100 / product.oldPrice)
                            Surface(color = LightGreen, shape = RoundedCornerShape(8.dp)) {
                                Text("🎉 $percent% OFF", color = FreshGreen, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("₹${product.price}", fontSize = 25.sp,
                                fontWeight = FontWeight.Bold, color = FreshGreen)
                            Spacer(Modifier.width(10.dp))
                            Text("₹${product.oldPrice}", color = GrayText)
                        }
                        Spacer(Modifier.height(18.dp))
                        Text("About this product", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(product.description, color = GrayText,
                            modifier = Modifier.padding(top = 8.dp))
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = onAdd, Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(15.dp)) {
                            Icon(Icons.Default.ShoppingCart, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add to Cart")
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// CART
// ============================================================

@Composable
private fun CartScreen(
    cart: List<CartItem>,
    subtotal: Int,
    delivery: Int,
    discount: Int,
    total: Int,
    coupon: String,
    couponApplied: Boolean,
    onCoupon: (String) -> Unit,
    onApplyCoupon: () -> Unit,
    onIncrease: (Int) -> Unit,
    onDecrease: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onCheckout: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold { p ->
        Column(Modifier.fillMaxSize().background(Background).padding(p)) {
            AppTopBar("My Cart 🛒", onBack)
            if (cart.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🛒", fontSize = 65.sp)
                        Text("Your cart is empty", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Add some fresh groceries first", color = GrayText)
                    }
                }
            } else {
                LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 10.dp)
                ) {
                    items(cart, key = { it.product.id }) { item ->
                        Card(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(item.product.emoji, fontSize = 40.sp)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(item.product.name, fontWeight = FontWeight.Bold)
                                    Text("₹${item.product.price} × ${item.quantity}",
                                        color = GrayText)
                                    Text("₹${item.product.price * item.quantity}",
                                        color = FreshGreen, fontWeight = FontWeight.Bold)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { onDecrease(item.product.id) }) {
                                        Icon(Icons.Default.Remove, null)
                                    }
                                    Text(item.quantity.toString(), fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { onIncrease(item.product.id) }) {
                                        Icon(Icons.Default.Add, null)
                                    }
                                }
                                IconButton(onClick = { onRemove(item.product.id) }) {
                                    Icon(Icons.Default.Delete, null, tint = Red)
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            Modifier.fillMaxWidth().padding(16.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Coupon Code 🏷️", fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = coupon,
                                        onValueChange = onCoupon,
                                        modifier = Modifier.weight(1f),
                                        placeholder = { Text("Try FRESH20") },
                                        singleLine = true
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Button(onClick = onApplyCoupon) { Text("Apply") }
                                }
                                if (couponApplied) {
                                    Text("🎉 Coupon applied successfully!",
                                        color = FreshGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Summary(subtotal, delivery, discount, total)
                    }
                }

                Button(
                    onClick = onCheckout,
                    Modifier.fillMaxWidth().padding(16.dp).height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Continue to Checkout • ₹$total", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun Summary(subtotal: Int, delivery: Int, discount: Int, total: Int) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Price Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            PriceRow("Items", "₹$subtotal")
            PriceRow("Delivery", if (delivery == 0) "FREE" else "₹$delivery")
            if (discount > 0) PriceRow("Discount", "-₹$discount")
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            PriceRow("Total", "₹$total", true)
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, modifier = Modifier.weight(1f),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = if (bold) FreshGreen else DarkText)
    }
}

// ============================================================
// ADDRESS / CHECKOUT / PAYMENT
// ============================================================

@Composable
private fun AddressScreen(
    address: Address,
    onSave: (Address) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(address.name) }
    var phone by remember { mutableStateOf(address.phone) }
    var line by remember { mutableStateOf(address.line) }
    var city by remember { mutableStateOf(address.city) }
    var pin by remember { mutableStateOf(address.pincode) }

    Scaffold { p ->
        Column(Modifier.fillMaxSize().background(Background).padding(p)) {
            AppTopBar("Delivery Address 📍", onBack)
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                item {
                    Field("Full Name", name) { name = it }
                    Field("Phone Number", phone) { phone = it }
                    Field("House / Street Address", line) { line = it }
                    Field("City", city) { city = it }
                    Field("Pincode", pin) { pin = it }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onSave(Address(name, phone, line, city, pin))
                        },
                        Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(15.dp),
                        enabled = name.isNotBlank() && phone.isNotBlank() &&
                                line.isNotBlank() && city.isNotBlank() && pin.isNotBlank()
                    ) {
                        Text("Save & Continue")
                    }
                }
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        singleLine = true,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun CheckoutScreen(
    cart: List<CartItem>,
    address: Address,
    subtotal: Int,
    delivery: Int,
    discount: Int,
    total: Int,
    onAddress: () -> Unit,
    onPayment: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold { p ->
        Column(Modifier.fillMaxSize().background(Background).padding(p)) {
            AppTopBar("Checkout 🧾", onBack)
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Delivery Address", fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                TextButton(onClick = onAddress) { Text("Change") }
                            }
                            Text("📍 ${address.name}")
                            Text("${address.line}, ${address.city} - ${address.pincode}",
                                color = GrayText)
                            Text("📞 ${address.phone}", color = GrayText)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Your Items", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                items(cart) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(it.product.emoji, fontSize = 30.sp)
                        Text("${it.product.name} × ${it.quantity}",
                            modifier = Modifier.weight(1f))
                        Text("₹${it.product.price * it.quantity}",
                            fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    Summary(subtotal, delivery, discount, total)
                }
            }
            Button(
                onClick = onPayment,
                Modifier.fillMaxWidth().padding(16.dp).height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Choose Payment • ₹$total") }
        }
    }
}

@Composable
private fun PaymentScreen(
    payment: String,
    total: Int,
    onPayment: (String) -> Unit,
    onPlaceOrder: () -> Unit,
    onBack: () -> Unit
) {
    val methods = listOf(
        "Cash on Delivery" to "💵",
        "UPI" to "📱",
        "Debit / Credit Card" to "💳"
    )
    Scaffold { p ->
        Column(Modifier.fillMaxSize().background(Background).padding(p)) {
            AppTopBar("Payment 💳", onBack)
            Text("Select a payment method",
                fontSize = 18.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp))
            methods.forEach { (name, emoji) ->
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onPayment(name) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        if (payment == name) LightGreen else Color.White
                    )
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(emoji, fontSize = 30.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(name, modifier = Modifier.weight(1f),
                            fontWeight = if (payment == name) FontWeight.Bold else FontWeight.Normal)
                        if (payment == name) Icon(Icons.Default.CheckCircle, null, tint = FreshGreen)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onPlaceOrder,
                Modifier.fillMaxWidth().padding(16.dp).height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Place Order • ₹$total") }
        }
    }
}

// ============================================================
// ORDER FLOW
// ============================================================

@Composable
private fun SuccessScreen(
    onTrack: () -> Unit,
    onOrders: () -> Unit,
    onHome: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().background(Background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎉", fontSize = 90.sp)
        Text("Order Placed!", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Thank you for shopping with FreshCart.",
            color = GrayText, modifier = Modifier.padding(10.dp))
        Button(onClick = onTrack, Modifier.fillMaxWidth().padding(top = 20.dp)) {
            Text("🚚 Track Order")
        }
        OutlinedButton(onClick = onOrders, Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("📦 View My Orders")
        }
        TextButton(onClick = onHome) { Text("Continue Shopping") }
    }
}

@Composable
private fun TrackingScreen(onBack: () -> Unit) {
    Scaffold { p ->
        Column(Modifier.fillMaxSize().background(Background).padding(p)) {
            AppTopBar("Track Order 🚚", onBack)
            Text(
                "🎉 Order Delivered!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = FreshGreen,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            Text(
                "All order tracking steps are completed.",
                color = GrayText,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
            val steps = listOf(
                "✅ Order Placed" to "Your order has been confirmed",
                "✅ Order Confirmed" to "Your order was accepted by FreshCart",
                "✅ Preparing" to "Fresh items have been prepared",
                "✅ Out for Delivery" to "Your rider has completed the delivery",
                "✅ Delivered" to "Your fresh groceries have been delivered"
            )
            steps.forEachIndexed { index, pair ->
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        if (index == steps.lastIndex) LightGreen else Color.White
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(pair.first, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text(pair.second, color = GrayText)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrdersScreen(
    orders: List<Order>,
    onTrack: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold { p ->
        Column(Modifier.fillMaxSize().background(Background).padding(p)) {
            AppTopBar("My Orders 📦", onBack)
            if (orders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No orders yet 📦", color = GrayText)
                }
            } else {
                LazyColumn {
                    items(orders) { order ->
                        Card(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Order #${order.id}", fontWeight = FontWeight.Bold)
                                Text("${order.items} items • ${order.payment}",
                                    color = GrayText)
                                Text("₹${order.total}", color = FreshGreen,
                                    fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Button(onClick = onTrack) { Text("🚚 Track Order") }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// PROFILE / SETTINGS
// ============================================================

@Composable
private fun ProfileScreen(
    name: String,
    email: String,
    cartCount: Int,
    favouriteCount: Int,
    onOrders: () -> Unit,
    onFavorites: () -> Unit,
    onSettings: () -> Unit,
    onNotifications: () -> Unit,
    onHelp: () -> Unit,
    onLogout: () -> Unit,
    onHome: () -> Unit
) {
    Scaffold { p ->
        Column(Modifier.fillMaxSize().background(Background).padding(p)) {
            AppTopBar("My Profile 👤", onHome)
            LazyColumn {
                item {
                    Card(Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(FreshGreen)) {
                        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(62.dp).clip(CircleShape).background(Color.White),
                                contentAlignment = Alignment.Center) {
                                Text("👤", fontSize = 35.sp)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(name, color = Color.White, fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold)
                                Text(email.ifBlank { "Welcome to FreshCart" },
                                    color = Color.White.copy(alpha = .85f))
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Stat("❤️", favouriteCount.toString(), "Favorites", Modifier.weight(1f))
                        Stat("🛒", cartCount.toString(), "Cart", Modifier.weight(1f))
                    }
                }
                item { ProfileItem("📦", "My Orders", onOrders) }
                item { ProfileItem("❤️", "My Favorites", onFavorites) }
                item { ProfileItem("🔔", "Notifications", onNotifications) }
                item { ProfileItem("⚙️", "Settings", onSettings) }
                item { ProfileItem("❓", "Help & Support", onHelp) }
                item {
                    Card(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
                            .clickable { onLogout() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(Color(0xFFFFEAEA))
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🚪", fontSize = 26.sp)
                            Spacer(Modifier.width(14.dp))
                            Text(
                                "Logout",
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.SemiBold,
                                color = Red
                            )
                            Icon(Icons.Default.Logout, null, tint = Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Stat(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(modifier.padding(4.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 24.sp)
            Text(value, fontWeight = FontWeight.Bold)
            Text(label, color = GrayText, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ProfileItem(emoji: String, title: String, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 26.sp)
            Spacer(Modifier.width(14.dp))
            Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            Icon(Icons.Default.ArrowForward, null, tint = GrayText)
        }
    }
}

@Composable
private fun SimplePage(
    title: String,
    items: List<Pair<String, () -> Unit>>,
    onBack: () -> Unit
) {
    Scaffold { p ->
        Column(Modifier.fillMaxSize().background(Background).padding(p)) {
            AppTopBar(title, onBack)
            items.forEach { (text, action) ->
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
                        .clickable { action() },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowForward, null, tint = GrayText)
                    }
                }
            }
        }
    }
}

// ============================================================
// AUTH / SPLASH
// ============================================================

@Composable
private fun Splash(onNext: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(900)
        onNext()
    }
    Box(Modifier.fillMaxSize().background(FreshGreen), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🛒", fontSize = 90.sp)
            Text("FreshCart", color = Color.White, fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold)
            Text("Fresh groceries. Simple shopping.",
                color = Color.White.copy(alpha = .9f))
        }
    }
}

@Composable
private fun Welcome(onNext: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🛒", fontSize = 90.sp)
        Text("FreshCart", color = FreshGreen, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
        Text("Your everyday grocery companion",
            color = GrayText, fontSize = 16.sp)
        Spacer(Modifier.height(30.dp))
        Button(onClick = onNext, Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp)) {
            Text("Get Started")
        }
    }
}

@Composable
private fun AuthScreen(
    title: String,
    button: String,
    name: String,
    email: String,
    password: String,
    showName: Boolean,
    onName: (String) -> Unit,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    authError: String,
    loading: Boolean,
    showForgotPassword: Boolean,
    onForgotPassword: () -> Unit,
    onSubmit: () -> Unit,
    footer: String,
    onFooter: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().background(Background).padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("🛒 FreshCart", color = FreshGreen, fontSize = 22.sp,
            fontWeight = FontWeight.Bold)
        Text(title, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Fresh groceries at your fingertips.",
            color = GrayText, modifier = Modifier.padding(bottom = 20.dp))

        if (showName) Field("Full Name", name, onName)

        OutlinedTextField(
            value = email,
            onValueChange = onEmail,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(14.dp),
            isError = authError.isNotBlank()
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPassword,
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(14.dp),
            isError = authError.isNotBlank()
        )

        if (showForgotPassword) {
            TextButton(
                onClick = onForgotPassword,
                enabled = !loading,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot Password?")
            }
        }

        if (authError.isNotBlank()) {
            Text(
                authError,
                color = if (authError.startsWith("Password reset email sent")) FreshGreen else Red,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        Button(
            onClick = onSubmit,
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(15.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text(button)
            }
        }

        TextButton(onClick = onFooter, Modifier.align(Alignment.CenterHorizontally)) {
            Text(footer)
        }
    }
}

