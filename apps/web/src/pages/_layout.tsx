import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { TenantSelector } from '../shared/TenantSelector';

export function AppLayout() {
  return (
    <div>
      <header>
        <div className="container" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div>
            <strong>Marketplace</strong>
          </div>
          <nav>
            <NavLink to="/" end>Home</NavLink>
            <NavLink to="/orders">Orders</NavLink>
            <NavLink to="/payments">Payments</NavLink>
            <NavLink to="/delivery">Delivery</NavLink>
          </nav>
          <TenantSelector />
        </div>
      </header>
      <main>
        <div className="container">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
