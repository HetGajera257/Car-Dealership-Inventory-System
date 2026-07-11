import React, { useState, useEffect } from 'react';
import { 
  Car, Plus, Search, Trash2, Edit, LogOut, User, 
  ShoppingCart, Shield, DollarSign, AlertCircle, X, 
  RefreshCw, SlidersHorizontal, CheckCircle
} from 'lucide-react';
import { api } from './api';

const CATEGORIES = [
  "SEDAN", "SUV", "HATCHBACK", "COUPE", "CONVERTIBLE", "WAGON", 
  "PICKUP_TRUCK", "MINIVAN", "SPORTS_CAR", "LUXURY_CAR", 
  "ELECTRIC_VEHICLE", "HYBRID_VEHICLE", "CROSSOVER", 
  "OFF_ROAD_VEHICLE", "COMMERCIAL_VEHICLE", "VAN"
];

export default function App() {
  // Session & UI views
  const [currentUser, setCurrentUser] = useState(api.getCurrentUser());
  const [authView, setAuthView] = useState('login'); // 'login' or 'register'
  
  // Auth Form State
  const [authForm, setAuthForm] = useState({
    username: '',
    email: '',
    password: '',
    role: 'USER' // USER or ADMIN
  });

  // Vehicles list
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(false);

  // Search & Filter State
  const [filters, setFilters] = useState({
    make: '',
    model: '',
    category: '',
    minPrice: '',
    maxPrice: ''
  });

  // Modals state
  const [modalState, setModalState] = useState({
    type: null, // 'add', 'edit', 'restock', 'delete'
    data: null  // vehicle object details
  });

  // Modal forms
  const [vehicleForm, setVehicleForm] = useState({
    make: '',
    model: '',
    category: 'SEDAN',
    price: '',
    quantity: ''
  });
  const [restockQuantity, setRestockQuantity] = useState(1);

  // Notification State
  const [notification, setNotification] = useState(null);

  // Auto-close notification
  useEffect(() => {
    if (notification) {
      const timer = setTimeout(() => {
        setNotification(null);
      }, 4000);
      return () => clearTimeout(timer);
    }
  }, [notification]);

  const showToast = (type, message) => {
    setNotification({ type, message });
  };

  // Fetch all vehicles
  const loadVehicles = async () => {
    setLoading(true);
    try {
      const data = await api.getVehicles();
      setVehicles(data);
    } catch (err) {
      showToast('error', 'Failed to load vehicles: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  // Run on mount if user is logged in
  useEffect(() => {
    if (currentUser) {
      loadVehicles();
    }
  }, [currentUser]);

  // Auth actions
  const handleAuthSubmit = async (e) => {
    e.preventDefault();
    try {
      if (authView === 'login') {
        const data = await api.login(authForm.email || authForm.username, authForm.password);
        setCurrentUser(api.getCurrentUser());
        showToast('success', `Welcome back, ${data.username}!`);
      } else {
        await api.register(authForm.username, authForm.email, authForm.password, authForm.role);
        showToast('success', 'Registration successful! Please login.');
        setAuthView('login');
        setAuthForm({ username: '', email: '', password: '', role: 'USER' });
      }
    } catch (err) {
      showToast('error', err.message);
    }
  };

  const handleLogout = () => {
    api.logout();
    setCurrentUser(null);
    setVehicles([]);
    showToast('success', 'Logged out successfully');
  };

  // Search actions
  const handleSearchSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const data = await api.searchVehicles({
        make: filters.make,
        model: filters.model,
        category: filters.category,
        minPrice: filters.minPrice ? parseFloat(filters.minPrice) : null,
        maxPrice: filters.maxPrice ? parseFloat(filters.maxPrice) : null,
      });
      setVehicles(data);
      showToast('success', `Found ${data.length} vehicle(s)`);
    } catch (err) {
      showToast('error', 'Search failed: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleClearFilters = () => {
    setFilters({ make: '', model: '', category: '', minPrice: '', maxPrice: '' });
    loadVehicles();
  };

  // Purchase vehicle
  const handlePurchase = async (vehicleId) => {
    try {
      await api.purchaseVehicle(vehicleId);
      showToast('success', 'Vehicle purchased successfully!');
      loadVehicles(); // refresh list
    } catch (err) {
      showToast('error', 'Purchase failed: ' + err.message);
    }
  };

  // Add/Update vehicle submit
  const handleVehicleFormSubmit = async (e) => {
    e.preventDefault();
    try {
      const parsedData = {
        make: vehicleForm.make,
        model: vehicleForm.model,
        category: vehicleForm.category,
        price: parseFloat(vehicleForm.price),
        quantity: parseInt(vehicleForm.quantity, 10)
      };

      if (modalState.type === 'add') {
        await api.addVehicle(parsedData);
        showToast('success', 'Vehicle added successfully!');
      } else {
        await api.updateVehicle(modalState.data.id, parsedData);
        showToast('success', 'Vehicle details updated!');
      }
      closeModal();
      loadVehicles();
    } catch (err) {
      showToast('error', 'Operation failed: ' + err.message);
    }
  };

  // Restock vehicle submit
  const handleRestockSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.restockVehicle(modalState.data.id, restockQuantity);
      showToast('success', `Restocked ${restockQuantity} units!`);
      closeModal();
      loadVehicles();
    } catch (err) {
      showToast('error', 'Restock failed: ' + err.message);
    }
  };

  // Delete vehicle submit
  const handleDeleteSubmit = async () => {
    try {
      await api.deleteVehicle(modalState.data.id);
      showToast('success', 'Vehicle deleted successfully.');
      closeModal();
      loadVehicles();
    } catch (err) {
      showToast('error', 'Delete failed: ' + err.message);
    }
  };

  // Open modals
  const openModal = (type, data = null) => {
    setModalState({ type, data });
    if (type === 'edit' && data) {
      setVehicleForm({
        make: data.make,
        model: data.model,
        category: data.category,
        price: data.price.toString(),
        quantity: data.quantity.toString()
      });
    } else if (type === 'add') {
      setVehicleForm({
        make: '',
        model: '',
        category: 'SEDAN',
        price: '',
        quantity: '1'
      });
    } else if (type === 'restock') {
      setRestockQuantity(5);
    }
  };

  const closeModal = () => {
    setModalState({ type: null, data: null });
  };

  // Auth Screen Render
  if (!currentUser) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#0b0f19] px-4 sm:px-6 lg:px-8 relative overflow-hidden">
        {/* Glow Effects */}
        <div className="absolute top-[-20%] left-[-10%] w-[50%] h-[50%] bg-blue-900/20 rounded-full blur-[120px] pointer-events-none"></div>
        <div className="absolute bottom-[-20%] right-[-10%] w-[50%] h-[50%] bg-indigo-900/20 rounded-full blur-[120px] pointer-events-none"></div>

        <div className="max-w-md w-full space-y-8 bg-[#111827] p-8 rounded-2xl border border-slate-800 shadow-2xl relative z-10">
          <div className="text-center">
            <div className="mx-auto h-14 w-14 bg-gradient-to-tr from-blue-500 to-indigo-600 rounded-2xl flex items-center justify-center shadow-lg shadow-blue-500/25">
              <Car className="h-8 w-8 text-white" />
            </div>
            <h2 className="mt-6 text-3xl font-extrabold text-white tracking-tight">
              {authView === 'login' ? 'Welcome Back' : 'Create Account'}
            </h2>
            <p className="mt-2 text-sm text-slate-400">
              {authView === 'login' 
                ? 'Sign in to access our premium dealership inventory' 
                : 'Register as a User or Admin to explore/manage catalog'}
            </p>
          </div>

          <form className="mt-8 space-y-6" onSubmit={handleAuthSubmit}>
            <div className="space-y-4">
              {authView === 'register' && (
                <div>
                  <label htmlFor="username" className="block text-sm font-medium text-slate-300">Username</label>
                  <input
                    id="username"
                    name="username"
                    type="text"
                    required
                    value={authForm.username}
                    onChange={(e) => setAuthForm({ ...authForm, username: e.target.value })}
                    className="mt-1 block w-full px-4 py-3 bg-[#1f2937] border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
                    placeholder="john_doe"
                  />
                </div>
              )}

              <div>
                <label htmlFor="email" className="block text-sm font-medium text-slate-300">
                  {authView === 'register' ? 'Email Address' : 'Username or Email'}
                </label>
                <input
                  id="email"
                  name="email"
                  type={authView === 'register' ? 'email' : 'text'}
                  required
                  value={authForm.email}
                  onChange={(e) => setAuthForm({ ...authForm, email: e.target.value })}
                  className="mt-1 block w-full px-4 py-3 bg-[#1f2937] border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
                  placeholder={authView === 'register' ? 'name@example.com' : 'Enter username or email'}
                />
              </div>

              <div>
                <label htmlFor="password" className="block text-sm font-medium text-slate-300">Password</label>
                <input
                  id="password"
                  name="password"
                  type="password"
                  required
                  value={authForm.password}
                  onChange={(e) => setAuthForm({ ...authForm, password: e.target.value })}
                  className="mt-1 block w-full px-4 py-3 bg-[#1f2937] border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
                  placeholder="••••••••"
                />
              </div>

            </div>

            <div>
              <button
                type="submit"
                className="w-full py-3 px-4 bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-medium rounded-xl hover:from-blue-500 hover:to-indigo-500 shadow-lg shadow-blue-500/20 focus:outline-none transition active:scale-[0.98]"
              >
                {authView === 'login' ? 'Sign In' : 'Sign Up'}
              </button>
            </div>
          </form>

          <div className="text-center mt-6">
            <button
              onClick={() => {
                setAuthView(authView === 'login' ? 'register' : 'login');
                setAuthForm({ username: '', email: '', password: '', role: 'USER' });
              }}
              className="text-sm text-blue-400 hover:underline hover:text-blue-300 font-medium transition"
            >
              {authView === 'login' 
                ? "Don't have an account? Sign Up" 
                : 'Already have an account? Sign In'}
            </button>
          </div>
        </div>

        {/* Global Toast Notification inside auth view */}
        {notification && <Toast notification={notification} onClose={() => setNotification(null)} />}
      </div>
    );
  }

  // Dashboard Main View
  return (
    <div className="min-h-screen bg-[#0b0f19] flex flex-col font-sans">
      {/* Top Navbar */}
      <nav className="bg-[#111827] border-b border-slate-800 sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Logo */}
            <div className="flex items-center gap-3">
              {/* Custom car logo mark */}
              <div className="h-10 w-10 flex-shrink-0 drop-shadow-[0_0_8px_rgba(59,130,246,0.5)]">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" width="40" height="40">
                  <defs>
                    <radialGradient id="nb" cx="50%" cy="45%" r="50%">
                      <stop offset="0%" stopColor="#2563EB"/>
                      <stop offset="100%" stopColor="#0F1E4A"/>
                    </radialGradient>
                    <linearGradient id="nr" x1="0%" y1="0%" x2="100%" y2="100%">
                      <stop offset="0%" stopColor="#60A5FA"/>
                      <stop offset="100%" stopColor="#1E3A8A"/>
                    </linearGradient>
                    <filter id="ng">
                      <feGaussianBlur in="SourceGraphic" stdDeviation="1.2" result="blur"/>
                      <feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge>
                    </filter>
                  </defs>
                  <circle cx="32" cy="32" r="31" fill="url(#nb)" stroke="url(#nr)" strokeWidth="1.5"/>
                  <g filter="url(#ng)" opacity="0.8">
                    <line x1="5" y1="30" x2="14" y2="30" stroke="#93C5FD" strokeWidth="1.5" strokeLinecap="round"/>
                    <line x1="5" y1="34" x2="12" y2="34" stroke="#93C5FD" strokeWidth="1" strokeLinecap="round"/>
                    <line x1="5" y1="27" x2="11" y2="27" stroke="#93C5FD" strokeWidth="0.8" strokeLinecap="round"/>
                  </g>
                  <g filter="url(#ng)">
                    <path d="M 12 35 L 14 28 L 20 24 L 27 22 L 38 22 L 44 26 L 50 29 L 52 32 L 52 37 L 46 37 Q 45 41 41 41 Q 37 41 36 37 L 26 37 Q 25 41 21 41 Q 17 41 16 37 L 12 37 Z" fill="white" opacity="0.95"/>
                    <path d="M 27 24 L 22 33 L 37 33 L 38 24 Z" fill="#BFDBFE" opacity="0.85"/>
                    <path d="M 39 24 L 38.5 33 L 45 33 L 42 26 Z" fill="#BFDBFE" opacity="0.75"/>
                    <line x1="37" y1="24" x2="37" y2="36" stroke="#93C5FD" strokeWidth="0.7" opacity="0.6"/>
                    <circle cx="42" cy="37" r="4.5" fill="#0F1E4A" stroke="white" strokeWidth="1.5"/>
                    <circle cx="42" cy="37" r="2" fill="#93C5FD" opacity="0.7"/>
                    <circle cx="20" cy="37" r="4.5" fill="#0F1E4A" stroke="white" strokeWidth="1.5"/>
                    <circle cx="20" cy="37" r="2" fill="#93C5FD" opacity="0.7"/>
                    <ellipse cx="51" cy="30" rx="2" ry="1.5" fill="#FDE68A" opacity="0.9"/>
                  </g>
                  <ellipse cx="32" cy="43" rx="20" ry="2" fill="#0F1E4A" opacity="0.4"/>
                </svg>
              </div>
              <div className="flex flex-col leading-tight">
                <span className="font-extrabold text-xl tracking-tight bg-gradient-to-r from-blue-300 via-white to-blue-200 bg-clip-text text-transparent">
                  DriveSelect
                </span>
                <span className="text-[9px] tracking-[0.2em] text-blue-400/70 uppercase font-semibold">Car Inventory</span>
              </div>
            </div>

            {/* Profile & Controls */}
            <div className="flex items-center gap-4">
              <div className="hidden sm:flex flex-col items-end border-r border-slate-800 pr-4">
                <span className="text-sm font-semibold text-white">{currentUser.username}</span>
                <span className="text-xs text-slate-400 flex items-center gap-1 mt-0.5">
                  {currentUser.role === 'ADMIN' ? (
                    <span className="bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 px-2 py-0.5 rounded-full text-[10px] font-bold tracking-wider uppercase">
                      Admin Mode
                    </span>
                  ) : (
                    <span className="bg-blue-500/10 text-blue-400 border border-blue-500/20 px-2 py-0.5 rounded-full text-[10px] font-bold tracking-wider uppercase">
                      Customer
                    </span>
                  )}
                  {currentUser.email}
                </span>
              </div>
              
              <button
                onClick={handleLogout}
                className="p-2 text-slate-400 hover:text-red-400 bg-slate-800/40 rounded-xl hover:bg-red-500/10 border border-slate-700/50 hover:border-red-500/20 transition duration-200"
                title="Log Out"
              >
                <LogOut className="h-5 w-5" />
              </button>
            </div>
          </div>
        </div>
      </nav>

      {/* Hero & Main Content */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        
        {/* Banner Headers */}
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 bg-[#111827]/60 p-6 rounded-2xl border border-slate-800/60">
          <div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white">Car Dealership Inventory</h1>
            <p className="text-slate-400 mt-1 text-sm sm:text-base">Browse models, query details, purchase instantly, or organize logs.</p>
          </div>
          {currentUser.role === 'ADMIN' && (
            <button
              onClick={() => openModal('add')}
              className="self-start md:self-auto flex items-center gap-2 px-5 py-3 bg-gradient-to-r from-emerald-600 to-teal-600 text-white font-medium rounded-xl hover:from-emerald-500 hover:to-teal-500 shadow-lg shadow-emerald-500/10 hover:shadow-emerald-500/20 transition active:scale-[0.98]"
            >
              <Plus className="h-5 w-5" />
              Add New Vehicle
            </button>
          )}
        </div>

        {/* Filters and Search Panel */}
        <div className="bg-[#111827] border border-slate-800 p-6 rounded-2xl shadow-xl">
          <div className="flex items-center gap-2 mb-4 text-white">
            <SlidersHorizontal className="h-5 w-5 text-blue-500" />
            <h2 className="font-bold text-lg">Search & Filter Catalog</h2>
          </div>
          
          <form onSubmit={handleSearchSubmit} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
              {/* Make input */}
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1">Make</label>
                <input
                  type="text"
                  placeholder="e.g. Tesla"
                  value={filters.make}
                  onChange={(e) => setFilters({ ...filters, make: e.target.value })}
                  className="w-full px-3 py-2 bg-[#1f2937] border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
                />
              </div>

              {/* Model input */}
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1">Model</label>
                <input
                  type="text"
                  placeholder="e.g. Model Y"
                  value={filters.model}
                  onChange={(e) => setFilters({ ...filters, model: e.target.value })}
                  className="w-full px-3 py-2 bg-[#1f2937] border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
                />
              </div>

              {/* Category selector */}
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1">Category</label>
                <select
                  value={filters.category}
                  onChange={(e) => setFilters({ ...filters, category: e.target.value })}
                  className="w-full px-3 py-2 bg-[#1f2937] border border-slate-700 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
                >
                  <option value="">All Categories</option>
                  {CATEGORIES.map((cat) => (
                    <option key={cat} value={cat}>{cat.replace('_', ' ')}</option>
                  ))}
                </select>
              </div>

              {/* Min Price */}
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1">Min Price ($)</label>
                <input
                  type="number"
                  placeholder="Min"
                  value={filters.minPrice}
                  onChange={(e) => setFilters({ ...filters, minPrice: e.target.value })}
                  className="w-full px-3 py-2 bg-[#1f2937] border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
                />
              </div>

              {/* Max Price */}
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1">Max Price ($)</label>
                <input
                  type="number"
                  placeholder="Max"
                  value={filters.maxPrice}
                  onChange={(e) => setFilters({ ...filters, maxPrice: e.target.value })}
                  className="w-full px-3 py-2 bg-[#1f2937] border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
                />
              </div>
            </div>

            <div className="flex justify-end gap-3 pt-2">
              <button
                type="button"
                onClick={handleClearFilters}
                className="px-4 py-2 border border-slate-700 text-slate-300 rounded-xl hover:bg-slate-800 transition"
              >
                Clear
              </button>
              <button
                type="submit"
                className="flex items-center gap-2 px-5 py-2.5 bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-medium rounded-xl hover:from-blue-500 hover:to-indigo-500 transition active:scale-[0.98]"
              >
                <Search className="h-4 w-4" />
                Search Inventory
              </button>
            </div>
          </form>
        </div>

        {/* Vehicles Inventory */}
        {loading ? (
          <div className="flex flex-col items-center justify-center py-20 gap-3">
            <RefreshCw className="h-10 w-10 text-blue-500 animate-spin" />
            <p className="text-slate-400 text-sm">Querying system database...</p>
          </div>
        ) : vehicles.length === 0 ? (
          <div className="bg-[#111827]/40 border border-slate-800 p-12 text-center rounded-2xl">
            <Car className="h-12 w-12 text-slate-600 mx-auto mb-3" />
            <h3 className="font-bold text-lg text-white">No Vehicles Found</h3>
            <p className="text-slate-400 mt-1 text-sm">Try clearing filters or adding vehicles to your inventory.</p>
            <button
              onClick={handleClearFilters}
              className="mt-4 px-4 py-2 bg-slate-800 text-slate-200 border border-slate-700 rounded-xl hover:bg-slate-700 transition"
            >
              Reset Filters
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {vehicles.map((v) => (
              <VehicleCard
                key={v.id}
                vehicle={v}
                isAdmin={currentUser.role === 'ADMIN'}
                onPurchase={handlePurchase}
                onEdit={(v) => openModal('edit', v)}
                onRestock={(v) => openModal('restock', v)}
                onDelete={(v) => openModal('delete', v)}
              />
            ))}
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="bg-[#111827] border-t border-slate-800/80 py-6 mt-12">
        <div className="max-w-7xl mx-auto px-4 text-center text-xs text-slate-500">
          <p>© 2026 DriveSelect Dealership Inc. Pair programming with Antigravity AI.</p>
        </div>
      </footer>

      {/* Modals Container */}
      {modalState.type === 'add' && (
        <VehicleFormModal
          title="Add New Vehicle"
          form={vehicleForm}
          setForm={setVehicleForm}
          onSubmit={handleVehicleFormSubmit}
          onClose={closeModal}
        />
      )}
      {modalState.type === 'edit' && (
        <VehicleFormModal
          title="Update Vehicle Details"
          form={vehicleForm}
          setForm={setVehicleForm}
          onSubmit={handleVehicleFormSubmit}
          onClose={closeModal}
        />
      )}
      {modalState.type === 'restock' && (
        <RestockModal
          vehicle={modalState.data}
          quantity={restockQuantity}
          setQuantity={setRestockQuantity}
          onSubmit={handleRestockSubmit}
          onClose={closeModal}
        />
      )}
      {modalState.type === 'delete' && (
        <DeleteConfirmModal
          vehicle={modalState.data}
          onSubmit={handleDeleteSubmit}
          onClose={closeModal}
        />
      )}

      {/* Toast Notification */}
      {notification && <Toast notification={notification} onClose={() => setNotification(null)} />}
    </div>
  );
}

// Subcomponents definitions

function Toast({ notification, onClose }) {
  const isError = notification.type === 'error';
  return (
    <div className="fixed top-5 right-5 z-50 flex items-center gap-3 px-4 py-3.5 rounded-xl border shadow-2xl bg-[#1e293b] animate-in fade-in slide-in-from-top-4 duration-300 max-w-sm border-slate-700">
      {isError ? (
        <AlertCircle className="h-5 w-5 text-red-500 flex-shrink-0" />
      ) : (
        <CheckCircle className="h-5 w-5 text-emerald-500 flex-shrink-0" />
      )}
      <div className="text-sm font-medium text-slate-200 pr-4 break-words">
        {notification.message}
      </div>
      <button 
        onClick={onClose}
        className="p-1 text-slate-500 hover:text-slate-300 transition flex-shrink-0"
      >
        <X className="h-4 w-4" />
      </button>
    </div>
  );
}

function VehicleCard({ vehicle, isAdmin, onPurchase, onEdit, onRestock, onDelete }) {
  const isOutOfStock = vehicle.quantity <= 0;

  return (
    <div className="bg-[#111827] border border-slate-800 rounded-2xl flex flex-col justify-between overflow-hidden shadow-lg group hover:border-slate-700/80 transition duration-300">
      {/* Decorative Vehicle Header Card */}
      <div className="h-32 bg-gradient-to-br from-blue-900/40 to-indigo-950/60 p-4 flex flex-col justify-between relative overflow-hidden">
        {/* Glow */}
        <div className="absolute inset-0 bg-gradient-to-t from-[#111827] to-transparent"></div>
        <div className="z-10 bg-slate-800/80 backdrop-blur-md px-2.5 py-1 rounded-full text-[10px] font-bold text-blue-400 self-start tracking-wider uppercase border border-blue-500/10">
          {vehicle.category.replace('_', ' ')}
        </div>
        <div className="z-10 text-white font-black text-2xl tracking-wide uppercase opacity-10 select-none self-end">
          {vehicle.make}
        </div>
      </div>

      {/* Info Body */}
      <div className="p-6 space-y-4 flex-1">
        <div>
          <h3 className="font-extrabold text-xl text-white flex items-center gap-1 group-hover:text-blue-400 transition duration-200">
            {vehicle.make} <span className="text-slate-400 font-normal">{vehicle.model}</span>
          </h3>
        </div>

        {/* Pricing & Stock Details */}
        <div className="flex items-baseline justify-between">
          <span className="text-2xl font-black text-white">${vehicle.price.toLocaleString()}</span>
          <div>
            {isOutOfStock ? (
              <span className="bg-red-500/10 border border-red-500/20 text-red-400 text-xs px-2.5 py-1 rounded-full font-bold">
                Sold Out
              </span>
            ) : (
              <span className={`text-xs px-2.5 py-1 rounded-full font-bold ${
                vehicle.quantity < 3 
                  ? 'bg-amber-500/10 border border-amber-500/20 text-amber-400' 
                  : 'bg-emerald-500/10 border border-emerald-500/20 text-emerald-400'
              }`}>
                {vehicle.quantity} available
              </span>
            )}
          </div>
        </div>
      </div>

      {/* Actions footer */}
      <div className="px-6 pb-6 pt-2 border-t border-slate-800/60 flex flex-col gap-3">
        <button
          disabled={isOutOfStock}
          onClick={() => onPurchase(vehicle.id)}
          className={`w-full py-3 rounded-xl font-bold flex items-center justify-center gap-2 transition duration-200 ${
            isOutOfStock
              ? 'bg-slate-800/40 border border-slate-700/50 text-slate-500 cursor-not-allowed'
              : 'bg-gradient-to-r from-blue-600 to-indigo-600 text-white hover:from-blue-500 hover:to-indigo-500 active:scale-[0.98] shadow-lg shadow-blue-500/10'
          }`}
        >
          <ShoppingCart className="h-4.5 w-4.5" />
          {isOutOfStock ? 'Temporarily Out of Stock' : 'Purchase Vehicle'}
        </button>

        {/* Admin only subpanel */}
        {isAdmin && (
          <div className="grid grid-cols-3 gap-2 mt-1">
            <button
              onClick={() => onEdit(vehicle)}
              className="py-2.5 border border-slate-700 rounded-xl bg-slate-800/20 hover:bg-slate-700/40 text-slate-300 hover:text-white flex items-center justify-center gap-1.5 text-xs font-semibold transition"
            >
              <Edit className="h-3.5 w-3.5" />
              Edit
            </button>
            <button
              onClick={() => onRestock(vehicle)}
              className="py-2.5 border border-slate-700 rounded-xl bg-slate-800/20 hover:bg-slate-700/40 text-slate-300 hover:text-white flex items-center justify-center gap-1.5 text-xs font-semibold transition"
            >
              <Plus className="h-3.5 w-3.5" />
              Stock
            </button>
            <button
              onClick={() => onDelete(vehicle)}
              className="py-2.5 border border-slate-700 rounded-xl bg-slate-800/20 hover:bg-red-500/10 text-slate-300 hover:text-red-400 flex items-center justify-center gap-1.5 text-xs font-semibold transition"
            >
              <Trash2 className="h-3.5 w-3.5" />
              Delete
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

function VehicleFormModal({ title, form, setForm, onSubmit, onClose }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-[#111827] border border-slate-800 rounded-2xl w-full max-w-md p-6 relative shadow-2xl">
        <button 
          onClick={onClose} 
          className="absolute top-4 right-4 text-slate-400 hover:text-white p-1 hover:bg-slate-800 rounded-lg transition"
        >
          <X className="h-5 w-5" />
        </button>

        <h3 className="font-extrabold text-xl text-white mb-6 pr-6">{title}</h3>

        <form onSubmit={onSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-semibold text-slate-300 mb-1">Make</label>
            <input
              type="text"
              required
              value={form.make}
              onChange={(e) => setForm({ ...form, make: e.target.value })}
              className="w-full px-3.5 py-2.5 bg-[#1f2937] border border-slate-700 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              placeholder="e.g. Chevrolet"
            />
          </div>

          <div>
            <label className="block text-sm font-semibold text-slate-300 mb-1">Model</label>
            <input
              type="text"
              required
              value={form.model}
              onChange={(e) => setForm({ ...form, model: e.target.value })}
              className="w-full px-3.5 py-2.5 bg-[#1f2937] border border-slate-700 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              placeholder="e.g. Corvette Z06"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-semibold text-slate-300 mb-1">Category</label>
              <select
                value={form.category}
                onChange={(e) => setForm({ ...form, category: e.target.value })}
                className="w-full px-3.5 py-2.5 bg-[#1f2937] border border-slate-700 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              >
                {CATEGORIES.map((cat) => (
                  <option key={cat} value={cat}>{cat.replace('_', ' ')}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-semibold text-slate-300 mb-1">Price ($)</label>
              <input
                type="number"
                required
                min="0"
                step="0.01"
                value={form.price}
                onChange={(e) => setForm({ ...form, price: e.target.value })}
                className="w-full px-3.5 py-2.5 bg-[#1f2937] border border-slate-700 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
                placeholder="0.00"
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-semibold text-slate-300 mb-1">Initial Quantity</label>
            <input
              type="number"
              required
              min="0"
              value={form.quantity}
              onChange={(e) => setForm({ ...form, quantity: e.target.value })}
              className="w-full px-3.5 py-2.5 bg-[#1f2937] border border-slate-700 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              placeholder="1"
            />
          </div>

          <div className="flex gap-3 justify-end pt-4">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 border border-slate-700 text-slate-300 rounded-xl hover:bg-slate-800 transition text-sm font-semibold"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-5 py-2.5 bg-gradient-to-r from-emerald-600 to-teal-600 text-white rounded-xl hover:from-emerald-500 hover:to-teal-500 text-sm font-semibold transition active:scale-[0.98]"
            >
              Save Vehicle
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function RestockModal({ vehicle, quantity, setQuantity, onSubmit, onClose }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-[#111827] border border-slate-800 rounded-2xl w-full max-w-sm p-6 relative shadow-2xl">
        <button 
          onClick={onClose} 
          className="absolute top-4 right-4 text-slate-400 hover:text-white p-1 hover:bg-slate-800 rounded-lg transition"
        >
          <X className="h-5 w-5" />
        </button>

        <h3 className="font-extrabold text-xl text-white mb-3">Restock Inventory</h3>
        <p className="text-slate-400 text-sm mb-6">
          Increase supply for <strong className="text-white">{vehicle.make} {vehicle.model}</strong>. Current stock is {vehicle.quantity}.
        </p>

        <form onSubmit={onSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-semibold text-slate-300 mb-1">Restock Quantity</label>
            <input
              type="number"
              required
              min="1"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-[#1f2937] border border-slate-700 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              placeholder="e.g. 5"
            />
          </div>

          <div className="flex gap-3 justify-end pt-4">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 border border-slate-700 text-slate-300 rounded-xl hover:bg-slate-800 transition text-sm font-semibold"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-5 py-2.5 bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-xl hover:from-blue-500 hover:to-indigo-500 text-sm font-semibold transition active:scale-[0.98]"
            >
              Confirm Restock
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function DeleteConfirmModal({ vehicle, onSubmit, onClose }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-[#111827] border border-slate-800 rounded-2xl w-full max-w-sm p-6 relative shadow-2xl">
        <button 
          onClick={onClose} 
          className="absolute top-4 right-4 text-slate-400 hover:text-white p-1 hover:bg-slate-800 rounded-lg transition"
        >
          <X className="h-5 w-5" />
        </button>

        <div className="mx-auto h-12 w-12 bg-red-500/10 text-red-500 rounded-full flex items-center justify-center mb-4">
          <Trash2 className="h-6 w-6 animate-pulse" />
        </div>

        <h3 className="font-extrabold text-xl text-white text-center mb-2">Delete Vehicle?</h3>
        <p className="text-slate-400 text-sm text-center mb-6">
          Are you sure you want to delete <strong className="text-white">{vehicle.make} {vehicle.model}</strong> from catalog? This action cannot be undone.
        </p>

        <div className="grid grid-cols-2 gap-3">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2.5 border border-slate-700 text-slate-300 rounded-xl hover:bg-slate-800 transition text-sm font-semibold"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={onSubmit}
            className="px-4 py-2.5 bg-red-600 text-white rounded-xl hover:bg-red-500 text-sm font-semibold transition active:scale-[0.98]"
          >
            Confirm Delete
          </button>
        </div>
      </div>
    </div>
  );
}
