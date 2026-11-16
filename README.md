# Trading Strategy Lab — Android starter upgrade

This package upgrades the uploaded **HTMLBackgroundRunner** Android project into a Trading Strategy Lab foundation. It preserves the existing application ID/package namespace and retains the user-started foreground service concept, while replacing the old HTML runner screen with a bundled mobile-first research dashboard.

## Included in this version

- Kotlin Android host with a bundled WebView dashboard.
- Existing foreground-service workflow retained and renamed for Trading Strategy Lab.
- Native Start/Stop controls and persistent foreground notification.
- Dark responsive dashboard with Dashboard, Historical Data, Strategy Builder, Backtesting Lab, Signal Center, AI Providers and System Control sections.
- CSV import using the browser File API and streaming text decoding; rows are written to IndexedDB in batches of 1,000.
- Basic OHLCV validation, duplicate timestamp de-duplication, invalid row counting and timeframe gap estimation.
- Local dataset metadata and candle storage using IndexedDB.
- CSV export through Android Storage Access Framework (first 50,000 rows per export in this version).
- Locally saved EMA crossover strategy definitions.
- Basic long-only EMA crossover backtest with configurable starting capital, fees and slippage; summary metrics and equity curve.
- GitHub Actions workflow to build and upload a debug APK artifact.

## Important limitations — not implemented yet

This is a working Phase 1 foundation plus a basic local data/backtest workflow, **not the complete 19-section production system described in the project brief**.

- Gemini, Groq and Cerebras adapters are not connected. Do not paste API keys into the web UI. Secure Android Keystore storage and provider-specific native networking are still required.
- No Binance/Dukascopy historical downloader, WebSocket live feed, daily autonomous discovery, AI strategy generation, optimizer, strategy arena, push signals, or real-money order execution.
- No Room database yet; imported candles currently live in WebView IndexedDB. Clearing app data removes this local database. Export CSV backups before reinstalling or clearing storage.
- No WorkManager scheduler, boot recovery, crash checkpointing, Google Drive backup, PDF/XLSX report generation or database restore yet.
- Backtesting is a first-pass long-only close-price simulation. It does not yet provide short positions, intrabar stop/target execution, spread modelling, funding, partial fills, walk-forward validation, out-of-sample testing, Sharpe ratio or a broker-grade fill model. The backtest loads at most 250,000 candles into memory; CSV export is capped at 50,000 candles.
- The foreground service represents a user-started visible research session. It does not keep JavaScript computation running when the app process is gone and Android may stop it under system policy. It cannot run while the phone is powered off or after a user force-stop.

## Build on GitHub from a mobile browser

1. Create or open your GitHub repository.
2. Upload the contents of this ZIP (upload the files inside the `TradingStrategyLab` folder, not the outer ZIP itself).
3. Commit the files to `main` or `master`.
4. Open the repository's **Actions** tab and allow GitHub Actions if prompted.
5. Run **Android Debug APK** using **Run workflow**, or push a commit to `main`/`master`.
6. Open the completed workflow run, scroll to **Artifacts**, and download `trading-strategy-lab-debug-apk`.
7. Extract the artifact ZIP and install `app-debug.apk` on your Android device. You may need to allow installation from your browser/file manager.

## Local build

Requires JDK 17 and Gradle 8.9:

```bash
gradle --no-daemon assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## CSV format

Required header columns (case-insensitive aliases are accepted):

```csv
timestamp,open,high,low,close,volume
2025-01-01T00:00:00Z,93500,93600,93400,93550,12.5
```

Unix timestamps in seconds or milliseconds are also accepted. Data should be chronological, although imported candles are sorted for preview/backtesting. Select the correct market and timeframe before importing. Source/timeframe metadata is saved with each dataset.

## Security notes

- No API credentials are requested or stored by this version.
- The dashboard is bundled locally in the APK; there is no remote frontend dependency.
- No trading orders are sent. The application is a research tool only.
- Do not commit private market datasets or credentials to a public GitHub repository.

## Preserved legacy source

The original `MainActivity.kt` and `RunnerService.kt` from the uploaded archive are included under `legacy/` as text reference copies. The active Android package and application ID remain `com.example.htmlbackgroundrunner` so this project can be used as an in-place upgrade path when signing configuration matches the installed app.
