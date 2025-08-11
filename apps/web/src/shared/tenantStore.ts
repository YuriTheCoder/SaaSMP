import { create } from 'zustand';

interface TenantState {
  tenantId: string;
  setTenantId: (id: string) => void;
}

const defaultTenant = localStorage.getItem('tenantId') || 'demo-tenant';

export const useTenantStore = create<TenantState>((set) => ({
  tenantId: defaultTenant,
  setTenantId: (id) => {
    localStorage.setItem('tenantId', id);
    set({ tenantId: id });
  },
}));
