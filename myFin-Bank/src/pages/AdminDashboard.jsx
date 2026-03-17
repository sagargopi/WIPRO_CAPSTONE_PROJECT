import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { adminAPI } from "../services/api";
import "../styles/AdminDashboard.css";

function AdminDashboard() {
  const [users, setUsers] = useState([]);
  const [admin, setAdmin] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [customerSearch, setCustomerSearch] = useState("");
  
  // Modal States
  const [showViewModal, setShowViewModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showLoanModal, setShowLoanModal] = useState(false);
  
  // Selection States
  const [selectedUser, setSelectedUser] = useState(null);
  const [selectedUserAccount, setSelectedUserAccount] = useState(null);
  const [editFormData, setEditFormData] = useState({ username: "", email: "", isActive: true });
  const [showEditPassword, setShowEditPassword] = useState(false);
  const [loanApprovals, setLoanApprovals] = useState([]);
  const [selectedLoan, setSelectedLoan] = useState(null);
  const [approvalForm, setApprovalForm] = useState({ status: "APPROVED", comments: "" });
  
  const navigate = useNavigate();

  const getUserActive = (user) => {
    if (!user) return true;
    if (typeof user.active === "boolean") return user.active;
    if (typeof user.isActive === "boolean") return user.isActive;
    return true;
  };

  // Helper to always fetch the freshest token
  const getAuthHeader = () => {
    const token = sessionStorage.getItem("adminToken") || sessionStorage.getItem("token");
    return { Authorization: `Bearer ${token}` };
  };

  useEffect(() => {
    const token = sessionStorage.getItem("adminToken") || sessionStorage.getItem("token");
    const adminData = sessionStorage.getItem("admin");

    if (!token) {
      navigate("/admin");
      return;
    }

    if (adminData) {
      setAdmin(JSON.parse(adminData));
    }

    fetchUsers();
    fetchLoanApprovals();
  }, [navigate]);

  async function fetchUsers() {
    try {
      const response = await adminAPI.get("/owner/users", {
        headers: getAuthHeader()
      });
      const normalizedUsers = (response.data || []).map((u) => ({
        ...u,
        active: getUserActive(u)
      }));
      setUsers(normalizedUsers);
      setLoading(false);
    } catch {
      setError("Access Denied: 403 Forbidden.");
      setLoading(false);
    }
  }

  async function fetchLoanApprovals() {
    try {
      const response = await adminAPI.get("/loan-approvals/pending", {
        headers: getAuthHeader()
      });
      setLoanApprovals(response.data || []);
    } catch (err) {
      console.error("Error fetching loan approvals:", err);
    }
  }

  async function fetchUserAccountDetails(userId) {
    try {
        const response = await adminAPI.get(`/owner/accounts/user/${userId}`, {
            headers: getAuthHeader()
        });
        if (typeof response.data === 'number') {
          setSelectedUserAccount({ balance: response.data, accountNumber: "Check DB" });
        } else {
          setSelectedUserAccount(response.data);
        }
    } catch {
        setSelectedUserAccount(null);
    }
  }

  const handleLogout = () => {
    sessionStorage.clear();
    navigate("/admin");
  };

  const filteredUsers = users.filter((user) => {
    const q = customerSearch.trim().toLowerCase();
    if (!q) return true;
    const username = (user.username || "").toLowerCase();
    return username.includes(q);
  });

  // --- Modal Handlers ---
  const handleViewUser = async (user) => {
    setSelectedUser(user);
    await fetchUserAccountDetails(user.id);
    setShowViewModal(true);
  };

  const handleEditUser = (user) => {
    setSelectedUser({ ...user, active: getUserActive(user) });
    setEditFormData({ 
      username: user.username, 
      password: "" // Keep password blank initially for security
    });
    setShowEditPassword(false);
    setShowEditModal(true);
  };

  const handleUpdateCustomerCredentials = async (e) => {
    e.preventDefault();
    if (!selectedUser) return;

    const updates = { username: editFormData.username };
    if (editFormData.password && editFormData.password.trim() !== "") {
      updates.password = editFormData.password;
    }

    try {
      await adminAPI.put(`/owner/users/${selectedUser.id}`, updates, {
        headers: getAuthHeader()
      });
      alert("Customer credentials updated successfully.");
      setShowEditModal(false);
      fetchUsers();
    } catch (err) {
      console.error(err);
      alert("Failed to update customer credentials.");
    }
  };

  const handleDeleteCustomer = async (userId) => {
    if (window.confirm("Are you sure you want to deactivate this customer? They will not be able to log in.")) {
      try {
        await adminAPI.delete(`/owner/users/${userId}`, {
          headers: getAuthHeader()
        });
        alert("Customer deactivated successfully.");
        if (selectedUser && selectedUser.id === userId) {
          setSelectedUser({ ...selectedUser, active: false, isActive: false });
        }
        setUsers((prev) =>
          prev.map((u) => (u.id === userId ? { ...u, active: false, isActive: false } : u))
        );
        setShowEditModal(false);
        fetchUsers();
      } catch (err) {
        console.error(err);
        alert("Failed to deactivate customer.");
      }
    }
  };

  const handleReactivateCustomer = async (userId) => {
    if (window.confirm("Reactivate this customer? They will be able to log in again.")) {
      try {
        await adminAPI.put(`/owner/users/${userId}`, { isActive: true }, {
          headers: getAuthHeader()
        });
        alert("Customer reactivated successfully.");
        if (selectedUser && selectedUser.id === userId) {
          setSelectedUser({ ...selectedUser, active: true, isActive: true });
        }
        setUsers((prev) =>
          prev.map((u) => (u.id === userId ? { ...u, active: true, isActive: true } : u))
        );
        setShowEditModal(false);
        fetchUsers();
      } catch (err) {
        console.error(err);
        alert("Failed to reactivate customer.");
      }
    }
  };

  const handleApproveLoan = async (e) => {
    e.preventDefault();
    if (!selectedLoan) return;

    const adminId = sessionStorage.getItem("ownerId") || 1;
    const endpoint = approvalForm.status === "APPROVED" 
      ? `/loan-approvals/${selectedLoan.id}/approve`
      : `/loan-approvals/${selectedLoan.id}/reject`;

    try {
      await adminAPI.post(endpoint, {}, {
        params: { adminId, comments: approvalForm.comments },
        headers: getAuthHeader()
      });

      alert(`Loan decision processed.`);
      fetchLoanApprovals();
      setShowLoanModal(false);
    } catch (err) {
      console.error(err);
      alert("Failed to process loan decision");
    }
  };

  if (loading) return <div className="loading">Verifying Admin Credentials...</div>;

  return (
    <div className="admin-container">
      <div className="admin-header">
        <h1>Admin Dashboard - MyFin Bank</h1>
        <button className="btn-logout" onClick={handleLogout}>Logout</button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      <div className="admin-content">
        <div className="admin-welcome">
          <h2>Welcome, {admin?.username || "Admin"}!</h2>
          <div className="admin-nav-links">
            <Link to="/admin/dashboard" className="nav-link active">🏦 Dashboard</Link>
            <Link to="/admin/notifications" className="nav-link">🔔 System Alerts</Link>
          </div>
        </div>

        {/* Users Table */}
        <div className="users-section">
          <div className="users-header-row">
            <h3>Registered Customers</h3>
            <input
              type="text"
              value={customerSearch}
              onChange={(e) => setCustomerSearch(e.target.value)}
              placeholder="Search customer by name..."
              className="users-search"
            />
          </div>
          <div className="table-responsive">
            <table className="users-table">
              <thead>
                <tr>
                  <th>ID</th><th>Username</th><th>Email</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.map(user => (
                  <tr key={user.id}>
                    <td>{user.id}</td>
                    <td>{user.username}</td>
                    <td>{user.email}</td>
                    <td>
                      <button className="btn-action btn-view" onClick={() => handleViewUser(user)}>View</button>
                      <button className="btn-action btn-edit" onClick={() => handleEditUser(user)}>Edit</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Loan Approvals Table */}
        <div className="loans-section">
          <h3>Pending Loan Approvals</h3>
          {loanApprovals.length === 0 ? <p className="no-data">No pending loans.</p> : (
            <div className="table-responsive">
              <table className="loans-table">
                <thead>
                  <tr>
                    <th>Loan ID</th><th>User ID</th><th>Amount</th><th>Status</th><th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {loanApprovals.map(loan => (
                    <tr key={loan.id}>
                      <td>{loan.loanId}</td>
                      <td>{loan.userId}</td>
                      <td>₹{loan.loanAmount}</td>
                      <td><span className="status-badge pending">{loan.approvalStatus}</span></td>
                      <td>
                        <button className="btn-action btn-approve" onClick={() => {setSelectedLoan(loan); setShowLoanModal(true);}}>Review</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* --- MODALS SECTION --- */}

      {showViewModal && selectedUser && (
        <div className="modal-overlay">
          <div className="modal-view-card"> 
            <div className="modal-view-header">
              <h2>Customer Profile</h2>
              <button className="close-x" onClick={() => setShowViewModal(false)}>&times;</button>
            </div>
            <div className="modal-view-body">
              <div className="user-avatar-large">👤</div>
              <div className="info-grid">
                <div className="info-row">
                  <span className="info-label">Username</span>
                  <span className="info-value">{selectedUser.username}</span>
                </div>
                <div className="info-row">
                  <span className="info-label">Account Number</span>
                  <span className="info-value">{selectedUserAccount?.accountNumber || "No Account Assigned"}</span>
                </div>
                <div className="info-row">
                  <span className="info-label">Wallet Balance</span>
                  <span className="info-value" style={{color: '#28a745', fontWeight: 'bold'}}>
                    ₹{selectedUserAccount?.balance?.toLocaleString() || "0.00"}
                  </span>
                </div>
              </div>
            </div>
            <div className="modal-view-footer">
              <button className="btn-modal-primary" onClick={() => setShowViewModal(false)}>Done</button>
            </div>
          </div>
        </div>
      )}

      {showEditModal && selectedUser && (
  <div className="modal-overlay">
    <div className="modal-view-card">
      <div className="modal-view-header">
        <h2>Manage Customer Access</h2>
        <button className="close-x" onClick={() => setShowEditModal(false)}>&times;</button>
      </div>
      <form onSubmit={handleUpdateCustomerCredentials}>
        <div className="modal-view-body">
          <div style={{ marginBottom: "12px", color: "#666", fontSize: "0.9rem" }}>
            Updating credentials changes login access. Deactivation blocks login but keeps records for audit.
          </div>
          <div style={{ marginBottom: "12px", fontSize: "0.95rem" }}>
            Status:{" "}
            <span style={{ fontWeight: 700, color: getUserActive(selectedUser) === false ? "#dc3545" : "#28a745" }}>
              {getUserActive(selectedUser) === false ? "DEACTIVATED" : "ACTIVE"}
            </span>
          </div>
          <div className="edit-form-container">
            <div className="form-group-custom">
              <label>New Username</label>
              <input 
                type="text" 
                className="form-input-custom" 
                value={editFormData.username} 
                onChange={(e) => setEditFormData({...editFormData, username: e.target.value})}
              />
            </div>
            <div className="form-group-custom">
              <label>New Password (Leave blank to keep current)</label>
              <div style={{ position: "relative" }}>
                <input 
                  type={showEditPassword ? "text" : "password"} 
                  className="form-input-custom" 
                  value={editFormData.password} 
                  onChange={(e) => setEditFormData({...editFormData, password: e.target.value})}
                  placeholder="Enter new password..."
                  style={{ paddingRight: "44px" }}
                />
                <button
                  type="button"
                  onClick={() => setShowEditPassword((v) => !v)}
                  aria-label={showEditPassword ? "Hide password" : "Show password"}
                  style={{
                    position: "absolute",
                    right: "10px",
                    top: "50%",
                    transform: "translateY(-50%)",
                    background: "transparent",
                    border: "none",
                    cursor: "pointer",
                    padding: 0,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    color: "#555"
                  }}
                >
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                    <path
                      d="M2 12C4.8 6.5 8.7 4 12 4s7.2 2.5 10 8c-2.8 5.5-6.7 8-10 8s-7.2-2.5-10-8Z"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinejoin="round"
                    />
                    <path
                      d="M12 15.5A3.5 3.5 0 1 0 12 8.5a3.5 3.5 0 0 0 0 7Z"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinejoin="round"
                    />
                    {showEditPassword && (
                      <path
                        d="M4 4l16 16"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                      />
                    )}
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>
        <div className="modal-view-footer" style={{ display: 'flex', justifyContent: 'space-between' }}>
          {getUserActive(selectedUser) === false ? (
            <button 
              type="button" 
              className="btn-delete-customer" 
              onClick={() => handleReactivateCustomer(selectedUser.id)}
              style={{ backgroundColor: '#28a745', color: 'white', border: 'none', padding: '10px 15px', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
            >
              Reactivate Customer
            </button>
          ) : (
            <button 
              type="button" 
              className="btn-delete-customer" 
              onClick={() => handleDeleteCustomer(selectedUser.id)}
              style={{ backgroundColor: '#dc3545', color: 'white', border: 'none', padding: '10px 15px', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
            >
              Deactivate Customer
            </button>
          )}
          
          <div>
            <button type="button" className="btn-cancel-custom" onClick={() => setShowEditModal(false)}>Cancel</button>
            <button type="submit" className="btn-modal-primary">Update Credentials</button>
          </div>
        </div>
      </form>
    </div>
  </div>
)}

      {showLoanModal && selectedLoan && (
        <div className="modal-overlay">
          <div className="modal-view-card">
            <div className="modal-view-header">
              <h2>Loan Approval Review</h2>
              <button className="close-x" onClick={() => setShowLoanModal(false)}>&times;</button>
            </div>
            <form onSubmit={handleApproveLoan}>
              <div className="modal-view-body">
                <div className="info-grid" style={{marginBottom: '25px'}}>
                  <div className="info-row">
                    <span className="info-label">Loan ID</span>
                    <span className="info-value">#{selectedLoan.loanId}</span>
                  </div>
                  <div className="info-row">
                    <span className="info-label">Requested Amount</span>
                    <span className="info-value" style={{color: '#28a745'}}>₹{selectedLoan.loanAmount}</span>
                  </div>
                </div>
                <div className="form-group-custom">
                  <label>Final Decision</label>
                  <select 
                    className="form-input-custom"
                    value={approvalForm.status} 
                    onChange={(e) => setApprovalForm({...approvalForm, status: e.target.value})}
                  >
                    <option value="APPROVED">APPROVE LOAN</option>
                    <option value="REJECTED">REJECT LOAN</option>
                  </select>
                </div>
                <div className="form-group-custom">
                  <label>Admin Comments / Remarks</label>
                  <textarea 
                    className="form-input-custom"
                    style={{minHeight: '100px', resize: 'vertical'}}
                    value={approvalForm.comments} 
                    onChange={(e) => setApprovalForm({...approvalForm, comments: e.target.value})}
                    placeholder="Provide a reason for this decision..."
                  ></textarea>
                </div>
              </div>
              <div className="modal-view-footer">
                <button type="button" className="btn-cancel-custom" onClick={() => setShowLoanModal(false)}>Back</button>
                <button type="submit" className="btn-modal-primary" style={{background: approvalForm.status === 'REJECTED' ? '#dc3545' : '#673ab7'}}>
                  Submit Decision
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminDashboard;
