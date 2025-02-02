# HDIS – Hospital Dormancy Information System

HDIS is a simple system for monitoring idle patients in a hospital.
This implementation is a minimal proof of concept built using the Biff template.

## Features
- Displays idle patient data in a structured table format.
- Built with Clojure, [XTDB](https://www.xtdb.com/), and [HTMX](https://htmx.org/).
- Uses hiccup-style HTML generation.
- No additional dependencies beyond the Biff template.

## System Overview

### Data
#### Raw Data
- **Admissions**, **Patient Information**, **Lab-Tests**, **Lab-Results** -stored in **S3**.
#### Derived Data
- **Idle Patient Data** is stored in an **XTDB** database, derived from raw data.

### Server
- Implemented in **Clojure**.
- Uses a background worker to process data changes.
- **XTDB-based** storage.
- **Hiccup-style** HTML generation.
- 🚧 *Planned Features:*
  - Alerting system (**TODO**)
  - Automated tests (**TODO**)

### Client  
- Built with **HTMX**.
- **Table-based** data visualization.
- **Custom sorting** implemented manually.
- 🚧 *Planned Features:*
  - Filtering (**TODO**)
  - Paging (**TODO**)
  - Alerting (**TODO**)

## Miscellaneous  
- No **CI/CD** setup yet (**TODO**).

---

### Development Setup

This project is based on the [**Biff**](https://biffweb.com/) template.

To start the development server:
```sh
clj -M:dev dev
