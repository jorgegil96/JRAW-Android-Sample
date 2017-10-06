package com.gmail.jorgegilcavazos.androidjrawsample;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.auth.RefreshTokenHandler;
import net.dean.jraw.auth.TokenStore;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.models.Submission;

import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int REQUEST_CODE = 1;

    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnUserlessAuth = (Button) findViewById(R.id.btnUserlessAuth);
        btnUserlessAuth.setOnClickListener(this);
        Button btnUserAuth = (Button) findViewById(R.id.btnUserAuth);
        btnUserAuth.setOnClickListener(this);
        Button btnCheckStatus = (Button) findViewById(R.id.btnCheckStatus);
        btnCheckStatus.setOnClickListener(this);
        Button btnRefreshToken = (Button) findViewById(R.id.btnRefreshToken);
        btnRefreshToken.setOnClickListener(this);
        Button btnLoadTopPost = (Button) findViewById(R.id.btnLoadSubmission);
        btnLoadTopPost.setOnClickListener(this);
        Button btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(this);


        RedditClient redditClient = new RedditClient(UserAgent.of("android",
                "com.gmail.jorgegilcavazos.androidjrawsample", "v0.0.1", "yourusername"));
        TokenStore store = new AndroidTokenStore(
                PreferenceManager.getDefaultSharedPreferences(this));
        RefreshTokenHandler refreshTokenHandler = new RefreshTokenHandler(store, redditClient);

        AuthenticationManager manager = AuthenticationManager.get();
        manager.init(redditClient, refreshTokenHandler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnUserlessAuth:
                doUserlessAuth();
                break;
            case R.id.btnUserAuth:
                doUserAuth();
                break;
            case R.id.btnCheckStatus:
                checkStatus();
                break;
            case R.id.btnRefreshToken:
                refreshToken();
                break;
            case R.id.btnLoadSubmission:
                loadSubmission();
                break;
            case R.id.btnLogout:
                logout();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Credentials credentials = ((MyApplication) getApplication())
                    .getInstalledAppCredentials();

            disposables.add(RedditService.userAuthentication(
                        AuthenticationManager.get().getRedditClient(),
                        credentials,
                        data.getStringExtra("RESULT_URL"))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableCompletableObserver() {
                        @Override
                        public void onComplete() {
                            String username = AuthenticationManager.get().getRedditClient()
                                    .getAuthenticatedUser();
                            Toast.makeText(MainActivity.this, "Logged in as " + username,
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(MainActivity.this, "Something went wrong",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
            );
        }
    }

    private void doUserlessAuth() {
        Credentials credentials = ((MyApplication) getApplication()).getUserlessAppCredentials();
        disposables.add(RedditService.userlessAuthentication(
                    AuthenticationManager.get().getRedditClient(), credentials)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Toast.makeText(MainActivity.this, "Authentication complete!",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this, "Something went wrong",
                                Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    private void doUserAuth() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    private void checkStatus() {
        Toast.makeText(
                this,
                AuthenticationManager.get().checkAuthState().toString(),
                Toast.LENGTH_SHORT)
                .show();
    }

    private void refreshToken() {
        if (!AuthenticationManager.get().getRedditClient().hasActiveUserContext()) {
            Toast.makeText(MainActivity.this, "No need to refresh userless auth tokens",
                    Toast.LENGTH_SHORT).show();
        } else {
            Credentials credentials = ((MyApplication) getApplicationContext())
                    .getInstalledAppCredentials();
            disposables.add(RedditService.refreshToken(credentials)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableCompletableObserver() {
                        @Override
                        public void onComplete() {
                            Toast.makeText(MainActivity.this, "Token refreshed",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(MainActivity.this, "Something went wrong",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
            );
        }
    }

    private void loadSubmission() {
        if (AuthenticationManager.get().checkAuthState() == AuthenticationState.READY) {
            disposables.add(RedditService
                    .getSubmission(AuthenticationManager.get().getRedditClient())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableSingleObserver<Submission>() {
                        @Override
                        public void onSuccess(Submission submission) {
                            Toast.makeText(MainActivity.this,
                                    "Submission loaded with title: " + submission.getTitle(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(MainActivity.this, "Something went wrong",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }));
        } else if (AuthenticationManager.get().checkAuthState() == AuthenticationState.NONE) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Token needs refresh", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        if (!AuthenticationManager.get().getRedditClient().hasActiveUserContext()) {
            Toast.makeText(MainActivity.this, "No need to logout of userless auth",
                    Toast.LENGTH_SHORT).show();
        } else {
            Credentials credentials = ((MyApplication) getApplicationContext())
                    .getInstalledAppCredentials();

            disposables.add(RedditService.logout(credentials)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableCompletableObserver() {
                        @Override
                        public void onComplete() {
                            Toast.makeText(MainActivity.this, "Deauthenticated!",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(MainActivity.this, "Something went wrong",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
            );
        }
    }
}
