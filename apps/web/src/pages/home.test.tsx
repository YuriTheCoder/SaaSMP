import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { AppLayout } from './_layout';
import { HomePage } from './home';
import { CartPage } from './cart';

function renderWithRoutes() {
  const router = createMemoryRouter([
    {
      path: '/',
      element: <AppLayout />,
      children: [
        { index: true, element: <HomePage /> },
        { path: 'cart/:cartId', element: <CartPage /> }
      ]
    }
  ], { initialEntries: ['/'] });
  render(<RouterProvider router={router} />);
  return router;
}

describe('HomePage', () => {
  it('renders and allows navigation mock', async () => {
    renderWithRoutes();
    expect(screen.getByText('Welcome')).toBeInTheDocument();
    // We do not hit network here; page-level e2e would with Playwright.
  });
});
