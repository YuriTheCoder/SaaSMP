import React, { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { addCartItem } from '../shared/api';

export function CartPage() {
  const { cartId } = useParams();
  const navigate = useNavigate();
  const [sku, setSku] = useState('sku-123');
  const [qty, setQty] = useState(1);
  const [items, setItems] = useState<{ sku: string; qty: number }[]>([]);
  const [error, setError] = useState<string | null>(null);

  async function onAdd() {
    if (!cartId) return;
    setError(null);
    try {
      await addCartItem(cartId, { sku, qty });
      setItems((arr) => [...arr, { sku, qty }]);
    } catch (e: any) {
      setError(e?.response?.data?.detail || e.message);
    }
  }

  function onCheckout() {
    if (!cartId) return;
    navigate(`/checkout/${cartId}`);
  }

  return (
    <div>
      <h2>Cart {cartId}</h2>
      <div className="card">
        <div className="grid">
          <div>
            <label>SKU</label>
            <input value={sku} onChange={(e) => setSku(e.target.value)} />
          </div>
          <div>
            <label>Quantity</label>
            <input type="number" value={qty} onChange={(e) => setQty(parseInt(e.target.value || '1', 10))} />
          </div>
        </div>
        <div style={{ marginTop: 12, display: 'flex', gap: 8 }}>
          <button onClick={onAdd}>Add Item</button>
          <button className="secondary" onClick={onCheckout}>Proceed to Checkout</button>
          <Link to="/">Back</Link>
        </div>
        {error && <p style={{ color: 'crimson' }}>{error}</p>}
      </div>

      <h3>Items</h3>
      {items.length === 0 ? <p>No items yet.</p> : (
        <ul>
          {items.map((it, idx) => <li key={idx}>{it.sku} x {it.qty}</li>)}
        </ul>
      )}
    </div>
  );
}
