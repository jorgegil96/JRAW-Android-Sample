package com.gmail.jorgegilcavazos.androidjrawsample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.ApatheticTokenStore;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.RefreshTokenHandler;
import net.dean.jraw.auth.TokenStore;
import net.dean.jraw.auth.VolatileTokenStore;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthHelper;
import net.dean.jraw.models.Submission;

import java.net.URL;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String CLIENT_ID = "yourclientid";
    private static final String REDIRECT_URL = "http://localhost/authorize_callback";
    public static final int REQUEST_CODE = 1;

    private CompositeDisposable disposables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        disposables = new CompositeDisposable();

        RedditClient redditClient = new RedditClient(UserAgent.of("android",
                "com.gmail.jorgegilcavazos.androidjrawsample", "v0.0.1", "yourusername"));
        TokenStore store = new VolatileTokenStore();
        RefreshTokenHandler refreshTokenHandler = new RefreshTokenHandler(store, redditClient);

        AuthenticationManager manager = AuthenticationManager.get();
        manager.init(redditClient, refreshTokenHandler);

        doUserlessAuth();
        //doUserAuth();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Credentials credentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
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
        Credentials credentials = Credentials.userlessApp(CLIENT_ID, UUID.randomUUID());
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

    private void loadSubmission() {
        disposables.add(RedditService.getSubmission(
                    AuthenticationManager.get().getRedditClient(), "6qkmuw")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Submission>() {
                    @Override
                    public void onSuccess(Submission submission) {
                        Toast.makeText(
                                MainActivity.this,
                                "Submission loaded with title: " + submission.getTitle(),
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}
