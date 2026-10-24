package com.shop.util;

import jakarta.servlet.http.HttpSession;

/**
 * One-shot "flash" messages that survive a redirect (Post/Redirect/Get). CoreRequestFilter moves the
 * message from the session into the request on the next page view, so it is displayed exactly once.
 */
public final class Flash {
    public static final String KEY = "flash";
    private Flash() { }

    /** @param type a Bootstrap alert colour: success | danger | warning | info */
    public static void set(HttpSession session, String type, String message) {
        session.setAttribute(KEY, new String[] { type, message });
    }
}
