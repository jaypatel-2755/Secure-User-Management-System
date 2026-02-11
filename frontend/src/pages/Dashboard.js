import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import API from "../api";
import "./dashboard.css";

function Dashboard() {
  const navigate = useNavigate();

  const [user, setUser] = useState(null);
  const [users, setUsers] = useState([]);
  const [activeTab, setActiveTab] = useState("profile");

  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [showProfileModal, setShowProfileModal] = useState(false);

  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [newName, setNewName] = useState("");

  // ✅ Load Profile
  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await API.get("/user/profile");
        setUser(res.data);
        setNewName(res.data.name);
      } catch (error) {
        localStorage.removeItem("token");
        navigate("/");
      }
    };

    fetchProfile();
  }, [navigate]);

  // ✅ Load All Users (Admin)
  const fetchUsers = async () => {
    try {
      const res = await API.get("/admin/users?page=0&size=100");
      setUsers(res.data.content);
      setActiveTab("users");
    } catch (error) {
      alert("Access Denied");
    }
  };

  // ✅ Admin Delete User
  const handleAdminDelete = async (id) => {
    if (!window.confirm("Delete this user?")) return;

    try {
      await API.delete(`/admin/delete/${id}`);
      fetchUsers();
    } catch (error) {
      alert("Delete failed");
    }
  };

  // ✅ Change Password
  const handleChangePassword = async (e) => {
    e.preventDefault();

    try {
      await API.put("/user/change-password", {
        oldPassword,
        newPassword,
      });

      alert("Password changed successfully");
      setShowPasswordModal(false);
      setOldPassword("");
      setNewPassword("");
    } catch (error) {
      alert("Password change failed");
    }
  };

  // ✅ Update Profile
  const handleUpdateProfile = async (e) => {
    e.preventDefault();

    try {
      const res = await API.put("/user/update-profile", {
        name: newName,
      });

      setUser(res.data);
      alert("Profile updated successfully");
      setShowProfileModal(false);
    } catch (error) {
      alert("Update failed");
    }
  };

  // ✅ Delete Own Account (USER only)
  const handleDeleteMyAccount = async () => {
    if (!window.confirm("Delete account permanently?")) return;

    try {
      await API.delete("/user/delete-account");
      localStorage.removeItem("token");
      navigate("/");
    } catch (error) {
      alert("Delete failed");
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/");
  };

  if (!user) return null;

  return (
    <div className="layout">

      {/* SIDEBAR */}
      <div className="sidebar">
        <h2 className="logo">UserPanel</h2>

        <button onClick={() => setActiveTab("profile")}>
          Profile
        </button>

        {user.role === "ADMIN" && (
          <button onClick={fetchUsers}>
            Manage Users
          </button>
        )}

        <button onClick={() => setShowPasswordModal(true)}>
          Change Password
        </button>

        {user.role === "USER" && (
          <button className="danger" onClick={handleDeleteMyAccount}>
            Delete Account
          </button>
        )}

        <button className="logout-btn" onClick={handleLogout}>
          Logout
        </button>
      </div>

      {/* CONTENT */}
      <div className="content">

        {activeTab === "profile" && (
          <div className="card">
            <h2>Welcome, {user.name} 👋</h2>
            <p><strong>Email:</strong> {user.email}</p>
            <span className="role-badge">{user.role}</span>

            <div style={{ marginTop: "20px" }}>
              <button onClick={() => setShowProfileModal(true)}>
                Edit Profile
              </button>
            </div>
          </div>
        )}

        {activeTab === "users" && user.role === "ADMIN" && (
          <div className="card">
            <h2>All Users</h2>

            <table className="modern-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td>{u.id}</td>
                    <td>{u.name}</td>
                    <td>{u.email}</td>
                    <td>{u.role}</td>
                    <td>
                      {u.email !== user.email && (
                        <button
                          className="delete-btn"
                          onClick={() => handleAdminDelete(u.id)}
                        >
                          Delete
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

      </div>

      {/* PROFILE MODAL */}
      {showProfileModal && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>Update Profile</h3>
            <form onSubmit={handleUpdateProfile}>
              <input
                type="text"
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                required
              />
              <button type="submit">Save</button>
              <button
                type="button"
                onClick={() => setShowProfileModal(false)}
              >
                Cancel
              </button>
            </form>
          </div>
        </div>
      )}

      {/* PASSWORD MODAL */}
      {showPasswordModal && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>Change Password</h3>
            <form onSubmit={handleChangePassword}>
              <input
                type="password"
                placeholder="Old Password"
                value={oldPassword}
                onChange={(e) => setOldPassword(e.target.value)}
                required
              />
              <input
                type="password"
                placeholder="New Password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                required
              />
              <button type="submit">Update</button>
              <button
                type="button"
                onClick={() => setShowPasswordModal(false)}
              >
                Cancel
              </button>
            </form>
          </div>
        </div>
      )}

    </div>
  );
}

export default Dashboard;
