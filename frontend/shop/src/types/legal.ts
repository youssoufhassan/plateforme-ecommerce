export interface LegalPageSummary {
  slug: string;
  title: string;
  version: number;
  updatedAt: string;
}

export interface LegalPage extends LegalPageSummary {
  content: string;
}
