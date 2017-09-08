# JRAW-Android-Sample

### Authenticating your client

Initialize the AuthenticationManager in your MainActivity's onCreate() method:

    RedditClient redditClient = new RedditClient(UserAgent.of("android",
          "com.example.yourapp", "v0.0.1", "yourusername"));
    TokenStore store = new AndroidTokenStore(PreferenceManager.getDefaultSharedPreferences(this));
    RefreshTokenHandler refreshTokenHandler = new RefreshTokenHandler(store, redditClient);
    AuthenticationManager.get().init(redditClient, refreshTokenHandler);
    
The AndroidTokenStore is an implementation of the TokenStore interface, this class is where you define how to save your authentication data. This sample uses Shared Preferences to store the token.

#### Userless authentication

    Credentials credentials = Credentials.userlessApp(CLIENT_ID, UUID.randomUUID());
    
    OAuthData oAuthData = AuthenticationManager.get().getRedditClient()
          .getOAuthHelper().easyAuth(credentials);
    AuthenticationManager.get().getRedditClient().authenticate(oAuthData);
    
    // You can now use the client!
    Submission submission = AuthenticationManager.get().getRedditClient().getRandomSubmission();
    
#### User authentication

    Credentials credentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
    
    OAuthHelper oAuthHelper = AuthenticationManager.get().getRedditClient().getOAuthHelper();
    // Add or remove scopes as necessary.
    String[] scopes = {"identity", "edit", "flair", "mysubreddits", "read", "vote",
                "submit", "subscribe", "history", "save"};
                
    // You should open this authorization url on a webview that lets the user sign in to reddit and
    // authorize your application to user their account.
    String authorizationUrl = oAuthHelper.getAuthorizationUrl(credentials, true, true, scopes).toExternalForm();
    
    // This is the url that loads when you click "Allow" on the reddit login authorization screen, 
    // it contains a code in it. See the LoginActivity's onCreate() method to view how to extract it from the
    // webview once it has loaded.
    String url = // URL obtained from webview.
    
    OAuthHelper oAuthHelper = AuthenticationManager.get().getRedditClient().getOAuthHelper();
    OAuthData oAuthData = oAuthHelper.onUserChallenge(url, credentials);
    AuthenticationManager.get().getRedditClient().authenticate(oAuthData);
    AuthenticationManager.get().onAuthenticated(oAuthData);
    
    // You can now use the client!
    String username = AuthenticationManager.get().getRedditClient().getAuthenticatedUser();
    Toast.makeText(this, "Logged in as " + username, Toast.LENGTH_SHORT).show();
    
    Submission submission = AuthenticationManager.get().getRedditClient().getRandomSubmission();
    
### Refreshing the token

Authentication tokens expire after an hour, at which point you should refresh them to continue to use the client.
You can check your authentication state using your AuthenticationManager instance.

    // One of NONE, NEEDS_REFRESH or READY.
    AuthenticationState authState = AuthenticationManager.get().checkAuthState();

You can refresh your token by calling:

    AuthenticationManager.get().refreshAccessToken(credentials);
    
