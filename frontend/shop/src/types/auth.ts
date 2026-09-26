export interface AuthResponse {
  token: string;
}

export interface UserProfile {
  email: string;
  firstName: string | null;
  lastName: string | null;
  phone: string | null;
  emailVerified: boolean;
  guest: boolean;
}
