import React from 'react';
import ReactDOM from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { AppLayout } from './pages/_layout';
import { HomePage } from './pages/home';
import { CartPage } from './pages/cart';
import { CheckoutPage } from './pages/checkout';
import { OrdersPage } from './pages/orders';
import { PaymentsPage } from './pages/payments';
import { DeliveryPage } from './pages/delivery';
import './styles.css';

const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'cart/:cartId', element: <CartPage /> },
      { path: 'checkout/:cartId', element: <CheckoutPage /> },
      { path: 'orders', element: <OrdersPage /> },
      { path: 'payments', element: <PaymentsPage /> },
      { path: 'delivery', element: <DeliveryPage /> }
    ]
  }
]);

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>
);
