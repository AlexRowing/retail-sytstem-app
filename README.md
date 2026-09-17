# Shelf Side

A CLI-based Point of Sale (POS) system written in Java. See `Group 11 CS Project 1 Scope.pdf`
and `Group 11 CS Project 1 Spec.pdf` for the full project scope and class design.

## Build

```
javac -d bin $(find src -name "*.java")
```

On Windows PowerShell:

```
Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName } | Set-Content sources.txt
javac -d bin "@sources.txt"
```

## Run

```
java -cp bin shelfside.Main
```

Default retailer login: `admin` / `admin123`

## Notes

- Inventory and categories are persisted locally as `inventory.json` and `categories.json`
  in the working directory (created automatically on first run).
- Checkout applies an 8% tax rate and prints/saves a text receipt (`receipt_<timestamp>.txt`).
  Sample promo codes: `SAVE10` (10% off), `SAVE20` (20% off).
- Receipts are saved as plain text rather than true PDF files to avoid adding an external
  PDF-generation dependency for this project's scope.
