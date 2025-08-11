import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { AppLayout } from './_layout';
import { CheckoutPage } from './checkout';
import * as api from '../shared/api';

vi.mock('../shared/api');

function renderOnCheckout(cartId = 'c123') {
  const router = createMemoryRouter([
    { path: '/', element: <AppLayout />, children: [ { path: 'checkout/:cartId', element: <CheckoutPage /> } ] }
  ], { initialEntries: [`/checkout/${cartId}`] });
  render(<RouterProvider router={router} />);
}

describe('CheckoutPage', () => {
  it('creates order then payment intent and confirm', async () => {
    (api.checkout as any).mockResolvedValue({ id: 'ord_1', cartId: 'c123', status: 'PLACED' });
    (api.createPaymentIntent as any).mockResolvedValue({ id: 'pi_1', status: 'requires_confirmation' });
    (api.confirmPaymentIntent as any).mockResolvedValue({ id: 'pi_1', status: 'succeeded' });

    renderOnCheckout();

    await userEvent.click(screen.getByRole('button', { name: /create order/i }));
    expect(await screen.findByText(/Order: ord_1/i)).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: /create payment intent/i }));
    expect(await screen.findByText(/Payment intent: pi_1/i)).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: /confirm payment/i }));
    expect(await screen.findByText(/Payment succeeded/i)).toBeInTheDocument();
  });
});
