import api from "@/services/http/api";
import type { AuthResponse, UserProfile } from "@/types/auth";

export async function login(
  email: string,
  password: string,
): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>("/auth/login", {
    email,
    password,
  });
  return data;
}

export async function register(
  email: string,
  password: string,
): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>("/auth/register", {
    email,
    password,
  });
  return data;
}

export async function fetchProfile(): Promise<UserProfile> {
  const { data } = await api.get<UserProfile>("/me");
  return data;
}

export async function verifyEmail(token: string): Promise<{ message: string }> {
  const { data } = await api.post("/auth/verify-email", { token });
  return data;
}

export async function resendVerification(): Promise<void> {
  await api.post("/auth/resend-verification");
}

export async function forgotPassword(
  email: string,
): Promise<{ message: string }> {
  const { data } = await api.post("/auth/forgot-password", { email });
  return data;
}

export async function resetPassword(
  token: string,
  newPassword: string,
): Promise<{ message: string }> {
  const { data } = await api.post("/auth/reset-password", {
    token,
    newPassword,
  });
  return data;
}

/** Commande invité : un code à 6 chiffres remplace le mot de passe. */
export async function requestGuestCode(
  email: string,
): Promise<{ message: string }> {
  const { data } = await api.post("/auth/guest/request-code", { email });
  return data;
}

export async function verifyGuestCode(
  email: string,
  code: string,
): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>("/auth/guest/verify-code", {
    email,
    code,
  });
  return data;
}

export async function setGuestPassword(
  password: string,
): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>("/auth/guest/set-password", {
    password,
  });
  return data;
}
