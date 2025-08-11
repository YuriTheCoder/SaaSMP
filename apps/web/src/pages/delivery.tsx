import React, { useState } from 'react';
import { getDeliveryTrack } from '../shared/api';

export function DeliveryPage() {
  const [deliveryId, setDeliveryId] = useState('');

  async function onTrack() {
    if (!deliveryId) return;
    const info = await getDeliveryTrack(deliveryId);
    window.open(info.wsUrl, '_blank');
  }

  return (
    <div>
      <h2>Delivery</h2>
      <div className="card">
        <label htmlFor="deliveryId">Delivery ID</label>
        <input id="deliveryId" value={deliveryId} onChange={(e) => setDeliveryId(e.target.value)} placeholder="del_xxx" />
        <div style={{ marginTop: 12 }}>
          <button onClick={onTrack} disabled={!deliveryId}>Open tracking websocket URL</button>
        </div>
      </div>
    </div>
  );
}
