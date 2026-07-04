# VS Tracker

An Android app for tracking Vinegar Syndrome limited edition releases: stock levels, price drops, sales, and your personal collection.

## What it does

Vinegar Syndrome sells limited edition physical media that sells out permanently once stock hits zero. VS Tracker lets you type in movie titles you care about and watches them for you.

The app has two tabs:

**Alerts**: type a movie title, search results come straight from Vinegar Syndrome's site, pick which edition to track (a title can have a "Limited Edition Slipcase" and a "Standard Edition" as separate variants), and the app checks it periodically in the background. You get a notification when:

* Stock drops below a threshold (500 units by default, configurable in Settings)
* The price drops from what was last seen
* An item goes on sale (price below its compare at price)

**Collection**: a record of releases you actually own, grouped by Vinegar Syndrome's imprint labels in this order: Vinegar Syndrome, Vinegar Syndrome Archive, Vinegar Syndrome Labs, Vinegar Syndrome Pictures, Vinegar Syndrome Ultra, Cinematographe, Degausser Video, Distribpix, Iconoscope, Pink Line, Reviver. Any other label sorts alphabetically after those. You can add a release directly from search, or with one tap from an Alerts row (which then offers to stop tracking stock for that item, since you already own it).

## How it works

There's no backend server. It's a self contained Android app using:

* Shopify's storefront predictive search endpoint (`/search/suggest.json`) to resolve a typed title to a product
* The `product-json` script tag embedded in each Vinegar Syndrome product page for exact price, compare at price, and inventory quantity per variant, since that data is not exposed in the storefront's JSON API
* Room for local storage of tracked movies and collection items
* WorkManager for periodic background checks (roughly every 30 minutes, subject to Android's battery optimization behavior)
* Plain Android notifications for alerts

Because there is no server, background checks depend on Android not restricting the app. Settings includes a way to check and fix this (exempting the app from battery optimization) and to manage notification permissions.

## Building

Requires Android SDK with platform 34 and build tools installed, plus a JDK 17.

```
./gradlew assembleRelease
```

The release build type is signed with the debug keystore so it installs without any extra keystore setup. This is meant for personal use, not distribution through an app store.

## Project layout

* `data/`: Room entities (`TrackedMovie`, `CollectionItem`), DAOs, the database and its migrations, and `AppSettings` for user preferences
* `network/`: the Shopify API client and response models
* `worker/`: the background `StockCheckWorker` and its scheduling helpers
* `notification/`: notification channel setup and alert posting
* `ui/`: Compose screens and view models

## Limitations

* Phone only: background checks can be delayed by Android's Doze mode if battery optimization is not disabled for the app.
* Price/stock data is scraped from public page content rather than a stable documented API, so a Vinegar Syndrome theme change could break parsing. If that happens, the `product-json` selector in `network/VinegarSyndromeApi.kt` is the place to fix it.
