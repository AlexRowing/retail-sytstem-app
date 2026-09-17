# Shelf Side — System Diagram

This diagram matches the final code. Every box is one class in `src/shelfside/`.

## How the classes fit together

```
                         +------------------------+
                         |   ShelfSideWrapper     |   <-- main(): program start
                         |   (main menu)          |
                         +-----------+------------+
                                     |
             creates & shares one    |    uses to check the demo login
             List<Item> + Storage    |
             with both sides         v
        +----------------------------+----------------------------+
        |                            |                            |
        v                            v                            v
+---------------+          +------------------+          +------------------+
|  RetailSide   |          |   ClientSide     |          |  LoginProcessor  |
| (manager menu)|          | (customer menu)  |          | (demo password)  |
| view/search/  |          | view/search/     |          +------------------+
| add/remove/   |          | details/cart/    |
| update stock  |          | checkout         |
+-------+-------+          +--------+---------+
        |                           |
        |  both read & write the    |
        |  same inventory through   |
        v                           v
              +--------------------------+
              |         Storage          |   load()/save() JSON file
              |  (mini JSON reader here) |   throws StorageException on trouble
              +------------+-------------+
                           |
                           v
                   +---------------+
                   | inventory.json|   (on disk; survives restarts)
                   +---------------+

Shared data type used everywhere:
   +---------------------------------------------+
   |                  Item                        |
   | id, name, priceCents, quantity, description  |
   | + validation, reduceStock(), money helpers   |
   +---------------------------------------------+

Small helper classes:
   Console          - reads keyboard input, prints text (shared by the 3 menus)
   StorageException - carries a clear error message from Storage
```

## Mermaid version (renders on GitHub)

```mermaid
classDiagram
    class ShelfSideWrapper {
        +main(args)
        +start()
        -mainMenu()
        -retailerLogin() boolean
    }
    class RetailSide {
        +findById(id) Item
        +search(term) List
        +addItem(...) Item
        +removeItem(id) boolean
        +updateQuantity(id, qty) boolean
        +updatePrice(id, cents) boolean
        +run()
    }
    class ClientSide {
        +search(term) List
        +addToCart(id, qty)
        +cartTotalCents() int
        +checkout() boolean
        +run()
    }
    class LoginProcessor {
        +check(user, pass) boolean
    }
    class Storage {
        +load() List
        +save(items)
    }
    class Item {
        -id, name, priceCents, quantity, description
        +reduceStock(n)
        +parsePriceToCents(text)$ int
        +formatCents(cents)$ String
    }
    class Console {
        +readLine(prompt) String
        +readIntInRange(prompt, min, max) int
    }
    class StorageException

    ShelfSideWrapper --> RetailSide : opens
    ShelfSideWrapper --> ClientSide : opens
    ShelfSideWrapper --> LoginProcessor : uses
    ShelfSideWrapper --> Storage : loads/saves
    ShelfSideWrapper --> Console : uses
    RetailSide --> Storage : saves changes
    ClientSide --> Storage : saves changes
    RetailSide --> Item : manages
    ClientSide --> Item : sells
    Storage ..> Item : builds
    Storage ..> StorageException : throws
```

## The one flow that ties it together (customer checkout)

1. `ShelfSideWrapper` loads `inventory.json` into a `List<Item>` via `Storage.load()`.
2. Customer picks items; `ClientSide.addToCart()` refuses any quantity above stock.
3. `ClientSide.checkout()` calls `Item.reduceStock()` on each item, then `Storage.save()`.
4. The new quantities are now on disk, so they are still there next time the app starts.
