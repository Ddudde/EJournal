let CACHE_NAME, urlsToCache, num, cacheWhitelist, prom, prefSite;

// Name our cache
CACHE_NAME = 'my-pwa-cache-v8';

cacheWhitelist = [CACHE_NAME];

prefSite = "/DipvLom";

function init() {
    num = 0;
    setInterval(() => {
        console.log('tick', num);
        num++;
    }, 2000);
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
        this.addEventListener("activate", e=>e.waitUntil(
            this.registration.navigationPreload.getState()
                .then(data => {
                    if(!data.enabled) {
                        return this.registration.navigationPreload.enable()
                    }
                })
        ));
    }
    this.addEventListener("activate", activateF);
    this.addEventListener('install', installF);
    this.addEventListener('fetch', fetchF);
    this.addEventListener('message', messageF);
    // console.log("try notif sw");
    // importScripts('https://www.gstatic.com/firebasejs/8.4.2/firebase-app.js');
    // importScripts('https://www.gstatic.com/firebasejs/7.16.1/firebase-messaging.js');
    // firebase.initializeApp({
    //     messagingSenderId: "781991460409",
    //     apiKey: "AIzaSyBrH7xUOxnVjhFGeVTIM9gZB0kxr-2xOwc",
    //     projectId: "e-journalfcm",
    //     appId: "1:781991460409:web:a900bf500869ddd6f097e8"
    // });
    // const messaging = firebase.messaging();
    // messaging.setBackgroundMessageHandler(async payload => {
    //     console.log(
    //         "[firebase-messaging-sw.js] Received background message ",
    //         payload
    //     );
    //     // Customize notification here
    //     const notificationTitle = "Background Message Title";
    //     const notificationOptions = {
    //         body: "Background Message body.",
    //         icon: "/DipvLom/static/media/knopka.c14f1cc6c607bb6edd74.png",
    //     };
    //
    //     return this.registration.showNotification(
    //         notificationTitle,
    //         notificationOptions,
    //     );
    // });
    this.addEventListener('push', event => {
        console.log('Event: Push', event);
        let data = {};
        if (event.data) data = event.data.json();
        console.log('SW: Push received', data)
        if (data.notification && data.notification.title) {
            // var title = 'Test title';
            // var body = {
            //     body: 'test text',
            //     tag: 'pwa',
            //     icon: '/DipvLom/static/media/knopka.c14f1cc6c607bb6edd74.png'
            // };
            event.waitUntil(this.registration.showNotification(data.notification.title, data.notification));
        } else {
            console.log('SW: No notification payload,  not showing notification')
        }
    });
    this.addEventListener('notificationclick', event => {
        //---access data from event using event.notification.data---
        console.log('On notification click: ', event.notification.data);
        var url = prefSite + '/';

        //---close the notification---
        event.notification.close();

        //---open the app and navigate to breaking.html
        // after clicking the notification---
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
    e.waitUntil(
// this.registration.unregister()
    //     .then(bool=>{
    //         if(bool) {
    //             console.log("Service worker unregister is successful");
    //         } else {
    //             console.log("Service worker unregister is failed");
    //         }
    //     })
        caches.keys().then(keyList => Promise.all(keyList
            .map(key => {
                if (!cacheWhitelist.includes(key)) {
                    console.log('Deleting cache: ' + key)
                    return caches.delete(key);
                }
            })
        ))
    );
}

function installF(e) {
    this.skipWaiting();
    e.waitUntil(caches.open(CACHE_NAME)
        .then(cache => prom.then(e => cache.addAll(urlsToCache)
            .then(r => {
                console.log('cached...');
                return r;
            })
            .catch(message => console.log(message))
        ))
    );
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
    e.respondWith(caches.match(e.request)
        .then(responseCache => {
            console.log(responseCache);
            if(responseCache) {
                return responseCache;
            } else {
                if(e.preloadResponse) {
                    return e.preloadResponse
                        .then(responsePre => responsePre
                            || setCache(e.request.url))
                        .catch(message => {
                            console.log(message);
                            if(e.request.destination == 'document') {
                                console.log("setIndexDoc...")
                                return caches.match(prefSite + "/")
                                    .then(responseCache => responseCache)
                            }
                        });
                }
                return setCache(e.request.url);
            }
        })
    );
}

init();