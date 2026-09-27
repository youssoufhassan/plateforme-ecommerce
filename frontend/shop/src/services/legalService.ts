import api from "@/services/http/api";
import type { LegalPage, LegalPageSummary } from "@/types/legal";

export async function fetchLegalPages(): Promise<LegalPageSummary[]> {
  const { data } = await api.get<LegalPageSummary[]>("/legal");
  return data;
}

export async function fetchLegalPage(slug: string): Promise<LegalPage> {
  const { data } = await api.get<LegalPage>(`/legal/${slug}`);
  return data;
}
