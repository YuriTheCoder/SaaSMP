import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { AppLayout } from './_layout';
import { PaymentsPage } from './payments';
import * as api from '../shared/api';

vi.mock('../shared/api');

describe('PaymentsPage', () => {
  it('refunds payment', async () => {
    (api.refundPayment as any).mockResolvedValue({ id: 're_1', status: 'succeeded' });
    const router = createMemoryRouter([
      { path: '/', element: <AppLayout />, children: [ { path: 'payments', element: <PaymentsPage /> } ] }
    ], { initialEntries: ['/payments'] });
    render(<RouterProvider router={router} />);

    await userEvent.type(screen.getByLabelText(/payment intent/i), 'pi_1');
    await userEvent.click(screen.getByRole('button', { name: /refund/i }));

    expect(await screen.findByText(/Refund re_1/i)).toBeInTheDocument();
  });
});
