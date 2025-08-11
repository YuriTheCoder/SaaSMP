import React, { useState } from 'react';
import { refundPayment } from '../shared/api';

export function PaymentsPage() {
  const [pi, setPi] = useState('');
  const [amount, setAmount] = useState(100);
  const [result, setResult] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function onRefund() {
    setResult(null); setError(null);
    try {
      const res = await refundPayment({ payment_intent: pi, amount });
      setResult(`Refund ${res.id} (${res.status})`);
    } catch (e: any) {
      setError(e?.response?.data?.detail || e.message);
    }
  }

  return (
    <div>
      <h2>Payments</h2>
      <div className="card">
        <div className="grid">
          <div>
            <label htmlFor="pi">Payment Intent</label>
            <input id="pi" value={pi} onChange={(e) => setPi(e.target.value)} placeholder="pi_xxx" />
          </div>
          <div>
            <label htmlFor="amount">Amount</label>
            <input id="amount" type="number" value={amount} onChange={(e) => setAmount(parseInt(e.target.value || '0', 10))} />
          </div>
        </div>
        <div style={{ marginTop: 12 }}>
          <button onClick={onRefund} disabled={!pi}>Refund</button>
        </div>
      </div>
      {result && <p style={{ color: 'green' }}>{result}</p>}
      {error && <p style={{ color: 'crimson' }}>{error}</p>}
    </div>
  );
}
