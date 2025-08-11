import MockAdapter from 'axios-mock-adapter';
import api, { createCart, addCartItem, checkout, createPaymentIntent, confirmPaymentIntent, refundPayment, dispatchDelivery } from './api';
import { useTenantStore } from './tenantStore';

function lastHeaders(mock: MockAdapter) {
  // @ts-expect-error private
  const calls = mock.history;
  const methods = ['post', 'patch', 'put', 'get'] as const;
  for (const m of methods) {
    if (calls[m] && calls[m].length > 0) {
      return calls[m][calls[m].length - 1].headers as Record<string, string>;
    }
  }
  return {} as Record<string, string>;
}

describe('api client', () => {
  const mock = new MockAdapter(api);
  beforeEach(() => {
    mock.reset();
    useTenantStore.getState().setTenantId('tenant-xyz');
  });

  it('sends X-Tenant-Id and Idempotency-Key on POST', async () => {
    mock.onPost('/v1/carts').reply(201, { id: 'c1', items: '/v1/carts/c1/items' });
    await createCart();
    const headers = lastHeaders(mock);
    expect(headers['X-Tenant-Id']).toBe('tenant-xyz');
    expect(headers['Idempotency-Key']).toBeTruthy();
  });

  it('sends X-Tenant-Id on GET', async () => {
    mock.onPost('/v1/carts').reply(201, { id: 'c1', items: '/v1/carts/c1/items' });
    await createCart();
    mock.onPost('/v1/carts/c1/items').reply(200, { cartId: 'c1', item: { sku: 's', qty: 1 } });
    await addCartItem('c1', { sku: 's', qty: 1 });
    const headers = lastHeaders(mock);
    expect(headers['X-Tenant-Id']).toBe('tenant-xyz');
  });

  it('covers payment and delivery calls', async () => {
    mock.onPost('/v1/carts/c1/checkout').reply(201, { id: 'o1' });
    await checkout('c1', {});

    mock.onPost('/v1/payment-intents').reply(201, { id: 'pi1', status: 'requires_confirmation' });
    await createPaymentIntent({ amount: 100, currency: 'BRL', method: 'card', orderId: 'o1' });

    mock.onPost('/v1/payment-intents/pi1/confirm').reply(200, { id: 'pi1', status: 'succeeded' });
    await confirmPaymentIntent('pi1');

    mock.onPost('/v1/refunds').reply(201, { id: 're1', status: 'succeeded' });
    await refundPayment({ payment_intent: 'pi1', amount: 50 });

    mock.onPost('/v1/deliveries/dispatch').reply(201, { id: 'd1', status: 'ASSIGNED', eta: 12 });
    await dispatchDelivery({ orderId: 'o1' });
  });
});
