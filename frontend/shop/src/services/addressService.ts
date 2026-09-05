import api from "./api";

export interface Address {
  id: string;
  street: string;
  city: string;
  postalCode: string;
  country: string;
}

export async function fetchAddresses(): Promise<Address[]> {
  const response = await api.get<Address[]>("/addresses");
  return response.data;
}

export async function createAddress(data: {
  street: string;
  city: string;
  postalCode: string;
  country: string;
}): Promise<Address> {
  const response = await api.post<Address>("/addresses", data);
  return response.data;
}
