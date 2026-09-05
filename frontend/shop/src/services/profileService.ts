import api from "./api";

export interface Profile {
  email: string;
  firstName: string | null;
  lastName: string | null;
  phone: string | null;
}

export async function fetchProfile(): Promise<Profile> {
  const response = await api.get<Profile>("/me");
  return response.data;
}

export async function updateProfile(data: {
  firstName: string;
  lastName: string;
  phone: string;
}): Promise<Profile> {
  const response = await api.put<Profile>("/me", data);
  return response.data;
}
