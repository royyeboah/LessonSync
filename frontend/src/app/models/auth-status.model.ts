export interface AuthStatus {
  authenticated: boolean;
  name: string | null;
  email: string | null;
  picture: string | null;
}
