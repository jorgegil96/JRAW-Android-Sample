package com.gmail.jorgegilcavazos.androidjrawsample;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.http.SubmissionRequest;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthHelper;
import net.dean.jraw.models.Submission;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

public class RedditService {

    public static Single<Submission> getSubmission(
            final RedditClient redditClient,
            final String submissionId) {
        return Single.create(new SingleOnSubscribe<Submission>() {
            @Override
            public void subscribe(SingleEmitter<Submission> e) throws Exception {
                SubmissionRequest.Builder builder = new SubmissionRequest.Builder(submissionId);

                SubmissionRequest submissionRequest = builder.build();

                try {
                    e.onSuccess(redditClient.getSubmission(submissionRequest));
                } catch (Exception ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                    }
                }
            }
        });
    }

    public static Completable userlessAuthentication(final RedditClient reddit,
                                              final Credentials credentials) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                try {
                    OAuthData oAuthData = reddit.getOAuthHelper().easyAuth(credentials);
                    reddit.authenticate(oAuthData);
                    e.onComplete();
                } catch (Exception ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                    }
                }
            }
        });
    }

    public static Completable userAuthentication(final RedditClient reddit, final Credentials credentials,
                                          final String url) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                OAuthHelper oAuthHelper = reddit.getOAuthHelper();

                try {
                    OAuthData oAuthData = oAuthHelper.onUserChallenge(url, credentials);
                    reddit.authenticate(oAuthData);
                    AuthenticationManager.get().onAuthenticated(oAuthData);
                    e.onComplete();
                } catch (Exception ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                    }
                }
            }
        });
    }

    public static Completable refreshToken(final RedditClient reddit, final Credentials credentials,
                                          final String refreshToken) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                OAuthHelper helper = reddit.getOAuthHelper();
                helper.setRefreshToken(refreshToken);

                try {
                    OAuthData oAuthData = helper.refreshToken(credentials);
                    reddit.authenticate(oAuthData);
                    e.onComplete();
                } catch (Exception ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                    }
                }
            }
        });
    }

    public static Completable deAuthenticate(final RedditClient reddit, final Credentials credentials) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                OAuthHelper helper = reddit.getOAuthHelper();
                try {
                    helper.revokeAccessToken(credentials);
                    reddit.deauthenticate();
                    e.onComplete();
                } catch (Exception ex) {
                    if (!e.isDisposed()) {
                        e.onError(ex);
                    }
                }
            }
        });
    }
}
