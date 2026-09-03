# LessonSync

Upload a photo of your class schedule and have the classes added to your Google Calendar.

A Spring Boot backend reads the image with Vertex AI (Gemini), keeps the extracted lectures in your
session so you can correct them, then creates a dedicated calendar and a recurring event per class
in your own Google account. The frontend is an Angular app.

There is no database. A timetable only has to survive from the upload until its events are created,
so it lives in the HTTP session and is discarded afterwards.

## Requirements

- Java 17+
- Node 18+
- A Google Cloud project with the **Google Calendar API** and **Vertex AI API** enabled

## Setting up Google OAuth

The backend never holds an access token of yours. Each user signs in through Google, and the
refresh token that comes back is stored on disk and used to mint access tokens automatically.

1. In the [Google Cloud console](https://console.cloud.google.com/apis/credentials), go to
   **APIs & Services > Credentials** and create an **OAuth client ID** of type
   **Web application**.
2. Under **Authorized redirect URIs**, add exactly:

   ```
   http://localhost:8080/auth/google/callback
   ```

   This has to match `google.oauth.redirect-uri` character for character, including the scheme,
   port, and lack of a trailing slash.
3. On the **OAuth consent screen**, add the scopes `.../auth/calendar`, `openid`, `email`, and
   `profile`. While the app is in testing mode, add yourself under **Test users**.
4. Copy the client ID and secret into a local override (git-ignored):

   ```bash
   cp backend/src/main/resources/application.properties.example \
      backend/src/main/resources/application-local.properties
   ```

   then fill in `google.oauth.client-id` and `google.oauth.client-secret`.

   You can also leave those two properties out and export the values instead, which is the better
   option for deployments:

   ```bash
   export GOOGLE_OAUTH_CLIENT_ID=your-client-id.apps.googleusercontent.com
   export GOOGLE_OAUTH_CLIENT_SECRET=your-client-secret
   ```

   A `credentials.json` downloaded from the console still works too: put it on the classpath at
   `backend/src/main/resources/credentials.json` and leave the two properties unset. On Railway,
   paste that same file into the `GOOGLE_OAUTH_CREDENTIALS_JSON` variable instead.

Vertex AI authenticates separately, through Application Default Credentials:

```bash
gcloud auth application-default login
```

## Running it

```bash
# Backend, on http://localhost:8080
cd backend && ./mvnw spring-boot:run

# Frontend, on http://localhost:4200
cd frontend && npm install && npm start
```

Open <http://localhost:4200>, click **Connect Google Calendar**, and approve the consent screen.
You land back on the app with your account connected and can upload a schedule.

## How the sign-in works

| Endpoint | Purpose |
| --- | --- |
| `GET /auth/google/login` | Redirects the browser to Google's consent screen |
| `GET /auth/google/callback` | Handles the redirect from Google and stores the credential |
| `GET /auth/google/status` | Reports whether the session has a connected account |
| `POST /auth/google/logout` | Revokes the token at Google and forgets it |

`/auth/google/login` must be opened as a normal browser navigation rather than fetched with XHR, so
that the session cookie is in place when Google redirects back.

The session cookie only records which Google account you are; the tokens live in the
`google.oauth.token-store-directory` (`tokens/` by default, git-ignored), keyed by Google account
id. Because the refresh token is kept, restarting the server does not require consenting again, and
the client library refreshes expired access tokens on its own.

Calendar requests made without a connected account come back as `401` with
`{"error": "google_auth_required"}`. The frontend interceptor watches for that and returns you to
the landing page.

## Configuration reference

| Property | Environment variable | Default |
| --- | --- | --- |
| `google.oauth.client-id` | `GOOGLE_OAUTH_CLIENT_ID` | — |
| `google.oauth.client-secret` | `GOOGLE_OAUTH_CLIENT_SECRET` | — |
| `google.oauth.credentials-json` | `GOOGLE_OAUTH_CREDENTIALS_JSON` | — |
| `google.oauth.redirect-uri` | `GOOGLE_OAUTH_REDIRECT_URI` | `http://localhost:8080/auth/google/callback` |
| `google.oauth.success-redirect-uri` | `GOOGLE_OAUTH_SUCCESS_REDIRECT_URI` | `http://localhost:4200/` |
| `google.oauth.failure-redirect-uri` | `GOOGLE_OAUTH_FAILURE_REDIRECT_URI` | `http://localhost:4200/` |
| `google.oauth.token-store-directory` | `GOOGLE_OAUTH_TOKEN_STORE_DIRECTORY` | `tokens` |
| `app.cors.allowed-origins` | `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:4200` |
| `server.servlet.session.cookie.same-site` | `SESSION_COOKIE_SAME_SITE` | `lax` |
| `server.servlet.session.cookie.secure` | `SESSION_COOKIE_SECURE` | `false` |
| `vertex.project-id` | `VERTEX_PROJECT_ID` | `class-scheduler-429214` |
| `vertex.location` | `VERTEX_LOCATION` | `us-central1` |
| `vertex.model` | `VERTEX_MODEL` | `gemini-2.0-flash-exp` |

### Deploying to Vercel + Railway

The frontend is an Angular 19 app. Its production files land in `dist/lesson-sync/browser`, not
`dist/lesson-sync`. Pointing Vercel at the repo root (or at the parent `dist` folder) produces the
platform `404: NOT_FOUND` page. This repo now includes `vercel.json` files that set the output
directory and rewrite every route to `index.html`.

1. In the Vercel project, set **Root Directory** to `frontend` (or leave it at the repo root; the
   root `vercel.json` builds the frontend either way).
2. Redeploy. The home page should render instead of `NOT_FOUND`.
3. Add a Vercel environment variable named `API_URL` with your Railway origin, for example
   `https://lessonsync-production.up.railway.app` (no trailing slash). The production build writes
   that value into `environment.prod.ts` so the app calls Railway instead of the Vercel origin.

On Railway, set the service **Root Directory** to `backend` (recommended) or deploy from the repo
root. Either way Nixpacks builds the Spring Boot jar and `start.sh` launches it. Railway injects
`PORT`; the app already binds to `${PORT:8080}`.

Then set these Railway variables. None of them can live in git: `application.properties` only
contains placeholders, and `credentials.json` is git-ignored.

| Variable | Value |
| --- | --- |
| `GOOGLE_OAUTH_CLIENT_ID` | OAuth client ID from Google Cloud |
| `GOOGLE_OAUTH_CLIENT_SECRET` | OAuth client secret |
| `GOOGLE_OAUTH_REDIRECT_URI` | `https://<your-railway-host>/auth/google/callback` |
| `GOOGLE_OAUTH_SUCCESS_REDIRECT_URI` | `https://<your-vercel-host>/` |
| `GOOGLE_OAUTH_FAILURE_REDIRECT_URI` | `https://<your-vercel-host>/` |
| `APP_CORS_ALLOWED_ORIGINS` | `https://<your-vercel-host>` |
| `SESSION_COOKIE_SAME_SITE` | `none` |
| `SESSION_COOKIE_SECURE` | `true` |
| `GOOGLE_APPLICATION_CREDENTIALS_JSON` | Full JSON of a Vertex AI **service account** key |
| `VERTEX_PROJECT_ID` | GCP project id (if it is not `class-scheduler-429214`) |

`GOOGLE_APPLICATION_CREDENTIALS_JSON` is a service account key (**IAM > Service accounts > Keys**),
not the OAuth client JSON. Vertex AI does not run on Railway without it; the usual error is
`The Application Default Credentials are not available`.

Alternatively, paste the downloaded OAuth client JSON into `GOOGLE_OAUTH_CREDENTIALS_JSON` instead
of setting the client id and secret separately.

On the Google OAuth client, add the Railway callback under **Authorized redirect URIs**:

```
https://<your-railway-host>/auth/google/callback
```

and add the Vercel origin under **Authorized JavaScript origins**.

Because timetables and tokens are held per instance, run a single Railway replica, or add sticky
sessions and a shared volume for `google.oauth.token-store-directory`.

## Tests

```bash
cd backend && ./mvnw test
cd frontend && npm test
```

Neither suite needs any external service.
