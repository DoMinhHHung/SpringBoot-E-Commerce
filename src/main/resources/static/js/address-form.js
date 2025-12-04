// address-form.js - Address form với map picker

let addressMapInitialized = false;
// userAddresses sẽ được set từ checkout.js hoặc profile.html
if (typeof userAddresses === 'undefined') {
    var userAddresses = [];
}

/**
 * Show add/edit address modal với map picker
 */
window.showAddressModal = function(addressId = null) {
    const isEdit = addressId !== null;
    // Helper để so sánh address ID
    const findAddress = (id) => {
        if (!id || !userAddresses) return null;
        return userAddresses.find(a => a.id == id || String(a.id) === String(id)) || null;
    };
    const address = isEdit ? findAddress(addressId) : null;

    const modalHTML = `
        <div class="modal fade" id="addressModal" tabindex="-1">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">
                            ${isEdit ? 'Sửa địa chỉ' : 'Thêm địa chỉ mới'}
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <form id="address-form">
                            <input type="hidden" id="address-id" value="${addressId || ''}">
                            
                            <!-- Toggle để bật/tắt map -->
                            <div class="mb-3">
                                <div class="form-check form-switch">
                                    <input class="form-check-input" type="checkbox" 
                                           id="enable-map-picker" 
                                           ${address?.latitude ? 'checked' : ''}
                                           onchange="toggleMapPicker()">
                                    <label class="form-check-label" for="enable-map-picker">
                                        <i class="bi bi-geo-alt"></i> Chọn vị trí từ bản đồ (tùy chọn)
                                    </label>
                                </div>
                            </div>
                            
                            <!-- Map Picker - Ẩn/hiện dựa vào toggle -->
                            <div id="map-picker-section" class="mb-3" 
                                 style="display: ${address?.latitude ? 'block' : 'none'};">
                                <label class="form-label">
                                    Chọn vị trí trên bản đồ
                                </label>
                                <div id="address-map" 
                                     style="height: 300px; width: 100%; border: 1px solid #ddd; border-radius: 4px;"></div>
                                <small class="text-muted">
                                    Click vào bản đồ hoặc kéo marker để chọn vị trí
                                </small>
                                <div class="mt-2">
                                    <button type="button" class="btn btn-sm btn-outline-primary" 
                                            onclick="getCurrentLocation()">
                                        <i class="bi bi-geo-alt"></i> Lấy vị trí hiện tại
                                    </button>
                                    <button type="button" class="btn btn-sm btn-outline-secondary ms-2" 
                                            onclick="clearMapSelection()">
                                        <i class="bi bi-x-circle"></i> Bỏ chọn
                                    </button>
                                </div>
                            </div>
                            
                            <div class="mb-3">
                                <label class="form-label">Nhãn địa chỉ (tùy chọn)</label>
                                <input type="text" class="form-control" id="address-label" 
                                       placeholder="VD: Nhà riêng, Công ty" 
                                       value="${address?.label || ''}">
                            </div>
                            
                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">Họ và tên <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="address-receiver-name" 
                                           required value="${address?.receiverName || ''}">
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">Số điện thoại <span class="text-danger">*</span></label>
                                    <input type="tel" class="form-control" id="address-receiver-phone" 
                                           required value="${address?.receiverPhone || ''}">
                                </div>
                            </div>
                            
                            <div class="mb-3">
                                <label class="form-label">Địa chỉ chi tiết <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="address-detail" 
                                       required value="${address?.addressDetail || ''}">
                            </div>
                            
                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">Tỉnh/TP <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="address-province" 
                                           required value="${address?.province || ''}">
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label class="form-label">Phường/Xã <span class="text-danger">*</span></label>
                                    <input type="text" class="form-control" id="address-ward" 
                                           required value="${address?.ward || ''}">
                                </div>
                            </div>
                            
                            <div class="form-check mb-3">
                                <input class="form-check-input" type="checkbox" id="address-is-default" 
                                       ${address?.isDefault === true ? 'checked' : ''}>
                                <label class="form-check-label" for="address-is-default">
                                    Đặt làm địa chỉ mặc định
                                </label>
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                        <button type="button" class="btn btn-primary" onclick="saveAddress()">Lưu</button>
                    </div>
                </div>
            </div>
        </div>
    `;

    // Remove existing modal
    const existing = document.getElementById('addressModal');
    if (existing) existing.remove();

    document.body.insertAdjacentHTML('beforeend', modalHTML);
    const modal = new bootstrap.Modal(document.getElementById('addressModal'));
    
    // Initialize map after modal is shown
    modal._element.addEventListener('shown.bs.modal', () => {
        const enableMap = document.getElementById('enable-map-picker').checked;
        if (enableMap) {
            const initialLat = address?.latitude || 10.7769;
            const initialLng = address?.longitude || 106.7009;
            initMapPicker('address-map', initialLat, initialLng, address ? 15 : 13);
            addressMapInitialized = true;
        }
    }, { once: true });

    // Cleanup when modal is hidden
    modal._element.addEventListener('hidden.bs.modal', () => {
        if (addressMapInitialized) {
            destroyMapPicker();
            addressMapInitialized = false;
        }
    });

    modal.show();
};

/**
 * Toggle map picker
 */
