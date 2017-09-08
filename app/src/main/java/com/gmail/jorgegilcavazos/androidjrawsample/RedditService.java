package com.gmail.jorgegilcavazos.androidjrawsample;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
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

    public static Single<Submission> getSubmission(final RedditClient redditClient) {
        return Single.create(new SingleOnSubscribe<Submission>() {
            @Override
            public void subscribe(SingleEmitter<Submission> e) throws Exception {
                try {
                    e.onSuccess(redditClient.getRandomSubmission());
                } catch (Exception ex) {
                    e.onError(ex);
                }
            }
        });
    }

    public static Completable userlessAuthentication(
            final RedditClient reddit,
            final Credentials credentials) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                try {
                    OAuthData oAuthData = reddit.getOAuthHelper().easyAuth(credentials);
                    reddit.authenticate(oAuthData);
                    e.onComplete();
                } catch (Exception ex) {
                    e.onError(ex);
                }
            }
        });
    }

    public static Completable userAuthentication(
            final RedditClient reddit,
            final Credentials credentials,
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
                    e.onError(ex);
                }
            }
        });
    }

    public static Completable refreshToken(final Credentials credentials) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                try {
                    AuthenticationManager.get().refreshAccessToken(credentials);
                    e.onComplete();
                } catch (Exception ex) {
                    e.onError(ex);
                }
            }
        });
    }

    public static Completable logout(final Credentials credentials) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                try {
                    AuthenticationManager.get().getRedditClient().getOAuthHelper()
                            .revokeAccessToken(credentials);
                    AuthenticationManager.get().getRedditClient().getOAuthHelper()
                            .revokeRefreshToken(credentials);
                    // Calling deauthenticate() isn't really necessary, since revokeAccessToken()
                    // already calls it.
                    // AuthenticationManager.get().getRedditClient().deauthenticate();

                    // As of JRAW 9.0.0, revoking the access/refresh token does not update the
                    // auth state to NONE (it instead remains as NEEDS_REFRESH), so to completely
                    // restart the session to a blank state you should re-instantiate the
                    // AuthenticationManager. See https://github.com/mattbdean/JRAW/issues/196

                    // AuthenticationManager.get().init(...., ....); uncomment this line.
                    e.onComplete();
                } catch (Exception ex) {
                    e.onError(ex);
                }
            }
        });
    }
}
