# TastyTracker

## Usage
Import the project into Android Studio, and launch the app on either a real device or emulated device running at least Android 5.0. 

Login with Facebook. The app will generate your first list, tracking AAPL, MSFT, and ES. Menu items allow adding symbols (utilizing the Tastyworks autocomplete), adding lists, and removing the current list. Long pressing a symbol row will remove it from the list.

## Next Steps
- Add individual stock detail graph
- Add multi user support (user specific watchlists)
- Better data management (not SharedPreferences)
- More stylish UI
- Better separation of responsibility (not just subscriptions in the activity)
- Lifecycle management of subscriptions