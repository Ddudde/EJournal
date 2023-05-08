let CACHE_NAME, urlsToCache, num, cacheWhitelist, prom, prefSite;

// Name our cache
CACHE_NAME = 'my-pwa-cache-v1';

cacheWhitelist = [CACHE_NAME];

prefSite = "/DipvLom";

function init() {
    // num = 0;
    // setInterval(() => {
    //     console.log('tick', num);
    //     num++;
    // }, 2000);
    prom = fetch(prefSite + "/asset-manifest.json")
        .then(response => response.json())
        .then(assets => {
            urlsToCache = [
                prefSite + "/",
                prefSite + "/static/media/fav512.png",
                prefSite + "/static/media/fav32.png",
                prefSite + "/static/media/fav16.png",
                prefSite + "/manifest.json",
                prefSite + "/static/js/app.js"
            ];
            Object.getOwnPropertyNames(assets.files).map((key, i, x, val = assets.files[key]) => {
                urlsToCache.push(val);
            });
        });
    if(this.registration.navigationPreload){
        this.addEventListener("activate", e=>e.waitUntil((async () => {
            const data = await this.registration.navigationPreload.getState();
            if(!data.enabled) {
                return await this.registration.navigationPreload.enable()
            }
        }) ()));
    }
    this.addEventListener("activate", activateF);
    this.addEventListener('install', installF);
    this.addEventListener('fetch', fetchF);
    this.addEventListener('message', messageF);
    this.addEventListener('push', event => {
        console.log('Event: Push', event);
        let data = {};
        if (event.data) data = event.data.json();
        console.log('SW: Push received', data)
        if (data.notification && data.notification.title) {
            event.waitUntil(this.registration.showNotification(data.notification.title, data.notification));
        } else {
            console.log('SW: No notification payload, not showing notification')
        }
    });
    this.addEventListener('notificationclick', event => {
        console.log('On notification click: ', event.notification.data);
        var url = prefSite + '/';
        event.notification.close();
        event.waitUntil(
            this.clients.openWindow(url)
        );
    });
}

function messageF(e) {
    console.log(`The client sent me a message: ${e.data}`);

    // e.source.postMessage("Hi client");
}

function activateF(e) {
    e.waitUntil(caches.keys().then(keyList => Promise.all(keyList
        .map(key => {
            if (!cacheWhitelist.includes(key)) {
                console.log('Deleting cache: ' + key)
                return caches.delete(key);
            }
        })
    )));
}

function installF(e) {
    this.skipWaiting();
    e.waitUntil(caches.open(CACHE_NAME).then(cache => prom
        .then(e => cache.addAll(urlsToCache)
            .then(r => {
                console.log('cached...');
                return r;
            })
            .catch(message => console.log(message))
    )));
}

function setCache(url) {
    console.log('try cache res');
    return caches.open(CACHE_NAME)
        .then(cache => cache.add(url)
            .then(r => r || fetch(url)
                .then(req => {
                    console.log("fetch!");
                    return req;
                })
                .catch(message => console.log(message))
            )
            .catch(message => console.log(message))
        )
}

function fetchF(e) {
    if(e.request.destination == '') return;
    e.respondWith((async () => {
        const responseCache = await caches.match(e.request)
        console.log(responseCache);
        if(responseCache) {
            return responseCache;
        } else {
            try {
                const responsePre = await e.preloadResponse;
                return responsePre || setCache(e.request.url);
            } catch (message) {
                console.log(message);
                if (e.request.destination == 'document') {
                    console.log("setIndexDoc...")
                    return caches.match(prefSite + "/");
                }
            }
        }
    }) ());
}

init();