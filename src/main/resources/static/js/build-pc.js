const componentTypes = ['CPU','MAINBOARD','RAM','VGA','STORAGE','PSU','COOLING','CASE','MONITOR'];
let selectedConfig = {};
let expandedTypes = {};   // lưu trạng thái mở/đóng

async function loadComponents() {
    const container = document.getElementById('buildPC-container');
    container.innerHTML = '';

    for (const type of componentTypes) {
        const section = document.createElement('div');
        section.classList.add('component-section');

        section.innerHTML = `
            <h3>${type} 
                <button class="btn btn-sm btn-info" onclick="toggleType('${type}')">
                    Xem tất cả
                </button>
            </h3>
            <div class="row" id="section-${type}"></div>
        `;
        container.appendChild(section);
    }
}

// Toggle xem tất cả / ẩn
async function toggleType(type) {
    const inner = document.getElementById(`section-${type}`);

    if (expandedTypes[type]) {
        // Nếu đang mở -> đóng
        inner.innerHTML = '';
        expandedTypes[type] = false;
        return;
    }

    // Nếu đang đóng -> load sản phẩm
    inner.innerHTML = 'Đang tải...';
    try {
        const res = await fetch(`/api/products/type/${type}`);
        const products = await res.json();
        inner.innerHTML = products.map(p => `
            <div class="col-md-3 mb-3">
                <div class="card">
                    <img src="${p.mainImage}" class="card-img-top" alt="${p.name}">
                    <div class="card-body">
                        <h5 class="card-title">${p.name}</h5>
                        <p class="card-text">${p.priceAfterDiscount.toLocaleString()}₫</p>
                        <button class="btn btn-sm btn-success" onclick="selectProduct('${type}','${p.id}','${p.name}','${p.priceAfterDiscount}')">Chọn</button>
                    </div>
                </div>
            </div>
        `).join('');
        expandedTypes[type] = true;
    } catch(err) {
        console.error(type, err);
        inner.innerHTML = 'Lỗi tải sản phẩm';
    }
}

function selectProduct(type, id, name, price) {
    selectedConfig[type] = { id, name, price: parseFloat(price) };
    updateSummary();
}

function updateSummary() {
    const body = document.getElementById('config-summary-body');
    body.innerHTML = '';
    let total = 0;
    for (const type in selectedConfig) {
        const item = selectedConfig[type];
        total += item.price;
        body.innerHTML += `<tr><td>${type} - ${item.name}</td><td>1</td><td>${item.price.toLocaleString()}₫</td></tr>`;
    }
    document.getElementById('config-total').textContent = total.toLocaleString() + '₫';
}

// Clear config
document.getElementById('clear-config').addEventListener('click', () => {
    selectedConfig = {};
    updateSummary();
});

// On load
document.addEventListener('DOMContentLoaded', () => {
    loadComponents();
});
