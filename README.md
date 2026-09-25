# TriMail

Native Kotlin Android mail client built with Jetpack Compose and Material 3.

TriMail provides three independent mailbox slots, a unified inbox foundation, and provider-specific account connection flows.

## Google account sign-in

For a Google mailbox, type the email address in a slot. Gmail addresses are detected as Google automatically, and the slot shows a Continue with Google button.

The button uses Android Credential Manager and Sign in with Google. It verifies that the Google account selected by the user matches the email typed in the slot. TriMail does not ask for or store the Google password.

Google's Android guidance requires a Google Cloud project and an OAuth web client ID for Sign in with Google. Configure the build with:

GOOGLE_WEB_CLIENT_ID=<your OAuth web client ID>

For GitHub Actions, add GOOGLE_WEB_CLIENT_ID as a repository secret. A build without this value still compiles, but the Google sign-in action reports that Google Sign-In is not configured.

## Current scope

The Google flow authenticates the selected Google identity into the local mailbox slot. Live Gmail message sync, Gmail API authorization, IMAP/SMTP, and actual email delivery are separate integrations and are not enabled by this repository yet.

The app remains native Kotlin Android. It does not use HTML or WebView for authentication.
