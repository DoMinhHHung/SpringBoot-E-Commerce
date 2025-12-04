// map-picker.js - Leaflet map picker với Nominatim integration

let map = null;
let marker = null;
let debounceTimer = null;
let isInitialized = false;

/**
 * Initialize Leaflet map
 */
function initMapPicker(containerId, initialLat = 10.7769, initialLng = 106.7009, initialZoom = 13) {
    if (isInitialized) {
        console.warn('Map already initialized');
        return;
    }

    // Initialize map
    map = L.map(containerId).setView([initialLat, initialLng], initialZoom);
    
    // Add OpenStreetMap tile layer
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        maxZoom: 19
    }).addTo(map);

    // Add initial marker
    marker = L.marker([initialLat, initialLng], { draggable: true }).addTo(map);

    // Click event
    map.on('click', function(e) {
        const lat = e.latlng.lat;
        const lng = e.latlng.lng;
        updateMarker(lat, lng);
        reverseGeocode(lat, lng);
    });

    // Drag event với debounce
    marker.on('dragend', function(e) {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            const pos = e.target.getLatLng();
            reverseGeocode(pos.lat, pos.lng);
        }, 500); // Debounce 500ms
    });

    isInitialized = true;
}

/**
 * Update marker position
 */
function updateMarker(lat, lng) {
    if (marker) {
        marker.setLatLng([lat, lng]);
    } else {
        marker = L.marker([lat, lng], { draggable: true }).addTo(map);
    }
    map.setView([lat, lng], map.getZoom());
}

/**
 * Get current coordinates
 */
function getCoordinates() {
    if (!marker) return null;
    const pos = marker.getLatLng();
    return {
        lat: pos.lat,
        lng: pos.lng
    };
}

/**
 * Set coordinates programmatically
 */
function setCoordinates(lat, lng) {
    updateMarker(lat, lng);
    if (map) {
        map.setView([lat, lng], 15);
    }
}

/**
 * Reverse geocode từ backend API
 */
async function reverseGeocode(lat, lng) {
    // Throttle: max 1 request/second
    const now = Date.now();
    if (window.lastGeocodeTime && (now - window.lastGeocodeTime < 1000)) {
        console.log('Throttled: Skipping geocode request');
        return;
    }
    window.lastGeocodeTime = now;

    try {
        console.log('Calling geocoding API for lat:', lat, 'lng:', lng);
        
        const response = await apiClient.request(
            `/geocoding/reverse?lat=${lat}&lng=${lng}`,
            { method: 'GET' }
        );

        console.log('Geocoding response:', response);

        if (response && response.fullAddress) {
            // Gọi callback global
            if (typeof window.onAddressGeocoded === 'function') {
                window.onAddressGeocoded(response);
            } else {
                console.warn('onAddressGeocoded callback not found');
            }
        } else {
            console.warn('No address found in geocoding response');
        }
    } catch (error) {
        console.error('Geocoding error:', error);
        if (typeof window.onGeocodeError === 'function') {
            window.onGeocodeError(error);
        }
    }
}

/**
 * Destroy map instance
 */
function destroyMapPicker() {
    if (map) {
        map.remove();
        map = null;
        marker = null;
        isInitialized = false;
    }
}