function toggleMapPicker() {
    const enableMap = document.getElementById('enable-map-picker').checked;
    const mapSection = document.getElementById('map-picker-section');
    
    if (enableMap) {
        mapSection.style.display = 'block';
        
        // Initialize map nếu chưa có
        if (!addressMapInitialized) {
            const addressId = document.getElementById('address-id').value;
            const findAddress = (id) => {
                if (!id || !userAddresses) return null;
                return userAddresses.find(a => a.id == id || String(a.id) === String(id)) || null;
            };
            const address = addressId ? findAddress(addressId) : null;
            const initialLat = address?.latitude || 10.7769;
            const initialLng = address?.longitude || 106.7009;
            initMapPicker('address-map', initialLat, initialLng, address ? 15 : 13);
            addressMapInitialized = true;
        }
    } else {
        mapSection.style.display = 'none';
        
        // Clear coordinates khi tắt map
        if (addressMapInitialized) {
            destroyMapPicker();
            addressMapInitialized = false;
        }
    }
}

/**
 * Clear map selection
 */
function clearMapSelection() {
    if (marker) {
        destroyMapPicker();
        addressMapInitialized = false;
        // Re-init với vị trí mặc định
        initMapPicker('address-map', 10.7769, 106.7009, 13);
        addressMapInitialized = true;
    }
}

/**
 * Callback khi geocode thành công
 * Khai báo global để map-picker.js có thể gọi
 */
window.onAddressGeocoded = function(response) {
    console.log('Geocoded response:', response);
    
    // Luôn điền vào form (không check empty)
    // Địa chỉ chi tiết: số nhà + đường
    const detailField = document.getElementById('address-detail');
    if (detailField) {
        let detailValue = '';
        if (response.houseNumber) {
            detailValue = response.houseNumber;
        }
        if (response.road) {
            detailValue = detailValue ? detailValue + ' ' + response.road : response.road;
        }
        if (detailValue) {
            detailField.value = detailValue;
            // Highlight để user thấy đã được điền
            detailField.classList.add('border-success');
            setTimeout(() => detailField.classList.remove('border-success'), 2000);
        }
    }
    
    // Phường/Xã
    const wardField = document.getElementById('address-ward');
    if (wardField && response.ward) {
        wardField.value = response.ward;
        wardField.classList.add('border-success');
        setTimeout(() => wardField.classList.remove('border-success'), 2000);
    }
    
    // Tỉnh/TP
    const provinceField = document.getElementById('address-province');
    if (provinceField && response.province) {
        provinceField.value = response.province;
        provinceField.classList.add('border-success');
        setTimeout(() => provinceField.classList.remove('border-success'), 2000);
    }
    
    // Hiển thị thông báo để user biết đã điền
    if (response.fullAddress) {
        showAlert('Đã điền địa chỉ: ' + response.fullAddress, 'success');
    }
};

/**
 * Callback khi geocode lỗi
 */
window.onGeocodeError = function(error) {
    console.error('Geocoding error:', error);
    showAlert('Không thể lấy địa chỉ từ vị trí đã chọn', 'warning');
};

/**
 * Get current location (browser geolocation)
 */
function getCurrentLocation() {
    if (!navigator.geolocation) {
        showAlert('Trình duyệt không hỗ trợ định vị', 'error');
        return;
    }

    showAlert('Đang lấy vị trí...', 'info');
    
    navigator.geolocation.getCurrentPosition(
        (position) => {
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;
            
            setCoordinates(lat, lng);
            reverseGeocode(lat, lng);
            showAlert('Đã lấy vị trí thành công', 'success');
        },
        (error) => {
            console.error('Geolocation error:', error);
            showAlert('Không thể lấy vị trí: ' + error.message, 'error');
        }
    );
}

async function saveAddress() {
    const form = document.getElementById('address-form');
    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    const addressId = document.getElementById('address-id').value;
    const isEdit = addressId !== null && addressId !== '';
    const enableMap = document.getElementById('enable-map-picker').checked;

    let latitude = null, longitude = null;
    if (enableMap && addressMapInitialized && marker) {
        const coords = getCoordinates();
        if (coords) {
            latitude = coords.lat;
            longitude = coords.lng;
        }
    }

    const addressData = {
        label: document.getElementById('address-label').value || null,
        receiverName: document.getElementById('address-receiver-name').value,
        receiverPhone: document.getElementById('address-receiver-phone').value,
        province: document.getElementById('address-province').value,
        ward: document.getElementById('address-ward').value,
        detail: document.getElementById('address-detail').value,
        isDefault: document.getElementById('address-is-default').checked,
        latitude: latitude,   
        longitude: longitude  
    };

    try {
        if (isEdit) {
            await apiClient.request(`/addresses/${addressId}`, {
                method: 'PUT',
                body: JSON.stringify(addressData)
            });
            showAlert('Đã cập nhật địa chỉ', 'success');
        } else {
            await apiClient.request('/addresses', {
                method: 'POST',
                body: JSON.stringify(addressData)
            });
            showAlert('Đã thêm địa chỉ', 'success');
        }

        const modal = bootstrap.Modal.getInstance(document.getElementById('addressModal'));
        modal.hide();

        // Reload addresses - ưu tiên loadUserAddresses (checkout) trước
        // Nếu đang ở checkout page, chỉ reload checkout addresses
        if (typeof loadUserAddresses === 'function') {
            await loadUserAddresses();
            // loadUserAddresses() sẽ tự động chọn default address hoặc address đầu tiên
        } else if (typeof loadAddresses === 'function') {
            // Nếu không phải checkout page, reload profile addresses
            await loadAddresses();
        }
    } catch (error) {
        showAlert('Lỗi: ' + error.message, 'error');
    }
}

