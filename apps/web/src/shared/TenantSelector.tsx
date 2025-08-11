import React from 'react';
import { useTenantStore } from './tenantStore';

export function TenantSelector() {
  const { tenantId, setTenantId } = useTenantStore();
  return (
    <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
      <span>Tenant</span>
      <input
        value={tenantId}
        onChange={(e) => setTenantId(e.target.value)}
        placeholder="tenant-123"
        style={{ width: 140 }}
      />
    </div>
  );
}
