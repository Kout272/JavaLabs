let countries = [
    { id: 1, name: 'Russia', code: '7' },
    { id: 2, name: 'United States', code: '1' },
    { id: 3, name: 'United Kingdom', code: '44' }
];
let editCountryId = null;

function renderTable() {
    const tbody = document.getElementById('country-table-body');
    tbody.innerHTML = '';
    countries.forEach(country => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${country.name}</td>
            <td>+${country.code}</td>
            <td>
                <button class="btn btn-warning btn-sm" onclick="openModal(${country.id})">✏️</button>
                <button class="btn btn-danger btn-sm" onclick="deleteCountry(${country.id})">🗑️</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function openModal(countryId) {
    const modalOverlay = document.getElementById('modal-overlay');
    const modalTitle = document.getElementById('modal-title');
    const nameInput = document.getElementById('modal-name');
    const codeInput = document.getElementById('modal-code');

    if (countryId) {
        const country = countries.find(c => c.id === countryId);
        modalTitle.textContent = 'Редактировать';
        nameInput.value = country.name;
        codeInput.value = country.code;
        editCountryId = countryId;
    } else {
        modalTitle.textContent = 'Добавить страну';
        nameInput.value = '';
        codeInput.value = '';
        editCountryId = null;
    }

    modalOverlay.style.display = 'block';
}

function closeModal() {
    const modalOverlay = document.getElementById('modal-overlay');
    modalOverlay.style.display = 'none';
}

function saveCountry() {
    const nameInput = document.getElementById('modal-name').value.trim();
    const codeInput = document.getElementById('modal-code').value.trim();

    if (!nameInput || !codeInput) {
        alert('Пожалуйста, заполните все поля');
        return;
    } //

    if (editCountryId) {
        const country = countries.find(c => c.id === editCountryId);
        country.name = nameInput;
        country.code = codeInput;
    } else {
        const newId = countries.length ? Math.max(...countries.map(c => c.id)) + 1 : 1;
        countries.push({ id: newId, name: nameInput, code: codeInput });
    }

    renderTable();
    closeModal();
}

function deleteCountry(id) {
    if (confirm('Вы уверены, что хотите удалить эту страну?')) {
        countries = countries.filter(c => c.id !== id);
        renderTable();
    }
}

function handleSearch() {
    const query = document.getElementById('search-input').value.trim().toLowerCase();
    const resultDiv = document.getElementById('search-result');

    if (!query) {
        resultDiv.innerHTML = '';
        resultDiv.style.display = 'none';
        return;
    }

    const country = countries.find(c =>
        c.name.toLowerCase().includes(query) ||
        c.code.toLowerCase().includes(query)
    );

    if (country) {
        resultDiv.innerHTML = `Результат: <span>${country.name} (+${country.code})</span>`;
        resultDiv.style.display = 'block';
    } else {
        resultDiv.innerHTML = 'Результат: <span>Страна не найдена</span>';
        resultDiv.style.display = 'block';
    }
}

renderTable();