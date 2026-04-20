package kata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * These tests exercise ShoppingThing through its PUBLIC API only.
 * They pin down the current behavior so you can refactor internals safely.
 *
 * If you rename public methods during refactoring, update the calls here in
 * the SAME step. Run `mvn test` after every change — if the bar stays green,
 * your refactoring is safe.
 */
class ShoppingThingTest {

    private static final double EPS = 1e-6;

    private ShoppingThing cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingThing();
    }

    @Nested
    @DisplayName("Subtotal (calc)")
    class Subtotal {

        @Test
        void emptyCart_subtotalIsZero() {
            assertEquals(0.0, cart.calc(), EPS);
        }

        @Test
        void singleItem_subtotalIsPriceTimesQty() {
            cart.addItem("Widget", 10.0, 3, 0.5, "general");
            assertEquals(30.0, cart.calc(), EPS);
        }

        @Test
        void multipleItems_subtotalIsSum() {
            cart.addItem("Widget", 10.0, 3, 0.5, "general");
            cart.addItem("Gadget", 7.5, 2, 0.2, "tech");
            assertEquals(30.0 + 15.0, cart.calc(), EPS);
        }
    }

    @Nested
    @DisplayName("Coupon discounts (doIt)")
    class CouponDiscounts {

        @Test
        void noCoupon_noCouponDiscount() {
            cart.addItem("X", 10.0, 6, 0.1, "g"); // subtotal 60
            assertEquals(60.0, cart.doIt(), EPS);
        }

        @Test
        void save10_subtotalOver50_tenPercentOff() {
            cart.addItem("X", 10.0, 6, 0.1, "g"); // subtotal 60
            cart.setCoupon("SAVE10");
            assertEquals(60.0 - 6.0, cart.doIt(), EPS);
        }

        @Test
        void save10_subtotalAtBoundary50_flatTwoOff() {
            cart.addItem("X", 10.0, 5, 0.1, "g"); // subtotal 50 (not > 50)
            cart.setCoupon("SAVE10");
            assertEquals(50.0 - 2.0, cart.doIt(), EPS);
        }

        @Test
        void save10_subtotalBetween20And50_flatTwoOff() {
            cart.addItem("X", 10.0, 3, 0.1, "g"); // subtotal 30
            cart.setCoupon("SAVE10");
            assertEquals(30.0 - 2.0, cart.doIt(), EPS);
        }

        @Test
        void save10_subtotalAtBoundary20_noCouponDiscount() {
            cart.addItem("X", 10.0, 2, 0.1, "g"); // subtotal 20 (not > 20)
            cart.setCoupon("SAVE10");
            assertEquals(20.0, cart.doIt(), EPS);
        }

        @Test
        void save10_subtotalBelow20_noCouponDiscount() {
            cart.addItem("X", 10.0, 1, 0.1, "g"); // subtotal 10
            cart.setCoupon("SAVE10");
            assertEquals(10.0, cart.doIt(), EPS);
        }

        @Test
        void save20_subtotalOver100_twentyPercentOff() {
            cart.addItem("X", 10.0, 12, 0.1, "g"); // subtotal 120
            cart.setCoupon("SAVE20");
            // bulk also kicks in: qty=12>=10 and price=10>5 -> 120 * 0.05 = 6
            // coupon 20% of 120 = 24
            assertEquals(120.0 - 24.0 - 6.0, cart.doIt(), EPS);
        }

        @Test
        void save20_subtotalBetween50And100_tenPercentOff() {
            cart.addItem("X", 10.0, 8, 0.1, "g"); // subtotal 80 (qty<10, no bulk)
            cart.setCoupon("SAVE20");
            assertEquals(80.0 - 8.0, cart.doIt(), EPS);
        }

        @Test
        void save20_subtotalAtOrBelow50_noCouponDiscount() {
            cart.addItem("X", 10.0, 4, 0.1, "g"); // subtotal 40
            cart.setCoupon("SAVE20");
            assertEquals(40.0, cart.doIt(), EPS);
        }

        @Test
        void unknownCoupon_isIgnored() {
            cart.addItem("X", 10.0, 6, 0.1, "g"); // subtotal 60
            cart.setCoupon("BOGUS");
            assertEquals(60.0, cart.doIt(), EPS);
        }

        @Test
        void freeshipCoupon_doesNotAffectDoIt() {
            cart.addItem("X", 10.0, 6, 0.1, "g"); // subtotal 60
            cart.setCoupon("FREESHIP");
            assertEquals(60.0, cart.doIt(), EPS);
        }
    }

    @Nested
    @DisplayName("Member discounts")
    class MemberDiscounts {

        @Test
        void goldMember_fifteenPercentOff() {
            cart.addItem("X", 10.0, 9, 0.1, "g"); // subtotal 90, qty<10 no bulk
            cart.setMember("G");
            assertEquals(90.0 - 13.5, cart.doIt(), EPS);
        }

        @Test
        void silverMember_fivePercentOff() {
            cart.addItem("X", 10.0, 9, 0.1, "g"); // subtotal 90
            cart.setMember("S");
            assertEquals(90.0 - 4.5, cart.doIt(), EPS);
        }

        @Test
        void nonMemberTierN_noMemberDiscount() {
            cart.addItem("X", 10.0, 9, 0.1, "g"); // subtotal 90
            cart.setMember("N");
            assertEquals(90.0, cart.doIt(), EPS);
        }

        @Test
        void unknownMemberTier_noMemberDiscount() {
            cart.addItem("X", 10.0, 9, 0.1, "g"); // subtotal 90
            cart.setMember("PLATINUM");
            assertEquals(90.0, cart.doIt(), EPS);
        }

        @Test
        void memberAndCoupon_discountsStack() {
            cart.addItem("X", 10.0, 9, 0.1, "g"); // subtotal 90
            cart.setCoupon("SAVE10"); // 90>50 -> 10% = 9
            cart.setMember("G");      // gold 15% = 13.5
            assertEquals(90.0 - 9.0 - 13.5, cart.doIt(), EPS);
        }
    }

    @Nested
    @DisplayName("Bulk discount")
    class BulkDiscount {

        @Test
        void qtyTenAndPriceOverFive_fivePercentOff() {
            cart.addItem("X", 10.0, 10, 0.1, "g"); // subtotal 100
            assertEquals(100.0 - 5.0, cart.doIt(), EPS);
        }

        @Test
        void qtyBelowTen_noBulkDiscount() {
            cart.addItem("X", 10.0, 9, 0.1, "g"); // subtotal 90
            assertEquals(90.0, cart.doIt(), EPS);
        }

        @Test
        void priceAtBoundaryFive_noBulkDiscount() {
            cart.addItem("X", 5.0, 10, 0.1, "g"); // subtotal 50 (price not > 5)
            assertEquals(50.0, cart.doIt(), EPS);
        }

        @Test
        void priceBelowFive_noBulkDiscount() {
            cart.addItem("X", 4.99, 10, 0.1, "g");
            assertEquals(49.9, cart.doIt(), EPS);
        }

        @Test
        void bulkAppliesPerEligibleItemIndependently() {
            cart.addItem("A", 10.0, 10, 0.1, "g"); // bulk eligible -> 5 off
            cart.addItem("B", 10.0, 9, 0.1, "g");  // not eligible
            cart.addItem("C", 4.0, 10, 0.1, "g");  // not eligible (price<=5)
            // subtotal = 100 + 90 + 40 = 230. Bulk = 10*10*0.05 = 5.
            assertEquals(230.0 - 5.0, cart.doIt(), EPS);
        }
    }

    @Nested
    @DisplayName("Shipping (ship)")
    class Shipping {

        @Test
        void emptyCart_shippingIsZero() {
            cart.setCountry("US");
            assertEquals(0.0, cart.ship(), EPS);
        }

        @Test
        void nullCountry_shippingIsZero() {
            cart.addItem("X", 10.0, 1, 3.0, "g");
            assertEquals(0.0, cart.ship(), EPS);
        }

        @Test
        void usLightPackage_flatFive() {
            cart.addItem("X", 10.0, 1, 0.5, "g");
            cart.setCountry("US");
            assertEquals(5.0, cart.ship(), EPS);
        }

        @Test
        void usAtOneKg_stillLightTier() {
            cart.addItem("X", 10.0, 1, 1.0, "g");
            cart.setCountry("US");
            assertEquals(5.0, cart.ship(), EPS);
        }

        @Test
        void usMediumPackage_flatTen() {
            cart.addItem("X", 10.0, 1, 3.0, "g");
            cart.setCountry("US");
            assertEquals(10.0, cart.ship(), EPS);
        }

        @Test
        void usHeavyPackage_tenPlusTwoPerKgOverFive() {
            cart.addItem("X", 10.0, 1, 7.0, "g");
            cart.setCountry("US");
            assertEquals(10.0 + 2 * 2.0, cart.ship(), EPS);
        }

        @Test
        void caLightPackage_flatEight() {
            cart.addItem("X", 10.0, 1, 0.5, "g");
            cart.setCountry("CA");
            assertEquals(8.0, cart.ship(), EPS);
        }

        @Test
        void caMediumPackage_flatFifteen() {
            cart.addItem("X", 10.0, 1, 3.0, "g");
            cart.setCountry("CA");
            assertEquals(15.0, cart.ship(), EPS);
        }

        @Test
        void caHeavyPackage_fifteenPlusThreePerKgOverFive() {
            cart.addItem("X", 10.0, 1, 7.0, "g");
            cart.setCountry("CA");
            assertEquals(15.0 + 3 * 2.0, cart.ship(), EPS);
        }

        @Test
        void internationalLight_flatTwenty() {
            cart.addItem("X", 10.0, 1, 0.5, "g");
            cart.setCountry("UK");
            assertEquals(20.0, cart.ship(), EPS);
        }

        @Test
        void internationalMedium_flatThirtyFive() {
            cart.addItem("X", 10.0, 1, 3.0, "g");
            cart.setCountry("UK");
            assertEquals(35.0, cart.ship(), EPS);
        }

        @Test
        void internationalHeavy_thirtyFivePlusFivePerKgOverFive() {
            cart.addItem("X", 10.0, 1, 7.0, "g");
            cart.setCountry("UK");
            assertEquals(35.0 + 5 * 2.0, cart.ship(), EPS);
        }

        @Test
        void freeshipCoupon_shippingIsZero() {
            cart.addItem("X", 10.0, 1, 7.0, "g");
            cart.setCountry("US");
            cart.setCoupon("FREESHIP");
            assertEquals(0.0, cart.ship(), EPS);
        }

        @Test
        void expressShipping_multipliesByOnePointFive() {
            cart.addItem("X", 10.0, 1, 3.0, "g");
            cart.setCountry("US");
            cart.setExpress(true);
            assertEquals(10.0 * 1.5, cart.ship(), EPS);
        }

        @Test
        void expressWithFreeship_freeshipWins() {
            cart.addItem("X", 10.0, 1, 3.0, "g");
            cart.setCountry("US");
            cart.setCoupon("FREESHIP");
            cart.setExpress(true);
            assertEquals(0.0, cart.ship(), EPS);
        }

        @Test
        void totalWeight_sumsAcrossAllItems() {
            // two items totaling 6kg -> US heavy tier
            cart.addItem("A", 10.0, 2, 2.0, "g"); // 4kg
            cart.addItem("B", 10.0, 1, 2.0, "g"); // 2kg
            cart.setCountry("US");
            assertEquals(10.0 + 2 * 1.0, cart.ship(), EPS);
        }
    }

    @Nested
    @DisplayName("Tax")
    class Tax {

        @Test
        void nullCountry_taxIsZero() {
            cart.addItem("X", 10.0, 6, 0.1, "g");
            assertEquals(0.0, cart.tax(), EPS);
        }

        @Test
        void usTax_sevenPercentOfDiscountedSubtotal() {
            cart.addItem("X", 10.0, 6, 0.1, "g"); // subtotal 60, doIt 60
            cart.setCountry("US");
            assertEquals(60.0 * 0.07, cart.tax(), EPS);
        }

        @Test
        void caTax_thirteenPercentOfDiscountedSubtotal() {
            cart.addItem("X", 10.0, 6, 0.1, "g");
            cart.setCountry("CA");
            assertEquals(60.0 * 0.13, cart.tax(), EPS);
        }

        @Test
        void internationalTax_twentyPercent() {
            cart.addItem("X", 10.0, 6, 0.1, "g");
            cart.setCountry("UK");
            assertEquals(60.0 * 0.20, cart.tax(), EPS);
        }

        @Test
        void tax_isComputedOnDiscountedSubtotalNotRawSubtotal() {
            cart.addItem("X", 10.0, 6, 0.1, "g"); // subtotal 60
            cart.setCoupon("SAVE10");             // 10% off -> doIt 54
            cart.setCountry("US");
            assertEquals(54.0 * 0.07, cart.tax(), EPS);
        }
    }

    @Nested
    @DisplayName("Total")
    class Total {

        @Test
        void total_isDiscountedPlusShippingPlusTax() {
            cart.addItem("Widget", 10.0, 5, 0.4, "g");  // 50.00, 2kg
            cart.addItem("Gadget", 20.0, 2, 1.0, "t");  // 40.00, 2kg
            cart.setCoupon("SAVE10");                   // 90>50 -> 9 off
            cart.setMember("S");                         // 90*0.05 = 4.5
            cart.setCountry("US");                       // 4kg -> 10 shipping
            // doIt = 90 - 9 - 4.5 = 76.5
            // ship = 10
            // tax  = 76.5 * 0.07 = 5.355
            double expected = 76.5 + 10.0 + 5.355;
            assertEquals(expected, cart.total(), EPS);
        }

        @Test
        void emptyCart_totalIsZero() {
            cart.setCountry("US");
            assertEquals(0.0, cart.total(), EPS);
        }
    }

    @Nested
    @DisplayName("Receipt (format)")
    class Receipt {

        @Test
        void receipt_containsHeaderAndAllSectionLabels() {
            cart.addItem("Widget", 10.0, 2, 0.5, "g");
            cart.setCountry("US");
            String receipt = cart.format();

            assertTrue(receipt.startsWith("Receipt"), "starts with header");
            assertTrue(receipt.contains("Widget"), "contains item name");
            assertTrue(receipt.contains("x2"), "contains quantity");
            assertTrue(receipt.contains("Subtotal:"), "has subtotal line");
            assertTrue(receipt.contains("Discounted:"), "has discounted line");
            assertTrue(receipt.contains("Shipping:"), "has shipping line");
            assertTrue(receipt.contains("Tax:"), "has tax line");
            assertTrue(receipt.contains("Total:"), "has total line");
        }

        @Test
        void receipt_formatsMoneyWithTwoDecimals() {
            cart.addItem("Widget", 10.0, 2, 0.5, "g");
            cart.setCountry("US");
            String receipt = cart.format();
            assertTrue(receipt.contains("$10.00"), "unit price formatted");
            assertTrue(receipt.contains("$20.00"), "line total formatted");
        }
    }
}
