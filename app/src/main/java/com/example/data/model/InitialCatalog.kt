package com.example.data.model

object InitialCatalog {
    const val OWNER_EMAIL = "mdseamrahman2525@gmail.com"
    const val OFFICIAL_BKASH_NUMBER = "01354246861"
    const val OFFICIAL_WHATSAPP_URL = "https://chat.whatsapp.com/HrVQr70upFb5fNqsTM5w32"

    const val GAME_ROBLOX_ID = "game_roblox"
    const val CAT_ROBUX_ID = "cat_roblox_robux"
    const val CAT_ROBLOX_PLUS_ID = "cat_roblox_plus"
    const val CAT_ROBLOX_GRAB_ID = "cat_roblox_grab"
    const val CAT_ROBLOX_OTHER_ID = "cat_roblox_other"

    val defaultStoreConfig = StoreConfig()

    val defaultGames = listOf(
        Game(
            id = GAME_ROBLOX_ID,
            name = "Roblox",
            slug = "roblox",
            description = "Official Roblox Robux top-ups & Roblox Plus Subscriptions with fast bKash payment verification.",
            imageUrl = "",
            themeColorHex = "#2997FF",
            active = true,
            featured = true,
            displayOrder = 1L
        )
    )

    val defaultCategories = listOf(
        Category(
            id = CAT_ROBUX_ID,
            gameId = GAME_ROBLOX_ID,
            name = "Roblox Robux",
            description = "Official Robux digital packages delivered directly via your Roblox Username.",
            type = CategoryType.CURRENCY.name,
            displayOrder = 1L,
            active = true,
            requiredInputFields = listOf("Roblox Username")
        ),
        Category(
            id = CAT_ROBLOX_PLUS_ID,
            gameId = GAME_ROBLOX_ID,
            name = "Roblox Plus Subscription",
            description = "Premium Roblox Plus subscription plans for boosted perks and benefits.",
            type = CategoryType.SUBSCRIPTION.name,
            displayOrder = 2L,
            active = true,
            requiredInputFields = listOf("Roblox Username")
        ),
        Category(
            id = CAT_ROBLOX_GRAB_ID,
            gameId = GAME_ROBLOX_ID,
            name = "Roblox GRAB",
            description = "Dedicated Roblox GRAB packages curated and published by the store owner.",
            type = CategoryType.GRAB.name,
            displayOrder = 3L,
            active = true,
            requiredInputFields = listOf("Roblox Username")
        ),
        Category(
            id = CAT_ROBLOX_OTHER_ID,
            gameId = GAME_ROBLOX_ID,
            name = "Other Roblox Products",
            description = "Additional Roblox gamepasses, bundles, and special digital services.",
            type = CategoryType.OTHER.name,
            displayOrder = 4L,
            active = true,
            requiredInputFields = listOf("Roblox Username")
        )
    )

    // Exact official Robux price list & Roblox Plus Subscription packages — zero modifications or rounding
    val defaultProducts = listOf(
        robuxProduct("prod_robux_25", 25, 70, 1, featured = false),
        robuxProduct("prod_robux_50", 50, 130, 2, featured = false),
        robuxProduct("prod_robux_80", 80, 185, 3, featured = false),
        robuxProduct("prod_robux_100", 100, 220, 4, featured = true),
        robuxProduct("prod_robux_150", 150, 305, 5, featured = false),
        robuxProduct("prod_robux_200", 200, 380, 6, featured = true),
        robuxProduct("prod_robux_300", 300, 530, 7, featured = false),
        robuxProduct("prod_robux_400", 400, 650, 8, featured = true),
        robuxProduct("prod_robux_500", 500, 820, 9, featured = false),
        robuxProduct("prod_robux_800", 800, 1220, 10, featured = false),
        robuxProduct("prod_robux_1000", 1000, 1420, 11, featured = true),
        Product(
            id = "prod_plus_7d",
            gameId = GAME_ROBLOX_ID,
            categoryId = CAT_ROBLOX_PLUS_ID,
            categoryType = CategoryType.SUBSCRIPTION.name,
            name = "Roblox Plus — 7 Days",
            subtitle = "7-Day Active Subscription",
            description = "Enjoy 7 full days of Roblox Plus benefits. Requires only your Roblox Username — no password needed.",
            quantityLabel = "Plus 7 Days",
            amountValue = 7L,
            priceBdt = 190L,
            imageUrl = "",
            available = true,
            active = true,
            featured = true,
            displayOrder = 12L
        ),
        Product(
            id = "prod_plus_1m",
            gameId = GAME_ROBLOX_ID,
            categoryId = CAT_ROBLOX_PLUS_ID,
            categoryType = CategoryType.SUBSCRIPTION.name,
            name = "Roblox Plus — 1 Month",
            subtitle = "30-Day Full Monthly Plan",
            description = "Full 1-Month Roblox Plus subscription plan with priority fulfillment and loyalty points rewards.",
            quantityLabel = "Plus 1 Month",
            amountValue = 30L,
            priceBdt = 650L,
            imageUrl = "",
            available = true,
            active = true,
            featured = true,
            displayOrder = 13L
        )
    )

    val defaultFaqs = listOf(
        FaqItem(
            id = "faq_1",
            question = "How do I pay for my Roblox top-up?",
            answer = "Select your desired Robux or Roblox Plus package, send the exact BDT amount via bKash Send Money to 01354246861, then enter your Roblox Username, sender bKash number, and Transaction ID on the checkout page.",
            displayOrder = 1L,
            active = true
        ),
        FaqItem(
            id = "faq_2",
            question = "Do you ever need my Roblox or Google password?",
            answer = "Never! MSR BD Game Top-Up NEVER asks for your Roblox password, Google password, or any personal password. We only need your public Roblox Username for order fulfillment.",
            displayOrder = 2L,
            active = true
        ),
        FaqItem(
            id = "faq_3",
            question = "How long does payment verification take?",
            answer = "Orders start in Pending status. Our team verifies your bKash Transaction ID manually and moves your order through Payment Verification, Processing, and Completed.",
            displayOrder = 3L,
            active = true
        ),
        FaqItem(
            id = "faq_4",
            question = "How do Loyalty Points & Membership Tiers work?",
            answer = "You earn 1 Loyalty Point for every ৳1 of verified and completed spending. Pending or rejected orders do not earn points. Reach 1,000 points for SILVER, 5,000 for GOLD, and 12,300 for PLATINUM.",
            displayOrder = 4L,
            active = true
        )
    )

    private fun robuxProduct(
        id: String,
        amount: Long,
        priceBdt: Long,
        order: Long,
        featured: Boolean
    ): Product {
        val formattedQty = if (amount >= 1000) "1,000 Robux" else "$amount Robux"
        return Product(
            id = id,
            gameId = GAME_ROBLOX_ID,
            categoryId = CAT_ROBUX_ID,
            categoryType = CategoryType.CURRENCY.name,
            name = formattedQty,
            subtitle = "Official Roblox Top-Up",
            description = "Fast & secure digital delivery of $formattedQty directly to your Roblox account.",
            quantityLabel = formattedQty,
            amountValue = amount,
            priceBdt = priceBdt,
            imageUrl = "",
            available = true,
            active = true,
            featured = featured,
            displayOrder = order
        )
    }
}
