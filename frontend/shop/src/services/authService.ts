import api from "./api";

export async function login(email: string, password: string): Promise<string> {
  const response = await api.post("/auth/login", { email, password });
  return response.data.token;
}

export async function register(
  email: string,
  password: string,
): Promise<string> {
  const response = await api.post("/auth/register", { email, password });
  return response.data.token;
}
export async function changePassword(
  currentPassword: string,
  newPassword: string,
): Promise<void> {
  await api.put("/auth/change-password", { currentPassword, newPassword });
}
