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
 *   1. God class        - one class does cart storage, pricing, discounts,
 *                         shipping, tax, and receipt formatting. Extract.
 *   2. Bad names        - fields a/b/c/d, methods calc/doIt/ship, vars tmp/x/flag2.
 *                         Rename.
 *   3. Duplicated code  - the "loop over items" pattern is copy-pasted in
 *                         calc(), doIt(), ship(), format(). Consolidate.
 *   4. Deep nesting     - doIt() and ship() have 4+ level if/else pyramids.
 *                         Use guard clauses, polymorphism, or lookup maps.
 *
 * Suggested path (one small step at a time, tests green after each):
 *   a) Rename private fields and locals to meaningful names.
 *   b) Extract an Item class (replace Map<String,Object>).
 *   c) Rename public methods: calc->subtotal, doIt->discountedSubtotal,
 *      ship->shippingCost, tax->taxAmount, format->receipt.
 *      (Hint: add new methods that delegate, then swap call sites, then remove old.)
 *   d) Extract DiscountPolicy, ShippingCalculator, TaxCalculator, ReceiptPrinter.
 *   e) Flatten nested ifs with guard clauses and lookup tables.
 *
 * NOTE: The tests call ShoppingThing's public API only, so any renames of
 * public methods must be done via a deprecation-friendly shim OR by updating
 * the tests in the SAME commit as the rename (the second is fine for a kata).
 */
public class ShoppingThing {

    private List<Map<String, Object>> a = new ArrayList<>();
    private String b;
    private String c;
    private String d;
    private boolean flag2 = false;

    public void addItem(String name, double price, int qty, double weight, String category) {
        Map<String, Object> m = new HashMap<>();
        m.put("n", name);
        m.put("p", price);
        m.put("q", qty);
        m.put("w", weight);
        m.put("c", category);
        a.add(m);
    }

    public void setCoupon(String code) { this.b = code; }
    public void setCountry(String country) { this.c = country; }
    public void setMember(String tier) { this.d = tier; }
    public void setExpress(boolean express) { this.flag2 = express; }

    public double calc() {
        double tmp = 0;
        for (int i = 0; i < a.size(); i++) {
            Map<String, Object> it = a.get(i);
            double p = (double) it.get("p");
            int q = (int) it.get("q");
            tmp += p * q;
        }
        return tmp;
    }

    public double doIt() {
        double tmp = calc();
        double x = 0;

        if (b != null) {
            if (b.equals("SAVE10")) {
                if (tmp > 50) {
                    x += tmp * 0.1;
                } else {
                    if (tmp > 20) {
                        x += 2;
                    }
                }
            } else {
                if (b.equals("SAVE20")) {
                    if (tmp > 100) {
                        x += tmp * 0.2;
                    } else {
                        if (tmp > 50) {
                            x += tmp * 0.1;
                        }
                    }
                }
            }
        }

        if (d != null) {
            if (d.equals("G")) {
                x += tmp * 0.15;
            } else {
                if (d.equals("S")) {
                    x += tmp * 0.05;
                }
            }
        }

        for (int i = 0; i < a.size(); i++) {
            Map<String, Object> it = a.get(i);
            int q = (int) it.get("q");
            double p = (double) it.get("p");
            if (q >= 10) {
                if (p > 5) {
                    x += p * q * 0.05;
                }
            }
        }

        return tmp - x;
    }

    public double ship() {
        if (b != null && b.equals("FREESHIP")) return 0;
        if (a.isEmpty()) return 0;

        double w = 0;
        for (int i = 0; i < a.size(); i++) {
            Map<String, Object> it = a.get(i);
            w += (double) it.get("w") * (int) it.get("q");
        }

        double s = 0;
        if (c != null) {
            if (c.equals("US")) {
                if (w <= 1) {
                    s = 5;
                } else {
                    if (w <= 5) {
                        s = 10;
                    } else {
                        s = 10 + (w - 5) * 2;
                    }
                }
            } else {
                if (c.equals("CA")) {
                    if (w <= 1) {
                        s = 8;
                    } else {
                        if (w <= 5) {
                            s = 15;
                        } else {
                            s = 15 + (w - 5) * 3;
                        }
                    }
                } else {
                    if (w <= 1) {
                        s = 20;
                    } else {
                        if (w <= 5) {
                            s = 35;
                        } else {
                            s = 35 + (w - 5) * 5;
                        }
                    }
                }
            }
        }

        if (flag2) s = s * 1.5;
        return s;
    }

    public double tax() {
        double sub = doIt();
        double r = 0;
        if (c != null) {
            if (c.equals("US")) {
                r = 0.07;
            } else {
                if (c.equals("CA")) {
                    r = 0.13;
                } else {
                    r = 0.20;
                }
            }
        }
        return sub * r;
    }

    public double total() {
        return doIt() + ship() + tax();
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Receipt\n");
        for (int i = 0; i < a.size(); i++) {
            Map<String, Object> it = a.get(i);
            double p = (double) it.get("p");
            int q = (int) it.get("q");
            sb.append(it.get("n"))
              .append(" x").append(q)
              .append(" @ $").append(String.format("%.2f", p))
              .append(" = $").append(String.format("%.2f", p * q))
              .append("\n");
        }
        sb.append("Subtotal: $").append(String.format("%.2f", calc())).append("\n");
        sb.append("Discounted: $").append(String.format("%.2f", doIt())).append("\n");
        sb.append("Shipping: $").append(String.format("%.2f", ship())).append("\n");
        sb.append("Tax: $").append(String.format("%.2f", tax())).append("\n");
        sb.append("Total: $").append(String.format("%.2f", total())).append("\n");
        return sb.toString();
    }
}
