import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { AppLayout } from './_layout';
import { DeliveryPage } from './delivery';
import * as api from '../shared/api';

vi.mock('../shared/api');

describe('DeliveryPage', () => {
  it('opens track url from backend', async () => {
    (api.getDeliveryTrack as any).mockResolvedValue({ id: 'd1', wsUrl: 'ws://localhost:8084/track/d1' });
    const open = window.open = vi.fn();
    const router = createMemoryRouter([
      { path: '/', element: <AppLayout />, children: [ { path: 'delivery', element: <DeliveryPage /> } ] }
    ], { initialEntries: ['/delivery'] });
    render(<RouterProvider router={router} />);

    await userEvent.type(screen.getByLabelText(/delivery id/i), 'd1');
    await userEvent.click(screen.getByRole('button', { name: /open tracking/i }));

    expect(open).toHaveBeenCalledWith('ws://localhost:8084/track/d1', '_blank');
  });
});
