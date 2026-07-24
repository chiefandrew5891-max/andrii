/**
 * Beauty Planner – Firebase Cloud Functions
 *
 * This file contains the callable backend functions for the Beauty Planner app.
 *
 * DEPLOYMENT NOTES
 * ----------------
 * To deploy only the new syncMasterProfile function run:
 *   firebase deploy --only functions:syncMasterProfile
 *
 * Existing deployed functions (bootstrapUser, syncIdentity, verifySubscription,
 * getAccessStatus, checkAppUpdate) are NOT defined here because they live in a
 * separate deployment. Do NOT remove or conflict with those functions when
 * deploying this file to the same Firebase project.
 *
 * FIRESTORE COLLECTION: masters/{userId}
 * This is the dedicated cross-app master-profile document intended to be read
 * by the Beauty Planner Client Booker app. It is separate from the existing
 * user identity / access / subscription documents.
 */

'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');

if (admin.apps.length === 0) {
    admin.initializeApp();
}

// ---------------------------------------------------------------------------
// syncMasterProfile
// ---------------------------------------------------------------------------

/**
 * Callable function: syncMasterProfile
 *
 * Writes master profile data supplied by the app into masters/{userId}.
 *
 * Security:
 *   - The caller must be authenticated (context.auth must be present).
 *   - The provided userId must match the authenticated Firebase UID so that a
 *     user can only write their own profile document.
 *
 * Firestore document path: masters/{userId}
 *
 * Fields written:
 *   userId                  – string  (from auth UID)
 *   ownerName               – string  (trimmed)
 *   searchableOwnerName     – string  (lowercase, for future search use)
 *   profileDisplayCustomName – boolean
 *   profilePhone            – string  (trimmed)
 *   profilePhoneVisible     – boolean
 *   profileSpecialization   – string  (trimmed)
 *   searchableSpecialization – string (lowercase, for future search use)
 *   profileRating           – number  (clamped 0–5)
 *   profileAvatarUrl        – string
 *   profileAvatarBase64     – string  (cropped avatar; takes precedence over URL)
 *   clientInteractionsEnabled – boolean
 *   serviceTemplates        – array   (parsed from serviceTemplatesJson)
 *   createdAt               – number  (epoch ms, set only on first creation)
 *   updatedAt               – number  (epoch ms, updated on every sync)
 *
 * Avatar precedence rule:
 *   If profileAvatarBase64 is non-empty it is the primary avatar source.
 *   Otherwise profileAvatarUrl is used. Both fields are stored so the client
 *   app can apply this rule itself.
 */
exports.syncMasterProfile = functions.https.onCall(async (data, context) => {
    // 1. Require authentication
    if (!context.auth) {
        throw new functions.https.HttpsError(
            'unauthenticated',
            'Authentication is required to sync a master profile.'
        );
    }

    // 2. Validate userId
    const userId = safeString(data.userId);
    if (!userId) {
        throw new functions.https.HttpsError(
            'invalid-argument',
            'userId is required.'
        );
    }

    // 3. Security: a user can only write their own profile
    if (context.auth.uid !== userId) {
        throw new functions.https.HttpsError(
            'permission-denied',
            'You can only sync your own master profile.'
        );
    }

    // 4. Normalize and validate all incoming fields
    const ownerName = safeString(data.ownerName);
    const profileDisplayCustomName = normalizeBoolean(data.profileDisplayCustomName);
    const profilePhone = safeString(data.profilePhone);
    const profilePhoneVisible = normalizeBoolean(data.profilePhoneVisible);
    const profileSpecialization = safeString(data.profileSpecialization);
    const profileRating = normalizeRating(data.profileRating);
    const profileAvatarUrl = safeString(data.profileAvatarUrl);
    // profileAvatarBase64 may be large – only accept strings, default to empty
    const profileAvatarBase64 = (typeof data.profileAvatarBase64 === 'string')
        ? data.profileAvatarBase64
        : '';
    const clientInteractionsEnabled = normalizeBoolean(data.clientInteractionsEnabled);
    const serviceTemplates = parseServiceTemplates(data.serviceTemplatesJson);

    const db = admin.firestore();
    const masterRef = db.doc(`masters/${userId}`);
    const now = Date.now();

    const profilePayload = {
        userId,
        ownerName,
        searchableOwnerName: ownerName.toLowerCase(),
        profileDisplayCustomName,
        profilePhone,
        profilePhoneVisible,
        profileSpecialization,
        searchableSpecialization: profileSpecialization.toLowerCase(),
        profileRating,
        profileAvatarUrl,
        profileAvatarBase64,
        clientInteractionsEnabled,
        serviceTemplates,
        updatedAt: now,
    };

    // 5. Preserve createdAt on the first write; update the rest via merge
    const existingDoc = await masterRef.get();
    if (!existingDoc.exists) {
        // First creation: set createdAt and write the full document
        profilePayload.createdAt = now;
        await masterRef.set(profilePayload);
    } else {
        // Subsequent updates: merge without overwriting createdAt
        await masterRef.set(profilePayload, { merge: true });
    }

    return { success: true, updatedAt: now };
});

// ---------------------------------------------------------------------------
// Helper utilities
// ---------------------------------------------------------------------------

/**
 * Coerce a value to a trimmed string. Returns '' for null / undefined.
 */
function safeString(value) {
    if (value === null || value === undefined) return '';
    return value.toString().trim();
}

/**
 * Normalize a value that may arrive as a native boolean (Android) or as the
 * string "true"/"false" (iOS) to an actual boolean.
 */
function normalizeBoolean(value) {
    if (value === null || value === undefined) return false;
    if (typeof value === 'boolean') return value;
    return value.toString().toLowerCase() === 'true';
}

/**
 * Normalize a rating value to a float in [0, 5].
 * Falls back to the default rating (4.7) when the value is missing or invalid.
 */
function normalizeRating(value) {
    if (value === null || value === undefined || value === '') return 4.7;
    const n = parseFloat(value.toString());
    if (isNaN(n)) return 4.7;
    return Math.max(0, Math.min(5, n));
}

/**
 * Parse serviceTemplates from either a JSON string (the format sent by the
 * mobile app) or a plain array. Returns an empty array on any parse error.
 *
 * Each valid template must be an object with at least an `id` string field.
 */
function parseServiceTemplates(json) {
    if (!json) return [];
    if (Array.isArray(json)) {
        return sanitizeServiceTemplates(json);
    }
    try {
        const parsed = JSON.parse(json.toString());
        if (!Array.isArray(parsed)) return [];
        return sanitizeServiceTemplates(parsed);
    } catch (_) {
        return [];
    }
}

/**
 * Validate and sanitize an array of service template objects, keeping only
 * entries that have a non-empty string `id` field.
 */
function sanitizeServiceTemplates(arr) {
    return arr.filter(item =>
        item !== null &&
        typeof item === 'object' &&
        typeof item.id === 'string' &&
        item.id.trim().length > 0
    );
}
