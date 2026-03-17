import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Navbar from "./components/Navbar";
import Footer from "./components/Footer";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import Transactions from "./pages/Transactions";
import Investments from "./pages/Investments";
import LoanApplication from "./pages/LoanApplication";
import Chat from "./pages/Chat";
import Notifications from "./pages/Notifications";
import AdminLogin from "./pages/AdminLogin";
import AdminRegister from "./pages/AdminRegister";
import AdminDashboard from "./pages/AdminDashboard";
import AdminChat from "./pages/AdminChat";

// IMPORT THE NEW ADMIN NOTIFICATIONS PAGE
import AdminNotifications from "./pages/AdminNotifications"; 

// Protected Route Component for Customers
function ProtectedRoute({ children }) {
  const token = sessionStorage.getItem("token");
  return token ? children : <Navigate to="/login" />;
}

// Protected Route Component for Admin
function AdminProtectedRoute({ children }) {
  // Checks for adminToken OR standard token (depending on your login logic)
  const adminToken = sessionStorage.getItem("adminToken") || sessionStorage.getItem("token");
  return adminToken ? children : <Navigate to="/admin" />;
}

function App() {
  return (
    <BrowserRouter>
      <Navbar />
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        
        {/* Protected Customer Routes */}
        <Route 
          path="/dashboard" 
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/transactions" 
          element={
            <ProtectedRoute>
              <Transactions />
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/investments" 
          element={
            <ProtectedRoute>
              <Investments />
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/loans" 
          element={
            <ProtectedRoute>
              <LoanApplication />
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/chat" 
          element={
            <ProtectedRoute>
              <Chat />
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/notifications" 
          element={
            <ProtectedRoute>
              <Notifications />
            </ProtectedRoute>
          } 
        />

        {/* Admin Routes */}
        <Route path="/admin" element={<AdminLogin />} />
        <Route path="/admin/register" element={<AdminRegister />} />
        <Route 
          path="/admin/dashboard" 
          element={
            <AdminProtectedRoute>
              <AdminDashboard />
            </AdminProtectedRoute>
          } 
        />
        
        {/* ADD THE ADMIN NOTIFICATIONS ROUTE HERE */}
        <Route 
          path="/admin/notifications" 
          element={
            <AdminProtectedRoute>
              <AdminNotifications />
            </AdminProtectedRoute>
          } 
        />

        <Route 
          path="/admin/chat" 
          element={
            <AdminProtectedRoute>
              <AdminChat />
            </AdminProtectedRoute>
          } 
        />

        {/* Default Route */}
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
      <Footer />
    </BrowserRouter>
  );
}

export default App;