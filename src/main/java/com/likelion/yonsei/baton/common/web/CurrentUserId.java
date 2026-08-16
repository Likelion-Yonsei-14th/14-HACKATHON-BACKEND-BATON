package com.likelion.yonsei.baton.common.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Resolves the authenticated user's id from an {@code Authorization: Bearer <api key>} header.
 *
 * <p>The API spec calls for "Bearer Token 또는 Session Cookie" auth but defines no login/token
 * endpoint beyond {@code POST /users} signup, which now returns a one-time opaque API key. Only
 * its SHA-256 hash is stored server-side, so this is a real (if minimal) bearer-token scheme, not
 * a stand-in — swap it out only if a session/JWT-issuing login flow is designed later.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUserId {
}
