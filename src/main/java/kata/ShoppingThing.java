package kata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * KATA: Safe Refactoring Practice
 * ================================
 * This class is intentionally bad. Your goal is to refactor it into clean code
 * WITHOUT breaking any tests in ShoppingThingTest. Run `mvn test` after every
 * change. If tests stay green, your refactoring is safe.
 *
 * Smells to hunt:
 *   1. God class       - one class does cart storage, pricing, discounts,
 *                        shipping, tax, and receipt formatting. Extract.
 *   2. Duplicated code - the "loop over items" pattern is copy-pasted in
 *                        subtotal(), discountedSubtotal(), shippingCost(),
 *                        receipt(). Consolidate it.
 *   3. Deep nesting    - discountedSubtotal() and shippingCost() have 4+ level
 *                        if/else pyramids. Use guard clauses, polymorphism,
 *                        or lookup maps.
 *
 * Suggested path (one small step at a time, tests green after each):
 *   a) Extract an Item class to replace Map<String, Object>.
 *   b) Extract DiscountPolicy - move coupon, member, and bulk discount logic.
 *   c) Extract ShippingCalculator - move weight + zone + express logic.
 *   d) Extract TaxCalculator - move the country-to-rate mapping.
 *   e) Extract ReceiptPrinter - move the formatting logic.
 *   f) Flatten nested ifs with guard clauses and lookup tables.
 */
public class ShoppingThing {

    private List<Map<String, Object>> items = new ArrayList<>();
    private String couponCode;
    private String country;
    private String memberTier;
    private boolean expressShipping = false;

    public void addItem(String name, double price, int quantity, double weight, String category) {
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("name", name);
        itemData.put("price", price);
        itemData.put("quantity", quantity);
        itemData.put("weight", weight);
        itemData.put("category", category);
        items.add(itemData);
    }

    public void setCoupon(String code) { this.couponCode = code; }
    public void setCountry(String country) { this.country = country; }
    public void setMember(String tier) { this.memberTier = tier; }
    public void setExpress(boolean express) { this.expressShipping = express; }

    public double subtotal() {
        double subtotal = 0;
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            double price = (double) item.get("price");
            int quantity = (int) item.get("quantity");
            subtotal += price * quantity;
        }
        return subtotal;
    }

    public double discountedSubtotal() {
        double subtotal = subtotal();
        double discount = 0;

        if (couponCode != null) {
            if (couponCode.equals("SAVE10")) {
                if (subtotal > 50) {
                    discount += subtotal * 0.1;
                } else {
                    if (subtotal > 20) {
                        discount += 2;
                    }
                }
            } else {
                if (couponCode.equals("SAVE20")) {
                    if (subtotal > 100) {
                        discount += subtotal * 0.2;
                    } else {
                        if (subtotal > 50) {
                            discount += subtotal * 0.1;
                        }
                    }
                }
            }
        }

        if (memberTier != null) {
            if (memberTier.equals("G")) {
                discount += subtotal * 0.15;
            } else {
                if (memberTier.equals("S")) {
                    discount += subtotal * 0.05;
                }
            }
        }

        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            int quantity = (int) item.get("quantity");
            double price = (double) item.get("price");
            if (quantity >= 10) {
                if (price > 5) {
                    discount += price * quantity * 0.05;
                }
            }
        }

        return subtotal - discount;
    }

    public double shippingCost() {
        if (couponCode != null && couponCode.equals("FREESHIP")) return 0;
        if (items.isEmpty()) return 0;

        double totalWeight = 0;
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            totalWeight += (double) item.get("weight") * (int) item.get("quantity");
        }

        double cost = 0;
        if (country != null) {
            if (country.equals("US")) {
                if (totalWeight <= 1) {
                    cost = 5;
                } else {
                    if (totalWeight <= 5) {
                        cost = 10;
                    } else {
                        cost = 10 + (totalWeight - 5) * 2;
                    }
                }
            } else {
                if (country.equals("CA")) {
                    if (totalWeight <= 1) {
                        cost = 8;
                    } else {
                        if (totalWeight <= 5) {
                            cost = 15;
                        } else {
                            cost = 15 + (totalWeight - 5) * 3;
                        }
                    }
                } else {
                    if (totalWeight <= 1) {
                        cost = 20;
                    } else {
                        if (totalWeight <= 5) {
                            cost = 35;
                        } else {
                            cost = 35 + (totalWeight - 5) * 5;
                        }
                    }
                }
            }
        }

        if (expressShipping) cost = cost * 1.5;
        return cost;
    }

    public double tax() {
        double discountedSubtotal = discountedSubtotal();
        double taxRate = 0;
        if (country != null) {
            if (country.equals("US")) {
                taxRate = 0.07;
            } else {
                if (country.equals("CA")) {
                    taxRate = 0.13;
                } else {
                    taxRate = 0.20;
                }
            }
        }
        return discountedSubtotal * taxRate;
    }

    public double total() {
        return discountedSubtotal() + shippingCost() + tax();
    }

    public String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Receipt\n");
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            double price = (double) item.get("price");
            int quantity = (int) item.get("quantity");
            sb.append(item.get("name"))
              .append(" x").append(quantity)
              .append(" @ $").append(String.format("%.2f", price))
              .append(" = $").append(String.format("%.2f", price * quantity))
              .append("\n");
        }
        sb.append("Subtotal: $").append(String.format("%.2f", subtotal())).append("\n");
        sb.append("Discounted: $").append(String.format("%.2f", discountedSubtotal())).append("\n");
        sb.append("Shipping: $").append(String.format("%.2f", shippingCost())).append("\n");
        sb.append("Tax: $").append(String.format("%.2f", tax())).append("\n");
        sb.append("Total: $").append(String.format("%.2f", total())).append("\n");
        return sb.toString();
    }
}
