(function () {
  const root = document.documentElement;
  const toggle = document.getElementById('themeToggle');
  const chartCanvas = document.getElementById('priceChart');
  let chart;

  function getCss(name) {
    return getComputedStyle(root).getPropertyValue(name).trim();
  }

  function renderChart(historyItems) {
    if (!chartCanvas || typeof Chart === 'undefined') return;

    const text = getCss('--color-text-muted');
    const border = getCss('--color-border');
    const primary = getCss('--color-primary');
    const fill = getCss('--color-primary-highlight');

    let labels;
    let data;

    if (Array.isArray(historyItems) && historyItems.length > 0) {
      const sorted = [...historyItems].sort((a, b) =>
        String(a.dateRecorded).localeCompare(String(b.dateRecorded))
      );
      labels = sorted.map((h) =>
        String(h.dateRecorded).replace('T', ' ').slice(0, 16)
      );
      data = sorted.map((h) => Number(h.price));
    } else {
      labels = ['Apr 18', 'Apr 22', 'Apr 26', 'Apr 30', 'May 04', 'May 08', 'May 12'];
      data = [1120, 1095, 1110, 1050, 1038, 1012, 999];
    }

    if (chart) chart.destroy();

    chart = new Chart(chartCanvas, {
      type: 'line',
      data: {
        labels,
        datasets: [{
          label: 'Динамика цены',
          data,
          borderColor: primary,
          backgroundColor: fill,
          fill: true,
          tension: 0.35,
          pointRadius: 3,
          pointHoverRadius: 5
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          x: { ticks: { color: text }, grid: { color: border } },
          y: { ticks: { color: text }, grid: { color: border } }
        }
      }
    });
  }

  function initTheme() {
    if (!toggle) return;
    toggle.addEventListener('click', () => {
      const next = root.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
      root.setAttribute('data-theme', next);
      renderChart();
    });
  }

  function initActiveNav() {
    const links = Array.from(document.querySelectorAll('.nav-link[href^="#"]'));
    links.forEach((link) => {
      link.addEventListener('click', () => {
        links.forEach((item) => item.classList.remove('is-active'));
        link.classList.add('is-active');
      });
    });
  }

  function initProducts() {
    if (!window.ApiClient || !ApiClient.Products) return;

    const statusChip = document.getElementById('productsStatus');
    const tbody = document.getElementById('productsTableBody');
    const pageInfo = document.getElementById('productsPageInfo');
    const prevBtn = document.getElementById('productsPrevPage');
    const nextBtn = document.getElementById('productsNextPage');
    const categoryInput = document.getElementById('productsFilterCategory');
    const minPriceInput = document.getElementById('productsFilterMinPrice');
    const maxPriceInput = document.getElementById('productsFilterMaxPrice');
    const applyBtn = document.getElementById('productsApplyFilters');
    const kpiProducts = document.getElementById('kpiProducts');
    const detailsBody = document.getElementById('productDetailsBody');
    const detailsStatus = document.getElementById('productDetailsStatus');
    const newProductBtn = document.getElementById('newProductBtn');

    const historyStatus = document.getElementById('priceHistoryStatus');
    const historyTbody = document.getElementById('priceHistoryTableBody');

    const modal = document.getElementById('productModal');
    const modalForm = document.getElementById('productForm');
    const modalCloseButtons = modal ? modal.querySelectorAll('.modal-close') : [];
    const modalName = document.getElementById('modalProductName');
    const modalCategory = document.getElementById('modalProductCategory');
    const modalPrice = document.getElementById('modalProductPrice');
    const modalDescription = document.getElementById('modalProductDescription');
    const modalError = document.getElementById('productFormError');

    if (!tbody || !statusChip) return;

    const state = {
      page: 0,
      size: 10,
      sortKey: 'id',
      sortDir: 'asc',
      category: '',
      minPrice: null,
      maxPrice: null,
      totalPages: 1,
      lastContent: [],
      selectedId: null,
    };

    function buildSortParam() {
      return `${state.sortKey},${state.sortDir}`;
    }

    function setChipStatus(el, text, tone = 'neutral') {
      if (!el) return;
      el.textContent = text;
      el.classList.remove('positive', 'negative');
      if (tone === 'positive') el.classList.add('positive');
      if (tone === 'negative') el.classList.add('negative');
    }

    function clearSelection() {
      Array.from(tbody.querySelectorAll('.product-row-selected')).forEach((tr) =>
        tr.classList.remove('product-row-selected')
      );
    }

    function renderDetails(product) {
      if (!detailsBody || !detailsStatus) return;
      if (!product) {
        detailsBody.innerHTML = '<p>Нажмите на строку в таблице товаров, чтобы увидеть подробности.</p>';
        setChipStatus(detailsStatus, 'Ожидание выбора');
        return;
      }

      setChipStatus(detailsStatus, 'Товар выбран', 'positive');
      const safe = (v) => (v == null || v === '' ? '—' : String(v));

      detailsBody.innerHTML = `
        <div class="details-grid">
          <div class="details-item">
            <span class="details-label">Наименование</span>
            <span class="details-value">${safe(product.name)}</span>
          </div>
          <div class="details-item">
            <span class="details-label">Категория</span>
            <span class="details-value">${safe(product.category)}</span>
          </div>
          <div class="details-item">
            <span class="details-label">Цена</span>
            <span class="details-value">${product.price != null ? product.price : '—'}</span>
          </div>
        </div>
        <div class="details-item" style="margin-top: 1rem;">
          <span class="details-label">Описание</span>
          <span class="details-value">${safe(product.description)}</span>
        </div>
      `;
    }

    function setHistoryStatus(text, tone = 'neutral') {
      setChipStatus(historyStatus, text, tone);
    }

    function renderHistoryRows(items) {
      if (!historyTbody) return;
      historyTbody.innerHTML = '';

      if (!items || items.length === 0) {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 3;
        td.textContent = 'История цен для этого товара не найдена.';
        td.style.color = 'var(--color-text-muted)';
        tr.appendChild(td);
        historyTbody.appendChild(tr);
        return;
      }

      items.forEach((h) => {
        const tr = document.createElement('tr');

        const tdDate = document.createElement('td');
        const raw = h.dateRecorded;
        tdDate.textContent = raw ? String(raw).replace('T', ' ').slice(0, 16) : '—';

        const tdPrice = document.createElement('td');
        tdPrice.textContent = h.price != null ? String(h.price) : '—';

        const tdStore = document.createElement('td');
        tdStore.textContent = h.storeName ?? '—';

        tr.appendChild(tdDate);
        tr.appendChild(tdPrice);
        tr.appendChild(tdStore);
        historyTbody.appendChild(tr);
      });
    }

    async function loadPriceHistory(productId) {
      if (!historyTbody || !historyStatus || !window.ApiClient || !ApiClient.PriceHistory) return;
      if (!productId) {
        historyTbody.innerHTML = '';
        setHistoryStatus('Ожидание выбора товара');
        renderChart(null);
        return;
      }

      try {
        setHistoryStatus('Загрузка…');
        const items = await ApiClient.PriceHistory.getByProduct(productId, { optimized: true });
        renderHistoryRows(items || []);
        setHistoryStatus(`Записей: ${items ? items.length : 0}`, 'positive');
        renderChart(items || []);
      } catch (err) {
        console.error('Не удалось загрузить историю цен', err);
        setHistoryStatus('Ошибка загрузки истории цен', 'negative');
        renderChart(null);
      }
    }

    function renderRows(content) {
      tbody.innerHTML = '';
      if (!content || content.length === 0) {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 4;
        td.textContent = 'Товары не найдены. Попробуйте изменить условия поиска.';
        td.style.color = 'var(--color-text-muted)';
        tr.appendChild(td);
        tbody.appendChild(tr);
        renderDetails(null);
        loadPriceHistory(null);
        return;
      }

      content.forEach((p) => {
        const tr = document.createElement('tr');
        tr.dataset.id = p.id != null ? String(p.id) : '';

        const tdName = document.createElement('td');
        const strong = document.createElement('strong');
        strong.textContent = p.name ?? '—';
        tdName.appendChild(strong);

        const tdCategory = document.createElement('td');
        tdCategory.textContent = p.category ?? '—';

        const tdPrice = document.createElement('td');
        tdPrice.textContent = p.price != null ? String(p.price) : '—';

        const tdDesc = document.createElement('td');
        tdDesc.textContent = p.description ?? '';

        tr.appendChild(tdName);
        tr.appendChild(tdCategory);
        tr.appendChild(tdPrice);
        tr.appendChild(tdDesc);

        tr.addEventListener('click', () => {
          clearSelection();
          tr.classList.add('product-row-selected');
          state.selectedId = p.id;
          renderDetails(p);
          loadPriceHistory(p.id);
        });

        tbody.appendChild(tr);
      });
    }

    function updatePagination(page, totalPages) {
      if (!pageInfo || !prevBtn || !nextBtn) return;
      pageInfo.textContent = `Страница ${page + 1} из ${totalPages || 1}`;
      prevBtn.disabled = page <= 0;
      nextBtn.disabled = page >= totalPages - 1;
    }

    function updateSortIndicators() {
      const headers = document.querySelectorAll('#productsTable thead th[data-sort-key]');
      headers.forEach((th) => {
        th.classList.remove('is-sorted-asc', 'is-sorted-desc');
        const key = th.getAttribute('data-sort-key');
        if (key === state.sortKey) {
          th.classList.add(state.sortDir === 'asc' ? 'is-sorted-asc' : 'is-sorted-desc');
        }
      });
    }

    async function loadProducts() {
      try {
        setChipStatus(statusChip, 'Загрузка…');
        const payload = await ApiClient.Products.search({
          category: state.category || undefined,
          minPrice: state.minPrice,
          maxPrice: state.maxPrice,
          page: state.page,
          size: state.size,
          sort: buildSortParam(),
          useNative: false,
        });

        const content = payload && payload.content ? payload.content : [];
        const totalPages = payload && typeof payload.totalPages === 'number'
          ? payload.totalPages
          : 1;

        state.totalPages = totalPages;
        state.lastContent = content;
        renderRows(content);
        updatePagination(state.page, totalPages);
        updateSortIndicators();

        setChipStatus(statusChip, `Загружено ${content.length} товаров`, 'positive');
        if (kpiProducts) {
          const total = payload && typeof payload.totalElements === 'number'
            ? payload.totalElements
            : content.length;
          kpiProducts.textContent = String(total);
        }
      } catch (err) {
        console.error('Не удалось загрузить товары', err);
        setChipStatus(statusChip, 'Ошибка загрузки товаров', 'negative');
      }
    }

    function openModal() {
      if (!modal) return;
      modal.classList.add('is-open');
      if (modalForm) modalForm.reset();
      if (modalError) modalError.textContent = '';
      if (modalName) modalName.focus();
    }

    function closeModal() {
      if (!modal) return;
      modal.classList.remove('is-open');
    }

    if (applyBtn) {
      applyBtn.addEventListener('click', (e) => {
        e.preventDefault();
        state.category = (categoryInput.value || '').trim() || '';
        state.minPrice = minPriceInput.value ? Number(minPriceInput.value) : null;
        state.maxPrice = maxPriceInput.value ? Number(maxPriceInput.value) : null;
        state.page = 0;
        loadProducts();
      });
    }

    [categoryInput, minPriceInput, maxPriceInput].forEach((input) => {
      if (!input) return;
      input.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
          e.preventDefault();
          state.category = (categoryInput.value || '').trim() || '';
          state.minPrice = minPriceInput.value ? Number(minPriceInput.value) : null;
          state.maxPrice = maxPriceInput.value ? Number(maxPriceInput.value) : null;
          state.page = 0;
          loadProducts();
        }
      });
    });

    if (prevBtn) {
      prevBtn.addEventListener('click', () => {
        if (state.page > 0) {
          state.page -= 1;
          loadProducts();
        }
      });
    }

    if (nextBtn) {
      nextBtn.addEventListener('click', () => {
        if (state.page < state.totalPages - 1) {
          state.page += 1;
          loadProducts();
        }
      });
    }

    const sortableHeaders = document.querySelectorAll('#productsTable thead th[data-sort-key]');
    sortableHeaders.forEach((th) => {
      th.addEventListener('click', () => {
        const key = th.getAttribute('data-sort-key');
        if (!key) return;
        if (state.sortKey === key) {
          state.sortDir = state.sortDir === 'asc' ? 'desc' : 'asc';
        } else {
          state.sortKey = key;
          state.sortDir = 'asc';
        }
        state.page = 0;
        loadProducts();
      });
    });

    if (newProductBtn) {
      newProductBtn.addEventListener('click', () => {
        openModal();
      });
    }

    modalCloseButtons.forEach((btn) => {
      btn.addEventListener('click', () => closeModal());
    });

    if (modal) {
      modal.addEventListener('click', (e) => {
        if (e.target === modal) {
          closeModal();
        }
      });
    }

    if (modalForm) {
      modalForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!window.ApiClient || !ApiClient.Products || !modalName) return;

        const payload = {
          name: modalName.value.trim() || null,
          category: modalCategory && modalCategory.value.trim()
            ? modalCategory.value.trim()
            : null,
          price: modalPrice && modalPrice.value
            ? Number(modalPrice.value)
            : null,
          description: modalDescription && modalDescription.value.trim()
            ? modalDescription.value.trim()
            : null,
        };

        try {
          setChipStatus(statusChip, 'Создание товара…');
          if (modalError) modalError.textContent = '';
          await ApiClient.Products.create(payload);
          setChipStatus(statusChip, 'Товар создан', 'positive');
          closeModal();
          state.page = 0;
          loadProducts();
        } catch (err) {
          console.error('Не удалось создать товар', err);
          if (modalError) {
            modalError.textContent = err.message || 'Ошибка при создании товара. См. консоль.';
          }
          setChipStatus(statusChip, 'Ошибка при создании товара', 'negative');
        }
      });
    }

    loadProducts();
    loadPriceHistory(null);
  }

  function initCategories() {
    if (!window.ApiClient || !ApiClient.Categories) return;

    const statusChip = document.getElementById('categoriesStatus');
    const tbody = document.getElementById('categoriesTableBody');
    const kpiCategories = document.getElementById('kpiCategories');
    const newCategoryBtn = document.getElementById('newCategoryBtn');

    const modal = document.getElementById('categoryModal');
    const modalForm = document.getElementById('categoryForm');
    const modalCloseButtons = modal ? modal.querySelectorAll('.modal-close') : [];
    const modalName = document.getElementById('modalCategoryName');
    const modalError = document.getElementById('categoryFormError');

    if (!tbody || !statusChip) return;

    function setStatus(text, tone = 'neutral') {
      statusChip.textContent = text;
      statusChip.classList.remove('positive', 'negative');
      if (tone === 'positive') statusChip.classList.add('positive');
      if (tone === 'negative') statusChip.classList.add('negative');
    }

    function renderRows(items) {
      tbody.innerHTML = '';

      if (!items || items.length === 0) {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 1;
        td.textContent = 'Категории не найдены.';
        td.style.color = 'var(--color-text-muted)';
        tr.appendChild(td);
        tbody.appendChild(tr);
        return;
      }

      items.forEach((c) => {
        const tr = document.createElement('tr');

        const tdName = document.createElement('td');
        const strong = document.createElement('strong');
        strong.textContent = c.name ?? '—';
        tdName.appendChild(strong);

        tr.appendChild(tdName);
        tbody.appendChild(tr);
      });
    }

    async function loadCategories() {
      try {
        setStatus('Загрузка…');
        const items = await ApiClient.Categories.getAll();
        renderRows(items || []);
        setStatus(`Загружено ${items ? items.length : 0} категорий`, 'positive');
        if (kpiCategories) kpiCategories.textContent = String(items ? items.length : 0);
      } catch (err) {
        console.error('Не удалось загрузить категории', err);
        setStatus('Ошибка загрузки категорий', 'negative');
      }
    }

    function openModal() {
      if (!modal) return;
      modal.classList.add('is-open');
      if (modalForm) modalForm.reset();
      if (modalError) modalError.textContent = '';
      if (modalName) modalName.focus();
    }

    function closeModal() {
      if (!modal) return;
      modal.classList.remove('is-open');
    }

    if (newCategoryBtn) {
      newCategoryBtn.addEventListener('click', () => {
        openModal();
      });
    }

    modalCloseButtons.forEach((btn) => {
      btn.addEventListener('click', () => closeModal());
    });

    if (modal) {
      modal.addEventListener('click', (e) => {
        if (e.target === modal) {
          closeModal();
        }
      });
    }

    if (modalForm) {
      modalForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!window.ApiClient || !ApiClient.Categories || !modalName) return;

        const payload = {
          name: modalName.value.trim() || null,
        };

        try {
          setStatus('Создание категории…');
          if (modalError) modalError.textContent = '';
          await ApiClient.Categories.create(payload);
          setStatus('Категория создана', 'positive');
          closeModal();
          loadCategories();
        } catch (err) {
          console.error('Не удалось создать категорию', err);
          if (modalError) {
            modalError.textContent =
              err.message || 'Ошибка при создании категории. См. консоль.';
          }
          setStatus('Ошибка при создании категории', 'negative');
        }
      });
    }

    loadCategories();
  }

  function initStores() {
    if (!window.ApiClient || !ApiClient.Stores) return;

    const statusChip = document.getElementById('storesStatus');
    const tbody = document.getElementById('storesTableBody');
    const kpiStores = document.getElementById('kpiStores');
    if (!tbody || !statusChip) return;

    function setStatus(text, tone = 'neutral') {
      statusChip.textContent = text;
      statusChip.classList.remove('positive', 'negative');
      if (tone === 'positive') statusChip.classList.add('positive');
      if (tone === 'negative') statusChip.classList.add('negative');
    }

    function renderRows(items) {
      tbody.innerHTML = '';
      if (!items || items.length === 0) {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 2;
        td.textContent = 'Магазины не найдены.';
        td.style.color = 'var(--color-text-muted)';
        tr.appendChild(td);
        tbody.appendChild(tr);
        return;
      }

      items.forEach((s) => {
        const tr = document.createElement('tr');

        const tdName = document.createElement('td');
        const strong = document.createElement('strong');
        strong.textContent = s.name ?? '—';
        tdName.appendChild(strong);

        const tdWebsite = document.createElement('td');
        tdWebsite.textContent = s.websiteUrl ?? '—';

        tr.appendChild(tdName);
        tr.appendChild(tdWebsite);
        tbody.appendChild(tr);
      });
    }

    async function loadStores() {
      try {
        setStatus('Загрузка…');
        const items = await ApiClient.Stores.getAll();
        renderRows(items);
        setStatus(`Загружено ${items.length} магазинов`, 'positive');
        if (kpiStores) kpiStores.textContent = String(items.length);
      } catch (err) {
        console.error('Не удалось загрузить магазины', err);
        setStatus('Ошибка загрузки магазинов', 'negative');
      }
    }

    loadStores();
  }

  window.addEventListener('load', () => {
    initTheme();
    initActiveNav();
    renderChart();
    initProducts();
    initCategories();
    initStores();
  });
})();