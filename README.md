# Refactoring Kata: Shopping Cart Pricing

## What This Project Does

This is a shopping cart pricing engine. Given a list of items, it computes:

- **Subtotal** — sum of `price × quantity` across all items
- **Discounts** — three independent discount types that stack:
  - *Coupon codes*: `SAVE10` (10% off orders >$50, or $2 flat off orders >$20), `SAVE20` (20% off orders >$100, or 10% off orders >$50), `FREESHIP` (waives shipping)
  - *Member tiers*: Gold (15% off), Silver (5% off)
  - *Bulk*: 5% off any line item where quantity ≥ 10 and unit price > $5
- **Shipping** — weight-based, per destination:
  | Zone | ≤1 kg | 1–5 kg | >5 kg |
  |------|-------|--------|-------|
  | US   | $5    | $10    | $10 + $2/kg over 5 |
  | CA   | $8    | $15    | $15 + $3/kg over 5 |
  | Intl | $20   | $35    | $35 + $5/kg over 5 |

  Express shipping multiplies the shipping cost by 1.5×.
- **Tax** — applied to the discounted subtotal: US 7%, CA 13%, all other countries 20%
- **Total** — discounted subtotal + shipping + tax
- **Receipt** — a formatted text summary of the order

## The Goal of This Kata

The class `ShoppingThing` implements all of the above correctly, but it is a mess. Your job is to **refactor it without changing its behaviour**. The 49 unit tests in `ShoppingThingTest` are your safety net — keep them green throughout.

### Code Smells to Fix

| Smell | Where to look |
|-------|---------------|
| **God class** | `ShoppingThing` handles cart storage, discount logic, shipping, tax, and receipt printing all at once. Extract focused classes. |
| **Bad names** | Fields `a`, `b`, `c`, `d`; locals `tmp`, `x`; methods `calc()`, `doIt()`, `ship()`; boolean `flag2`; map keys `"n"`, `"p"`, `"q"`, `"w"`, `"c"`. Rename everything. |
| **Duplicated code** | The "loop over all items" pattern is copy-pasted in `calc()`, `doIt()` (bulk discount), `ship()`, and `format()`. Consolidate it. |
| **Deep nesting** | `doIt()` and `ship()` have 4+ levels of nested `if/else`. Flatten with guard clauses, or replace with lookup tables / strategy objects. |

### Suggested Refactoring Path

Work in small steps. Run `mvn test` after each one.

1. **Rename** all fields, locals, and methods to meaningful names.
2. **Extract `Item`** — replace `Map<String, Object>` with a proper value class.
3. **Extract `DiscountPolicy`** — move coupon, member, and bulk discount logic out of `ShoppingThing`.
4. **Extract `ShippingCalculator`** — move the weight + zone + express logic.
5. **Extract `TaxCalculator`** — move the country-to-rate mapping.
6. **Extract `ReceiptPrinter`** — move the formatting logic.
7. **Flatten conditionals** — use guard clauses, `Map<String, Double>` rate lookups, or a strategy pattern to replace the nested `if/else` pyramids.

### Rules

- **Never change the tests** to make a broken refactoring pass. Only update them when you rename a public method, and do so in the same commit as the rename.
- **Run `mvn test` after every change** — not just at the end.
- **One smell at a time.** Mixing multiple refactorings in one step makes it hard to spot what broke something.

## IDE Refactoring Shortcuts

Use these shortcuts to let your IDE do the mechanical work safely — the tool rewrites all call sites for you.

### IntelliJ IDEA

| Action | Windows | Mac |
|--------|---------|-----|
| **Refactor This** (shows all options) | `Ctrl+Alt+Shift+T` | `Ctrl+T` |
| Rename | `Shift+F6` | `Shift+F6` |
| Extract Method | `Ctrl+Alt+M` | `Cmd+Alt+M` |
| Extract Variable | `Ctrl+Alt+V` | `Cmd+Alt+V` |
| Extract Constant | `Ctrl+Alt+C` | `Cmd+Alt+C` |
| Extract Field | `Ctrl+Alt+F` | `Cmd+Alt+F` |
| Extract Parameter | `Ctrl+Alt+P` | `Cmd+Alt+P` |
| Inline (opposite of extract) | `Ctrl+Alt+N` | `Cmd+Alt+N` |
| Move class/member | `F6` | `F6` |
| Change Method Signature | `Ctrl+F6` | `Cmd+F6` |
| Find Usages | `Alt+F7` | `Option+F7` |
| Run tests in current file | `Ctrl+Shift+F10` | `Ctrl+Shift+R` |
| Rerun last test | `Shift+F10` | `Ctrl+R` |

> **Tip:** When in doubt, place the cursor on a symbol and press **Refactor This** — IntelliJ shows only the refactorings that apply to that context.

---

### VS Code (with Java Extension Pack)

| Action | Windows | Mac |
|--------|---------|-----|
| **Rename Symbol** | `F2` | `F2` |
| **Refactor / Extract...** (context menu) | `Ctrl+.` | `Cmd+.` |
| Extract to Method | Select code → `Ctrl+.` → *Extract to method* | Select code → `Cmd+.` → *Extract to method* |
| Extract to Variable | Select code → `Ctrl+.` → *Extract to variable* | Select code → `Cmd+.` → *Extract to variable* |
| Extract to Field | Select code → `Ctrl+.` → *Extract to field* | Select code → `Cmd+.` → *Extract to field* |
| Extract to Constant | Select code → `Ctrl+.` → *Extract to constant* | Select code → `Cmd+.` → *Extract to constant* |
| Find All References | `Shift+Alt+F12` | `Shift+Option+F12` |
| Go to Definition | `F12` | `F12` |
| Peek References | `Shift+F12` | `Shift+F12` |
| Run Tests (Command Palette) | `Ctrl+Shift+P` → *Test: Run All Tests* | `Cmd+Shift+P` → *Test: Run All Tests* |
| Run Test at cursor | `Ctrl+Shift+P` → *Test: Run Test at Cursor* | `Cmd+Shift+P` → *Test: Run Test at Cursor* |

> **Tip:** In VS Code, most extract refactorings are under `Ctrl+.` / `Cmd+.`. Select the exact expression or block you want to extract first, then trigger the shortcut.

---

## Running the Tests

```bash
mvn test
```

Expected output: **49 tests, 0 failures.**
