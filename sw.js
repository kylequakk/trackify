const CACHE_NAME = 'Tracalify-v1';
const ASSETS = [
  '/',
  '/index.html',
  '/pages/homePage.html',
  'https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;600;700;900&display=swap'
];

// Install Service Worker and cache assets
self.addEventListener('install', (e) => {
  e.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(ASSETS))
  );
});

// Fetching assets from cache when offline
self.addEventListener('fetch', (e) => {
  e.respondWith(
    caches.match(e.request).then((response) => {
      return response || fetch(e.request);
    })
  );
});
