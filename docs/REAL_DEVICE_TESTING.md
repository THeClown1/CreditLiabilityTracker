# OnePlus Nord 6 test checklist

Use a test card with fictitious last four digits and messages sent from a second phone. Do not use real account data for initial tests.

## Install and migration

1. Connect the Nord 6 with USB debugging enabled and select it as the Android Studio deployment target.
2. Run the app from Android Studio. Installing the version 3 database should preserve version 2 cards and transactions while creating the EMI and statement tables.
3. Add a test card and record its four digit suffix.

## SMS permission and receive path

1. On the dashboard, enable SMS tracking and accept the explanation and Android permission prompt.
2. Send this SMS from another phone to the Nord:
   `ICICI Bank Credit Card XX1234 used for INR 8999 at FLIPKART on 28-Oct-26.`
3. Confirm a high confidence transaction is saved for a matching card; use the review inbox when confidence is below the automatic-save threshold or the card does not match.
4. Send an OTP and a marketing message containing card wording. Confirm they do not enter the review inbox.
5. Deny SMS permission, reopen the app, and verify tracking reports permission as disabled. Re-enable it from app settings.
6. Repeat while the app is backgrounded, after screen lock, and after reboot. Record OS version and battery optimization settings if delivery is delayed.

## Duplicate and financial behavior

1. Send the same transaction SMS twice. Confirm the liability increases once.
2. Record a matching purchase manually, then send its SMS. Confirm the fingerprint / near-match protection prevents a second liability entry.
3. Send a payment and refund message; confirm each appears as a liability credit and not as spending.
4. Add one statement and one EMI obligation; verify bill, unbilled, due date, and monthly EMI summaries.
5. Test Delete All Data only with test records and confirm the dashboard returns to its empty state.

## Record results

Note Android version/build, app version, permission state, whether delivery occurred in foreground/background/after reboot, sender ID format, and any parser correction needed. Never copy complete real SMS bodies or full card numbers into production logs or issue reports.