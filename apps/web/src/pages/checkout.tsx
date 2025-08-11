import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import { checkout, createPaymentIntent, confirmPaymentIntent } from '../shared/api';

export function CheckoutPage() {
  const { cartId } = useParams();
  const [amount, setAmount] = useState(1000);
  const [currency, setCurrency] = useState('BRL');
  const [method, setMethod] = useState('card');
  const [orderId, setOrderId] = useState<string | null>(null);
  const [piId, setPiId] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function onCheckout() {
    if (!cartId) return;
    setMessage(null); setError(null);
    try {
      const order = await checkout(cartId, { email: 'buyer@example.com' });
      setOrderId(order.id);
      setMessage(`Order created: ${order.id}`);
    } catch (e: any) {
      setError(e?.response?.data?.detail || e.message);
    }
  }

  async function onCreatePI() {
    if (!orderId) return;
    setMessage(null); setError(null);
    try {
      const pi = await createPaymentIntent({ amount, currency, method, orderId });
      setPiId(pi.id);
      setMessage(`Payment intent: ${pi.id} (${pi.status})`);
    } catch (e: any) {
      setError(e?.response?.data?.detail || e.message);
    }
  }

  async function onConfirm() {
    if (!piId) return;
    setMessage(null); setError(null);
    try {
      const res = await confirmPaymentIntent(piId);
      setMessage(`Payment ${res.status}`);
    } catch (e: any) {
      setError(e?.response?.data?.detail || e.message);
    }
  }

  return (
    <div>
      <h2>Checkout for cart {cartId}</h2>

      <div className="card">
        <button onClick={onCheckout} disabled={!cartId}>Create Order</button>
        {orderId && <p>Order: {orderId}</p>}
      </div>

      <div className="card">
        <div className="grid">
          <div>
            <label>Amount (cents)</label>
            <input type="number" value={amount} onChange={(e) => setAmount(parseInt(e.target.value || '0', 10))} />
          </div>
          <div>
            <label>Currency</label>
            <input value={currency} onChange={(e) => setCurrency(e.target.value)} />
          </div>
          <div>
            <label>Method</label>
            <select value={method} onChange={(e) => setMethod(e.target.value)}>
              <option value="card">Card</option>
              <option value="pix">PIX</option>
              <option value="boleto">Boleto</option>
            </select>
          </div>
        </div>
        <div style={{ marginTop: 12, display: 'flex', gap: 8 }}>
          <button onClick={onCreatePI} disabled={!orderId}>Create Payment Intent</button>
          <button onClick={onConfirm} disabled={!piId}>Confirm Payment</button>
        </div>
      </div>

      {message && <p style={{ color: 'green' }}>{message}</p>}
      {error && <p style={{ color: 'crimson' }}>{error}</p>}
    </div>
  );
}
