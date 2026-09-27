import api from "@/services/http/api";
import type { Order } from "@/types/order";

export interface Shipment {
  carrier: string;
  trackingNumber: string;
  shippedAt: string;
}

export async function fetchShipment(orderId: string): Promise<Shipment> {
  const { data } = await api.get<Shipment>(`/orders/${orderId}/shipment`);
  return data;
}

/** La facture exige le jeton : on la récupère en binaire, puis on la propose au téléchargement. */
export async function downloadInvoice(
  orderId: string,
  orderNumber: string,
): Promise<void> {
  const response = await api.get(`/orders/${orderId}/invoice`, {
    responseType: "blob",
  });

  const url = URL.createObjectURL(response.data);
  const link = document.createElement("a");

  link.href = url;
  link.download = `facture-${orderNumber}.pdf`;
  document.body.appendChild(link);
  link.click();

  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

export async function updateProfile(payload: {
  firstName?: string;
  lastName?: string;
  phone?: string;
}): Promise<void> {
  await api.put("/me", payload);
}

export async function changePassword(
  currentPassword: string,
  newPassword: string,
): Promise<void> {
  await api.put("/auth/change-password", { currentPassword, newPassword });
}

export async function updateMarketingConsent(
  marketing: boolean,
): Promise<void> {
  await api.put("/me/consents", { marketing });
}

/** Export des données personnelles, proposé en téléchargement. */
export async function exportPersonalData(): Promise<void> {
  const { data } = await api.get("/me/export");

  const blob = new Blob([JSON.stringify(data, null, 2)], {
    type: "application/json",
  });

  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");

  link.href = url;
  link.download = "mes-donnees-shahin.json";
  document.body.appendChild(link);
  link.click();

  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

export async function deleteAccount(password: string): Promise<void> {
  await api.delete("/me", { data: { password } });
}

export type { Order };
