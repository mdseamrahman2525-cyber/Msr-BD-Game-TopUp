package com.example

import com.example.data.model.CategoryType
import com.example.data.model.InitialCatalog
import com.example.data.model.MembershipTier
import com.example.data.model.OwnerOrderMessage
import com.example.ui.viewmodel.StoreFormatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {

    @Test
    fun official_initial_launch_only_has_roblox_active() {
        assertEquals(1, InitialCatalog.defaultGames.size)
        val game = InitialCatalog.defaultGames.first()
        assertEquals("Roblox", game.name)
        assertTrue(game.active)
    }

    @Test
    fun official_initial_categories_match_specification_and_grab_is_empty() {
        val names = InitialCatalog.defaultCategories.map { it.name }
        assertEquals(
            listOf(
                "Roblox Robux",
                "Roblox Plus Subscription",
                "Roblox GRAB",
                "Other Roblox Products"
            ),
            names
        )
        assertTrue(InitialCatalog.defaultProducts.none { it.categoryType == CategoryType.GRAB.name })
        assertTrue(InitialCatalog.defaultProducts.none { it.categoryType == CategoryType.OTHER.name })
    }

    @Test
    fun official_robux_and_plus_prices_are_exact() {
        val robuxMap = InitialCatalog.defaultProducts
            .filter { it.categoryType == CategoryType.CURRENCY.name }
            .associate { it.amountValue to it.priceBdt }

        val expectedRobux = mapOf(
            25L to 70L,
            50L to 130L,
            80L to 185L,
            100L to 220L,
            150L to 305L,
            200L to 380L,
            300L to 530L,
            400L to 650L,
            500L to 820L,
            800L to 1220L,
            1000L to 1420L
        )
        assertEquals(expectedRobux, robuxMap)

        val subsMap = InitialCatalog.defaultProducts
            .filter { it.categoryType == CategoryType.SUBSCRIPTION.name }
            .associate { it.quantityLabel to it.priceBdt }
        assertEquals(
            mapOf(
                "Plus 7 Days" to 190L,
                "Plus 1 Month" to 650L
            ),
            subsMap
        )
    }

    @Test
    fun loyalty_membership_tier_thresholds_compute_correctly() {
        val cfg = InitialCatalog.defaultStoreConfig
        assertEquals(MembershipTier.STANDARD, MembershipTier.computeTier(0L, cfg.silverThreshold, cfg.goldThreshold, cfg.platinumThreshold))
        assertEquals(MembershipTier.STANDARD, MembershipTier.computeTier(999L, cfg.silverThreshold, cfg.goldThreshold, cfg.platinumThreshold))
        assertEquals(MembershipTier.SILVER, MembershipTier.computeTier(1000L, cfg.silverThreshold, cfg.goldThreshold, cfg.platinumThreshold))
        assertEquals(MembershipTier.GOLD, MembershipTier.computeTier(5000L, cfg.silverThreshold, cfg.goldThreshold, cfg.platinumThreshold))
        assertEquals(MembershipTier.PLATINUM, MembershipTier.computeTier(12300L, cfg.silverThreshold, cfg.goldThreshold, cfg.platinumThreshold))
    }

    @Test
    fun bonus_drop_schedule_targets_10th_20th_30th() {
        val cal1 = Calendar.getInstance().apply { set(2026, Calendar.NOVEMBER, 5, 10, 0, 0) }
        val drop1 = StoreFormatters.computeNextBonusDrop(cal1.timeInMillis)
        assertFalse(drop1.isDropDayToday)
        assertTrue(drop1.nextDateLabel.startsWith("10 "))

        val cal2 = Calendar.getInstance().apply { set(2026, Calendar.NOVEMBER, 20, 12, 0, 0) }
        val drop2 = StoreFormatters.computeNextBonusDrop(cal2.timeInMillis)
        assertTrue(drop2.isDropDayToday)
        assertTrue(drop2.nextDateLabel.startsWith("20 "))

        val cal3 = Calendar.getInstance().apply { set(2026, Calendar.NOVEMBER, 30, 9, 0, 0) }
        val drop3 = StoreFormatters.computeNextBonusDrop(cal3.timeInMillis)
        assertTrue(drop3.isDropDayToday)
        assertTrue(drop3.nextDateLabel.startsWith("30 "))
    }

    @Test
    fun owner_order_message_defaults_and_owner_email_are_configured() {
        assertEquals("mdseamrahman2525@gmail.com", InitialCatalog.OWNER_EMAIL)
        val msg = OwnerOrderMessage(
            orderId = "ord_123",
            orderCode = "MSR-20261119-4242",
            customerUid = "uid_1",
            customerName = "Test Gamer",
            customerEmail = "gamer@example.com",
            productName = "1000 Robux",
            totalAmountBdt = 1420L,
            targetAccountInput = "GamerRbx",
            paymentSenderNumber = "01700000000",
            transactionId = "TRX8899"
        )
        assertFalse(msg.read)
        assertEquals("MSR-20261119-4242", msg.orderCode)
        assertEquals(1420L, msg.totalAmountBdt)
        assertEquals("bKash", msg.paymentMethod)
        assertEquals("01354246861", InitialCatalog.defaultStoreConfig.nagadNumber)
        assertTrue(InitialCatalog.defaultStoreConfig.appDownloadUrl.startsWith("https://"))
        assertEquals("Done", com.example.data.model.OrderStatus.COMPLETED.label)
    }

    @Test
    fun payment_phone_number_requires_exact_11_digit_bd_format() {
        val validator = com.example.data.repository.PaymentInputValidator
        assertTrue(validator.validateBdPhoneNumber("01354246861").isValid)
        assertTrue(validator.validateBdPhoneNumber("01712345678").isValid)
        assertFalse(validator.validateBdPhoneNumber("0171234567").isValid) // 10 digits
        assertFalse(validator.validateBdPhoneNumber("017123456789").isValid) // 12 digits
        assertFalse(validator.validateBdPhoneNumber("02712345678").isValid) // invalid prefix
        assertFalse(validator.validateBdPhoneNumber("01712 345678").isValid) // space
    }

    @Test
    fun bkash_and_nagad_transaction_id_validation_rejects_fake_formats() {
        val validator = com.example.data.repository.PaymentInputValidator

        // Valid authentic bKash (strict 10-char alphanumeric with letters + digits)
        assertTrue(validator.validateTransactionId("bKash", "6ICOTYGEYS").isValid)
        assertTrue(validator.validateTransactionId("bKash", "AFE8DCEWA2").isValid)
        assertTrue(validator.validateTransactionId("bKash", "BKA9X23M10").isValid)

        // Rejected invalid examples explicitly flagged
        assertFalse(validator.validateTransactionId("bKash", "ETF5Y FEG").isValid)
        assertFalse(validator.validateTransactionId("bKash", "fhw14gnsjv5jh").isValid)
        assertFalse(validator.validateTransactionId("Nagad", "ETF5Y FEG").isValid)
        assertFalse(validator.validateTransactionId("Nagad", "fhw14gnsjv5jh").isValid)

        // Edge cases
        assertFalse(validator.validateTransactionId("bKash", "1234567890").isValid) // digits only
        assertFalse(validator.validateTransactionId("bKash", "ABCDEFGHIJ").isValid) // letters only
        assertFalse(validator.validateTransactionId("bKash", "74CLKT6G").isValid) // 8 chars too short for bKash

        // Nagad accepts 8-10 chars alphanumeric
        assertTrue(validator.validateTransactionId("Nagad", "74CLKT6G").isValid)
        assertTrue(validator.validateTransactionId("Nagad", "0000ZOVE").isValid)
        assertTrue(validator.validateTransactionId("Nagad", "BKA9X23M10").isValid)
    }

    @Test
    fun roblox_username_syntax_validation_works() {
        val verifier = com.example.data.repository.RobloxAccountVerifier
        assertTrue(verifier.isValidUsernameSyntax("Roblox"))
        assertTrue(verifier.isValidUsernameSyntax("Builderman_1"))
        assertFalse(verifier.isValidUsernameSyntax("ab"))
        assertFalse(verifier.isValidUsernameSyntax("_badname"))
        assertFalse(verifier.isValidUsernameSyntax("bad__name"))
        assertFalse(verifier.isValidUsernameSyntax("bad name"))
    }

    @Test
    fun section_bangla_translator_translates_all_default_copy_and_dynamic_text() {
        val translator = com.example.ui.components.SectionBanglaTranslator
        val cfg = InitialCatalog.defaultStoreConfig
        val hasBangla = { s: String -> s.any { it in '\u0980'..'\u09FF' } }

        val supportBn = translator.translateSupportInfo(cfg.supportInfo, cfg.supportPhone)
        assertTrue(hasBangla(supportBn))

        val paymentBn = translator.translatePaymentInstructions(cfg.paymentInstructions, cfg.bkashNumber, "bKash")
        assertTrue(hasBangla(paymentBn))
        assertTrue(paymentBn.contains(cfg.bkashNumber))

        val privacyBn = translator.translatePrivacyPolicy(cfg.privacyPolicyText)
        assertTrue(hasBangla(privacyBn))

        val termsBn = translator.translateTerms(cfg.termsText)
        assertTrue(hasBangla(termsBn))

        val rewardBn = translator.translateRewardDescription(cfg.rewardDescription, cfg)
        assertTrue(hasBangla(rewardBn))

        InitialCatalog.defaultFaqs.forEach { faq ->
            val qBn = translator.translateFaqQuestion(faq.id, faq.question)
            val aBn = translator.translateFaqAnswer(faq.id, faq.answer)
            assertTrue("FAQ ${faq.id} question should translate", hasBangla(qBn))
            assertTrue("FAQ ${faq.id} answer should translate", hasBangla(aBn))
        }

        val customBn = translator.translateDynamicEnglish("Please check Transaction ID for your Pending order.")
        assertTrue(hasBangla(customBn))
    }
}
