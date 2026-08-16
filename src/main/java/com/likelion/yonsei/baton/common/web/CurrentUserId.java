package com.likelion.yonsei.baton.common.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Resolves the authenticated user's id from the {@code X-User-Id} request header.
 *
 * <p>The API spec calls for "Bearer Token 또는 Session Cookie" auth, but no login/token-issuing
 * endpoint is defined anywhere in the spec (only {@code POST /users} signup). Until a real auth
 * flow is designed, callers authenticate by passing the id returned from signup directly in this
 * header. Swap this resolver's implementation out once real session/JWT issuance exists.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUserId {
}
