import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { AppLayout } from './_layout';
import { CartPage } from './cart';
import * as api from '../shared/api';

vi.mock('../shared/api');

function renderOnCart(cartId = 'c123') {
  const router = createMemoryRouter([
    { path: '/', element: <AppLayout />, children: [ { path: 'cart/:cartId', element: <CartPage /> } ] }
  ], { initialEntries: [`/cart/${cartId}`] });
  render(<RouterProvider router={router} />);
}

describe('CartPage', () => {
  it('adds an item and shows in list', async () => {
    (api.addCartItem as any).mockResolvedValue({ cartId: 'c123', item: { sku: 'sku-123', qty: 1 } });
    renderOnCart('c123');
    await userEvent.click(screen.getByRole('button', { name: /add item/i }));
    expect(await screen.findByText(/sku-123 x 1/i)).toBeInTheDocument();
  });
});
