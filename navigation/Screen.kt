package com.example.temolink.navigation

sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object Login : Screen("login")
    object Register : Screen("register?role={role}") {
        fun createRoute(role: String = "") = "register?role=$role"
    }
    object Marketplace : Screen("marketplace")
    
    // Farmer Screens
    object FarmerDashboard : Screen("farmer_dashboard")
    object AddProduce : Screen("add_produce")
    object ManageListings : Screen("manage_listings")
    object EditProduce : Screen("edit_produce/{produceId}") {
        fun createRoute(produceId: String) = "edit_produce/$produceId"
    }
    object FarmerOrders : Screen("farmer_orders")
    
    // Buyer Screens
    object BuyerDashboard : Screen("buyer_dashboard")
    object FarmerProfile : Screen("farmer_profile/{farmerId}") {
        fun createRoute(farmerId: String) = "farmer_profile/$farmerId"
    }
    object BuyerOrders : Screen("buyer_orders")
    object Payment : Screen("payment/{produceId}/{quantity}") {
        fun createRoute(produceId: String, quantity: Int) = "payment/$produceId/$quantity"
    }
    
    // Shared Screens
    object ProduceDetails : Screen("produce_details/{produceId}") {
        fun createRoute(produceId: String) = "produce_details/$produceId"
    }
    object Profile : Screen("profile")
    object Messages : Screen("messages")
    object Chat : Screen("chat/{threadId}/{otherUserId}") {
        fun createRoute(threadId: String, otherUserId: String) = "chat/$threadId/$otherUserId"
    }
    object Notifications : Screen("notifications")
}
