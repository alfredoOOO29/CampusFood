/**
 * CampusFood - Client-side Cart & UI Microinteractions
 */
const CampusCart = {
  KEY: 'campusfood_cart_items',

  getItems() {
    try {
      const items = localStorage.getItem(this.KEY);
      return items ? JSON.parse(items) : [];
    } catch (e) {
      console.error('Error reading cart from localStorage', e);
      return [];
    }
  },

  saveItems(items) {
    try {
      localStorage.setItem(this.KEY, JSON.stringify(items));
      this.updateBadge();
    } catch (e) {
      console.error('Error saving cart to localStorage', e);
    }
  },

  addItem(product) {
    const items = this.getItems();
    const existingIndex = items.findIndex(item => String(item.id) === String(product.id));

    const qtyToAdd = parseInt(product.quantity, 10) || 1;

    if (existingIndex > -1) {
      items[existingIndex].quantity = (parseInt(items[existingIndex].quantity, 10) || 1) + qtyToAdd;
    } else {
      items.push({
        id: product.id,
        nombre: product.nombre,
        precio: parseFloat(product.precio) || 0,
        imagenUrl: product.imagenUrl || '',
        vendedor: product.vendedor || 'Estudiante',
        ubicacion: product.ubicacion || '',
        quantity: qtyToAdd
      });
    }

    this.saveItems(items);
    this.showToast(`¡"${product.nombre}" agregado al carrito!`);
    return items;
  },

  updateQuantity(productId, newQty) {
    let items = this.getItems();
    const qty = parseInt(newQty, 10);
    if (qty <= 0) {
      this.removeItem(productId);
      return;
    }
    items = items.map(item => {
      if (String(item.id) === String(productId)) {
        item.quantity = qty;
      }
      return item;
    });
    this.saveItems(items);
  },

  removeItem(productId) {
    let items = this.getItems();
    items = items.filter(item => String(item.id) !== String(productId));
    this.saveItems(items);
    this.showToast('Producto eliminado del carrito.');
  },

  clearCart() {
    localStorage.removeItem(this.KEY);
    this.updateBadge();
    this.showToast('Carrito vaciado.');
  },

  getTotalCount() {
    const items = this.getItems();
    return items.reduce((acc, item) => acc + (parseInt(item.quantity, 10) || 0), 0);
  },

  getSubtotal() {
    const items = this.getItems();
    return items.reduce((acc, item) => acc + ((parseFloat(item.precio) || 0) * (parseInt(item.quantity, 10) || 1)), 0);
  },

  updateBadge() {
    const count = this.getTotalCount();
    const badgeEls = document.querySelectorAll('.cf-cart-badge-count');
    badgeEls.forEach(el => {
      el.textContent = count;
      if (count > 0) {
        el.style.display = 'flex';
      } else {
        el.style.display = 'none';
      }
    });
  },

  showToast(message) {
    let toastContainer = document.getElementById('cf-toast-container');
    if (!toastContainer) {
      toastContainer = document.createElement('div');
      toastContainer.id = 'cf-toast-container';
      toastContainer.style.cssText = 'position: fixed; bottom: 24px; right: 24px; z-index: 1090; display: flex; flex-direction: column; gap: 10px;';
      document.body.appendChild(toastContainer);
    }

    const toast = document.createElement('div');
    toast.className = 'alert shadow-lg d-flex align-items-center gap-2 mb-0 py-2 px-3 text-white';
    toast.style.cssText = 'background-color: #0f3e2e; border-radius: 9999px; font-size: 0.9rem; animation: slideUp 0.3s ease;';
    toast.innerHTML = `<i class="bi bi-check-circle-fill text-success"></i> <span>${message}</span>`;

    toastContainer.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(10px)';
      toast.style.transition = 'all 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, 2500);
  }
};

document.addEventListener('DOMContentLoaded', () => {
  CampusCart.updateBadge();

  // Handle password toggle buttons
  document.querySelectorAll('.cf-password-toggle').forEach(btn => {
    btn.addEventListener('click', () => {
      const targetId = btn.getAttribute('data-target');
      const input = document.getElementById(targetId);
      if (input) {
        const isPass = input.type === 'password';
        input.type = isPass ? 'text' : 'password';
        btn.innerHTML = isPass ? '<i class="bi bi-eye-slash"></i>' : '<i class="bi bi-eye"></i>';
      }
    });
  });

  // Handle favorite heart toggle buttons
  document.querySelectorAll('.cf-btn-favorite').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      e.stopPropagation();
      btn.classList.toggle('active');
      const icon = btn.querySelector('i');
      if (icon) {
        if (btn.classList.contains('active')) {
          icon.className = 'bi bi-heart-fill';
          CampusCart.showToast('Guardado en favoritos');
        } else {
          icon.className = 'bi bi-heart';
        }
      }
    });
  });

  // Initialize Bootstrap dropdowns explicitly
  if (typeof bootstrap !== 'undefined' && bootstrap.Dropdown) {
    document.querySelectorAll('[data-bs-toggle="dropdown"]').forEach(el => {
      bootstrap.Dropdown.getOrCreateInstance(el);
    });
  }
});

