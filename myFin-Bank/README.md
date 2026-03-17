# MyFin Bank — Frontend (React + Vite)

This folder contains the frontend UI for the MyFin Bank capstone project. For full project setup and startup order (Eureka + services + UI), see the root README: `../README.md`.

## Tech

- React + Vite
- React Router
- Axios
- Bootstrap

## Project Structure

```text
myFin-Bank/
  public/                    Static assets
  src/
    components/              Shared UI components (Navbar, Footer)
    pages/                   Route pages (Customer + Admin)
    services/                API clients (Axios base URLs)
    styles/                  Page/component CSS
    utils/                   Small utilities (e.g., transaction ID generation)
    App.jsx                  Routes + protected route wrappers
    main.jsx                 App bootstrap
  index.html                 Vite HTML entry
  package.json               Scripts and dependencies
  vite.config.js             Vite configuration
```

### Routing

Routes are defined in `src/App.jsx`. There are two protected route wrappers:

- `ProtectedRoute`: customer authentication (checks `sessionStorage.token`)
- `AdminProtectedRoute`: admin authentication (checks `sessionStorage.adminToken` or `sessionStorage.token`)

Customer pages are under `src/pages/` (e.g., Dashboard, Transactions, Loans, Investments, Chat, Notifications). Admin pages are also under `src/pages/` with `Admin*` prefixes (e.g., AdminDashboard, AdminChat, AdminNotifications).

### Backend API Targets

Backend base URLs are configured in `src/services/api.js`:

- User service: `http://localhost:8082`
- Owner/Admin service: `http://localhost:8083`

If you change backend ports, update this file accordingly.

## Scripts

From `myFin-Bank/`:

```powershell
npm install
npm run dev
```

Other scripts:

```powershell
npm run lint
npm run build
npm run preview
```
