import React, { useState } from 'react';
import { dispatchDelivery } from '../shared/api';

export function OrdersPage() {
  const [orderId, setOrderId] = useState('');
  const [result, setResult] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function onDispatch() {
    setResult(null); setError(null);
    try {
      const res = await dispatchDelivery({ orderId });
      setResult(`Delivery ${res.id} (${res.status}) ETA ${res.eta}m`);
    } catch (e: any) {
      setError(e?.response?.data?.detail || e.message);
    }
  }

  return (
    <div>
      <h2>Orders</h2>
      <div className="card">
        <label>Order ID</label>
        <input value={orderId} onChange={(e) => setOrderId(e.target.value)} placeholder="ord_xxx" />
        <div style={{ marginTop: 12 }}>
          <button onClick={onDispatch} disabled={!orderId}>Dispatch Delivery</button>
        </div>
      </div>
      {result && <p style={{ color: 'green' }}>{result}</p>}
      {error && <p style={{ color: 'crimson' }}>{error}</p>}
    </div>
  );
}
