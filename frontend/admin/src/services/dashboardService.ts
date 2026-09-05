import api from "./api";

export interface DashboardStats {
  totalOrders: number;
  totalCustomers: number;
  totalProducts: number;
  totalRevenue: number;
  pendingOrders: number;
}

export const dashboardService = {
  getStats: () =>
    api.get<DashboardStats>("/admin/dashboard/stats").then((r) => r.data),
};
