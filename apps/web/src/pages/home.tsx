import React, { useState } from 'react';
import { createCart } from '../shared/api';
import { useNavigate } from 'react-router-dom';

export function HomePage() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  async function onStartCart() {
    setLoading(true); setError(null);
    try {
      const cart = await createCart();
      navigate(`/cart/${cart.id}`);
    } catch (e: any) {
      setError(e?.response?.data?.detail || e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <h2>Welcome</h2>
      <p>Start a new cart and add items, then checkout and pay.</p>
      <button onClick={onStartCart} disabled={loading}>{loading ? 'Creating...' : 'Create Cart'}</button>
      {error && <p style={{ color: 'crimson' }}>{error}</p>}
    </div>
  );
}
