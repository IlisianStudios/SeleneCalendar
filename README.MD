# Modern Lunar Calendar

A highly customized lunar calendar system inspired by the Chaldean calendar but modified significantly to suit modern use cases. This project extends Java's `Calendar` class, providing an alternative lunar-based timekeeping system.

## 📌 Project Overview

This calendar system is **not** a traditional Chaldean calendar anymore due to multiple deviations from its historical base. Instead, it provides a **modernized lunar-based** alternative with custom month and year calculations.

## 🚀 Key Features

- **Lunar-based months**: Each month aligns with lunations rather than fixed days.
- **Custom year calculations**: Derived from lunation cycles rather than a solar year.
- **Extended `Calendar` class**: Works seamlessly with existing Java date utilities.
- **Support for arithmetic operations**: `add()` and `roll()` behave predictably within the lunar system.
- **Efficient date conversions**: Converts between Julian Days (JD) and internal time representation.

## ⚙️ Design Decisions

- **Deviation from Chaldean rules**: Adjusted for usability and consistency.
- **Field computation logic**:
  - `computeTime()`: Converts calendar fields to epoch time.
  - `computeFields()`: Converts epoch time back into calendar fields.
- **Handling of `roll()` and `add()`**:
  - `roll()` keeps larger fields unchanged (e.g., rolling the day won't affect the month).
  - `add()` properly adjusts larger fields when needed.
- **Avoiding unnecessary recomputation**:
  - Flags like `areFieldsSet` and `areAllFieldsSet` ensure minimal recalculations.
- **Integration with existing Java utilities**: Compatible with `DateFormat` and other Java time classes.

## 🔧 Installation & Usage

1. **Clone the repository**:
   ```sh
   git clone https://github.com/YOUR-ORG/YOUR-REPO.git
   cd YOUR-REPO
2. **Build the project**:
   ```sh
   ./gradlew build

## 📖 Contributing

We welcome contributions! To contribute:

1. **Fork the repository** on GitHub.
2. **Create a new branch** for your feature or bugfix:
3. **Submit a pull request** with detailed changes.

