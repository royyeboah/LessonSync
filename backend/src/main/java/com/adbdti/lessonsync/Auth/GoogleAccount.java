package com.adbdti.lessonsync.Auth;

/**
 * The Google identity behind a connected session.
 *
 * @param userId the Google account subject id, used as the key of the stored credential
 * @param email  the account's email address, shown in the UI
 */
public record GoogleAccount(String userId, String email) {
}
