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
4. Copy the client ID and secret into the backend configuration:

   ```bash
   cp backend/src/main/resources/application.properties.example \
      backend/src/main/resources/application.properties
   ```

   then fill in `google.oauth.client-id` and `google.oauth.client-secret`.
   `application.properties` is git-ignored.

   You can also leave those two properties out and export the values instead, which is the better
   option for deployments:

   ```bash
   export GOOGLE_OAUTH_CLIENT_ID=your-client-id.apps.googleusercontent.com
   export GOOGLE_OAUTH_CLIENT_SECRET=your-client-secret
   ```

   A `credentials.json` downloaded from the console still works too: put it on the classpath at
   `backend/src/main/resources/credentials.json` and leave the two properties unset.

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
| `google.oauth.redirect-uri` | `GOOGLE_OAUTH_REDIRECT_URI` | `http://localhost:8080/auth/google/callback` |
| `google.oauth.success-redirect-uri` | `GOOGLE_OAUTH_SUCCESS_REDIRECT_URI` | `http://localhost:4200/` |
| `google.oauth.failure-redirect-uri` | `GOOGLE_OAUTH_FAILURE_REDIRECT_URI` | `http://localhost:4200/` |
| `google.oauth.token-store-directory` | `GOOGLE_OAUTH_TOKEN_STORE_DIRECTORY` | `tokens` |
| `app.cors.allowed-origins` | `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:4200` |

### Deploying

A production build assumes the app and the API are served from the same origin, which keeps the
session cookie same-site and avoids CORS altogether. That is why `apiUrl` is empty in
`frontend/src/environments/environment.prod.ts`.

If you do split them across different sites, the session cookie has to be allowed to travel
cross-site, and the deployed origins have to be listed:

```properties
server.servlet.session.cookie.same-site=none
server.servlet.session.cookie.secure=true
app.cors.allowed-origins=https://your-frontend.example.com
```

Remember to register the deployed callback URL on the OAuth client as well, and point
`google.oauth.redirect-uri` and the two frontend redirect URIs at the deployed hosts.

Because timetables and tokens are held per instance, run a single instance, or add sticky sessions
and a shared volume for `google.oauth.token-store-directory`.

## Tests

```bash
cd backend && ./mvnw test
cd frontend && npm test
```

Neither suite needs any external service.
