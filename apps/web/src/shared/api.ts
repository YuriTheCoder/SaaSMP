import axios from 'axios';
import { useTenantStore } from './tenantStore';

const api = axios.create({ baseURL: '' });

function generateIdempotencyKey(): string {
  // Use Web Crypto if available; otherwise fallback
  const g: any = globalThis as any;
  if (g.crypto && typeof g.crypto.randomUUID === 'function') {
    return g.crypto.randomUUID();
  }
  // RFC4122 v4-like fallback (not cryptographically strong)
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

api.interceptors.request.use((config) => {
  const tenantId = useTenantStore.getState().tenantId;
  config.headers = config.headers || {};
  config.headers['X-Tenant-Id'] = tenantId;
  const method = (config.method || '').toLowerCase();
  if (['post', 'patch', 'put'].includes(method)) {
    const idemp = generateIdempotencyKey();
    config.headers['Idempotency-Key'] = idemp;
  }
  return config;
});

export interface CartCreateResponse { id: string; items: string; }
export interface AddItemRequest { sku: string; qty: number; }

export async function createCart() {
  const res = await api.post<CartCreateResponse>('/v1/carts');
  return res.data;
}

export async function addCartItem(cartId: string, item: AddItemRequest) {
  const res = await api.post(`/v1/carts/${cartId}/items`, item);
  return res.data as { cartId: string; item: AddItemRequest };
}

export async function checkout(cartId: string, body: Record<string, unknown>) {
  const res = await api.post(`/v1/carts/${cartId}/checkout`, body);
  return res.data as { id: string; cartId: string; status: string };
}

export async function createPaymentIntent(body: { amount: number; currency: string; method: string; orderId: string; }) {
  const res = await api.post('/v1/payment-intents', body);
  return res.data as { id: string; status: string };
}

export async function confirmPaymentIntent(id: string) {
  const res = await api.post(`/v1/payment-intents/${id}/confirm`);
  return res.data as { id: string; status: string };
}

export async function refundPayment(body: { payment_intent: string; amount: number }) {
  const res = await api.post('/v1/refunds', body);
  return res.data as { id: string; status: string };
}

export async function dispatchDelivery(body: { orderId: string }) {
  const res = await api.post('/v1/deliveries/dispatch', body);
  return res.data as { id: string; status: string; eta: number };
}

export async function getDeliveryTrack(id: string) {
  const res = await api.get(`/v1/deliveries/${id}/track`);
  return res.data as { id: string; wsUrl: string };
}

export default api;
